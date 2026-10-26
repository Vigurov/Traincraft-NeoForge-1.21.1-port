package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** the rest of a substation's 2 x 2 x 2 blocks: solid, drawn by the substation itself; breaking one takes it all down */
public class SubstationPartBlock extends Block {
    public SubstationPartBlock(Properties props) {
        super(props);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** the substation block this part belongs to: one of the blocks down, west and north of it */
    public static BlockPos master(BlockGetter level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos)) {
            if (level.getBlockState(p).getBlock() instanceof SubstationBlock) return p.immutable();
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(train.blocks.TCBlocks.SUBSTATION.get());
    }
}
