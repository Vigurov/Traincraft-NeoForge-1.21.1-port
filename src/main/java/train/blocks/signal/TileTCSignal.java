package train.blocks.signal;

import ebf.tim.blocks.BlockSignal;
import ebf.tim.blocks.TileSwitch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import train.blocks.TCBlocks;

/**
 * The Traincraft signal (the Swiss one): green while a train is in the 3 by 16 blocks in front of it or it is
 * powered, red otherwise; it looks every 5 ticks.
 */
public class TileTCSignal extends TileSwitch implements BlockSignal.Ticking {
    public TileTCSignal(BlockPos pos, BlockState state) {
        super(TCBlocks.SIGNAL_TILE.get(), pos, state);
    }

    @Override
    public void serverTick() {
        tickTile();
        if (currentTick % 5 == 0) {
            detectNearby(3, 16, true, false);
        }
    }
}
