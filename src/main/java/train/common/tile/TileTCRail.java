package train.common.tile;

import net.minecraft.core.BlockPos;
import ebf.tim.utility.CommonUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import train.common.api.TrackRecord;
import train.common.blocks.BlockTCRail;
import train.common.blocks.BlockTCRailGag;
import train.common.core.handlers.ConfigHandler;
import train.common.items.TCRailTypes;
import train.common.library.BlockIDs;
import train.common.library.EnumTracks;
import train.common.library.TraincraftRegistry;

import java.util.ArrayList;
import java.util.List;

public class TileTCRail extends BlockEntity {

	public double r;
	public double cx;
	public double cy;
	public double cz;
	public double slopeLength;
	public double slopeAngle;

	public int ballastMaterial;
	public int ballastMetadata;
	public int ballastColour;
	private String type;

	private TCRailTypes.RailTypes railType;
	private int facingMeta;
	public boolean isLinkedToRail = false;
	public int linkedX;
	public int linkedY;
	public int linkedZ;
	public boolean hasModel = true;
	private boolean switchActive = false;

	public Player lastPlayerToInteract = null;
	private int updateTicks;
	public Item		idDrop;

	public TileTCRail(BlockPos pos, BlockState state) {
		super(train.common.ClassicTracks.TILE_RAIL, pos, state);
		if(this.level != null)
			facingMeta = this.getBlockMeta();
	}

	public int getFacing() {

		return facingMeta;
	}







	public void setFacing(int facing) {
		this.facingMeta = facing;
	}

