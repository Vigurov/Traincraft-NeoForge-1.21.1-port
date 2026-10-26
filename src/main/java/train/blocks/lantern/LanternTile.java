package train.blocks.lantern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import train.blocks.TCBlocks;

import java.util.Random;

/** the lantern: just its glass colour, a random one at first, set with the colour screen */
public class LanternTile extends BlockEntity {
    private static final Random rand = new Random();
    private int color = rand.nextInt(0x1000000);

    public LanternTile(BlockPos pos, BlockState state) {
        super(TCBlocks.LANTERN_TILE.get(), pos, state);
    }

    public int getColor() {
        return color;
    }

    public String getColorString() {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public void setColor(int c) {
        color = c & 0xFFFFFF;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("randomColor", color);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("randomColor")) color = tag.getInt("randomColor");
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
