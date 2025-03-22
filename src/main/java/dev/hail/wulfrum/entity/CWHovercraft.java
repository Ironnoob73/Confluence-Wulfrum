package dev.hail.wulfrum.entity;

import dev.hail.wulfrum.WulfrumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class CWHovercraft extends AbstractMonster implements Enemy, GeoEntity, DeathAnimOptions {
    protected int attackInternal = 20;
    AttackPhase attackPhase = AttackPhase.CIRCLE;
    Vec3 moveTargetPoint = Vec3.ZERO;
    BlockPos anchorPoint = BlockPos.ZERO;
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);

    public CWHovercraft(EntityType<? extends CWHovercraft> entityType, Level level, Builder builder) {
        super(entityType, level, builder);
        this.moveControl = new FlyingMoveControl(this, 20, false);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                BlockPos ground = getBlockPosBelowThatAffectsMyMovement();
                float f = 0.91F;
                if (this.onGround()) {
                    f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                }

                float f1 = 0.16277137F / (f * f * f);
                f = 0.91F;
                if (this.onGround()) {
                    f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                }

                this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(f));
            }
        }
    }
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 2, true));
        //goalSelector.addGoal(2, new SweepAttackGoal());
        //goalSelector.addGoal(3, new CircleAroundAnchorGoal());
        goalSelector.addGoal(4, new KeepOnTargetGoal(this));

        registerTargetGoal(targetSelector);
    }
    protected void registerTargetGoal(GoalSelector targetSelector){
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    enum AttackPhase {
        CIRCLE,
        SWOOP
    }

    class CircleAroundAnchorGoal extends MoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        @Override
        public boolean canUse() {
            return getTarget() == null || attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            this.distance = 5.0F + random.nextFloat() * 10.0F;
            this.height = -4.0F + random.nextFloat() * 9.0F;
            this.clockwise = random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
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
        private static final int CAT_SEARCH_TICK_DELAY = 20;
        private boolean isScaredOfCat;
        private int catSearchTick;

        @Override
        public boolean canUse() {
            return getTarget() != null && attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingentity = getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                if (livingentity instanceof Player player && (livingentity.isSpectator() || player.isCreative())) {
                    return false;
                }

                if (!this.canUse()) {
                    return false;
                } else {
                    if (tickCount > this.catSearchTick) {
                        this.catSearchTick = tickCount + 20;
                        List<Cat> list = level()
                                .getEntitiesOfClass(Cat.class, getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);

                        for (Cat cat : list) {
                            cat.hiss();
                        }

                        this.isScaredOfCat = !list.isEmpty();
                    }

                    return !this.isScaredOfCat;
                }
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
            if(bee.distanceTo(bee.getTarget()) < 10){

            }
        }
    }

    @Override
    public void tick() {
        // TODO: 仇恨值
        Vec3 pos = position();
        /*LivingEntity livingentity = getTarget();
        if (livingentity != null) {
            moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ());
            if (getBoundingBox().inflate(0.2F).intersects(livingentity.getBoundingBox())) {
                doHurtTarget(livingentity);
                attackPhase = AttackPhase.CIRCLE;
                if (!isSilent()) {
                    level().levelEvent(1039, blockPosition(), 0);
                }
            } else if (horizontalCollision || hurtTime > 0) {
                attackPhase = AttackPhase.CIRCLE;
            }
        }*/
        //setTarget(level().getNearestPlayer(pos.x, pos.y, pos.z, 40, true));
        super.tick();
        // 在super.tick()结束后更新面向方向即可覆盖原版AI
        TEUtils.updateEntityRotation(this, this.getDeltaMovement().multiply(1, 1, 1));

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
