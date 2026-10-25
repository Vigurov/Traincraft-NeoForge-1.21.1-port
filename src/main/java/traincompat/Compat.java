package traincompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** odds and ends the classic track code needs */
public final class Compat {
    private Compat() {}

    /** the level behind a block getter (render regions and the like hand out a BlockGetter) */
    public static Level world(BlockGetter access) {
        if (access instanceof Level) return (Level) access;
        return net.minecraft.client.Minecraft.getInstance().level;
    }

    public static AABB relative(AABB box, BlockPos pos) {
        return box == null ? null : box.move(-pos.getX(), -pos.getY(), -pos.getZ());
    }
}
