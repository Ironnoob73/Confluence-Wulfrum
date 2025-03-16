package dev.hail.wulfrum.block;

import dev.hail.wulfrum.WulfrumMod;
import dev.hail.wulfrum.item.CWItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CWBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WulfrumMod.MODID);
    public static final Supplier<Block> WULFRUM_PLATING = registerWithItem("wulfrum_plating", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    private static <B extends Block> Supplier<B> registerWithItem(String newName, Supplier<B> supplier) {
        DeferredBlock<B> block = BLOCKS.register(newName, supplier);
        CWItems.ITEMS.registerSimpleBlockItem(newName, block);
        return block;
    }
}
