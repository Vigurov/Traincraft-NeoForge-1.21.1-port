package ebf.tim.registry;

import ebf.tim.TrainsInMotion;
import ebf.tim.blocks.RailTileEntity;
import ebf.tim.blocks.rails.BlockRailCore;
import ebf.tim.items.ItemRail;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TiMBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TrainsInMotion.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TrainsInMotion.MODID);

    public static final DeferredBlock<BlockRailCore> RAIL = BLOCKS.register("timrail", () -> new BlockRailCore(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noCollission().strength(0.7F).sound(SoundType.METAL)
                    .noOcclusion().pushReaction(PushReaction.BLOCK)));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RailTileEntity>> RAIL_TILE = TILES.register("timrail",
            () -> BlockEntityType.Builder.of(RailTileEntity::new, RAIL.get()).build(null));

    public static final java.util.List<DeferredItem<?>> TABLE_ITEMS = new java.util.ArrayList<>();

    /** the tables: TiM's train and rail tables, Traincraft's assembly tables (tier 1-3) */
    public static final DeferredBlock<ebf.tim.blocks.BlockDynamic> TRAIN_TABLE = table("block.traintable", "traintable", MapColor.WOOD, SoundType.WOOD);
    public static final DeferredBlock<ebf.tim.blocks.BlockDynamic> RAIL_TABLE = table("block.railtable", "railtable", MapColor.METAL, SoundType.WOOD);
    public static final DeferredBlock<ebf.tim.blocks.BlockDynamic> TABLE_TIER1 = table("block.traintabletier1", "traintabletier1", MapColor.STONE, SoundType.STONE);
    public static final DeferredBlock<ebf.tim.blocks.BlockDynamic> TABLE_TIER2 = table("block.traintabletier2", "traintabletier2", MapColor.STONE, SoundType.STONE);
    public static final DeferredBlock<ebf.tim.blocks.BlockDynamic> TABLE_TIER3 = table("block.traintabletier3", "traintabletier3", MapColor.STONE, SoundType.STONE);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ebf.tim.blocks.TileEntityStorage>> TABLE_TILE = TILES.register("table",
            () -> BlockEntityType.Builder.of(ebf.tim.blocks.TileEntityStorage::new, TRAIN_TABLE.get(), RAIL_TABLE.get(),
                    TABLE_TIER1.get(), TABLE_TIER2.get(), TABLE_TIER3.get()).build(null));

    private static DeferredBlock<ebf.tim.blocks.BlockDynamic> table(String name, String table, MapColor color, SoundType sound) {
        DeferredBlock<ebf.tim.blocks.BlockDynamic> b = BLOCKS.register(name, () -> new ebf.tim.blocks.BlockDynamic(
                BlockBehaviour.Properties.of().mapColor(color).strength(2.5F).sound(sound), table));
        TABLE_ITEMS.add(TiMGenericRegistry.ITEMS.registerSimpleBlockItem(b));
        return b;
    }

    public static final DeferredItem<ItemRail> RAIL_ITEM = TiMGenericRegistry.ITEMS.register("rail", () -> new ItemRail(new Item.Properties()));

    public static Item railItem() {
        return RAIL_ITEM.get();
    }
}
