package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * The energy a train under the contact wire gets: from the substations that feed the wire it is under, the
 * nearest along the line first. On the way some is lost, the more the longer the way (wire and feeder: about 5 % in
 * 24 blocks), like the voltage drop that makes real lines want a substation every so often.
 */
public final class CatenaryNetwork {
    private CatenaryNetwork() {}

    /** the share lost per block of wire or feeder the energy goes along */
    public static final double LOSS_PER_BLOCK = 0.002;
    /** how far along the line a substation still feeds a train (blocks) */
    public static final double REACH = 1024;

    /** gives a train at x, y, z up to max (what arrives at it) from the line over it; how much it got */
    public static int draw(Level level, double x, double y, double z, int max) {
        if (max <= 0) return 0;
        CatenaryMastTile.Span span = CatenaryMastTile.spanOver(level, x, y, z);
        if (span == null) return 0;
        //along the line from the train out both ways: the masts by how far they are (Dijkstra over the spans)
        Map<BlockPos, Double> dist = new HashMap<>();
        PriorityQueue<Object[]> queue = new PriorityQueue<>((a, b) -> Double.compare((double) a[1], (double) b[1]));
        Vec3 at = new Vec3(x, span.wireY(), z);
        for (CatenaryMastTile m : new CatenaryMastTile[]{span.near(), span.far()}) {
            queue.add(new Object[]{m, m.contactPoint().distanceTo(at)});
        }
        List<Object[]> sources = new ArrayList<>();
        while (!queue.isEmpty() && dist.size() < 4096) {
            Object[] e = queue.poll();
            CatenaryMastTile m = (CatenaryMastTile) e[0];
            double d = (double) e[1];
            if (d > REACH || dist.containsKey(m.getBlockPos())) continue;
            dist.put(m.getBlockPos(), d);
            if (m.feeder != null && level.isLoaded(m.feeder) && level.getBlockEntity(m.feeder) instanceof SubstationTile sub) {
                sources.add(new Object[]{sub, d + sub.feederPoint().distanceTo(m.feederPoint())});
            }
            for (BlockPos o : m.links) {
                if (dist.containsKey(o) || !level.isLoaded(o) || !(level.getBlockEntity(o) instanceof CatenaryMastTile n)) continue;
                queue.add(new Object[]{n, d + m.contactPoint().distanceTo(n.contactPoint())});
            }
        }
        //the nearest substations first
        sources.sort((a, b) -> Double.compare((double) a[1], (double) b[1]));
        int got = 0;
        for (Object[] src : sources) {
            SubstationTile sub = (SubstationTile) src[0];
            double keep = Math.max(0.05, 1 - LOSS_PER_BLOCK * (double) src[1]);
            int need = (int) Math.ceil((max - got) / keep);
            int taken = sub.give(need);
            got += (int) Math.floor(taken * keep);
            if (got >= max) break;
        }
        return Math.min(got, max);
    }
}
