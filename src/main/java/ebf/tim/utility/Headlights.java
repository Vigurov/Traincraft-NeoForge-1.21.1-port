package ebf.tim.utility;

import ebf.tim.entities.GenericRailTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Real light from a locomotive's headlights: while the lamps are on, invisible light blocks (vanilla
 * minecraft:light) a few blocks ahead of the front along the track, brightest close by, moved along as the train
 * goes and taken away behind it. They only go into air, and the ones still standing are saved with the
 * locomotive so none is left behind after a reload. (Neither TC 4.5 nor 1.12 lit the world: their lamps only
 * glowed.)
 */
public class Headlights {
    /** blocks ahead of the front bogie, and the light level there */
    private static final double[] AHEAD = {2.5, 5.5, 8.5};
    private static final int[] LEVEL = {15, 13, 10};

    private final Set<BlockPos> placed = new HashSet<>();

    /** call on the server every tick */
    public void tick(GenericRailTransport t) {
        Level level = t.level();
        if (level.isClientSide || t.tickCount % 4 != 0) return;
        Map<BlockPos, Integer> want = new HashMap<>();
        if (t.getBoolean(GenericRailTransport.boolValues.LAMP) && t.frontBogie != null && t.backBogie != null) {
            double fx = t.frontBogie.posX - t.backBogie.posX, fz = t.frontBogie.posZ - t.backBogie.posZ;
            double len = Math.sqrt(fx * fx + fz * fz);
            if (len > 1.0E-4) {
                fx /= len;
                fz /= len;
                for (int i = 0; i < AHEAD.length; i++) {
                    BlockPos p = BlockPos.containing(t.frontBogie.posX + fx * AHEAD[i], t.getY() + 1, t.frontBogie.posZ + fz * AHEAD[i]);
                    //just above the rails, or one higher if that is taken
                    BlockPos at = free(level, p) ? p : free(level, p.above()) ? p.above() : null;
                    if (at != null) want.merge(at, LEVEL[i], Math::max);
                }
            }
        }
        //take away what is not wanted any more (only if it is still our light block)
        placed.removeIf(p -> {
            if (want.containsKey(p)) return false;
            if (level.isLoaded(p) && level.getBlockState(p).is(Blocks.LIGHT)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
            return true;
        });
        for (Map.Entry<BlockPos, Integer> e : want.entrySet()) {
            BlockState s = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, e.getValue());
            if (!level.getBlockState(e.getKey()).equals(s)) {
                level.setBlock(e.getKey(), s, 3);
            }
            placed.add(e.getKey());
        }
    }

    /** air, or one of our own light blocks */
    private boolean free(Level level, BlockPos p) {
        if (!level.isLoaded(p)) return false;
        BlockState s = level.getBlockState(p);
        return s.isAir() || (s.is(Blocks.LIGHT) && placed.contains(p));
    }

    /** the locomotive leaves the world (broken, unloaded): its light goes too */
    public void clear(Level level) {
        if (level.isClientSide) return;
        for (BlockPos p : placed) {
            if (level.isLoaded(p) && level.getBlockState(p).is(Blocks.LIGHT)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        placed.clear();
    }

    public void save(CompoundTag tag) {
        tag.putLongArray("headlights", placed.stream().mapToLong(BlockPos::asLong).toArray());
    }

    /** the ones saved are taken away on the next update unless the lamps still want them */
    public void load(CompoundTag tag) {
        for (long l : tag.getLongArray("headlights")) placed.add(BlockPos.of(l));
    }
}
