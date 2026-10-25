package train;

import ebf.tim.TrainsInMotion;
import ebf.tim.networking.PacketInteract;
import ebf.tim.registry.TiMGenericRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
                ebf.tim.items.ItemRail.addCreativeVariants(output::accept);
                for (var item : TiMGenericRegistry.TRANSPORT_ITEMS) {
                    output.accept(item.get());
                }
            })
            .build());

    public Traincraft(IEventBus modBus) {
        TrainRegistry.register();
        TiMGenericRegistry.ENTITIES.register(modBus);
        ebf.tim.registry.TiMBlocks.BLOCKS.register(modBus);
        ebf.tim.registry.TiMBlocks.TILES.register(modBus);
        TiMGenericRegistry.ITEMS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerPayloads);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(TiMGenericRegistry::registerSkins);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(PacketInteract.TYPE, PacketInteract.CODEC, PacketInteract::handle);
    }
}
