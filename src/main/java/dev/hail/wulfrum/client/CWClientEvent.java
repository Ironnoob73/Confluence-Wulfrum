package dev.hail.wulfrum.client;

import dev.hail.wulfrum.block.CWBlocks;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import org.confluence.mod.client.connected.CTModel;
import org.confluence.mod.client.connected.ModelSwapper;
import org.confluence.mod.client.connected.behaviour.ConnectedTextureBehaviour;
import org.confluence.mod.client.connected.behaviour.SimpleCTBehaviour;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class CWClientEvent {
    public static final ModelSwapper MODEL_SWAPPER = new ModelSwapper();

    public static void register(IEventBus modEventBus) {
        MODEL_SWAPPER.registerListeners(modEventBus);

        register(CWBlocks.WULFRUM_PLATING.get(), () -> new SimpleCTBehaviour(CWAllSpriteShifts.WULFRUM_PLATING));
    }

    private static void register(Block entry, Supplier<ConnectedTextureBehaviour> behaviorSupplier) {
        MODEL_SWAPPER.getCustomBlockModels().register(entry, model -> new CTModel(model, behaviorSupplier.get()));
    }
}
