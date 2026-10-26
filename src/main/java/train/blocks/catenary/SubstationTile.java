package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import train.blocks.TCBlocks;

import java.util.LinkedHashSet;
import java.util.Set;

/** a substation: what it has taken in and not yet given to the line, and the masts its feeders go to */
public class SubstationTile extends BlockEntity {
    /** the most it holds (about a second of a big supply) and takes in a tick from beside it */
    public static final int CAPACITY = 20000, INTAKE = 2000;

    public final Set<BlockPos> feeders = new LinkedHashSet<>();
    /** the generators (any block giving energy) its power cables come from */
    public final Set<BlockPos> inputs = new LinkedHashSet<>();
    /** the most power lines in: each ends on the incoming gantry's beam (bays side by side) */
    public static final int MAX_INPUTS = 16;
    public final Buffer energy = new Buffer();

    public final class Buffer extends EnergyStorage {
        Buffer() {
            super(CAPACITY, INTAKE, CAPACITY);
        }

        void set(int e) {
            energy = Math.max(0, Math.min(CAPACITY, e));
            setChanged();
        }

        @Override
        public int receiveEnergy(int max, boolean simulate) {
            int got = super.receiveEnergy(max, simulate);
            if (got > 0 && !simulate) setChanged();
            return got;
        }

        @Override
        public boolean canExtract() {
            //it gives to the line only, not back to the cables around it
            return false;
        }
    }

    public SubstationTile(BlockPos pos, BlockState state) {
        super(TCBlocks.SUBSTATION_TILE.get(), pos, state);
    }

    /**
     * server tick: what the line has not taken goes (a substation stores nothing: with the generators stopped the
     * line goes dead within seconds), then energy in from the generators and cables beside it that don't push it
     * in themselves. Running, it holds about a second of what it is given
     */
    public void tickServer() {
        int e = energy.getEnergyStored();
        if (e > 0) energy.set(e - Math.max(1, e / 20));
        if (energy.getEnergyStored() >= CAPACITY) return;
        //along its power cables
        for (BlockPos in : inputs) {
            if (!level.isLoaded(in)) continue;
            IEnergyStorage src = level.getCapability(Capabilities.EnergyStorage.BLOCK, in, null);
            if (src == null || !src.canExtract()) continue;
            int got = src.extractEnergy(Math.min(INTAKE, CAPACITY - energy.getEnergyStored()), false);
            if (got > 0) energy.set(energy.getEnergyStored() + got);
            if (energy.getEnergyStored() >= CAPACITY) return;
        }
        for (Direction d : Direction.values()) {
            IEnergyStorage near = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(d), d.getOpposite());
            if (near == null || near == energy || !near.canExtract()) continue;
            int got = near.extractEnergy(Math.min(INTAKE, CAPACITY - energy.getEnergyStored()), false);
            if (got > 0) energy.set(energy.getEnergyStored() + got);
            if (energy.getEnergyStored() >= CAPACITY) break;
        }
    }

    /** takes out up to max for the line */
    public int give(int max) {
        int g = Math.min(max, energy.getEnergyStored());
        if (g > 0) energy.set(energy.getEnergyStored() - g);
        return g;
    }

    public void addFeeder(BlockPos mast) {
        if (feeders.add(mast.immutable())) changed();
    }

    public void removeFeeder(BlockPos mast) {
        if (feeders.remove(mast)) changed();
    }

    /** taken down: the masts let go of its feeders, and each feeder cable drops here */
    public void dropAllFeeders() {
        for (BlockPos m : feeders) {
            if (level.getBlockEntity(m) instanceof CatenaryMastTile t) t.setFeeder(null);
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                    new ItemStack(TCBlocks.FEEDER_CABLE.get()));
        }
        feeders.clear();
        for (BlockPos ignored : inputs) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                    new ItemStack(TCBlocks.FEEDER_CABLE.get()));
        }
        inputs.clear();
    }

    /** a point of its model (pixels from its middle, turned with its facing as it is drawn) in the world */
    private Vec3 modelPoint(float px, float py, float pz) {
        double a = Math.toRadians(switch (getBlockState().getValue(SubstationBlock.FACING)) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        });
        double x = px / 16.0, z = pz / 16.0;
        //as GL's turn about y by a
        double wx = x * Math.cos(a) + z * Math.sin(a), wz = -x * Math.sin(a) + z * Math.cos(a);
        return new Vec3(worldPosition.getX() + 1 + wx, worldPosition.getY() + py / 16.0, worldPosition.getZ() + 1 + wz);
    }

    /** where the conductor leaves the rectifier hall for the outgoing gantry */
    public Vec3 hallPoint() {
        return modelPoint(9.5f, 21, 9.2f);
    }

    /** where the feeders leave it: under the glass string on top of its gantry */
    public Vec3 feederPoint() {
        return modelPoint(FEEDER_X, FEEDER_Y, FEEDER_Z);
    }

    /** where the i-th of n power lines comes in: on the incoming gantry's beam, the bays spread along it */
    public Vec3 inputPoint(int i, int n) {
        return modelPoint(inputX(i, n), IN_Y, IN_Z);
    }

    public static float inputX(int i, int n) {
        return -IN_HALF + 2 * IN_HALF * (i + 0.5f) / Math.max(1, n);
    }

    /** the model's points (pixels, from its middle; RenderSubstation draws them there) */
    public static final float FEEDER_X = 0, FEEDER_Y = 26.6f, FEEDER_Z = 13.5f, IN_Y = 35.2f, IN_Z = -13.5f, IN_HALF = 12.5f;

    public void addInput(BlockPos p) {
        if (inputs.add(p.immutable())) changed();
    }

    public void removeInput(BlockPos p) {
        if (inputs.remove(p)) changed();
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("feeders", feeders.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("inputs", inputs.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        feeders.clear();
        for (long l : tag.getLongArray("feeders")) feeders.add(BlockPos.of(l));
        inputs.clear();
        for (long l : tag.getLongArray("inputs")) inputs.add(BlockPos.of(l));
        energy.set(tag.getInt("energy"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag t = new CompoundTag();
        t.putLongArray("feeders", feeders.stream().mapToLong(BlockPos::asLong).toArray());
        t.putLongArray("inputs", inputs.stream().mapToLong(BlockPos::asLong).toArray());
        return t;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
