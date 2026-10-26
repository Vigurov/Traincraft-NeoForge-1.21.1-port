package ebf.tim.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.BiFunction;

/**
 * A TiM signal (1.12 BlockSignal): a switch that gives no power and can't be clicked; it shows the redstone it
 * gets, and its tile may look for trains on its own.
 */
public class BlockSignal extends BlockSwitch {
    public BlockSignal(Properties properties, BiFunction<BlockPos, BlockState, ? extends TileSwitch> tile) {
        super(properties, tile);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileSwitch t) {
            t.setStrength(level.getBestNeighborSignal(pos));
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, from, isMoving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileSwitch t) {
            t.setStrength(level.getBestNeighborSignal(pos));
        }
    }

    /** the tile's own update (1.12 ITickable), on the server */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, t) -> {
            if (t instanceof Ticking tick) tick.serverTick();
        };
    }

    /** a signal tile that updates itself */
    public interface Ticking {
        void serverTick();
    }
}
