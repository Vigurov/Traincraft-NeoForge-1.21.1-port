package ebf.tim.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * A TiM switch or signal tile: a redstone strength the renderer shows (1.12 TileSwitch). The 1.12 animation
 * fields are left out, neither the switch stand nor the signal uses them.
 */
public class TileSwitch extends BlockEntity {
    public int strength = 0;
    public int currentTick = 0;

    public TileSwitch(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int toggleEnabled() {
        setStrength(strength < 15 ? 15 : 0);
        return strength;
    }

    public void setStrength(int e) {
        if (e != strength) {
            strength = e;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public int getStrength() {
        return strength;
    }

    /** the 1.12 facing number (south 0, east 1, north 2, west 3): the way the placer looked */
    public int facing() {
        Direction d = getBlockState().hasProperty(HorizontalDirectionalBlock.FACING) ? getBlockState().getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        return switch (d) {
            case SOUTH -> 0;
            case EAST -> 1;
            case WEST -> 3;
            default -> 2;
        };
    }

    public void tickTile() {
        currentTick++;
        if (currentTick > 200) {
            currentTick = 0;
        }
    }

    /**
     * full strength while a cart or transport is in the area in front (width across, depth along the facing),
     * otherwise the redstone strength into the block (or off)
     */
    public void detectNearby(int width, int depth, boolean useRedstone, boolean left) {
        double[] s, e;
        switch (facing()) {
            case 0 -> { e = left ? new double[]{width, 4, 0} : new double[]{0, 4, 0}; s = left ? new double[]{0, -1, -depth} : new double[]{-width, -1, -depth}; }
            case 1 -> { e = left ? new double[]{depth, 4, width} : new double[]{depth, 4, 0}; s = left ? new double[]{0, -1, 0} : new double[]{0, -1, -width}; }
            case 2 -> { e = left ? new double[]{0, 4, depth} : new double[]{width, 4, depth}; s = left ? new double[]{-width, -1, 0} : new double[]{0, -1, 0}; }
            default -> { e = left ? new double[]{0, 4, 0} : new double[]{0, 4, width}; s = left ? new double[]{-depth, -1, -width} : new double[]{-depth, -1, 0}; }
        }
        BlockPos p = worldPosition;
        AABB box = new AABB(p.getX() + s[0], p.getY() + s[1], p.getZ() + s[2], p.getX() + e[0], p.getY() + e[1], p.getZ() + e[2]);
        //1.12 TiM transports were minecarts
        if (!level.getEntities((Entity) null, box, x -> x instanceof AbstractMinecart || x instanceof ebf.tim.entities.GenericRailTransport).isEmpty()) {
            setStrength(15);
        } else {
            setStrength(useRedstone ? level.getDirectSignalTo(p) : 0);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("e", new int[]{strength});
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int[] e = tag.getIntArray("e");
        strength = e.length > 0 ? e[0] : 0;
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
