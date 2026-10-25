package ebf.tim.blocks;

import ebf.tim.items.ItemRail;
import ebf.tim.registry.TiMBlocks;
import ebf.tim.utility.XmlBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * a TiM rail's shape (1.12 metadata) and its data: materials, gauge and the path the renderer draws.
 * @author Eternal Blue Flame
 */
public class RailTileEntity extends BlockEntity {
    private int meta=0;
    private XmlBuilder data = new XmlBuilder();
    /**client: the drawn geometry, rebuilt when the data changes (1.12 kept a display list)*/
    public Object renderCache = null;

    public RailTileEntity(BlockPos pos, BlockState state) {
        super(TiMBlocks.RAIL_TILE.get(), pos, state);
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int i){
        if(meta==i){
            return;
        }
        meta=i;
        markDirty();
    }

    public void setData(XmlBuilder d){
        if(!data.equals(d)) {
            data = d;
            markDirty();
        }
    }

    public XmlBuilder getData() {
        return data;
    }

    public void dropItem(){
        ItemStack drop = ItemRail.setStackData(
                new ItemStack(TiMBlocks.railItem(), 1), data.getItemStack("rail"),
                data.getItemStack("ballast"), data.getItemStack("ties"), data.getItemStack("wires"));
        if(drop!=null && level != null) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY() + 0.5f, worldPosition.getZ(), drop));
        }
    }

    /**save and tell the clients*/
    public void markDirty() {
        renderCache = null;
        if (this.level != null) {
            setChanged();
            if (!level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("meta", meta);
        if(data!=null && !data.isEmpty()) {
            tag.put("raildata", data.tag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        meta=tag.getInt("meta");
        if(tag.contains("raildata")) {
            data = new XmlBuilder(tag.getCompound("raildata"));
        }
        renderCache = null;
    }
}
