package dev.hail.wulfrum.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraentity.entity.monster.AbstractMonster;
import org.confluence.terraentity.entity.monster.Hornet;
import org.confluence.terraentity.entity.monster.demoneye.DemonEyeSurroundTargetGoal;
import org.confluence.terraentity.entity.monster.demoneye.DemonEyeWanderGoal;
import org.confluence.terraentity.entity.util.DeathAnimOptions;
import org.confluence.terraentity.mixin.accessor.EntityAccessor;
import org.confluence.terraentity.utils.TEUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class CWHovercraft extends AbstractMonster implements Enemy, FlyingAnimal, GeoEntity, DeathAnimOptions {
    protected  int attackInternal = 20;
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);

    public CWHovercraft(EntityType<? extends CWHovercraft> entityType, Level level, Builder builder) {
        super(entityType, level, builder);
        this.moveControl = new FlyingMoveControl(this, 20, true);
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
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 2, true));
        goalSelector.addGoal(2, new KeepOnTargetGoal(this));
        goalSelector.addGoal(8, new WanderGoal());
        goalSelector.addGoal(9, new FloatGoal(this));

        registerTargetGoal(targetSelector);
    }
    protected void registerTargetGoal(GoalSelector targetSelector){
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }
    protected class WanderGoal extends Goal {
        private static final int WANDER_THRESHOLD = 22;

        WanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return CWHovercraft.this.getTarget() == null &&  CWHovercraft.this.navigation.isDone() && CWHovercraft.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return CWHovercraft.this.navigation.isInProgress();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                CWHovercraft.this.navigation.moveTo(CWHovercraft.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0);
            }

        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3= CWHovercraft.this.getViewVector(0.0F);

            Vec3 vec32 = HoverRandomPos.getPos(CWHovercraft.this, 8, 7, vec3.x, vec3.z, 1.5707964F, 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(CWHovercraft.this, 8, 4, -2, vec3.x, vec3.z, 1.5707963705062866);
        }
    }

    protected class KeepOnTargetGoal extends Goal {
        private final int FIND_PATH_TIME = 200;
        int timeToRepath;
        CWHovercraft bee;
//        Vec3 targetPos;

        public KeepOnTargetGoal(CWHovercraft bee) {
            this.setFlags(EnumSet.of(Flag.MOVE));
            this.bee = bee;
        }

        public boolean canUse() {

            return bee.getTarget() != null && bee.getTarget().isAlive() && (--timeToRepath <= 0 || timeToRepath < FIND_PATH_TIME - attackInternal);
        }

        public boolean canContinueToUse() {
            return bee.getTarget() != null && bee.getTarget().isAlive() && bee.getNavigation().getPath() != null && bee.getNavigation().getPath().getDistToTarget() > 1.0F;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                bee.swing(InteractionHand.MAIN_HAND);
                timeToRepath = FIND_PATH_TIME;
                bee.navigation.moveTo(bee.navigation.createPath(BlockPos.containing(vec3), 1), 1.5f);
                System.out.println("moving");
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3= CWHovercraft.this.getViewVector(0.0F);

            Vec3 vec32 = HoverRandomPos.getPos(CWHovercraft.this, 8, 7, vec3.x, vec3.z, 1.5707964F, 6, 3);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(CWHovercraft.this, 8, 4, -2, vec3.x, vec3.z, 1.5707963705062866);
        }

        public void tick() {
            bee.lookControl.setLookAt(bee.getTarget());
            bee.lookAt(bee.getTarget(), 360, 360);
            if(bee.distanceTo(bee.getTarget()) < 10){

            }
        }
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


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("fly"))));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return CACHE;
    }
}
