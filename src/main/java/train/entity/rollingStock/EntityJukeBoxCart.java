package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import ebf.tim.registry.TiMOres;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import train.library.Info;

import java.util.List;
import java.util.function.Consumer;

/**
 * The jukebox cart: plays an internet radio stream (MP3, from an .m3u/.pls link) to the players around it,
 * louder the closer they are. 1.12 kept the TC4 class but never made it a TiM transport, so it was not in the
 * game; the stats and model placement are TC 4.5's, the recipe the commented-out 1.12 one.
 */
public class EntityJukeBoxCart extends GenericRailTransport {
	public static final EntityDataAccessor<String> STREAM = SynchedEntityData.defineId(EntityJukeBoxCart.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<Boolean> PLAYING = SynchedEntityData.defineId(EntityJukeBoxCart.class, EntityDataSerializers.BOOLEAN);

	/** set by the client: the GUI, and the playing that follows the synced state every tick */
	public static Consumer<EntityJukeBoxCart> openScreen = j -> {};
	public static Consumer<EntityJukeBoxCart> clientTick = j -> {};
	public static Consumer<EntityJukeBoxCart> clientRemoved = j -> {};

	/** this player's own loudness for this jukebox (1.12: the GUI's + and -), client side */
	public float volume = 1.0f;
	/** the client's player of the stream (a train.core.util.MP3Player), client side */
	public Object player;

	public EntityJukeBoxCart(EntityType<? extends GenericRailTransport> type, Level worldObj) {
		super(type, worldObj);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STREAM, "");
		builder.define(PLAYING, false);
	}

	public String getStreamURL() {
		return entityData().get(STREAM);
	}

	public boolean isPlaying() {
		return entityData().get(PLAYING);
	}

	/** server side, from the GUI */
	public void recievePacket(String url, boolean playing) {
		entityData().set(STREAM, url);
		entityData().set(PLAYING, playing && !url.isBlank());
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			clientTick.accept(this);
		}
	}

	@Override
	public void onClientRemoval() {
		super.onClientRemoval();
		clientRemoved.accept(this);
	}

	/** right click with an empty hand: the jukebox screen (the stake and the rest keep their use) */
	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (player.getItemInHand(hand).isEmpty() && !player.isShiftKeyDown()) {
			if (level().isClientSide) openScreen.accept(this);
			return InteractionResult.sidedSuccess(level().isClientSide);
		}
		return super.interact(player, hand);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("StreamUrl", getStreamURL());
		tag.putBoolean("isPlaying", isPlaying());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData().set(STREAM, tag.getString("StreamUrl"));
		entityData().set(PLAYING, tag.getBoolean("isPlaying"));
	}

	//main stats
	@Override
	public String transportName(){return "JukeBox Cart";}
	@Override
	public String transportcountry(){return "";}
	@Override
	public String transportYear(){return "";}
	@Override
	public boolean isFictional(){return true;}
	@Override
	public int getInventoryRows(){return 0;}
	@Override
	public List<TrainsInMotion.transportTypes> getTypes(){
		return TrainsInMotion.transportTypes.OTHER.singleton();
	}
	@Override
	public float weightKg(){return 8000f;}

	//Model stuff
	@Override
	public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelJukeBox()};}
	@Override
	public float[][] modelOffsets(){return new float[][]{{0.0f, 0.63f, 0.0f}};}
	@Override
	public float[][] modelRotations(){return new float[][]{{0.0f, 180.0f, 180.0f}};}
	@Override
	public void registerSkins(){
		SkinRegistry.addSkin(this.getClass(),
				new TransportSkin(Info.modID,"textures/trains/jukebox.png","Default", "description.jukebox.default"));
	}
	@Override
	public String getDefaultSkin(){
		return Info.modID+":"+"Default";
	}

	//recipe: TC 4.5's / the 1.12 assembly table III one
	@Override
	public ItemStack[] getRecipe() {
		return new ItemStack[]{
				new ItemStack(TiMOres.ingotSteel.get(), 5), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameSteel.get(), 2),
				new ItemStack(TiMOres.ingotSteel.get(), 2), null, null, null, null, new ItemStack(Blocks.JUKEBOX, 1)};
	}
	@Override
	public String getCraftingTable(){return "traintabletier3";}

	//these are separated for being fiddly.
	@Override
	public float[][] getRiderOffsets(){return null;}
	@Override
	public float[] getHitboxSize(){return new float[]{3.7f, 2.1f, 1.1f};}
	@Override
	public float[] rotationPoints() {return new float[]{1.85f, -1.85f};}

	@Override
	public float getMaxFuel(){return 1;}
}
