package dev.hail.wulfrum.entity;

import org.confluence.terraentity.entity.monster.AbstractMonster;
import org.confluence.terraentity.entity.monster.prefab.AbstractPrefab;
import org.confluence.terraentity.init.TESounds;

import java.util.function.Supplier;

public class CWPrefab extends AbstractPrefab {
    public CWPrefab(int health, int armor, int attack, int followRange, float knockBack, float knockbackResistance) {
        super(health, armor, attack, followRange, knockBack, knockbackResistance);
    }
    public static Supplier<AbstractMonster.Builder> HOVERCRAFT_BUILDER = () ->
            (new AbstractPrefab(15, 4, 12, 32, 0.0F, 0.9F))
                    .getPrefab().setHurtSound(TESounds.ROUTINE_HURT).setDeathSound(TESounds.ROUTINE_DEATH)
                    .setMovementSpeed(0.5F);
}
