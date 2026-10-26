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
            e.register(train.blocks.TCBlocks.WORKBENCH_MENU.get(), train.client.gui.GuiTrainCraftingBlock::new);
        });
        modBus.addListener(this::armourColours);
        modBus.addListener(this::armourExtensions);
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e) -> bookPages());
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e) -> train.devtest.AutoTest.init());
        NeoForge.EVENT_BUS.addListener(ClientKeyHandler::onClientTick);
    }

    private static java.util.List<train.items.ItemTCArmor> paintable() {
        java.util.List<train.items.ItemTCArmor> l = new java.util.ArrayList<>();
        for (var i : train.items.TCItems.ARMOUR) {
            if (i.get().color != 0) l.add(i.get());
        }
        return l;
    }

    /** dyed clothes: the item icon's layer 0 is the dyed part, layer 1 the overlay (1.12 ArmourColours) */
    private void armourColours(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? net.minecraft.util.FastColor.ARGB32.opaque(((train.items.ItemTCArmor) stack.getItem()).getColor(stack)) : -1,
                paintable().toArray(new net.minecraft.world.item.Item[0]));
    }

    /** on the body, an undyed piece has its own colour, not leather's */
    private void armourExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public int getDefaultDyeColor(net.minecraft.world.item.ItemStack stack) {
                return net.minecraft.util.FastColor.ARGB32.opaque(((train.items.ItemTCArmor) stack.getItem()).getColor(stack));
            }
        }, paintable().toArray(new net.minecraft.world.item.Item[0]));
        //the switch stand and signal items draw their models
        event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new train.client.render.RenderSwitchBlocks.Items();
                return renderer;
            }
        }, train.blocks.TCBlocks.HIGH_STAR.get().asItem(), train.blocks.TCBlocks.SIGNAL.get().asItem());
    }

    /** the recipe book: opening it, and Traincraft's pages (1.12 Traincraft.load) */
    private static void bookPages() {
        train.items.ItemCraftGuide.openScreen = () -> net.minecraft.client.Minecraft.getInstance().setScreen(new ebf.tim.gui.GUICraftBook());
        ebf.tim.gui.GUICraftBook.addPage(Traincraft.MODID, "Traincraft\n" +
                "Developers: \n" +
                "Eternal Blue Flame\n" +
                "Canitzp, ComputerButter\n\n" +
                "Project Overseer:\nSpitfire4466\n\n" +
                "Lead artists: \nBroscolotos, Riggs64");
        ebf.tim.gui.GUICraftBook.addPage(Traincraft.MODID, "Honorable Mentions:\n" +
                "Mr. Brutal,\n" +
                "helldiver, DAYdiecast,\n" +
                "BlockStormTwo, FriscoWolf,\n" +
                "ChandlerBingUA, KiraKun,\n" +
                "NitroxydeX");
        ebf.tim.gui.GUICraftBook.addPage(Traincraft.MODID, "DISCLAIMER:\n" +
                "All transport into including\n" +
                "but not limited to\n" +
                "weight, year, country, \n" +
                "seating capacity, etc...\n" +
                "may be inaccurate, this\n" +
                "is written to the best of \n" +
                "our knowledge and we\n" +
                "encourage the community to\n" +
                "correct us, with citation.");
        ebf.tim.gui.GUICraftBook.addPage(Traincraft.MODID,
                "WARNING:\nThis release is an alpha,\n" +
                        "and some features may be\n" +
                        "missing, buggy, or\n" +
                        "incomplete.\n" +
                        "We appreciate your\n" +
                        "patience and reports as\n" +
                        "we work on adding back all\n" +
                        "of the missing features,\nand many many more.");
        ebf.tim.gui.GUICraftBook.addPage(Traincraft.MODID,
                "I WILL STATE THIS AGAIN\n" +
                        "This release is an alpha,\n" +
                        "and some features may be\n" +
                        "missing, buggy, or\n" +
                        "incomplete.\n" +
                        "We appreciate your\n" +
                        "patience and reports as\n" +
                        "we work on adding back all\n" +
                        "of the missing features,\nand many many more.");
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
        event.registerBlockEntityRenderer(train.blocks.TCBlocks.PILLAR_TILE.get(), train.client.render.RenderBridgePillar::new);
        event.registerBlockEntityRenderer(train.blocks.TCBlocks.SWITCH_STAND_TILE.get(), train.client.render.RenderSwitchBlocks::new);
        event.registerBlockEntityRenderer(train.blocks.TCBlocks.SIGNAL_TILE.get(), train.client.render.RenderSwitchBlocks::new);
        train.blocks.lantern.LanternBlock.openScreen = t -> net.minecraft.client.Minecraft.getInstance().setScreen(new train.client.gui.LanternScreen(t));
    }
}
