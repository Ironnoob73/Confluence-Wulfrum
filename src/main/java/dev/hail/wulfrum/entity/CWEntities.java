package dev.hail.wulfrum.entity;

import dev.hail.wulfrum.WulfrumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CWEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, WulfrumMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CWHovercraft>> WULFRUM_HOVERCRAFT = registerEntity("wulfrum_hovercraft", (e,l)->new CWHovercraft(e,l, CWPrefab.HOVERCRAFT_BUILDER.get()),1.0F, 0.5F);

    public static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> entityFactory, float width, float height) {
        return registerEntity(name, entityFactory, MobCategory.MONSTER, width, height);
    }
    public static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> entityFactory, MobCategory category, float width, float height) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(entityFactory, category).sized(width, height).clientTrackingRange(10).build(Key(name)));
    }
    public static String Key(String key) {
        return "confluence_wulfrum:" + key;
    }
}
