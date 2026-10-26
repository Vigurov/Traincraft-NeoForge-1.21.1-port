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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A catenary mast: stands beside the track with its cantilever over it (towards whoever placed it). Masts are
 * joined by the catenary wire item; the line it makes carries energy to the electric trains under it.
 */
public class CatenaryMastBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<CatenaryMastBlock> CODEC = simpleCodec(CatenaryMastBlock::new);
    private static final VoxelShape MAST = Block.box(5, 0, 5, 11, 16, 11);

    public CatenaryMastBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.EAST));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    /** the blocks above it the mast stands in (about 4.4 high) */
    public static final int PARTS = 4;

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        for (int i = 1; i <= PARTS; i++) {
            if (!c.getLevel().getBlockState(c.getClickedPos().above(i)).canBeReplaced()) return null;
        }
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide || old.is(this)) return;
        for (int i = 1; i <= PARTS; i++) {
            if (level.getBlockState(pos.above(i)).canBeReplaced()) {
                level.setBlock(pos.above(i), train.blocks.TCBlocks.CATENARY_MAST_PART.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return MAST;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CatenaryMastTile(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        //a mast taken down lets its spans go (each span gives its wire back)
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CatenaryMastTile t && !level.isClientSide) {
            t.dropAllSpans();
        }
        super.onRemove(state, level, pos, newState, moved);
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            for (int i = 1; i <= PARTS; i++) {
                if (level.getBlockState(pos.above(i)).is(train.blocks.TCBlocks.CATENARY_MAST_PART.get())) level.removeBlock(pos.above(i), false);
            }
        }
    }
}
