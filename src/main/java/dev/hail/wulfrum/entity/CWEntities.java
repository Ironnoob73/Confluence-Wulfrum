package dev.hail.wulfrum.entity;

import dev.hail.wulfrum.WulfrumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraentity.init.TEEntities;

public class CWEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, WulfrumMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CWHovercraft>> WULFRUM_HOVERCRAFT = TEEntities.registerEntity("wulfrum_hovercraft", CWHovercraft::new,1.1F, 1.1F);
}
