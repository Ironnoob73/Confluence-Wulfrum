package dev.hail.wulfrum;

import com.mojang.logging.LogUtils;
import dev.hail.wulfrum.block.CWBlocks;
import dev.hail.wulfrum.client.CWClientEvent;
import dev.hail.wulfrum.entity.CWEntities;
import dev.hail.wulfrum.entity.CWHovercraft;
import dev.hail.wulfrum.entity.CWHovercraftRenderer;
import dev.hail.wulfrum.item.CWItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.mod.Confluence;
import org.slf4j.Logger;

import java.util.function.Consumer;

@Mod(WulfrumMod.MODID)
public class WulfrumMod
{
    public static final String MODID = "confluence_wulfrum";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("confluence_wulfrum.name"))
            .icon(() -> CWItems.WULFRUM_SCRAP.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CWItems.WULFRUM_SCRAP.get());
                output.accept(CWItems.ENERGY_CORE.get());
                output.accept(CWItems.WULFRUM_BATTERY.get());
                output.accept(CWBlocks.WULFRUM_PLATING.get().asItem());
                output.accept(CWItems.WULFRUM_BLUNDERBUSS.get());
                output.accept(CWItems.WULFRUM_HOVERCRAFT_SPAWN_EGG.get());
            }).build());

    public WulfrumMod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);

        CWBlocks.BLOCKS.register(modEventBus);
        CWItems.ITEMS.register(modEventBus);
        CWEntities.ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath("confluence_wulfrum", path);
    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            eventBus(CWClientEvent::register);
        }
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(CWEntities.WULFRUM_HOVERCRAFT.get(), CWHovercraftRenderer::new);
        }
        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(CWEntities.WULFRUM_HOVERCRAFT.get(), CWHovercraft.createAttributes().build());
        }
    }
    static void eventBus(Consumer<IEventBus> consumer) {
        ModList.get().getModContainerById(Confluence.MODID).ifPresent(container -> {
            IEventBus eventBus = container.getEventBus();
            if (eventBus != null) consumer.accept(eventBus);
        });
    }
}
