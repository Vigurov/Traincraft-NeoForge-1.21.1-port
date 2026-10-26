package train.blocks;

import ebf.tim.registry.TiMBlocks;
import ebf.tim.registry.TiMGenericRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import train.blocks.machines.MachineBlock;
import train.blocks.machines.MachineMenu;
import train.blocks.machines.MachineTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Traincraft's own blocks and items (the TiM ones are in ebf.tim.registry): machines, oil sand and petroleum ore,
 * ballast, and the parts that are not TiM parts. Names are the 1.12 ones.
 */
public class TCBlocks {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "traincraft");
    public static final List<DeferredItem<?>> ITEMS = new ArrayList<>();

    public static final DeferredBlock<MachineBlock> DISTIL = machine("block.distil", "distil", MapColor.METAL);
    public static final DeferredBlock<MachineBlock> HEARTH_FURNACE = machine("block.hearthfurnace", "hearthfurnace", MapColor.STONE);
    public static final DeferredBlock<MachineBlock> DIESEL_GENERATOR = machine("block.dieselgenerator", "dieselgenerator", MapColor.METAL);
    public static final DeferredBlock<MachineBlock> WINDMILL = machine("block.windmill", "windmill", MapColor.METAL);
    public static final DeferredBlock<MachineBlock> WATER_WHEEL = machine("block.waterwheel", "waterwheel", MapColor.WOOD);
    public static final DeferredBlock<train.blocks.BridgePillarBlock> BRIDGE_PILLAR = block("block.bridgepillar", () -> new train.blocks.BridgePillarBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> ORE_PETROLEUM = simple("block.petroleum", BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
            .strength(3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> OIL_SAND = simple("block.oilsand", BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
            .strength(2.0F).sound(SoundType.SAND));
    public static final DeferredBlock<Block> BALLAST = simple("block.ballast", BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
            .strength(3.0F, 6.0F).sound(SoundType.GRAVEL));

    public static final DeferredBlock<train.blocks.lantern.LanternBlock> LANTERN = block("block.lantern", () -> new train.blocks.lantern.LanternBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()));

    /** 1.12: wood, the TiM train table's textures */
    public static final DeferredBlock<train.blocks.bench.BlockTrainWorkbench> PART_TABLE = block("block.parttable", () -> new train.blocks.bench.BlockTrainWorkbench(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)));

    /** 1.12 BlockSwitchStand (rock) and BlockTCSignal (lit like a lamp); both drawn by RenderSwitchBlocks */
    public static final DeferredBlock<ebf.tim.blocks.BlockSwitch> HIGH_STAR = block("block.highstar", () -> new ebf.tim.blocks.BlockSwitch(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL).noOcclusion(),
            train.blocks.switchstand.TileSwitchStand::new));
    public static final DeferredBlock<ebf.tim.blocks.BlockSignal> SIGNAL = block("block.signal", () -> new ebf.tim.blocks.BlockSignal(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL).noOcclusion().lightLevel(s -> 15),
            train.blocks.signal.TileTCSignal::new));

    /** catenary: masts beside the track and the wire strung between them */
    public static final DeferredBlock<train.blocks.catenary.CatenaryMastBlock> CATENARY_MAST = block("catenary_mast", () -> new train.blocks.catenary.CatenaryMastBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredItem<train.blocks.catenary.CatenaryWireItem> CATENARY_WIRE = TiMGenericRegistry.ITEMS.register("catenary_wire",
            () -> new train.blocks.catenary.CatenaryWireItem(new Item.Properties()));
    /** a traction substation and the feeder cable from it to a mast */
    public static final DeferredBlock<train.blocks.catenary.SubstationBlock> SUBSTATION = block("substation", () -> new train.blocks.catenary.SubstationBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 8.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredItem<train.blocks.catenary.FeederCableItem> FEEDER_CABLE = TiMGenericRegistry.ITEMS.register("feeder_cable",
            () -> new train.blocks.catenary.FeederCableItem(new Item.Properties()));

    public static final DeferredItem<Item> BALLOON = item("balloon"), PROPELLER = item("propeller"),
            COPPER_RAIL = item("copperrail"), STEEL_RAIL = item("steelrail"),
            REINFORCED_PLASTIC = item("reinforcedplastic"), REINFORCED_PLATES = item("reinforcedplates"),
            COMPOSITE_WRENCH = TiMGenericRegistry.ITEMS.registerSimpleItem("composite_wrench", new Item.Properties().stacksTo(1));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineTile>> MACHINE_TILE = TiMBlocks.TILES.register("machine",
            () -> BlockEntityType.Builder.of(MachineTile::new, DISTIL.get(), HEARTH_FURNACE.get(), DIESEL_GENERATOR.get(), WINDMILL.get(), WATER_WHEEL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.BridgePillarBlock.Tile>> PILLAR_TILE = TiMBlocks.TILES.register("bridgepillar",
            () -> BlockEntityType.Builder.of(train.blocks.BridgePillarBlock.Tile::new, BRIDGE_PILLAR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.lantern.LanternTile>> LANTERN_TILE = TiMBlocks.TILES.register("lantern",
            () -> BlockEntityType.Builder.of(train.blocks.lantern.LanternTile::new, LANTERN.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.bench.BlockTrainWorkbench.Tile>> WORKBENCH_TILE = TiMBlocks.TILES.register("parttable",
            () -> BlockEntityType.Builder.of(train.blocks.bench.BlockTrainWorkbench.Tile::new, PART_TABLE.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.switchstand.TileSwitchStand>> SWITCH_STAND_TILE = TiMBlocks.TILES.register("highstar",
            () -> BlockEntityType.Builder.of(train.blocks.switchstand.TileSwitchStand::new, HIGH_STAR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.signal.TileTCSignal>> SIGNAL_TILE = TiMBlocks.TILES.register("signal",
            () -> BlockEntityType.Builder.of(train.blocks.signal.TileTCSignal::new, SIGNAL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.catenary.SubstationTile>> SUBSTATION_TILE = TiMBlocks.TILES.register("substation",
            () -> BlockEntityType.Builder.of(train.blocks.catenary.SubstationTile::new, SUBSTATION.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<train.blocks.catenary.CatenaryMastTile>> CATENARY_TILE = TiMBlocks.TILES.register("catenary_mast",
            () -> BlockEntityType.Builder.of(train.blocks.catenary.CatenaryMastTile::new, CATENARY_MAST.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<train.blocks.bench.TrainWorkbenchMenu>> WORKBENCH_MENU =
            MENUS.register("trainworkbench", () -> IMenuTypeExtension.create(train.blocks.bench.TrainWorkbenchMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE_MENU =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::fromNetwork));

    static {
        ITEMS.add(COMPOSITE_WRENCH);
        ITEMS.add(CATENARY_WIRE);
        ITEMS.add(FEEDER_CABLE);
    }

    private static DeferredBlock<MachineBlock> machine(String name, String kind, MapColor color) {
        DeferredBlock<MachineBlock> b = TiMBlocks.BLOCKS.register(name, () -> new MachineBlock(BlockBehaviour.Properties.of()
                .mapColor(color).strength(3.5F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()
                .lightLevel(s -> s.getValue(MachineBlock.LIT) && !kind.equals("windmill") && !kind.equals("waterwheel") ? 13 : 0), kind));
        ITEMS.add(TiMGenericRegistry.ITEMS.registerSimpleBlockItem(b));
        return b;
    }

    private static <B extends Block> DeferredBlock<B> block(String name, java.util.function.Supplier<B> b) {
        DeferredBlock<B> r = TiMBlocks.BLOCKS.register(name, b);
        ITEMS.add(TiMGenericRegistry.ITEMS.registerSimpleBlockItem(r));
        return r;
    }

    private static DeferredBlock<Block> simple(String name, BlockBehaviour.Properties props) {
        DeferredBlock<Block> b = TiMBlocks.BLOCKS.registerSimpleBlock(name, props);
        ITEMS.add(TiMGenericRegistry.ITEMS.registerSimpleBlockItem(b));
        return b;
    }

    private static DeferredItem<Item> item(String name) {
        DeferredItem<Item> i = TiMGenericRegistry.ITEMS.registerSimpleItem(name);
        ITEMS.add(i);
        return i;
    }

    public static void init() {}
}
