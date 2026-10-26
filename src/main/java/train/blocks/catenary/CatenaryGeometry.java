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

    /** how far a cantilever may reach out, from the mast's middle: they are made to measure for each mast */
    public static final double MIN_REACH = 0.75, MAX_REACH = 3.0;

    public static Vec3 anchor(BlockPos pos, BlockState state, double height) {
        return anchor(pos, state, height, REACH);
    }

    public static Vec3 anchor(BlockPos pos, BlockState state, double height, double reach) {
        Direction d = state.hasProperty(CatenaryMastBlock.FACING) ? state.getValue(CatenaryMastBlock.FACING) : Direction.EAST;
        return new Vec3(pos.getX() + 0.5 + d.getStepX() * reach, pos.getY() + height, pos.getZ() + 0.5 + d.getStepZ() * reach);
    }

    /** the cantilever of the mast at pos (its own reach when it is a loaded mast) */
    public static Vec3 anchor(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state, double height) {
        return anchor(pos, state, height, level.getBlockEntity(pos) instanceof CatenaryMastTile m ? m.reach : REACH);
    }

    /**
     * where the middle of the track is along the line from (ox, oz) in the direction (dx, dz), crossing the track
     * block b: on a classic turn where the line meets its arc, on anything else the block's middle. NaN when b is no
     * track
     */
    public static double trackCrossing(net.minecraft.world.level.BlockGetter level, BlockPos b, double ox, double oz, double dx, double dz) {
        var be = level.getBlockEntity(b);
        java.util.List<train.common.tile.TileTCRail> rails = new java.util.ArrayList<>();
        if (be instanceof train.common.tile.TileTCRail r) rails.add(r);
        else if (be instanceof train.common.tile.TileTCRailGag g) {
            for (int i = 0; i < g.originX.size(); i++) {
                if (level.getBlockEntity(new BlockPos(g.originX.get(i), g.originY.get(i), g.originZ.get(i))) instanceof train.common.tile.TileTCRail r) rails.add(r);
            }
            if (rails.isEmpty() && g.originX.isEmpty()) return Double.NaN;
        } else if (!isTrack(level.getBlockState(b))) {
            return Double.NaN;
        }
        double best = Double.NaN;
        for (var r : rails) {
            if (r.r <= 0) continue;
            //|o + d s - c| = r
            double px = ox - r.cx, pz = oz - r.cz, bq = px * dx + pz * dz, cq = px * px + pz * pz - r.r * r.r, disc = bq * bq - cq;
            if (disc < 0) continue;
            for (double s : new double[]{-bq - Math.sqrt(disc), -bq + Math.sqrt(disc)}) {
                //only a crossing near this block counts: the arc may come back elsewhere
                double hx = ox + dx * s, hz = oz + dz * s;
                if (s > 0 && Math.abs(hx - (b.getX() + 0.5)) <= 1.2 && Math.abs(hz - (b.getZ() + 0.5)) <= 1.2 && (Double.isNaN(best) || s < best)) best = s;
            }
        }
        if (!Double.isNaN(best)) return best;
        return (b.getX() + 0.5 - ox) * dx + (b.getZ() + 0.5 - oz) * dz;
    }

    public static boolean isTrack(BlockState st) {
        return st.getBlock() instanceof train.common.blocks.BlockTCRail || st.getBlock() instanceof train.common.blocks.BlockTCRailGag
                || st.getBlock() instanceof net.minecraft.world.level.block.BaseRailBlock;
    }

    /**
     * how far across the middle of the track is from the point (x, y, z) of a contact wire over it: on a classic turn
     * from its arc, on a straight from the block's middle line; NaN when there is no track under it
     */
    public static double offTrack(net.minecraft.world.level.BlockGetter level, double x, double y, double z, double dx, double dz) {
        for (int dy = 0; dy >= -5; dy--) {
            BlockPos b = BlockPos.containing(x, y + dy, z);
            //a turn's blocks do not cover all of its arc (it cuts across block corners): its rails are looked for
            //around too, and the distance taken from the arc
            double best = Double.NaN;
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    for (var r : rails(level, b.offset(ox, 0, oz))) {
                        if (r.r <= 0) continue;
                        double d = Math.abs(Math.hypot(x - r.cx, z - r.cz) - r.r);
                        if (Double.isNaN(best) || d < best) best = d;
                    }
                }
            }
            if (!Double.isNaN(best)) return best;
            if (!isTrack(level.getBlockState(b))) continue;
            //a straight: across the wire's way, from the block's middle
            double ex = b.getX() + 0.5 - x, ez = b.getZ() + 0.5 - z;
            return Math.abs(ex * -dz + ez * dx);
        }
        return Double.NaN;
    }

    /** the classic track pieces a block belongs to (itself, or the pieces a filler block is part of) */
    private static java.util.List<train.common.tile.TileTCRail> rails(net.minecraft.world.level.BlockGetter level, BlockPos b) {
        java.util.List<train.common.tile.TileTCRail> l = new java.util.ArrayList<>();
        var be = level.getBlockEntity(b);
        if (be instanceof train.common.tile.TileTCRail r) l.add(r);
        else if (be instanceof train.common.tile.TileTCRailGag g) {
            for (int i = 0; i < g.originX.size(); i++) {
                if (level.getBlockEntity(new BlockPos(g.originX.get(i), g.originY.get(i), g.originZ.get(i))) instanceof train.common.tile.TileTCRail r) l.add(r);
            }
        }
        return l;
    }
}
