package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import train.blocks.TCBlocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A mast and the masts its wire spans go to (each span is kept on both ends), and the substation that feeds the
 * line here, if a feeder cable comes to this mast. A mast holds no energy: the line only carries it, from the
 * substations to the trains under it (CatenaryNetwork).
 */
public class CatenaryMastTile extends BlockEntity {
    /** the longest span between two masts, in blocks (real ones are 50-70 m) */
    public static final int MAX_SPAN = 24;
    public final Set<BlockPos> links = new LinkedHashSet<>();
    /** the substation whose feeder cable comes to this mast */
    public BlockPos feeder;
    /** how far its cantilever reaches out (blocks from its middle): over the middle of the track it stands by */
    public float reach = (float) CatenaryGeometry.REACH;

    /** the masts loaded on the server, per level, for the trains to find the wire over them */
    private static final Map<Level, Set<CatenaryMastTile>> LOADED = new WeakHashMap<>();

    public static Set<CatenaryMastTile> loaded(Level level) {
        return LOADED.getOrDefault(level, Collections.emptySet());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        //on both sides: the server feeds trains from it, the client finds the wires to cut
        if (level != null) {
            LOADED.computeIfAbsent(level, l -> Collections.newSetFromMap(new WeakHashMap<>())).add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) loaded(level).remove(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null) loaded(level).remove(this);
    }

    /** the span whose contact wire is over x, y, z: its two masts and the wire's height there, or null */
    public record Span(CatenaryMastTile near, CatenaryMastTile far, double wireY) {}

    public static Span spanOver(Level level, double x, double y, double z) {
        for (CatenaryMastTile t : loaded(level)) {
            if (t.isRemoved()) continue;
            Vec3 a = t.contactPoint();
            //quick reject: the train is nowhere near this mast's spans
            if (Math.abs(a.x - x) > MAX_SPAN + 1 || Math.abs(a.z - z) > MAX_SPAN + 1) continue;
            for (BlockPos o : t.links) {
                if (o.asLong() < t.worldPosition.asLong() || !(level.getBlockEntity(o) instanceof CatenaryMastTile other)) continue;
                Vec3 b = other.contactPoint();
                double dx = b.x - a.x, dz = b.z - a.z, l2 = dx * dx + dz * dz;
                double s = l2 < 1e-6 ? 0 : Math.max(0, Math.min(1, ((x - a.x) * dx + (z - a.z) * dz) / l2));
                double px = a.x + dx * s - x, pz = a.z + dz * s - z;
                double wireY = a.y + (b.y - a.y) * s;
                double above = wireY - y;
                //under it: within a pantograph's reach across, the wire a pantograph's height over the train's wheels
                if (px * px + pz * pz > 0.8 * 0.8 || above < 1.8 || above > 4.2) continue;
                return new Span(t, other, wireY);
            }
        }
        return null;
    }

    /** for tests: how far across from (x, z) the nearest span is and how high its wire is over y: {across, above} */
    public static double[] nearestSpan(Level level, double x, double y, double z) {
        double[] best = {Double.MAX_VALUE, 0};
        for (CatenaryMastTile t : loaded(level)) {
            if (t.isRemoved()) continue;
            Vec3 a = t.contactPoint();
            for (BlockPos o : t.links) {
                if (o.asLong() < t.worldPosition.asLong() || !(level.getBlockEntity(o) instanceof CatenaryMastTile other)) continue;
                Vec3 b = other.contactPoint();
                double dx = b.x - a.x, dz = b.z - a.z, l2 = dx * dx + dz * dz;
                double s = l2 < 1e-6 ? 0 : Math.max(0, Math.min(1, ((x - a.x) * dx + (z - a.z) * dz) / l2));
                double d = Math.hypot(a.x + dx * s - x, a.z + dz * s - z);
                if (d < best[0]) best = new double[]{d, a.y + (b.y - a.y) * s - y};
            }
        }
        return best;
    }

    public void setFeeder(BlockPos substation) {
        feeder = substation == null ? null : substation.immutable();
        changed();
    }

    /** where a feeder cable comes to this mast: a bracket at its top, on the side away from the track */
    public Vec3 feederPoint() {
        BlockState st = getBlockState();
        net.minecraft.core.Direction d = st.hasProperty(CatenaryMastBlock.FACING) ? st.getValue(CatenaryMastBlock.FACING) : net.minecraft.core.Direction.EAST;
        return new Vec3(worldPosition.getX() + 0.5 - d.getStepX() * 0.62, worldPosition.getY() + 4.05, worldPosition.getZ() + 0.5 - d.getStepZ() * 0.62);
    }

    /** the contact wire of a span: where it is held at this mast */
    public Vec3 contactPoint() {
        return CatenaryGeometry.anchor(worldPosition, getBlockState(), CatenaryGeometry.CONTACT_Y, reach);
    }

    /**
     * the cantilever made to measure: out to the middle of the track in front of the mast (a classic turn's arc, or a
     * track block's middle), as far as MAX_REACH; one block when there is no track there
     */
    public void fitReach() {
        if (level == null) return;
        Direction f = getBlockState().hasProperty(CatenaryMastBlock.FACING) ? getBlockState().getValue(CatenaryMastBlock.FACING) : Direction.EAST;
        double ox = worldPosition.getX() + 0.5, oz = worldPosition.getZ() + 0.5;
        float fitted = (float) CatenaryGeometry.REACH;
        search:
        for (double t = 0.5; t <= CatenaryGeometry.MAX_REACH + 0.5; t += 0.25) {
            for (int dy : new int[]{0, 1, -1}) {
                BlockPos b = BlockPos.containing(ox + f.getStepX() * t, worldPosition.getY() + dy, oz + f.getStepZ() * t);
                double s = CatenaryGeometry.trackCrossing(level, b, ox, oz, f.getStepX(), f.getStepZ());
                if (Double.isNaN(s)) continue;
                fitted = (float) Math.max(CatenaryGeometry.MIN_REACH, Math.min(CatenaryGeometry.MAX_REACH, s));
                break search;
            }
        }
        if (Math.abs(fitted - reach) > 1e-3) {
            reach = fitted;
            changed();
        }
    }

    public CatenaryMastTile(BlockPos pos, BlockState state) {
        super(TCBlocks.CATENARY_TILE.get(), pos, state);
    }

    public void link(BlockPos other) {
        if (links.add(other.immutable())) changed();
    }

    public void unlink(BlockPos other) {
        if (links.remove(other)) changed();
    }

    /** the mast is gone: the other ends forget it, and each span's wire drops here */
    public void dropAllSpans() {
        for (BlockPos o : links) {
            if (level.getBlockEntity(o) instanceof CatenaryMastTile t) t.unlink(worldPosition);
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                    new ItemStack(TCBlocks.CATENARY_WIRE.get()));
        }
        links.clear();
        if (feeder != null) {
            if (level.getBlockEntity(feeder) instanceof SubstationTile sub) sub.removeFeeder(worldPosition);
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                    new ItemStack(TCBlocks.FEEDER_CABLE.get()));
            feeder = null;
        }
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("links", links.stream().mapToLong(BlockPos::asLong).toArray());
        if (feeder != null) tag.putLong("feeder", feeder.asLong());
        tag.putFloat("reach", reach);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        for (long l : tag.getLongArray("links")) links.add(BlockPos.of(l));
        feeder = tag.contains("feeder") ? BlockPos.of(tag.getLong("feeder")) : null;
        reach = tag.contains("reach") ? tag.getFloat("reach") : (float) CatenaryGeometry.REACH;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
