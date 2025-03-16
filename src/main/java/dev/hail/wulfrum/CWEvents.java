package dev.hail.wulfrum;

import dev.hail.wulfrum.item.CWItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.confluence.terra_curio.api.event.RegisterAccessoriesComponentUpdateEvent;

import static org.confluence.mod.Confluence.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CWEvents {

    @SubscribeEvent
    public static void registerUnitType(RegisterAccessoriesComponentUpdateEvent.UnitType event) {
        event.register(CWItems.WULFRUM$BATTERY);
    }
}
