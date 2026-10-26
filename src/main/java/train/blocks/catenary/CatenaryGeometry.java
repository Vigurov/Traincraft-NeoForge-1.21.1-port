package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** where a mast holds its wires: the same for drawing them and for the trains under them */
public final class CatenaryGeometry {
    private CatenaryGeometry() {}

    /** the cantilever's reach (to the middle of the track beside the mast) and the wires' heights, in blocks */
    public static final double REACH = 1.0, CONTACT_Y = 3.03, MESSENGER_Y = 59.4 / 16;

    public static Vec3 anchor(BlockPos pos, BlockState state, double height) {
        Direction d = state.hasProperty(CatenaryMastBlock.FACING) ? state.getValue(CatenaryMastBlock.FACING) : Direction.EAST;
        return new Vec3(pos.getX() + 0.5 + d.getStepX() * REACH, pos.getY() + height, pos.getZ() + 0.5 + d.getStepZ() * REACH);
    }
}
