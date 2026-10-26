package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** the mast above its bottom block, as high as it stands: solid where the lattice is; breaking it takes the mast down */
public class CatenaryMastPartBlock extends Block {
    private static final VoxelShape MAST = Block.box(5, 0, 5, 11, 16, 11);

    public CatenaryMastPartBlock(Properties props) {
        super(props);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return MAST;
    }

    /** the mast this part belongs to, below it */
    public static BlockPos master(BlockGetter level, BlockPos pos) {
        for (int i = 1; i <= CatenaryMastBlock.PARTS; i++) {
            if (level.getBlockState(pos.below(i)).getBlock() instanceof CatenaryMastBlock) return pos.below(i);
        }
        return null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (state.is(newState.getBlock()) || level.isClientSide) return;
        BlockPos m = master(level, pos);
        if (m != null) level.destroyBlock(m, true);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(train.blocks.TCBlocks.CATENARY_MAST.get());
    }
}
