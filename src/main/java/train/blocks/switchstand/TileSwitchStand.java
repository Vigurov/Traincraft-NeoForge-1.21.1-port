package train.blocks.switchstand;

import ebf.tim.blocks.TileSwitch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import train.blocks.TCBlocks;

/** the high star switch stand: on or off, drawn by RenderSwitchBlocks */
public class TileSwitchStand extends TileSwitch {
    public TileSwitchStand(BlockPos pos, BlockState state) {
        super(TCBlocks.SWITCH_STAND_TILE.get(), pos, state);
    }
}
