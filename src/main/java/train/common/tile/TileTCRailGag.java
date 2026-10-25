package train.common.tile;
import net.minecraft.core.BlockPos;

import ebf.tim.utility.CommonUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import train.common.blocks.BlockTCRail;
import train.common.items.TCRailTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileTCRailGag extends BlockEntity {

	public TileTCRailGag(BlockPos pos, BlockState state) {
		super(train.common.ClassicTracks.TILE_RAIL_GAG, pos, state);
	}


	protected Random rand = new Random();
	public List<Integer> originX = new ArrayList<>();
	public List<Integer> originY = new ArrayList<>();
	public List<Integer> originZ = new ArrayList<>();
	public String type = "";
	public float bbHeight = 0.125f;
	public boolean allowStockPlacement = false;
	public boolean canPlaceRollingstock = false;


	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);


		if(nbt.contains("Xorigins")){
			originX= new ArrayList<Integer>();
			int[] org=nbt.getIntArray("Xorigins");
			for (int i:org){
				originX.add(i);
			}
			originY= new ArrayList<Integer>();
			org=nbt.getIntArray("Yorigins");
			for (int i:org){
				originY.add(i);
			}
			originZ= new ArrayList<Integer>();
			org=nbt.getIntArray("Zorigins");
			for (int i:org){
				originZ.add(i);
			}
		} else {
			originX.add(nbt.getInt("originX"));
			originY.add(nbt.getInt("originY"));
			originZ.add(nbt.getInt("originZ"));
		}
		bbHeight = nbt.getFloat("bbHeight");
		type = nbt.getString("type");
		canPlaceRollingstock = nbt.getBoolean("canPlaceRollingstock");
	}





	public void setCanPlaceRollingStock(boolean canPlace){
		TileTCRail tile = (TileTCRail) CommonUtil.getTileEntity(level, originX.get(0), originY.get(0), originZ.get(0));
		if (tile != null){
			if (tile.getTrackFromName().getRailType() == TCRailTypes.RailTypes.STRAIGHT || tile.getTrackFromName().getRailType() == TCRailTypes.RailTypes.DIAGONAL){
				canPlaceRollingstock = canPlace;
			}
		}
	}




   
	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		int[] xorg=new int[originX.size()];
		for (int i=0; i<originX.size();i++){
			xorg[i]=originX.get(i);
		}
		nbt.putIntArray("Xorigins", xorg);

		int[] yorg=new int[originY.size()];
		for (int i=0; i<originY.size();i++){
			yorg[i]=originY.get(i);
		}
		nbt.putIntArray("Yorigins", yorg);

		int[] zorg=new int[originZ.size()];
		for (int i=0; i<originZ.size();i++){
			zorg[i]=originZ.get(i);
		}
		nbt.putIntArray("Zorigins", zorg);
		nbt.putFloat("bbHeight", bbHeight);
		if (type.equals("")){
			type = "null";
		}
		nbt.putString("type", type);
		nbt.putBoolean("canPlaceRollingstock", canPlaceRollingstock);
	}






	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	/** the 1.7.10 block metadata (track direction, switch state) */
	public int getBlockMeta() {
		BlockState state = getBlockState();
		for (var p : state.getProperties()) {
			if (p.getName().equals("meta")) return (Integer) state.getValue(p);
		}
		return 0;
	}

	/** tell the clients (1.7.10 world.markBlockForUpdate) */
	public void markDirty() {
		if (level != null) {
			setChanged();
			if (!level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}
}
