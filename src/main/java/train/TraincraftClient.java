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
    }
}
