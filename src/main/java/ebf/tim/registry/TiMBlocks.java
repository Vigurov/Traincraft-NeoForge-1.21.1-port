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

    public static final DeferredItem<ItemRail> RAIL_ITEM = TiMGenericRegistry.ITEMS.register("rail", () -> new ItemRail(new Item.Properties()));

    public static Item railItem() {
        return RAIL_ITEM.get();
    }
}
