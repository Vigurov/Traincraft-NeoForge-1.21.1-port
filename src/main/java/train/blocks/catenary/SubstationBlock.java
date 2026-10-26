package train.blocks.catenary;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A traction substation, 2 x 2 blocks and 2 high (this block at its north west bottom corner, the rest parts):
 * takes energy (FE) from the generators its power cables come from and from the blocks beside it, and feeds the
 * catenary through feeder cables strung from it to masts. A real one turns the grid's high voltage into the line's
 * 3 kV.
 */
public class SubstationBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<SubstationBlock> CODEC = simpleCodec(SubstationBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public SubstationBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    /** the 2 x 2 x 2 blocks it takes, from its corner */
    public static Iterable<BlockPos> cells(BlockPos corner) {
        return BlockPos.betweenClosed(corner, corner.offset(1, 1, 1));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        for (BlockPos p : cells(c.getClickedPos())) {
            if (!p.equals(c.getClickedPos()) && !c.getLevel().getBlockState(p).canBeReplaced()) return null;
        }
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide || old.is(this)) return;
        for (BlockPos p : cells(pos)) {
            if (!p.equals(pos)) level.setBlock(p, train.blocks.TCBlocks.SUBSTATION_PART.get().defaultBlockState(), 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SubstationTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof SubstationTile t) t.tickServer();
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SubstationTile t && !level.isClientSide) {
            t.dropAllFeeders();
        }
        super.onRemove(state, level, pos, newState, moved);
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            for (BlockPos p : cells(pos)) {
                if (!p.equals(pos) && level.getBlockState(p).is(train.blocks.TCBlocks.SUBSTATION_PART.get())) level.removeBlock(p, false);
            }
        }
    }
}
