package ebf.tim.blocks.rails;

import ebf.tim.entities.EntityBogie;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;

/**
 * TiM rail. todo: port the 1.12 rail (tile entity data, shapes, render); for now a plain rail
 * @author Eternal Blue Flame
 */
public class BlockRailCore extends RailBlock {
    public BlockRailCore(Properties properties) {
        super(properties);
    }

    /**the rail shape for a bogie (1.12 getRailDirection with the cart), as 1.12 metadata*/
    public int getRailMeta(Level world, BlockPos pos, EntityBogie cart) {
        return getRailDirection(world.getBlockState(pos), world, pos, null).ordinal();
    }
}
