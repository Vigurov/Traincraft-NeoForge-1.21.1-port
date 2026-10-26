package train;

import ebf.tim.registry.TiMGenericRegistry;
import ebf.tim.render.RenderWagon;
import ebf.tim.utility.ClientKeyHandler;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Traincraft.MODID, dist = Dist.CLIENT)
public class TraincraftClient {
    public TraincraftClient(IEventBus modBus) {
        modBus.addListener(ClientKeyHandler::register);
        modBus.addListener(this::registerRenderers);
        modBus.addListener(ebf.tim.registry.TiMFluids::registerClient);
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e) ->
        {
            e.register(ebf.tim.registry.TiMMenus.TRANSPORT.get(), ebf.tim.gui.TransportScreen::new);
            e.register(ebf.tim.registry.TiMMenus.TABLE.get(), ebf.tim.gui.TableScreen::new);
            e.register(train.blocks.TCBlocks.MACHINE_MENU.get(), train.blocks.machines.MachineScreen::new);
        });
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e) -> train.devtest.AutoTest.init());
        NeoForge.EVENT_BUS.addListener(ClientKeyHandler::onClientTick);
    }

    @SuppressWarnings("unchecked")
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (var type : TiMGenericRegistry.TRANSPORTS) {
            event.registerEntityRenderer((EntityType) type.get(), RenderWagon::new);
        }
        event.registerBlockEntityRenderer(ebf.tim.registry.TiMBlocks.RAIL_TILE.get(), ebf.tim.render.RailRenderer::new);
        event.registerBlockEntityRenderer(train.common.ClassicTracks.TILE_RAIL, train.client.render.RenderTCRail::new);
        event.registerBlockEntityRenderer(train.blocks.TCBlocks.MACHINE_TILE.get(), train.client.render.RenderGeneratorDiesel::new);
        event.registerBlockEntityRenderer(train.blocks.TCBlocks.LANTERN_TILE.get(), train.client.render.RenderLantern::new);
        train.blocks.lantern.LanternBlock.openScreen = t -> net.minecraft.client.Minecraft.getInstance().setScreen(new train.client.gui.LanternScreen(t));
    }
}
