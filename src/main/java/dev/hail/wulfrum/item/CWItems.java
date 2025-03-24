package dev.hail.wulfrum.item;

import dev.hail.wulfrum.WulfrumMod;
import dev.hail.wulfrum.entity.CWEntities;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.mod.common.item.CustomRarityItem;
import org.confluence.terra_curio.api.primitive.UnitValue;
import org.confluence.terra_curio.api.primitive.ValueType;
import org.confluence.terra_curio.common.component.ModRarity;
import org.confluence.terra_curio.common.init.TCAttributes;
import org.confluence.terra_curio.common.item.curio.BaseCurioItem;
import org.confluence.terraentity.init.TEItems;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
import static org.confluence.terra_curio.common.component.AccessoriesComponent.units;

public class CWItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WulfrumMod.MODID);
    public static final DeferredItem<Item> WULFRUM_SCRAP = register("wulfrum_scrap", ModRarity.BLUE);
    public static final DeferredItem<Item> ENERGY_CORE = register("energy_core", ModRarity.BLUE);
    public static final ValueType<Unit, UnitValue> WULFRUM$BATTERY = ValueType.ofUnit("wulfrum_battery");
    public static final Supplier<BaseCurioItem> WULFRUM_BATTERY = registerCurio("wulfrum_battery", builder -> builder
            .rarity(ModRarity.BLUE)
            .accessories(units(WULFRUM$BATTERY))
            .attribute(TCAttributes.getMagicDamage(), 0.07, ADD_MULTIPLIED_TOTAL)
            .tooltips(1));

    public static final Supplier<CWBlunderbussItem> WULFRUM_BLUNDERBUSS = ITEMS.registerItem("wulfrum_blunderbuss", properties -> new CWBlunderbussItem(new Item.Properties().durability(30), ModRarity.BLUE, 7.3F, 75, 25, 2.25F, 0.04F, 10));

    public static final DeferredItem<Item> WULFRUM_HOVERCRAFT_SPAWN_EGG = TEItems.registerEgg("wulfrum_hovercraft_spawn_egg", CWEntities.WULFRUM_HOVERCRAFT, 0xffffff, 0xab0d0d);

    public static DeferredItem<Item> register(String id, ModRarity rarity) {
        return ITEMS.register(id, () -> new CustomRarityItem(rarity));
    }
    private static Supplier<BaseCurioItem> registerCurio(String name, Consumer<BaseCurioItem.Builder> consumer) {
        return ITEMS.register(name, () -> {
            BaseCurioItem.Builder builder = BaseCurioItem.builder(name);
            consumer.accept(builder);
            return builder.build();
        });
    }
}
