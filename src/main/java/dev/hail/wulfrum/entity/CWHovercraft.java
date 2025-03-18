package dev.hail.wulfrum.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraentity.entity.monster.AbstractMonster;
import org.confluence.terraentity.entity.monster.demoneye.DemonEyeSurroundTargetGoal;
import org.confluence.terraentity.entity.monster.demoneye.DemonEyeWanderGoal;
import org.confluence.terraentity.mixin.accessor.EntityAccessor;
import org.confluence.terraentity.utils.TEUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class CWHovercraft extends AbstractMonster implements Enemy, FlyingAnimal {

    public DemonEyeSurroundTargetGoal surroundTargetGoal;
    public CWHovercraft(EntityType<? extends CWHovercraft> entityType, Level level, Builder builder) {
        super(entityType, level, builder);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("fly"))));
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }
    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    protected void registerGoals() {
        surroundTargetGoal = new DemonEyeSurroundTargetGoal(this);
        goalSelector.addGoal(0, surroundTargetGoal);
        goalSelector.addGoal(1, new DemonEyeWanderGoal(this));
        goalSelector.addGoal(2, new HovercraftLeaveGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }
    @Override
    public void move(@NotNull MoverType pType, @NotNull Vec3 motion) {
        if (dead) {
            super.move(pType, motion);
            return;
        }

        Vec3 collide = ((EntityAccessor) this).callCollide(motion);
        if (collide.x != motion.x) {
            motion = new Vec3(motion.x < 0 ? 0.22 : -0.22, motion.y, motion.z);
        }
        if (collide.y != motion.y) {
            boolean downward = motion.y < 0;
            motion = new Vec3(motion.x, downward ? Mth.clamp(-motion.y, 0.1, 0.22) : Mth.clamp(-motion.y, -0.22, -0.1), motion.z);
            if (surroundTargetGoal.targetPos != null && getTarget() != null) {
                surroundTargetGoal.targetPos = surroundTargetGoal.targetPos.with(Direction.Axis.Y, getTarget().position().y + (downward ? 2 : -1));
            }
        }
        if (collide.z != motion.z) {
            motion = new Vec3(motion.x, motion.y, motion.z < 0 ? 0.3 : -0.3);
        }

        setDeltaMovement(motion);
        super.move(pType, motion);
    }
    @Override
    public void tick() {
        // TODO: 仇恨值
        Vec3 pos = position();
        setTarget(level().getNearestPlayer(pos.x, pos.y, pos.z, 40, true));
        super.tick();
        // 在super.tick()结束后更新面向方向即可覆盖原版AI
        TEUtils.updateEntityRotation(this, this.getDeltaMovement().multiply(1, -1, 1));

    }

    public class HovercraftLeaveGoal extends Goal {
        protected final CWHovercraft mob;
        private Vec3 targetMotion;
        public HovercraftLeaveGoal(CWHovercraft mob){
            this.mob = mob;
        }
        @Override
        public void start(){
            RandomSource random = mob.getRandom();
            double x = random.nextDouble() - 0.5;
            double y = 0.1 + (0.6 - 0.1) * random.nextDouble();  // 0.1-0.6
            double z = random.nextDouble() - 0.5;
            targetMotion = new Vec3(x, y, z).normalize().scale(0.25);
        }

        @Override
        public boolean canUse(){
            return false;
        }

        @Override
        public void tick(){
            ServerLevel level = (ServerLevel) mob.level();

            Vec3 motion = mob.getDeltaMovement();
            if(motion.length() < 0.5){
                mob.addDeltaMovement(targetMotion);
            }
        }
    }
}
