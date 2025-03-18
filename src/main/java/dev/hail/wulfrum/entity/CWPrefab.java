package dev.hail.wulfrum.entity;

import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import org.confluence.terraentity.entity.ai.goal.LookForwardWanderFlyGoal;
import org.confluence.terraentity.entity.monster.AbstractMonster;
import org.confluence.terraentity.entity.monster.prefab.AbstractPrefab;
import org.confluence.terraentity.init.TESounds;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.function.Supplier;

public class CWPrefab extends AbstractPrefab {
    public CWPrefab(int health, int armor, int attack, int followRange, float knockBack, float knockbackResistance) {
        super(health, armor, attack, followRange, knockBack, knockbackResistance);
        this.SIMPLE_MONSTER.setNavigation((e) -> new FlyingPathNavigation(e, e.level())).setSafeFall(1000.0F)
                .setNoGravity().setPushable(false).setNoFriction().addGoal((g, e) -> {
            g.addGoal(2, new LookForwardWanderFlyGoal(e, 0.2F, 0.0F));
        }).setController((c, e) -> {
            c.add(new AnimationController<>(e, "move", 10, (state) -> {
                state.setAnimation(RawAnimation.begin().thenLoop("fly"));
                return PlayState.CONTINUE;
            }));
        });
    }
    public static Supplier<AbstractMonster.Builder> HOVERCRAFT_BUILDER = () ->
            (new AbstractPrefab(15, 4, 12, 32, 0.0F, 0.9F))
                    .getPrefab().setHurtSound(TESounds.ROUTINE_HURT).setDeathSound(TESounds.ROUTINE_DEATH)
                    .setNoAttachAttack().setMovementSpeed(0.5F).setNoGravity();
}
