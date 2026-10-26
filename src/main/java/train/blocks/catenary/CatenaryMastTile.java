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
 * A mast and the masts its wire spans go to (each span is kept on both ends), and the energy (FE) the line has at
 * it. A mast takes energy from any block beside it (generators push it in; from those that don't, it draws). Along
 * each span energy flows from the fuller mast to the emptier one, as much as a span carries, and some of it is lost
 * on the way, the more the longer the span (about 5 % over 24 blocks): far from a generator a line has less, and a
 * long one wants more feeding points, like the substations of a real line.
 */
public class CatenaryMastTile extends BlockEntity {
    /** the longest span between two masts, in blocks (real ones are 50-70 m) */
    public static final int MAX_SPAN = 24;
    /** energy a mast holds, takes in a tick from beside it, and a span carries in a tick */
    public static final int CAPACITY = 4000, INTAKE = 1000, SPAN_RATE = 500;
    /** the share of what goes along a span lost per block of its length */
    public static final double LOSS_PER_BLOCK = 0.002;

    public final Set<BlockPos> links = new LinkedHashSet<>();
    public final Buffer energy = new Buffer();

    /** the mast's energy: takes from outside at most INTAKE a tick, gives as much as asked */
    public final class Buffer extends EnergyStorage {
        Buffer() {
            super(CAPACITY, INTAKE, CAPACITY);
        }

        void set(int e) {
            energy = Math.max(0, Math.min(CAPACITY, e));
        }

        @Override
        public int receiveEnergy(int max, boolean simulate) {
            int got = super.receiveEnergy(max, simulate);
            if (got > 0 && !simulate) setChanged();
            return got;
        }
    }

    /** the masts loaded on the server, per level, for the trains to find the wire over them */
    private static final Map<Level, Set<CatenaryMastTile>> LOADED = new WeakHashMap<>();

    public static Set<CatenaryMastTile> loaded(Level level) {
        return LOADED.getOrDefault(level, Collections.emptySet());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
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

    /** server tick: energy in from beside the mast, then along its spans */
    public void tickServer() {
        if (energy.getEnergyStored() < CAPACITY) {
            for (Direction d : Direction.values()) {
                IEnergyStorage near = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(d), d.getOpposite());
                if (near == null || near == energy || !near.canExtract()) continue;
                int want = Math.min(INTAKE, CAPACITY - energy.getEnergyStored());
                int got = near.extractEnergy(want, false);
                if (got > 0) energy.receiveEnergy(got, false);
                if (energy.getEnergyStored() >= CAPACITY) break;
            }
        }
        //each span once, by its mast with the lower position
        for (BlockPos o : links) {
            if (o.asLong() < worldPosition.asLong() || !level.isLoaded(o)) continue;
            if (!(level.getBlockEntity(o) instanceof CatenaryMastTile other)) continue;
            int a = energy.getEnergyStored(), b = other.energy.getEnergyStored();
            if (Math.abs(a - b) < 2) continue;
            CatenaryMastTile from = a > b ? this : other, to = a > b ? other : this;
            int move = Math.min(SPAN_RATE, Math.abs(a - b) / 2);
            double keep = Math.max(0, 1 - LOSS_PER_BLOCK * Math.sqrt(worldPosition.distSqr(o)));
            int arrives = (int) Math.floor(move * keep);
            int room = CAPACITY - to.energy.getEnergyStored();
            if (arrives > room) {
                arrives = room;
                move = (int) Math.ceil(arrives / Math.max(keep, 1e-6));
            }
            if (move <= 0) continue;
            from.energy.set(from.energy.getEnergyStored() - move);
            to.energy.set(to.energy.getEnergyStored() + arrives);
            from.setChanged();
            to.setChanged();
        }
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

    /**
     * energy for a train at x, y, z from the contact wire over it: from the two masts of the span it is under, at
     * most max; how much it got
     */
    public static int feed(Level level, double x, double y, double z, int max) {
        Span span = spanOver(level, x, y, z);
        if (span == null) return 0;
        CatenaryMastTile t = span.near(), other = span.far();
        int got = 0;
        int fromNear = Math.min(max / 2 + max % 2, t.energy.getEnergyStored());
        t.energy.set(t.energy.getEnergyStored() - fromNear);
        got += fromNear;
        int fromFar = Math.min(max - got, other.energy.getEnergyStored());
        other.energy.set(other.energy.getEnergyStored() - fromFar);
        got += fromFar;
        if (got < max) {
            int more = Math.min(max - got, t.energy.getEnergyStored());
            t.energy.set(t.energy.getEnergyStored() - more);
            got += more;
        }
        t.setChanged();
        other.setChanged();
        return got;
    }

    /** the contact wire of a span: where it is held at this mast */
    public Vec3 contactPoint() {
        return CatenaryGeometry.anchor(worldPosition, getBlockState(), CatenaryGeometry.CONTACT_Y);
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
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("links", links.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        for (long l : tag.getLongArray("links")) links.add(BlockPos.of(l));
        energy.set(tag.getInt("energy"));
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
