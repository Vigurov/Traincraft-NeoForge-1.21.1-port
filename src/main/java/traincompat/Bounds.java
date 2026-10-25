package traincompat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.WeakHashMap;

/** 1.7.10 Block.setBlockBounds: a box per block (relative to the block) */
public final class Bounds {
    private static final Map<Block, AABB> bounds = new WeakHashMap<>();

    private Bounds() {}

    public static void set(Block b, double x1, double y1, double z1, double x2, double y2, double z2) {
        bounds.put(b, new AABB(x1, y1, z1, x2, y2, z2));
    }

    public static AABB get(Block b) {
        AABB box = bounds.get(b);
        return box == null ? new AABB(0, 0, 0, 1, 1, 1) : box;
    }
}
