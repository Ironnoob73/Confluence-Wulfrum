package dev.hail.wulfrum.item;

import dev.hail.wulfrum.WulfrumMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CWItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WulfrumMod.MODID);
    public static final DeferredItem<Item> WULFRUM_SCRAP = ITEMS.registerSimpleItem("wulfrum_scrap", new Item.Properties());
}
