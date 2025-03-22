package dev.hail.wulfrum.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraentity.entity.monster.AbstractMonster;
import org.confluence.terraentity.entity.util.DeathAnimOptions;
import org.confluence.terraentity.utils.TEUtils;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class CWHovercraft extends AbstractMonster implements Enemy, GeoEntity, DeathAnimOptions {
    protected int attackInternal = 20;
    AttackPhase attackPhase = AttackPhase.CIRCLE;
    Vec3 moveTargetPoint = Vec3.ZERO;
    BlockPos anchorPoint = BlockPos.ZERO;
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);

    public CWHovercraft(EntityType<? extends CWHovercraft> entityType, Level level, Builder builder) {
        super(entityType, level, builder);
        this.moveControl = new HMoveControl(this);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new AttackStrategyGoal());
        //goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.5, false));
        goalSelector.addGoal(2, new SweepAttackGoal());
        goalSelector.addGoal(3, new CircleAroundAnchorGoal());
        //goalSelector.addGoal(4, new KeepOnTargetGoal(this));

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

    class AttackStrategyGoal extends Goal {
        private int nextSweepTick;

        @Override
        public boolean canUse() {
            LivingEntity livingentity = getTarget();
            return livingentity != null && canAttack(livingentity, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            attackPhase = AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        @Override
        public void stop() {
            anchorPoint = level()
                    .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, anchorPoint)
                    .above(10 + random.nextInt(20));
        }

        @Override
        public void tick() {
            if (attackPhase == AttackPhase.CIRCLE) {
                this.nextSweepTick--;
                if (this.nextSweepTick <= 0) {
                    attackPhase = AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((8 + random.nextInt(4)) * 20);
                    playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + random.nextFloat() * 0.1F);
                }
            }
        }

        private void setAnchorAboveTarget() {
            anchorPoint = Objects.requireNonNull(getTarget()).blockPosition().above(20 + random.nextInt(20));
            if (anchorPoint.getY() < level().getSeaLevel()) {
                anchorPoint = new BlockPos(
                        anchorPoint.getX(), level().getSeaLevel() + 1, anchorPoint.getZ()
                );
            }
        }
    }
    class CircleAroundAnchorGoal extends MoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        @Override
        public boolean canUse() {
            return attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            this.distance = 5.0F + random.nextFloat() * 10.0F;
            this.height = -4.0F + random.nextFloat() * 9.0F;
            this.clockwise = random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
            setNoGravity(true);
        }

        @Override
        public void tick() {
            if (random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + random.nextFloat() * 9.0F;
            }

            if (random.nextInt(this.adjustedTickDelay(250)) == 0) {
                this.distance++;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = random.nextFloat() * 2.0F * (float) Math.PI;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (moveTargetPoint.y < getY() && !level().isEmptyBlock(blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (moveTargetPoint.y > getY() && !level().isEmptyBlock(blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }
        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(anchorPoint)) {
                anchorPoint = blockPosition();
            }

            this.angle = this.angle + this.clockwise * 15.0F * (float) (Math.PI / 180.0);
            moveTargetPoint = Vec3.atLowerCornerOf(anchorPoint)
                    .add(this.distance * Mth.cos(this.angle), -4.0F + this.height, this.distance * Mth.sin(this.angle));
        }
    }
    class SweepAttackGoal extends MoveTargetGoal {

        @Override
        public boolean canUse() {
            return getTarget() != null && attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingentity = getTarget();
            if (livingentity == null) {
                setNoGravity(false);
                return false;
            } else if (!livingentity.isAlive()) {
                setNoGravity(false);
                return false;
            } else {
                if (livingentity instanceof Player player && (livingentity.isSpectator() || player.isCreative())) {
                    setNoGravity(false);
                    return false;
                }

                if (!this.canUse()) {
                    setNoGravity(false);
                    return false;
                }
                return true;
            }
        }
    }
    abstract class MoveTargetGoal extends Goal {
        public MoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return moveTargetPoint.distanceToSqr(getX(), getY(), getZ()) < 4.0;
        }
    }


    protected class KeepOnTargetGoal extends Goal {
        private final int FIND_PATH_TIME = 200;
        int timeToRepath;
        CWHovercraft bee;

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
                bee.navigation.moveTo(bee.navigation.createPath(BlockPos.containing(vec3), 1), 0.5f);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3= CWHovercraft.this.getViewVector(0.0F);

            Vec3 vec32 = HoverRandomPos.getPos(CWHovercraft.this, 8, 7, vec3.x, vec3.z, 1.5707964F, 6, 3);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(CWHovercraft.this, 8, 4, -2, vec3.x, vec3.z, 1.5707963705062866);
        }

        public void tick() {
            bee.lookControl.setLookAt(Objects.requireNonNull(bee.getTarget()));
            bee.lookAt(bee.getTarget(), 360, 360);
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

    class HMoveControl extends MoveControl {
        private float speed = 0.1F;

        public HMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (horizontalCollision) {
                setYRot(getYRot() + 180.0F);
                this.speed = 0.1F;
            }

            double d0 = moveTargetPoint.x - getX();
            double d1 = moveTargetPoint.y - getY();
            double d2 = moveTargetPoint.z - getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d3) > 1.0E-5F) {
                double d4 = 1.0 - Math.abs(d1 * 0.7F) / d3;
                d0 *= d4;
                d2 *= d4;
                d3 = Math.sqrt(d0 * d0 + d2 * d2);
                double d5 = Math.sqrt(d0 * d0 + d2 * d2 + d1 * d1);
                float f = getYRot();
                float f1 = (float)Mth.atan2(d2, d0);
                float f2 = Mth.wrapDegrees(getYRot() + 90.0F);
                float f3 = Mth.wrapDegrees(f1 * (180.0F / (float)Math.PI));
                setYRot(Mth.approachDegrees(f2, f3, 4.0F) - 90.0F);
                yBodyRot = getYRot();
                if (Mth.degreesDifferenceAbs(f, getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 1.8F, 0.005F * (1.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }

                float f4 = (float)(-(Mth.atan2(-d1, d3) * 180.0F / (float)Math.PI));
                setXRot(f4);
                float f5 = getYRot() + 90.0F;
                double d6 = (double)(this.speed * Mth.cos(f5 * (float) (Math.PI / 180.0))) * Math.abs(d0 / d5);
                double d7 = (double)(this.speed * Mth.sin(f5 * (float) (Math.PI / 180.0))) * Math.abs(d2 / d5);
                double d8 = (double)(this.speed * Mth.sin(f4 * (float) (Math.PI / 180.0))) * Math.abs(d1 / d5);
                Vec3 vec3 = getDeltaMovement();
                setDeltaMovement(vec3.add(new Vec3(d6, d8, d7).subtract(vec3).scale(0.2)));
            }
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
