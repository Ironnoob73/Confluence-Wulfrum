package dev.hail.wulfrum.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraentity.entity.monster.AbstractMonster;
import org.confluence.terraentity.entity.monster.demoneye.DemonEyeSurroundTargetGoal;
import org.confluence.terraentity.entity.util.DeathAnimOptions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class CWHovercraft extends AbstractMonster implements Enemy, GeoEntity, DeathAnimOptions {
    AttackPhase attackPhase = AttackPhase.CIRCLE;
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);

    public CWHovercraft(EntityType<? extends CWHovercraft> entityType, Level level, Builder builder) {
        super(entityType, level, builder);
        this.moveControl = new HMoveControl(this);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new HIdleGoal());
        goalSelector.addGoal(2, new HFloatGoal());
        goalSelector.addGoal(3, new HSurroundTargetGoal(this));

        registerTargetGoal(targetSelector);
    }
    protected void registerTargetGoal(GoalSelector targetSelector){
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    enum AttackPhase {
        CIRCLE,
        SWOOP
    }

    class HIdleGoal extends Goal{
        public HIdleGoal(){
            setFlags(EnumSet.of(Flag.LOOK));
        }
        @Override
        public boolean canUse() {
            return getTarget() == null;
        }
        @Override
        public void start(){
            attackPhase = AttackPhase.CIRCLE;
        }
        @Override
        public void tick() {
            move(MoverType.SELF, new Vec3(0,-0.1,0));
        }
    }
    class HFloatGoal extends Goal{
        int time = 80;
        public HFloatGoal(){
            setFlags(EnumSet.of(Flag.MOVE));
        }
        @Override
        public boolean canUse() {
            return getTarget() != null && attackPhase == AttackPhase.CIRCLE;
        }
        @Override
        public void start(){
            time = 80;
        }
        @Override
        public void tick() {
            time--;
            if (getTarget() != null){
                lookControl.setLookAt(getTarget());
                lookAt(getTarget(), 360, 360);
                setDeltaMovement(new Vec3(0, (getTarget().position().y + 5 - position().y) * 0.1, 0));
            }
        }
        @Override
        public boolean canContinueToUse(){
            return time > 0 && getTarget() != null;
        }
        @Override
        public void stop(){
            attackPhase = AttackPhase.SWOOP;
        }
    }
    public class HSurroundTargetGoal extends DemonEyeSurroundTargetGoal{
        public HSurroundTargetGoal(Mob mob) {
            super(mob);
            setFlags(EnumSet.of(Flag.MOVE));
        }
        @Override
        public boolean canUse() {
            return getTarget() != null && getTarget().isAlive() && attackPhase == AttackPhase.SWOOP;
        }
        @Override
        public void stop(){
            attackPhase = AttackPhase.CIRCLE;
        }
    }

    @Override
    public void tick() {
        super.tick();
    }
    @Override
    protected PathNavigation createNavigation(Level p_level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, p_level) {
            public boolean isStableDestination(BlockPos p_27947_) {
                return !this.level.getBlockState(p_27947_.below()).isAir();
            }

            public void tick() {
                super.tick();
            }
        };
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    static class HMoveControl extends MoveControl {

        public HMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("fly"))));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return CACHE;
    }
}