	public void setType(String type) {
		CommonUtil.markBlockForUpdate(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
		this.type = type;
		for (EnumTracks rail : EnumTracks.values()) {
			if (rail.getLabel().equals(type)) {
				track = rail;
			}
		}
	}

	public String getTrackName() {

		return this.type;
	}





	public void setBallastMaterial(int  ballast) {
		CommonUtil.markBlockForUpdate(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
		this.ballastMaterial = ballast;
	}

	public int getBallastMaterial(){
		return ballastMaterial;
	}



	private EnumTracks renderType = null;

	public TCRailTypes.RailTypes getRailType(){

		railType = getTrackFromName().getRailType();

		return railType;
	}

	private EnumTracks track = null;

	@Deprecated
	public EnumTracks getTrack(){
		if(track==null) {
			for (EnumTracks rail : EnumTracks.values()) {
				if (rail.getLabel().equals(getTrackName())) {
					track = rail;
				}
			}
		}
		return track;
	}

	public TrackRecord getTrackFromName(){
		return TraincraftRegistry.findTrackRecordByName(this.getTrackName());
	}


	public EnumTracks getTrackType(){
		if (renderType == null){
			if(hasModel && getTrackName() != null){
				for(EnumTracks rail : EnumTracks.values()){
					if (rail.getLabel().equals(getTrackName())){
						renderType = rail;
					}
				}
			}
		}
		return renderType;
	}

	public boolean getSwitchState() {
		return switchActive;
	}



	public void printInfo() {
		System.out.println(type);
		System.out.println(getSwitchState());
		System.out.println(TCRailTypes.isStraightTrack(this));
	}

	private byte checkBlockXZ=0;
    public void update() {
        legacy_updateEntity();
    }

   
	public void legacy_updateEntity() {
		if (level.isClientSide || !TCRailTypes.isSwitchTrack(this)) {

			return;
		}

		if (updateTicks % 11 == 0 || updateTicks==1) {
			boolean flag = level.hasNeighborSignal(new BlockPos(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));

			if(checkBlockXZ==0){
				if(CommonUtil.getBlockAt(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ()+1) instanceof BlockTCRail){
					checkBlockXZ=1;
				} else {
					checkBlockXZ=2;
				}
			}
			if(!flag){
				if(checkBlockXZ==1){
					flag = level.hasNeighborSignal(new BlockPos(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()+1));
					if(!flag){
						flag = level.hasNeighborSignal(new BlockPos(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()-1));
					}
				} else if(checkBlockXZ==2){
					flag = level.hasNeighborSignal(new BlockPos(worldPosition.getX()+1, worldPosition.getY(), worldPosition.getZ()));
					if(!flag){
						flag = level.hasNeighborSignal(new BlockPos(worldPosition.getX()-1, worldPosition.getY(), worldPosition.getZ()));
					}
				}
			}
			if (getSwitchState() != flag) {
				setSwitchState(flag);
			}
		}

		updateTicks++;
	}



	public void setSwitchState(boolean state) {
		this.switchActive = state;
		BlockEntity te1;
		int a = 0;
		int b = 0;
		int c = 0;
		switch (getBlockMeta()) {
			case 0:
				c = 1;
				break;
			case 1:
				a = -1;
				break;
			case 2:
				c = -1;
				break;
			case 3:
				a = 1;
				break;
			default:
				org.slf4j.LoggerFactory.getLogger("traincraft").warn("Unsupported block meta for switch state.");
				return;
		}
		int offsetX = a;
		int offsetY = b;
		int offsetZ = c;
		while (Math.abs(offsetX) < getTrack().getSwitchSize() && Math.abs(offsetY) < getTrack().getSwitchSize() && Math.abs(offsetZ) < getTrack().getSwitchSize()) {
			te1 = CommonUtil.getTileEntity(level, worldPosition.getX() + offsetX, worldPosition.getY() + offsetY, worldPosition.getZ() + offsetZ);
			if (te1 instanceof TileTCRail) {
				if (getSwitchState()) {
					if (getTrackName().contains("SWITCH") && getTrackName().contains("LEFT")) {
						((TileTCRail) te1).setType(EnumTracks.MEDIUM_LEFT_TURN.getLabel());
						((TileTCRail) te1).switchActive=true;
					} else if (getTrackName().contains("SWITCH") && getTrackName().contains("RIGHT")) {
						((TileTCRail) te1).setType(EnumTracks.MEDIUM_RIGHT_TURN.getLabel());
						((TileTCRail) te1).switchActive=true;
					}
				} else {
					((TileTCRail) te1).setType(EnumTracks.SMALL_STRAIGHT.getLabel());
					((TileTCRail) te1).switchActive=false;
				}
			}
			offsetX += a;
			offsetY += b;
			offsetZ += c;
		}

		this.markDirty();
		CommonUtil.markBlockForUpdate(this.level, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		facingMeta = nbt.getByte("Orientation");
		r = nbt.getDouble("r");
		cx = nbt.getDouble("cx");
		cy = nbt.getDouble("cy");
		cz = nbt.getDouble("cz");
		cy = nbt.getDouble("cy");

		slopeLength = nbt.getDouble("slopeLength");
		slopeAngle = nbt.getDouble("slopeAngle");
		linkedX = nbt.getInt("linkedX");
		linkedY = nbt.getInt("linkedY");
		linkedZ = nbt.getInt("linkedZ");
		ballastMetadata = nbt.getInt("ballastMetadata");
		ballastColour = nbt.getInt("ballastColour");
		if (nbt.contains("type")) {
			type = nbt.getString("type");
		} else {
			type = EnumTracks.SMALL_STRAIGHT.getLabel();
		}
		if(nbt.contains("ballastMaterial")) {
			ballastMaterial = nbt.getInt("ballastMaterial");
		} else {
			ballastMaterial=0;
		}


		isLinkedToRail = nbt.getBoolean("isLinkedToRail");
		hasModel = nbt.getBoolean("hasModel");
		switchActive = nbt.getBoolean("switchActive");
		idDrop = nbt.contains("idDrop", 8) ? BuiltInRegistries.ITEM.get(ResourceLocation.parse(nbt.getString("idDrop"))) : null;


		/**
		 * Hacky TC Code to fix already placed slopes
		 * ETERNAL NOTE: checking if it's a slope before checking what kind of slope, in theory, should improve performance
		 */
		if(type.contains("SLOPE")) {
			if (type.equals(EnumTracks.SLOPE_WOOD.getLabel())
					|| type.equals(EnumTracks.SLOPE_GRAVEL.getLabel())
					|| type.equals(EnumTracks.SLOPE_BALLAST.getLabel())
					|| type.equals(EnumTracks.SLOPE_SNOW_GRAVEL.getLabel())
						|| type.equals(EnumTracks.SLOPE_DYNAMIC.getLabel())) {
				slopeAngle = 0.13;
			} else if (type.equals(EnumTracks.LARGE_SLOPE_WOOD.getLabel())
					|| type.equals(EnumTracks.LARGE_SLOPE_GRAVEL.getLabel())
					|| type.equals(EnumTracks.LARGE_SLOPE_BALLAST.getLabel())
					|| type.equals(EnumTracks.LARGE_SLOPE_SNOW_GRAVEL.getLabel())
					|| type.equals(EnumTracks.LARGE_SLOPE_DYNAMIC.getLabel())) {
				slopeAngle = 0.0666;
			} else if (type.equals(EnumTracks.VERY_LARGE_SLOPE_WOOD.getLabel())
					|| type.equals(EnumTracks.VERY_LARGE_SLOPE_GRAVEL.getLabel())
					|| type.equals(EnumTracks.VERY_LARGE_SLOPE_BALLAST.getLabel())
					|| type.equals(EnumTracks.VERY_LARGE_SLOPE_SNOW_GRAVEL.getLabel())
					|| type.equals(EnumTracks.VERY_LARGE_SLOPE_DYNAMIC.getLabel())) {
				slopeAngle = 0.0444;
			}
			else if (type.equals(EnumTracks.LARGE_CURVED_SLOPE_DYNAMIC.getLabel())){
				slopeAngle = 0.1558;
			}
		}
	}


   
	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putByte("Orientation", (byte) facingMeta);
		nbt.putDouble("r", r);
		nbt.putDouble("cx", cx);
		nbt.putDouble("cy", cy);
		nbt.putDouble("cz", cz);
		nbt.putDouble("slopeLength", slopeLength);
		nbt.putDouble("slopeAngle", slopeAngle);
		nbt.putInt("linkedX", linkedX);
		nbt.putInt("linkedY", linkedY);
		nbt.putInt("linkedZ", linkedZ);
		nbt.putInt("ballastMetadata", ballastMetadata);
		nbt.putInt("ballastColour", ballastColour);
		if (type != null) {
			nbt.putString("type", type);
		}
		if (ballastMaterial  != 0) {
			nbt.putInt("ballastMaterial", ballastMaterial);
		}
		nbt.putBoolean("isLinkedToRail", isLinkedToRail);
		nbt.putBoolean("hasModel", hasModel);
		nbt.putBoolean("switchActive", switchActive);
		if (idDrop != null) nbt.putString("idDrop", BuiltInRegistries.ITEM.getKey(idDrop).toString());
	}


   



	public void changeSwitchState(Level level, TileTCRail tileEntity, int i, int j, int k) {
		if (tileEntity.getTrackName() != null && (tileEntity.getTrackName().contains("SWITCH"))) {
			BlockEntity te1;
			int a = 0;
			int b = 0;
			int c = 0;
			switch (tileEntity.getBlockMeta()) {
				case 0:
					c = 1;
					break;
				case 1:
					a = -1;
					break;
				case 2:
					c = -1;
					break;
				case 3:
					a = 1;
					break;
				default:
					org.slf4j.LoggerFactory.getLogger("traincraft").warn("Unsupported block meta for switch state.");
					return;
			}
			int offsetX = a;
			int offsetY = b;
			int offsetZ = c;
			while (Math.abs(offsetX) < tileEntity.getTrack().getSwitchSize() && Math.abs(offsetY) < tileEntity.getTrack().getSwitchSize() && Math.abs(offsetZ) < tileEntity.getTrack().getSwitchSize()) {
				te1 = CommonUtil.getTileEntity(level, i + offsetX, j + offsetY, k + offsetZ);
				if (te1 != null && te1 instanceof TileTCRail) {
					if (tileEntity.getSwitchState()) {
						if (tileEntity.getTrackName().contains("SWITCH") && tileEntity.getTrackName().contains("LEFT")) {
							((TileTCRail) te1).setType(EnumTracks.MEDIUM_LEFT_TURN.getLabel());
							((TileTCRail) te1).switchActive=true;
						} else if (tileEntity.getTrackName().contains("SWITCH") && tileEntity.getTrackName().contains("RIGHT")) {
							((TileTCRail) te1).setType(EnumTracks.MEDIUM_RIGHT_TURN.getLabel());
							((TileTCRail) te1).switchActive=true;
						}
					} else {
						((TileTCRail) te1).setType(EnumTracks.SMALL_STRAIGHT.getLabel());
						((TileTCRail) te1).switchActive=false;
					}
				}
				offsetX += a;
				offsetY += b;
				offsetZ += c;
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
