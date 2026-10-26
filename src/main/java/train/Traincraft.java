package train;

import ebf.tim.TrainsInMotion;
import ebf.tim.networking.PacketInteract;
import ebf.tim.registry.TiMGenericRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import train.library.TrainRegistry;

@Mod(Traincraft.MODID)
public class Traincraft {
    public static final String MODID = TrainsInMotion.MODID;

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> tcTab = TABS.register("traincraft", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.traincraft"))
            .icon(() -> TiMGenericRegistry.TRANSPORT_ITEMS.isEmpty() ? new ItemStack(TiMGenericRegistry.STAKE.get()) : new ItemStack(TiMGenericRegistry.TRANSPORT_ITEMS.get(0).get()))
            .displayItems((params, output) -> {
                output.accept(TiMGenericRegistry.STAKE.get());
                for (var t : ebf.tim.registry.TiMBlocks.TABLE_ITEMS) output.accept(t.get());
                for (var t : train.blocks.TCBlocks.ITEMS) output.accept(t.get());
                output.accept(train.items.TCItems.recipeBook.get());
                for (var t : train.items.TCItems.ARMOUR) output.accept(t.get());
                ebf.tim.items.ItemRail.addCreativeVariants(output::accept);
                for (train.common.library.ItemIDs id : train.common.library.ItemIDs.values()) {
                    if (id.item != null && !train.common.library.ItemIDs.isRetired(id)) output.accept(id.item);
                }
                for (var item : TiMGenericRegistry.TRANSPORT_ITEMS) {
                    if (!train.core.handlers.ConfigHandler.enabled(item.get().info())) continue;
                    output.accept(item.get());
                }
            })
            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> timCraftingTab = TABS.register("timcrafting", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.timcrafting"))
            .icon(() -> new ItemStack(ebf.tim.registry.TiMItems.mediumDieselEngine.get()))
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(MODID, "traincraft"))
            .displayItems((params, output) -> {
                for (var i : ebf.tim.registry.TiMItems.PARTS) output.accept(i.get());
                for (var i : ebf.tim.registry.TiMOres.MATERIALS) output.accept(i.get());
                output.accept(ebf.tim.registry.TiMItems.emptyCanister.get());
                output.accept(ebf.tim.registry.TiMItems.dieselCanister.get());
                for (var f : ebf.tim.registry.TiMFluids.ALL) output.accept(f.bucket.get());
            })
            .build());

    public Traincraft(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        train.core.handlers.ConfigHandler.register(container, modBus);
        TrainRegistry.register();
        ebf.tim.registry.TiMItems.init();
        ebf.tim.registry.TiMOres.init();
        ebf.tim.registry.TiMFluids.init();
        ebf.tim.registry.TiMFluids.TYPES.register(modBus);
        ebf.tim.registry.TiMFluids.FLUIDS.register(modBus);
        ebf.tim.registry.TiMMenus.MENUS.register(modBus);
        train.blocks.TCBlocks.init();
        train.blocks.TCBlocks.MENUS.register(modBus);
        train.items.TCItems.init(modBus);
        modBus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e) ->
                e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, train.blocks.TCBlocks.MACHINE_TILE.get(),
                        (be, side) -> switch (be.kind) { case "dieselgenerator", "windmill", "waterwheel" -> be.energyOut; default -> null; }));
        TiMGenericRegistry.ENTITIES.register(modBus);
        ebf.tim.registry.TiMBlocks.BLOCKS.register(modBus);
        ebf.tim.registry.TiMBlocks.TILES.register(modBus);
        TiMGenericRegistry.ITEMS.register(modBus);
        TABS.register(modBus);
        train.common.ClassicTracks.register(modBus);
        modBus.addListener(this::commonSetup);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.TagsUpdatedEvent e) -> ebf.tim.utility.RecipeManager.tagsChanged());
        modBus.addListener(this::registerPayloads);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(TiMGenericRegistry::registerSkins);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(PacketInteract.TYPE, PacketInteract.CODEC, PacketInteract::handle)
                .playToServer(train.blocks.lantern.PacketLantern.TYPE, train.blocks.lantern.PacketLantern.CODEC, train.blocks.lantern.PacketLantern::handle)
                .playToServer(train.core.network.PacketSetJukeboxStreamingUrl.TYPE, train.core.network.PacketSetJukeboxStreamingUrl.CODEC, train.core.network.PacketSetJukeboxStreamingUrl::handle);
    }
}
