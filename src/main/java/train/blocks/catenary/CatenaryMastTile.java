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

import java.util.LinkedHashSet;
import java.util.Set;

/** a mast and the masts its wire spans go to (each span is kept on both ends) */
public class CatenaryMastTile extends BlockEntity {
    /** the longest span between two masts, in blocks (real ones are 50-70 m) */
    public static final int MAX_SPAN = 24;

    public final Set<BlockPos> links = new LinkedHashSet<>();

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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        for (long l : tag.getLongArray("links")) links.add(BlockPos.of(l));
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
