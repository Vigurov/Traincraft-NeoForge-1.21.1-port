package ebf.tim.entities;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.models.Bogie;
import ebf.tim.registry.TiMGenericRegistry;
import ebf.tim.render.TransportRenderData;
import ebf.tim.utility.BitList;
import ebf.tim.utility.CollisionBox;
import ebf.tim.utility.CommonUtil;
import ebf.tim.utility.HitboxDynamic;
import ebf.tim.utility.ItemStackSlot;
import ebf.tim.utility.XmlBuilder;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.*;

import static ebf.tim.TrainsInMotion.transportTypes.*;
import static ebf.tim.utility.CommonUtil.radianF;

/**
 * <h1>Generic Rail Transport</h1>
 * this is the base for all trains and rollingstock.
 *
 * 1.21 port: the bogies are part of this entity (their positions are synced data), the seats are the vanilla
 * passengers placed at {@link #getRiderOffsets()}, the hitbox boxes are NeoForge part entities. The movement,
 * linking and drag code is the 1.12 one.
 * @author Eternal Blue Flame
 */
public class GenericRailTransport extends Entity implements IEntityWithComplexSpawn {

    /*
     * <h2>variables</h2>
     */
    /**defines the colors, the outer array is for each different color, and the inner int[] is for the RGB color*/
    public List<Integer> colorsFrom = new ArrayList<>();
    public List<Integer> colorsTo = new ArrayList<>();
    /**the front entity bogie*/
    public EntityBogie frontBogie = null;
    /**the back entity bogie*/
    public EntityBogie backBogie = null;
    /**the server-sided persistent UUID of the transport linked to the front of this,*/
    public UUID frontLinkedTransport = null;
    /**the id of the rollingstock linked to the front*/
    public Integer frontLinkedID =null;
    /**the server-sided persistent UUID of the transport linked to the back of this,*/
    public UUID backLinkedTransport = null;
    /**the id of the rollingstock linked to the back*/
    public Integer backLinkedID = null;
    /**the id of the lead entity in the consist*/
    public Integer consistLeadID=null;
    /**the destination for routing*/
    public String destination ="";
    /**used to initialize a large number of variables that are used to calculate everything from movement to linking.
     * 0 is for rider positions
     * 1 is for bogie initialization and updating
     * 2 is for cached locomotive velocity.
     * */
    public Vec3f[] cachedVectors = new Vec3f[]{
            new Vec3f(0,0,0),new Vec3f(0,0,0),new Vec3f(0,0,0),new Vec3f(0,0,0)};
    /**the health of the entity, similar to that of EntityLiving*/
    private int health = 20;
    /**the list of items used for the inventory and crafting slots.*/
    public List<ItemStackSlot> inventory = new ArrayList<>();
    /**whether or not this needs to update the datawatchers*/
    public boolean updateWatchers = false;
    public HitboxDynamic collisionHandler=null;
    private final PartEntity<?>[] parts;
    float prevRotationRoll;
    float rotationRoll;
    /**calculated movement speed, first value is used for GUI and speed, second is used for render effects.*/
    @Deprecated //TODO: value 1 gets the number right more often, but value 2 gets direction right. HOW
    public float[] velocity=new float[]{0,0};
    public int deathTick=0;
    public float pullingWeight=0;
    /**1.12 entity motion: what the transport moved the last time it moved noticeably, per tick*/
    public double motionX, motionZ;
    /**the position the motion is measured from (1.12 prevPosX/Z, which vanilla resets every tick in 1.21)*/
    private double motionFromX, motionFromZ;

    private float ticksSinceLastVelocityChange=1;

    private List<GenericRailTransport> consist = new ArrayList<>();

    public TransportRenderData renderData = new TransportRenderData();

    public XmlBuilder entityData = new XmlBuilder();


    /**the array of booleans, defined as bits
     * 0- brake: defines the brake
     * 1- locked: defines if transport is locked to owner and key holders
     * 2- lamp: defines if lamp is on
     * 3- creative: defines if the transport should consume fuels and be able to derail.
     * 4- coupling: defines if the transport is looking to couple with other transports.
     * 5- inventory whitelist: defines if the inventory is a whitelist
     * 6- running: defines if te transport is running (usually only on trains).
     * 7-15 are unused.
     */
    private BitList bools = new BitList();

    public enum boolValues{BRAKE(0), LOCKED(1), LAMP(2), CREATIVE(3), COUPLINGFRONT(4), COUPLINGBACK(5), WHITELIST(6), RUNNING(7), DERAILED(8);
        public int index;
        boolValues(int index){this.index = index;}
    }

    public boolean getBoolean(boolValues index){
        return getBoolean(index.index);
    }
    public boolean getBoolean(int index){
        if(level()!=null && level().isClientSide){
            return bools.getFromInt(index, this.entityData().get(BOOLS));
        }
        return bools.get(index);
    }

    public void setBoolean(boolValues index, boolean value){
        setBoolean(index.index, value);
    }
    public void setBoolean(int index, boolean value){
        if(getBoolean(index)!=value) {
            bools.set(index, value);
            updateWatchers = true;
        }
    }

    public GenericRailTransport(EntityType<? extends GenericRailTransport> type, Level world){
        super(type, world);
        noPhysics = true;
        initInventorySlots();
        if(world!=null) {
            collisionHandler = new HitboxDynamic(getHitboxSize()[0],getHitboxSize()[1],getHitboxSize()[2], this);
            parts = collisionHandler.interactionBoxes.toArray(new PartEntity<?>[0]);
            //the parts need ids next to ours, the client gives them the same ones (see recreateFromPacket)
            this.setId(ENTITY_COUNTER.getAndAdd(parts.length + 1) + 1);
        } else {
            parts = new PartEntity<?>[0];
        }
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (parts != null) {
            for (int i = 0; i < parts.length; i++) {
                parts[i].setId(id + i + 1);
            }
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return parts.length > 0;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return parts;
    }

    /*
     * <h2>synced data</h2>
     */
    public static final EntityDataAccessor<Float> VELOCITY = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> BOOLS = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> FRONT_LINKED_ID = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> BACK_LINKED_ID = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> ACCELERATOR = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.STRING);
    /**the bogie positions: the client places and turns the transport from these, like 1.12 did from the bogie entities*/
    public static final EntityDataAccessor<org.joml.Vector3f> FRONT_BOGIE = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<org.joml.Vector3f> BACK_BOGIE = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.VECTOR3);
    /**the bogies' heights above the rail block (yOffset), used by the renderer*/
    public static final EntityDataAccessor<Float> BOGIE_Y_OFFSET = SynchedEntityData.defineId(GenericRailTransport.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(VELOCITY, 0.0F);
        builder.define(BOOLS, BitList.newInt());
        builder.define(FRONT_LINKED_ID, -1);
        builder.define(BACK_LINKED_ID, -1);
        builder.define(ACCELERATOR, 0);
        builder.define(SKIN, "");
        builder.define(FRONT_BOGIE, new org.joml.Vector3f());
        builder.define(BACK_BOGIE, new org.joml.Vector3f());
        builder.define(BOGIE_Y_OFFSET, 0.425f);
    }

    /**1.12 name of the synced data; vanilla's own entityData field is the XML data here*/
    public SynchedEntityData entityData() {
        return super.getEntityData();
    }

    /**
     * <h2>Entity first placed initialization</h2>
     * this is only ever called once, from the entity's item instance when the entity is first placed.
     */
    public void entityFirstInit(ItemStack item){}

    /**
     * override this to customize the inventory slots.
     * call this in the override if you just want to add more slots to the existing planned inventory size
     */
    public void initInventorySlots(){
        if (getInventoryRows()>0) {
            int index=40;
            for(int r = 0; r< getInventoryRows(); r++){
                for (int c=0;c<9;c++){
                    inventory.add(new ItemStackSlot(this, index, -97 + (c * 18), -19 + (r * 18) + ((int)((11 - getInventoryRows()) * 0.5f) * 18)));
                    index++;
                }
            }
        }
        //EntityTrainCore has it's own tanker management because fuel slots.
        if(getTypes().contains(TANKER) || getTypes().contains(TENDER)){
            inventory.add(tankerInputSlot());
            inventory.add(tankerOutputSlot());
        }
    }

    public ItemStackSlot fuelSlot(){
        if(getTypes().contains(STEAM)) {
            return new ItemStackSlot(this, 400, 114, 32).setOverlay(Items.COAL);
        }
        if(getTypes().contains(ELECTRIC)) {
            return new ItemStackSlot(this, 400, 114, 32).setOverlay(Items.REDSTONE);
        }
        return new ItemStackSlot(this, 400, 114, 32);
    }
    public ItemStackSlot waterSlot(){
        return new ItemStackSlot(this, 401,150,32).setOverlay(Items.WATER_BUCKET);
    }

    public ItemStackSlot tankerInputSlot(){
        return new ItemStackSlot(this, 400,150,-8).setOverlay(Items.WATER_BUCKET);
    }
    public ItemStackSlot tankerOutputSlot(){
        return new ItemStackSlot(this, 401,150,32).setOverlay(Items.BUCKET);
    }

    /*
     * <h2>base entity overrides</h2>
     */
    @Override
    public boolean isPushable() {return false;}

    @Override
    public boolean isPickable() {return !isRemoved();}

    @Override
    public boolean canBeCollidedWith() {return false;}

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256 * 256;
    }

    /**the client places the transport from the synced bogie positions; the server's position packets are not needed*/
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {}

    @Override
    public void recreateFromPacket(net.minecraft.network.protocol.game.ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        setId(packet.getId());
    }

    /*
     * <h2>interaction</h2>
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return interact(player, false, false, -1) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /**
     * the 1.12 interaction ids: -1 right click, 1 GUI, 5 lamp, 6 lock, 7 coupling, 10 creative, 13 unlink, 15 brake.
     * EntityTrainCore adds the driving ones. Server side.
     */
    public boolean interact(Player p, boolean isFront, boolean isBack, int key) {
        if (p == null || level().isClientSide) {
            return false;
        }
        //check if the player has permission first.
        if (!getPermissions(p, false, false)) {
            CommonUtil.sendChat(p, CommonUtil.translate("You don't have permission to do that."));
            return false;
        }
        switch (key){
            case -1: {//right click
                ItemStack held = p.getItemInHand(InteractionHand.MAIN_HAND);
                if (!held.isEmpty() && held.getItem() == TiMGenericRegistry.stakeItem()) {
                    boolean toset = !getBoolean(boolValues.COUPLINGFRONT);
                    setBoolean(boolValues.COUPLINGFRONT, toset);
                    setBoolean(boolValues.COUPLINGBACK, toset);
                    if(!p.isShiftKeyDown()) {
                        CommonUtil.sendChat(p, CommonUtil.translate(toset ? "message.linking" : "message.notlinking"));
                    } else if(frontLinkedTransport!=null || backLinkedTransport!=null){
                        //calling the method from itself is a very lazy way to do this, but it's less to write.
                        interact(p,isFront,isBack,13);
                        CommonUtil.sendChat(p, CommonUtil.translate("message.unlinked"));
                    } else {
                        CommonUtil.sendChat(p, CommonUtil.translate("message.nolinks"));
                    }
                    return true;
                }
                //be sure the player has permission to enter the transport, and that the transport has a seat open.
                if (getRiderOffsets() != null && getPermissions(p, false, true)) {
                    if (getPassengers().size() < getRiderOffsets().length && !p.isPassenger()) {
                        return p.startRiding(this);
                    }
                    return false;
                }
                return false;
            }
            case 15: {//toggle brake
                setBoolean(boolValues.BRAKE, !getBoolean(boolValues.BRAKE));
                updateLinks();
                return true;
            }case 5: { //Toggle lamp
                setBoolean(boolValues.LAMP, !getBoolean(boolValues.LAMP));
                return true;
            }case 6:{ //Toggle locked
                setBoolean(boolValues.LOCKED, !getBoolean(boolValues.LOCKED));
                return true;
            }case 10:{ //Toggle transport creative mode
                setBoolean(boolValues.CREATIVE, !getBoolean(boolValues.CREATIVE));
                return true;
            }case 7:{ //Toggle coupling for both ends
                boolean toset = !getBoolean(boolValues.COUPLINGFRONT);
                setBoolean(boolValues.COUPLINGFRONT, toset);
                setBoolean(boolValues.COUPLINGBACK, toset);
                return true;
            }case 13:{ //unlink transports
                unlink(frontLinkedID);
                frontLinkedTransport = null;
                frontLinkedID = null;
                unlink(backLinkedID);
                backLinkedTransport = null;
                backLinkedID = null;
                updateWatchers = true;
                updateConsist();
                return true;
            }
        }
        return false;
    }

    /**drops this transport from the link of the given neighbour*/
    private void unlink(@Nullable Integer otherID){
        if (otherID == null || !(level().getEntity(otherID) instanceof GenericRailTransport transport)){
            return;
        }
        if(transport.frontLinkedID !=null && transport.frontLinkedID == this.getId()){
            transport.frontLinkedTransport = null;
            transport.frontLinkedID = null;
        } else {
            transport.backLinkedTransport = null;
            transport.backLinkedID = null;
        }
        transport.updateWatchers = true;
        transport.updateConsist();
    }

    /*
     * <h2>passengers</h2>
     * 1.12 had seat entities; the passengers sit at the rider offsets in boarding order, the first one drives.
     */
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getRiderOffsets() != null && getPassengers().size() < getRiderOffsets().length;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        int i = getPassengers().indexOf(passenger);
        if (i < 0 || getRiderOffsets() == null || i >= getRiderOffsets().length) {
            return;
        }
        Vec3f seat = new Vec3f(getRiderOffsets()[i][0], getRiderOffsets()[i][1], getRiderOffsets()[i][2])
                .rotatePoint(getXRot(), getYRot(), 0f);
        // the 1.7.10 seat height is the eyes: put them 0.45 above the seat, which matches the view from inside the cab
        callback.accept(passenger, getX() + seat.xCoord, getY() + seat.yCoord + 0.45 - passenger.getEyeHeight(), getZ() + seat.zCoord);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 side = new Vec3(0, 0, getHitboxSize()[2] * 0.5 + 0.6).yRot(-getYRot() * radianF);
        return new Vec3(passenger.getX() + side.x, getY() + 0.1, passenger.getZ() + side.z);
    }

    /**if the given passenger is in the control seat (1.12 seat 0) of a locomotive*/
    public boolean isControlSeat(Entity passenger){
        return !getPassengers().isEmpty() && getPassengers().get(0) == passenger;
    }

    /**
     * <h2>damage and destruction</h2>
     * the damage done is handled manually so we can compensate for basically everything, and if health is 0 or lower, we destroy the entity.
     */
    @Override
    public boolean hurt(DamageSource damageSource, float amount){
        if(level().isClientSide){
            return true;
        }
        if (damageSource.getDirectEntity() instanceof GenericRailTransport){
            return false;
        }
        boolean projectile = damageSource.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE);
        boolean explosion = damageSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
        //if its a creative player, destroy instantly
        if (damageSource.getDirectEntity() instanceof Player && ((Player) damageSource.getDirectEntity()).isCreative() && !projectile){
            health -=20;
            //if its reinforced and its not an explosion
        } else if (isReinforced() && !projectile && !explosion){
            health -=1;
            //if it is an explosion and it's reinforced, or it's not an explosion and isn't reinforced
        } else if ((explosion && isReinforced()) || (!isReinforced() && !projectile)){
            health -=5;
            //if it isn't reinforced and is an explosion
        } else if (explosion && !isReinforced()){
            health-=20;
        }
        //cover overheating, or other damage to self.
        if (damageSource.getEntity() == this){
            health-=20;
        }

        //on Destruction
        if (health<1 && deathTick==0){
            deathTick=this.tickCount;
            //be sure we drop the inventory items on death.
            dropAllItems();
            if(!(damageSource.getDirectEntity() instanceof Player pl && pl.isCreative()) && getItem()!=null) {
                spawnAtLocation(getCartItem(), 1);
            }
            discard();
            return true;
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        //be sure the front and back links are removed in the case of this entity being removed from the world.
        if (reason.shouldDestroy()) {
            unlink(frontLinkedID);
            unlink(backLinkedID);
        }
    }

    @Override
    public ItemStack getPickResult() {
        return getItem() == null ? ItemStack.EMPTY : getCartItem();
    }

    /*
     * <h2> Data Syncing and Saving </h2>
     */
    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeNbt(entityData.tag);
        buffer.writeFloat(getYRot());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        CompoundTag t = additionalData.readNbt();
        entityData = new XmlBuilder(t == null ? new CompoundTag() : t);
        setYRot(additionalData.readFloat());
        yRotO = getYRot();
    }

    /**loads the entity's save file*/
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("timdata")) {
            entityData = new XmlBuilder(tag.getCompound("timdata"));
        }
        bools.set(tag.getByteArray("bools"));
        //load links
        if (tag.hasUUID("frontLink")) {
            frontLinkedTransport = tag.getUUID("frontLink");
        }
        if (tag.hasUUID("backLink")) {
            backLinkedTransport = tag.getUUID("backLink");
        }
        if (entityData.containsString("skin")) {
            String skin = entityData.getString("skin");
            if (SkinRegistry.getSkin(this, null, false, skin) == null) {
                entityData.putString("skin", getDefaultSkin());
            }
        }
        //load bogie positions
        if (tag.contains("frontBogieX")) {
            frontBogie = new EntityBogie(this, tag.getDouble("frontBogieX"), tag.getDouble("frontBogieY"), tag.getDouble("frontBogieZ"), true);
            backBogie = new EntityBogie(this, tag.getDouble("backBogieX"), tag.getDouble("backBogieY"), tag.getDouble("backBogieZ"), false);
            frontBogie.velocity[0] = tag.getDouble("frontBogieVX");
            frontBogie.velocity[1] = tag.getDouble("frontBogieVZ");
            backBogie.velocity[0] = tag.getDouble("backBogieVX");
            backBogie.velocity[1] = tag.getDouble("backBogieVZ");
        }

        rotationRoll = tag.getFloat("rotationRoll");
        prevRotationRoll = tag.getFloat("prevRotationRoll");
        health = tag.contains("health") ? tag.getInt("health") : 20;

        updateWatchers = true;
    }

    /**saves the entity to server world*/
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("timdata", entityData.tag);
        tag.putByteArray("bools", bools.getBits());
        //frontLinkedTransport and backLinkedTransport bogies
        if (frontLinkedTransport != null){
            tag.putUUID("frontLink", frontLinkedTransport);
        }
        if (backLinkedTransport != null){
            tag.putUUID("backLink", backLinkedTransport);
        }
        if (frontBogie != null && backBogie != null) {
            tag.putDouble("frontBogieX", frontBogie.posX);
            tag.putDouble("frontBogieY", frontBogie.posY);
            tag.putDouble("frontBogieZ", frontBogie.posZ);
            tag.putDouble("backBogieX", backBogie.posX);
            tag.putDouble("backBogieY", backBogie.posY);
            tag.putDouble("backBogieZ", backBogie.posZ);
            tag.putDouble("frontBogieVX", frontBogie.velocity[0]);
            tag.putDouble("frontBogieVZ", frontBogie.velocity[1]);
            tag.putDouble("backBogieVX", backBogie.velocity[0]);
            tag.putDouble("backBogieVZ", backBogie.velocity[1]);
        }
        tag.putFloat("rotationRoll", rotationRoll);
        tag.putFloat("prevRotationRoll", prevRotationRoll);
        tag.putInt("health", health);
    }

    /*
     * <h2>movement</h2>
     */
    public boolean hasDrag(){
        for(GenericRailTransport t:getConsist()){
            if(t.getAccelerator()!=0){
                return false;
            }
        }
        return true;
    }

    public void updatePosition(){

        //reposition bogies to be sure they are the right distance
        if(!level().isClientSide) {

            //do scaled rail boosting but keep it capped to the max velocity of the rail
            BlockPos here = BlockPos.containing(getX(), getY(), getZ());
            BlockState state = level().getBlockState(here);
            Block b = state.getBlock();
            if (b instanceof BaseRailBlock){
                setBoolean(boolValues.DERAILED, false);

                if (b == Blocks.POWERED_RAIL) {
                    if (state.getValue(PoweredRailBlock.POWERED) &&
                            //this part keeps it capped
                            getVelocity() < maxBoost((BaseRailBlock) b)) {
                        float boost = CommonUtil.getMaxRailSpeed(level(), (BaseRailBlock) b, this, getX(), getY(), getZ()) * 0.005f;
                        appendMovement(Math.copySign(cachedVectors[2].yCoord,boost));
                    }
                }
            } else {
                //set the derail state based on whether or not there's a valid rail block below.
                //the blocks below and above count too: around the top end of a slope the train's centre
                //is part way between the slope and the track one block up
                setBoolean(boolValues.DERAILED, !CommonUtil.isRailBlockAt(level(),getX(),getY(),getZ())
                        && !CommonUtil.isRailBlockAt(level(),getX(),getY()-1,getZ())
                        && !CommonUtil.isRailBlockAt(level(),getX(),getY()+1,getZ()));
            }


            //handle yaw changes for derail
            if(getBoolean(boolValues.DERAILED)) {
                Entity front = frontLinkedID == null ? null : level().getEntity(frontLinkedID);
                Entity back = backLinkedID == null ? null : level().getEntity(backLinkedID);
                if(front instanceof GenericRailTransport && back instanceof GenericRailTransport){
                    setYRot(CommonUtil.atan2degreesf(front.getZ() - back.getZ(), front.getX() - back.getX()));
                } else if (front instanceof GenericRailTransport){
                    setYRot(CommonUtil.atan2degreesf(front.getZ() - getZ(), front.getX() - getX()));
                } else if (back instanceof GenericRailTransport){
                    setYRot(CommonUtil.atan2degreesf(getZ() - back.getZ(), getX() - back.getX()));
                }
            }


            //actually move
            finalMove();
            //only update velocity if we've moved to any significance.
            if(Math.abs(getX()-motionFromX)>0.0625 || Math.abs(getZ()-motionFromZ)>0.0625) {
                motionX = (getX() - motionFromX)/ticksSinceLastVelocityChange;
                motionZ = (getZ() - motionFromZ)/ticksSinceLastVelocityChange;
                motionFromX = getX();
                motionFromZ = getZ();
                entityData().set(VELOCITY, getVelocity());

                ticksSinceLastVelocityChange=1;
            } else {
                motionX = (getX() - motionFromX)/ticksSinceLastVelocityChange;
                motionZ = (getZ() - motionFromZ)/ticksSinceLastVelocityChange;
                ticksSinceLastVelocityChange++;
                //otherwise clients keep showing the last real speed after the train stops
                entityData().set(VELOCITY, getVelocity());
            }
        }
    }

    public void appendMovement(double velocity){

        //the logic gets stupid if it's not sorted from one end or another.
        //todo: this is a trash fix, it would be better for the list to be reliably sorted
        if(frontLinkedID!=null && backLinkedID!=null){
            return;
        }
        GenericRailTransport last = this;
        for(GenericRailTransport t:getConsist()) {
            if(t.backBogie==null || t.frontBogie==null){
                continue;
            }
            if(t.backLinkedID!=null && last.backLinkedID!=null
                    && last.getId()==t.backLinkedID
                    && t.getId()==last.backLinkedID){
                t.backBogie.addVelocity(t, -velocity);
                t.frontBogie.addVelocity(t, -velocity);
            } else if(t.frontLinkedID!=null && last.frontLinkedID!=null
                    && last.getId()==t.frontLinkedID
                    && t.getId()==last.frontLinkedID){
                t.backBogie.addVelocity(t, -velocity);
                t.frontBogie.addVelocity(t, -velocity);
            } else {
                t.backBogie.addVelocity(t, velocity);
                t.frontBogie.addVelocity(t, velocity);
            }
        }
    }

    public void addLinkingMove(double velocity){
        backBogie.addLinking(this, velocity);
        frontBogie.addLinking(this, velocity);
    }

    public void finalMove(){
        cachedVectors[1] = new Vec3f(-rotationPoints()[0], 0, 0).rotatePoint(0, getYRot(), 0)
                .addVector(backBogie.posX,backBogie.posY,backBogie.posZ);
        setPos(cachedVectors[1].xCoord, cachedVectors[1].yCoord,cachedVectors[1].zCoord);

        frontBogie.minecartMove(this);
        backBogie.minecartMove(this);
        //reset the y coord so they will re-calculate the yaw
        if(hasDrag()) {
            applyDrag();
        }
        cachedVectors[2].yCoord=0;
        //update rotation
        setRotation((CommonUtil.atan2degreesf(
                backBogie.posZ - frontBogie.posZ,
                backBogie.posX - frontBogie.posX)),
                CommonUtil.calculatePitch(frontBogie.posY + frontBogie.yOffset, backBogie.posY + backBogie.yOffset, Math.abs(rotationPoints()[0]) + Math.abs(rotationPoints()[1])));

        //reset the vector when we're done so it wont break trains.
        cachedVectors[1]= new Vec3f(0,0,0);
        //update the collision handler's positions
        if(collisionHandler!=null) {
            collisionHandler.position(getX(), getY(), getZ(), getXRot(), getYRot());
        }
    }

    double maxBoost(BaseRailBlock booster){
        if(this.transportTopSpeed()>0){
            return Math.min(transportTopSpeed(),
                    CommonUtil.getMaxRailSpeed(level(), booster,this, getX(),getY(),getZ()));
        }
        return CommonUtil.getMaxRailSpeed(level(), booster,this, getX(),getY(),getZ());
    }

    /**
     * the logic of this is directly related to EnttiyTrainCore#calculateAcceleration
     */
    public void applyDrag(){
        boolean canSlope=true;
        float drag = 0.9998f, brakeBuff = 0, slope = 0;
        //check if lope things can be done at all
        for(GenericRailTransport stock : getConsist()) {
            if(stock!=this && getAccelerator()!=0){
                canSlope=false;
                break;
            }
        }
        if(canSlope) {
            if (getBoolean(boolValues.BRAKE)) {
                //realistically would be more like 2.4, but 5 makes gameplay more dramatic
                brakeBuff += weightKg() * 5.0f;
            }
            if (getXRot() != 0) {
                //vanilla uses 0.0078125 per tick for slope speed.
                //0.00017361 would be that divided by 45 since vanilla slopes are 45 degree angles.
                //scale by entity pitch
                //pitch goes from -90 to 90, so it's inherently directional, stop that.
                slope += (0.00017361f) * Math.abs(getXRot());
            }
            appendMovement(slope * Mth.sin((getYRot()-90)*radianF));
        }

        //now do drag stuff

        //scale drag for derail, or air lateral friction. if you do both at the same time then it's way too much.
        if(getBoolean(boolValues.DERAILED)){
            drag*=CommonUtil.getBlockAt(level(),getX(),getY(),getZ()).getFriction();
        } else if (cachedVectors[2].yCoord > 0) {
            drag -= ((getFriction() * cachedVectors[2].yCoord * 4.448f));
        }

        //add in the drag from combined weight, plus brakes.
        if(pullingWeight!=0) {//in theory this should never be 0, but we know forge is dumb
            drag -= ((getAccelerator()==0?getFriction()*0.75:getFriction()*2.5) * (pullingWeight + brakeBuff)) / 44480;
        }
        //cap the drag to prevent weird behavior.
        // if it goes to 1 or higher then we speed up, which is bad, if it's below 0 we reverse, which is also bad
        if (drag > 0.9999f) {
            drag = 0.9999f;
        } else if (drag < 0f) {
            drag = 0f;
        }

        for(GenericRailTransport t : getConsist()){
            if(t.frontBogie==null || t.backBogie==null){continue;}
            t.frontBogie.drag(t,drag);
            t.backBogie.drag(t,drag);
        }
    }

    public float getFriction(){return 0.0015f;}

    /**
     * <h2> on entity update </h2>
     *
     * defines what should be done every tick
     * managing the bogies, speed, acceleration, direction, rotation, links and collisions.
     */
    @Override
    public void tick() {
        //regen health after a while
        if(health<20 && tickCount%40==0){
            if(health>15){
                health=20;
            } else {
                health+=5;
            }
        }

        //if the cart has fallen out of the map, destroy it.
        if (getY() < level().getMinBuildHeight() - 64.0D){
            discard();
            return;
        }

        //always be sure the bogies exist on the server.
        if (!level().isClientSide && (frontBogie == null || backBogie == null)) {
            //spawn front bogie
            cachedVectors[1] = new Vec3f(rotationPoints()[1],0,0).rotatePoint(getXRot(), getYRot(),0);
            frontBogie = new EntityBogie(this, getX() + cachedVectors[1].xCoord, getY() + cachedVectors[1].yCoord, getZ() + cachedVectors[1].zCoord, true);
            //spawn back bogie
            cachedVectors[1] = new Vec3f(rotationPoints()[0],0,0).rotatePoint(getXRot(), getYRot(),0);
            backBogie = new EntityBogie(this, getX() + cachedVectors[1].xCoord, getY() + cachedVectors[1].yCoord, getZ() + cachedVectors[1].zCoord, false);

            if(!(this instanceof EntityTrainCore)) {
                updatePosition();
            }
            motionFromX = getX();
            motionFromZ = getZ();
            motionX = 0;
            motionZ = 0;
            entityData().set(VELOCITY, getVelocity());
        }

        //CLIENT UPDATE: place and turn the transport from the bogies the server sent
        if(level().isClientSide){
            org.joml.Vector3f f = entityData().get(FRONT_BOGIE), b = entityData().get(BACK_BOGIE);
            if (f.lengthSquared() != 0 || b.lengthSquared() != 0) {
                if (frontBogie == null) {
                    frontBogie = new EntityBogie(this, f.x, f.y, f.z, true);
                    backBogie = new EntityBogie(this, b.x, b.y, b.z, false);
                }
                frontBogie.setPosition(f.x, f.y, f.z);
                backBogie.setPosition(b.x, b.y, b.z);
                frontBogie.yOffset = backBogie.yOffset = entityData().get(BOGIE_Y_OFFSET);
                setRotation(CommonUtil.atan2degreesf(
                        backBogie.posZ - frontBogie.posZ,
                        backBogie.posX - frontBogie.posX),
                        CommonUtil.calculatePitch(
                                frontBogie.posY, backBogie.posY,
                                Math.abs(rotationPoints()[0]) + Math.abs(rotationPoints()[1])));
                cachedVectors[1] = new Vec3f(-rotationPoints()[0], 0, 0).rotatePoint(0, getYRot(), 0)
                        .addVector(backBogie.posX,backBogie.posY,backBogie.posZ);
                if (tickCount < 2) {
                    //no smooth move from the spawn position
                    setPos(cachedVectors[1].xCoord, cachedVectors[1].yCoord, cachedVectors[1].zCoord);
                    setOldPosAndRot();
                }
                setPos(cachedVectors[1].xCoord, cachedVectors[1].yCoord, cachedVectors[1].zCoord);
                if (tickCount < 2) {
                    yRotO = getYRot();
                    xRotO = getXRot();
                }
            }
            //signed speed along the transport for the wheel animation: positive moves towards the back
            Vec3f dir = new Vec3f(1, 0, 0).rotatePoint(0, getYRot(), 0);
            velocity[1] = (float) ((getX() - xo) * dir.xCoord + (getZ() - zo) * dir.zCoord);
            frontLinkedID = intOrNull(entityData().get(FRONT_LINKED_ID));
            backLinkedID = intOrNull(entityData().get(BACK_LINKED_ID));
            if(renderData!=null && renderData.bogies!=null){
                for(Bogie bogie : renderData.bogies){
                    bogie.updatePosition(this, null);
                    bogie.updateRotation(this);
                }
            }
            if(collisionHandler!=null) {
                collisionHandler.position(getX(), getY(), getZ(), getXRot(), getYRot());
                collisionHandler.updateCollidingEntities(this);
                for (Entity e : collisionHandler.collidingEntities) {
                    manageCollision(e);
                }
            }
        }


        /*
         * check if the bogies exist, and if they do, check if they are actually moving or colliding.
         * if it is clear then we need to add velocity to the bogies based on the current state of the train's speed and fuel, and reposition the train.
         * but either way we have to position the bogies around the train, just to be sure they don't accidentally fly off at some point.
         */
        else if (backBogie !=null && frontBogie != null && tickCount>5){
            frontBogie.onUpdate();
            backBogie.onUpdate();

            if((frontLinkedID!=null || backLinkedID!=null) && getConsist().size()==1){
                updateLinks();
            }
            //calculate for slopes, friction, and drag
            if (hasDrag()) {
                applyDrag();
            }
            //update positions related to linking, this NEEDS to come after drag
            if(frontLinkedID!=null && level().getEntity(frontLinkedID) instanceof GenericRailTransport front){
               manageLink(front);
            }
            if(backLinkedID!=null && level().getEntity(backLinkedID) instanceof GenericRailTransport back){
               manageLink(back);
            }
            //for some off reason, this madness works pretty reliably from my tests.
            cachedVectors[1]=new Vec3f(rotationPoints()[1],0,0).rotatePoint(0,getYRot(),0)
                    .addVector(getX(),0,getZ()).subtract((float)frontBogie.posX,0,(float)frontBogie.posZ);
            frontBogie.velocity[2]+=cachedVectors[1].xCoord;
            frontBogie.velocity[3]+=cachedVectors[1].zCoord;
            updatePosition();

            if(collisionHandler!=null){
                collisionHandler.updateCollidingEntities(this);
                for (Entity e : collisionHandler.collidingEntities) {
                    manageCollision(e);
                }
            }
        } else if (backBogie != null && collisionHandler != null) {
            collisionHandler.position(getX(), getY(), getZ(), getXRot(), getYRot());
        }

        if (!level().isClientSide) {
            //tell the clients where the bogies are
            if (frontBogie != null && backBogie != null) {
                entityData().set(FRONT_BOGIE, new org.joml.Vector3f((float) frontBogie.posX, (float) frontBogie.posY, (float) frontBogie.posZ));
                entityData().set(BACK_BOGIE, new org.joml.Vector3f((float) backBogie.posX, (float) backBogie.posY, (float) backBogie.posZ));
                entityData().set(BOGIE_Y_OFFSET, frontBogie.yOffset);
            }

            if (tickCount %20==0){
                manageFuel();

                if (!entityData.containsString("ownername") || entityData.getString("ownername").equals("")) {
                    UUID owner = entityData.getUUID("owner");
                    Player player = owner == null ? null : level().getPlayerByUUID(owner);
                    if (player != null) {
                        entityData.putString("ownername",player.getName().getString());
                        updateWatchers = true;
                    }
                }
                //sync the linked transports with client, and on server, easier to use an ID than a UUID.
                Entity linkedTransport = frontLinkedTransport == null ? null : ((net.minecraft.server.level.ServerLevel) level()).getEntity(frontLinkedTransport);
                if (linkedTransport instanceof GenericRailTransport
                        && (frontLinkedID == null || linkedTransport.getId() != frontLinkedID)
                        && (backLinkedID == null || linkedTransport.getId() != backLinkedID)) {
                    frontLinkedID = linkedTransport.getId();
                    updateWatchers = true;
                }
                linkedTransport = backLinkedTransport == null ? null : ((net.minecraft.server.level.ServerLevel) level()).getEntity(backLinkedTransport);
                if (linkedTransport instanceof GenericRailTransport
                        && (backLinkedID == null || linkedTransport.getId() != backLinkedID)
                        && (frontLinkedID == null || linkedTransport.getId() != frontLinkedID)) {
                    backLinkedID = linkedTransport.getId();
                    updateWatchers = true;
                }
            }

            if(updateWatchers){
                entityData().set(BOOLS, bools.toInt());
                entityData().set(FRONT_LINKED_ID, frontLinkedID!=null?frontLinkedID:-1);
                entityData().set(BACK_LINKED_ID, backLinkedID!=null?backLinkedID:-1);
                if (entityData.containsString("skin")) {
                    entityData().set(SKIN, entityData.getString("skin"));
                }
                updateWatchers = false;
            }
        }
    }

    private static Integer intOrNull(int id){
        return id < 0 ? null : id;
    }

    public void manageCollision(Entity e){
        //on client we need to push away players.
        if(level().isClientSide){
            if (e instanceof Player) {
                double[] motion = CommonUtil.rotatePoint(0.1, 0,
                        CommonUtil.atan2degreesf(e.getZ()- getZ(), e.getX()-getX()));
                e.push(motion[0], 0.05, motion[2]);
            }
        } else {
            if (e instanceof ItemEntity) {
                if (getTypes() != null && getTypes().contains(TrainsInMotion.transportTypes.HOPPER) && e.getY() > this.getY() + 0.5f &&
                        !((ItemEntity) e).getItem().isEmpty() && isItemValidForSlot(0, ((ItemEntity) e).getItem())) {
                    addItem(((ItemEntity) e).getItem());
                    e.discard();
                }

            } else if (e instanceof CollisionBox colliding) {

                //which end of each transport touches the other: the one whose bogie is nearer to the other's centre.
                GenericRailTransport other = colliding.host;
                if (other != null && other != this && frontBogie != null && backBogie != null
                        && other.frontBogie != null && other.backBogie != null) {
                    boolean thisFront = distSq(frontBogie, other) < distSq(backBogie, other);
                    boolean otherFront = distSq(other.frontBogie, this) < distSq(other.backBogie, this);
                    boolean thisOpen = getBoolean(thisFront ? boolValues.COUPLINGFRONT : boolValues.COUPLINGBACK)
                            && (thisFront ? frontLinkedID == null : backLinkedID == null);
                    boolean otherOpen = other.getBoolean(otherFront ? boolValues.COUPLINGFRONT : boolValues.COUPLINGBACK)
                            && (otherFront ? other.frontLinkedID == null : other.backLinkedID == null);
                    if (thisOpen && otherOpen) {
                        if (thisFront) {
                            setFrontLinkedTransport(other);
                        } else {
                            setbackLinkedTransport(other);
                        }
                        if (otherFront) {
                            other.setFrontLinkedTransport(this);
                        } else {
                            other.setbackLinkedTransport(this);
                        }
                        updateConsist();
                        Player listener = level().getNearestPlayer(this,20);
                        if(listener!=null && getItem()!=null && other.getItem()!=null){
                            CommonUtil.sendChat(listener, "Linked the " +
                                    getItem().getDescription().getString() + " to the " +
                                    other.getItem().getDescription().getString());
                        }
                        return;
                    }
                }

                if (getBoolean(boolValues.BRAKE)) {
                    appendMovement(-(Math.max(Math.abs(getVelocity()), (Math.abs(colliding.host.getVelocity()))*0.25)+0.05)*0.5);
                } else {
                    appendMovement(-(Math.max(Math.abs(getVelocity()), (Math.abs(colliding.host.getVelocity()))*0.25)+0.05));
                }

                if (colliding.host.getBoolean(boolValues.BRAKE)) {
                    appendMovement((Math.max(Math.abs(getVelocity()), (Math.abs(colliding.host.getVelocity()))*0.25)+0.05)*0.5);
                } else {
                    appendMovement((Math.max(Math.abs(getVelocity()), (Math.abs(colliding.host.getVelocity()))*0.25)+0.05));
                }

            } else if (e instanceof LivingEntity) {
                if (!getBoolean(boolValues.BRAKE) && collisionHandler.front!=null) {
                    boolean pushable = true;
                    for (GenericRailTransport t: getConsist()){
                        if(t.getAccelerator()!=0){
                            pushable = false;
                            break;
                        }
                    }
                    if (pushable) {
                        double distanceFront = e.distanceToSqr(collisionHandler.front);
                        double distanceBack = e.distanceToSqr(collisionHandler.back);
                        if (distanceFront < distanceBack) {
                            appendMovement(-0.005);
                        } else {
                            appendMovement(0.005);
                        }
                    }
                }

                double[] motion = CommonUtil.rotatePoint(0.025, 0,
                        CommonUtil.atan2degreesf(getZ() - e.getZ(), getX() - e.getX()));
                e.push(motion[0], 0.05, motion[2]);
                if (e instanceof ServerPlayer sp) {
                    sp.hurtMarked = true;
                }

                //hurt entity if going fast
                if (getVelocity() > 0.25f) {
                    e.hurt(damageSources().generic(), getVelocity() * 0.5f);
                }
            }
        }
    }

    private static double distSq(EntityBogie a, Entity b){
        return (a.posX - b.getX()) * (a.posX - b.getX()) + (a.posZ - b.getZ()) * (a.posZ - b.getZ());
    }

    public int getAccelerator(){return 0;}

    public void setFrontLinkedTransport(GenericRailTransport other){
        frontLinkedID=other.getId();
        frontLinkedTransport=other.getUUID();
        setBoolean(boolValues.COUPLINGFRONT, false);
        updateWatchers = true;
    }

    public void setbackLinkedTransport(GenericRailTransport other){
        backLinkedID=other.getId();
        backLinkedTransport=other.getUUID();
        setBoolean(boolValues.COUPLINGBACK, false);
        updateWatchers = true;
    }

    private GenericRailTransport transportById(Integer id){
        if (id == null) {
            return null;
        }
        Entity e = level().getEntity(id);
        return e instanceof GenericRailTransport ? (GenericRailTransport) e : null;
    }

    /**
     * iterates all the links to check if the stock has a train
     * called on linking changes and when a train changes running states
     */
    public void updateConsist(){
        List<GenericRailTransport> transports = new ArrayList<>();
        List<Integer> IDs = new ArrayList<>();
        Integer lead=null;
        GenericRailTransport link=this;
        transports.add(link);
        IDs.add(getId());
        if(getAccelerator()!=0){
            lead=getId();
        }
        for (Integer start : new Integer[]{frontLinkedID, backLinkedID}) {
            link = transportById(start);
            while (link != null) {
                if (!transports.contains(link)) {
                    if (link.getAccelerator() != 0) {
                        lead = link.getId();
                    }
                    transports.add(link);
                    IDs.add(link.getId());
                    if (link.frontLinkedID != null && !IDs.contains(link.frontLinkedID)) {
                        link = transportById(link.frontLinkedID);
                    } else if (link.backLinkedID != null && !IDs.contains(link.backLinkedID)) {
                        link = transportById(link.backLinkedID);
                    }
                } else {
                    link = null;
                }
            }
        }

        for(GenericRailTransport t:transports){
            t.consist=transports;
            t.consistLeadID=lead;
        }

        updateLinks();
    }

    public void updateLinks(){
        Integer running=null;
        for (GenericRailTransport t : getConsist()) {
            if(t.getAccelerator()!=0){
                running=t.getId();
            }
        }
        //now tell everything in the list, including this, that there's a new list, and provide said list.
        for (GenericRailTransport t : getConsist()) {
            t.setValuesOnLinkUpdate(getConsist());
            t.consistLeadID=running;
        }
    }

    /**
     * called on linking changes and when a train changes running states
     * @see #updateConsist()
     * @param consist the list of entities in the consist
     */
    public void setValuesOnLinkUpdate(List<GenericRailTransport> consist){
        pullingWeight=0;
        this.consist=consist;
        for(GenericRailTransport t : consist) {
            pullingWeight +=t.weightKg();
        }
    }

    /**
     * May return a 0 length array when consist is being updated.
     */
    public List<GenericRailTransport> getConsist(){
        if(consist.size()<2 && frontLinkedID!=null && backLinkedID!=null){
            updateConsist();
        }
        if(consist.size()>0) {
            return consist;
        } else {
            return Collections.singletonList(this);
        }
    }

    //gets the power for acceleration math, result is in MHP, has a fallback that roughly converts TE to MHP
    public float getPower(){return (transportMetricHorsePower()>0f?transportMetricHorsePower()
            :transportTractiveEffort()*0.0035571365f);}
    /**gets the multiplication of fuel consumption, 1 is normal, 2 would be double, 1.5 would be halfway between the two, etc.*/
    public float getFuelEfficiency(){return 1;}

    public float getRunningEfficiency(){return 0.7f;}

    /**
     * used to define if the rider should sit based on the seat ID
     */
    public boolean shouldRiderSit(int seat){
        return shouldRiderSit();
    }
    public boolean shouldRiderSit(){
        return true;
    }

    /** the 1.12 Railcraft link distance, used for the rest spacing of a new coupling */
    public float getOptimalDistance(GenericRailTransport cart) {
        return getHitboxSize()[0] * 0.5f;
    }

    /**
     * <h2>manage links</h2>
     */
    /** spacing of each coupled neighbour (by entity id), taken when the link is first seen */
    private final java.util.Map<Integer, Double> linkRest = new java.util.HashMap<>();

    /** the locomotive that drives this consist: one with the throttle open, else any locomotive, else none */
    private GenericRailTransport consistDriver() {
        GenericRailTransport any = null;
        for (GenericRailTransport t : getConsist()) {
            if (t instanceof EntityTrainCore) {
                if (t.getAccelerator() != 0) {
                    return t;
                }
                any = t;
            }
        }
        return any;
    }

    /** whether walking the coupling chain from this through other reaches target */
    private boolean chainLeadsTo(GenericRailTransport other, GenericRailTransport target) {
        GenericRailTransport prev = this, cur = other;
        for (int i = 0; i < 64 && cur != null; i++) {
            if (cur == target) {
                return true;
            }
            Integer next = (cur.frontLinkedID != null && cur.frontLinkedID == prev.getId()) ? cur.backLinkedID : cur.frontLinkedID;
            prev = cur;
            cur = transportById(next);
        }
        return false;
    }

    /**
     * Couplings. A car follows the neighbour on its locomotive's side: every tick it moves exactly by the change of
     * their spacing, keeping the spacing they had when they coupled. (TiM used springs to both neighbours, aimed at
     * half the hitbox lengths: coupled trains could not start, long ones bunched up, oscillated and overtook the loco.)
     * Without a locomotive in the consist, a soft spring to both neighbours.
     */
    public void manageLink(GenericRailTransport other) {
        if(other.backBogie ==null || other.frontBogie ==null || backBogie ==null || frontBogie ==null){
            return;
        }
        GenericRailTransport driver = consistDriver();
        if (driver == this || (driver == null && getAccelerator() != 0)) {
            return;
        }

        double vecX = other.getX() - getX();
        double vecZ = other.getZ() - getZ();
        double dist = Math.sqrt(vecX * vecX + vecZ * vecZ);
        Double rest = linkRest.get(other.getId());
        if (rest == null) {
            rest = Math.min(dist, (double)(getOptimalDistance(other) + other.getOptimalDistance(this)));
            linkRest.put(other.getId(), rest);
        }
        //moving towards our back (positive, the yaw vector points front -> back) closes up on a neighbour behind us.
        //Which side the neighbour is on comes from where it really is: after a pile-up the cars can be out of order,
        //and a spring signed by front/back link then pushed them apart for ever
        double backX = backBogie.posX - frontBogie.posX, backZ = backBogie.posZ - frontBogie.posZ;
        double side = vecX * backX + vecZ * backZ;
        double towardsOther = Math.abs(side) > 1e-6 ? Math.signum(side)
                : (frontLinkedID != null && other.getId() == frontLinkedID) ? -1 : 1;

        //a derailed loco gives no usable speed (its bogies slide about): the cars brake and only keep their spacing,
        //following it made them run through each other
        if (driver != null && driver.getBoolean(boolValues.DERAILED)) {
            for (int i = 0; i < 2; i++) {
                frontBogie.velocity[i] *= 0.85;
                backBogie.velocity[i] *= 0.85;
            }
            driver = null;
        }
        if (driver != null) {
            if (!chainLeadsTo(other, driver)) {
                return; //the neighbour on the far side follows us instead
            }
            //move with the locomotive: towards the neighbour on its side when it pulls, away when it pushes, at its
            //speed (our own speed and direction are dropped: on curves they could turn round and run away) ...
            double lvx = driver.frontBogie.velocity[0], lvz = driver.frontBogie.velocity[1];
            double leadSpeed = Math.abs(lvx) + Math.abs(lvz);
            double pull = (lvx * (driver.getX() - getX()) + lvz * (driver.getZ() - getZ())) >= 0 ? 1 : -1;
            frontBogie.velocity[0] = frontBogie.velocity[1] = 0;
            backBogie.velocity[0] = backBogie.velocity[1] = 0;
            //... and trim the spacing softly: positions show a move one tick late, a full correction overshoots
            double trim = Math.max(-0.5, Math.min(0.5, (dist - rest) * 0.4));
            double move = pull * leadSpeed + trim;
            if (Math.abs(move) > 0.0005) {
                addLinkingMove(towardsOther * move);
            }
            return;
        }

        double springDist = Math.max(-0.1, Math.min(0.1, (dist - rest) * 0.3));
        if(Math.abs(springDist)>0.002) {
            addLinkingMove(towardsOther * springDist);
        }
    }

    /**
     * <h2>Permissions handler</h2>
     * Used to check if the player has permission to do whatever it is the player is trying to do.
     *
     * @param player the player attenpting to interact.
     * @param driverOnly can this action only be done by the driver/conductor?
     * @return if the player has permission to continue
     */
    public boolean getPermissions(Player player, boolean driverOnly, boolean decreaseTicketStack) {
        //make sure the player is not null, and be sure that driver only rules are applied.
        if (player ==null){
            return false;
        } else if (driverOnly && (player.getVehicle() != this || !isControlSeat(player))){
                return false;
        }

        //be sure operators and owners can do whatever
        if ((player.isCreative() && player.hasPermissions(2))
                || (entityData!=null && entityData.containsString("ownername") && entityData.getString("ownername").equals(player.getName().getString()))) {
            return true;
        }

        //all else fails, just return if this is locked.
        return !getBoolean(boolValues.LOCKED);
    }

    protected void setRotation(float yaw, float pitch) {
        setYRot(yaw);
        setXRot(pitch);
    }

    protected void setRotation(float yaw, float pitch, float roll){
        setRotation(yaw, pitch);
        this.prevRotationRoll = this.rotationRoll = roll;
    }

    /**defines the ID of the owner*/
    public String getOwnerName(){return entityData.containsString("ownername")?entityData.getString("ownername"):"";}

    public void setOwner(UUID owner){
        entityData.putUUID("owner", owner);
    }

    public TransportSkin getTexture(Player viewer){
        return getSkinList(viewer, false) == null ? null : getSkinList(viewer, false).get(getSkinName());
    }

    public TransportSkin getCurrentSkin(){
        return getSkinList(null, false) == null ? null : getSkinList(null, false).get(getSkinName());
    }

    private String getSkinName(){
        if (level() != null && level().isClientSide) {
            String s = entityData().get(SKIN);
            return s.isEmpty() ? getDefaultSkin() : s;
        }
        if(!this.entityData.containsString("skin")){
            this.entityData.putString("skin", getDefaultSkin());
            updateWatchers = true;
        }
        return this.entityData.getString("skin");
    }

    public String getCurrentSkinName(){
        TransportSkin s = getCurrentSkin();
        return s==null||s.name==null?"":s.name;
    }

    //only works when called from server
    public void setSkin(String s){
        this.entityData.putString("skin", s);
        updateWatchers = true;
    }

    public float getVelocity(){
        return level().isClientSide?entityData().get(VELOCITY):
                (float)(Math.abs(motionX)+Math.abs(motionZ));
    }

    public TransportSkin getTextureByID(Player viewer, boolean isPaintBucket, String skinId){
        return getSkinList(viewer, isPaintBucket) == null ? null : getSkinList(viewer, isPaintBucket).get(skinId);
    }

    /**
     * Method to allow entities to override TransportSkin interactions.
     * If the player is null, then the call is being made for saving and loading, and usually should not be modified.
     */
    public Map<String, TransportSkin> getSkinList(Player viewer, boolean isPaintBucket){
        return SkinRegistry.getTransportSkins(getClass());
    }

    /**
     * return the name for the default TransportSkin of the transport.
     */
    public String getDefaultSkin(){
        return getSkinList(null, false) == null || getSkinList(null, false).isEmpty() ? "" : getSkinList(null, false).keySet().iterator().next();
    }

    /*
     * <h2>inventory</h2>
     * todo: the 1.12 inventory, GUI and fluid tanks
     */
    public int getSizeInventory() {
        return inventory.size();
    }

    public ItemStack getCartItem(){
        return new ItemStack(getItem());
    }

    public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
        return true;
    }

    public void addItem(ItemStack item){
        for (ItemStackSlot s : inventory) {
            if (!s.getHasStack() && s.getSlotID() >= 40 && s.getSlotID() < 400) {
                s.setStack(item.copy());
                return;
            }
        }
    }

    public int calculatePercentageOfSlotsUsed(int indexes){
        if (inventory.isEmpty()) {
            return 0;
        }
        int used = 0;
        for (ItemStackSlot s : inventory) {
            if (s.getHasStack()) used++;
        }
        return (int) Math.ceil(indexes * (used / (float) inventory.size()));
    }

    public ItemStack getFirstBlock(int index){
        return ItemStack.EMPTY;
    }

    public void dropAllItems() {
        for (ItemStackSlot s : inventory) {
            if (s.getHasStack()) {
                spawnAtLocation(s.getStack(), 1);
                s.setStack(ItemStack.EMPTY);
            }
        }
    }

    /**1.12 fluid tank info, none yet*/
    public FluidTankInfo[] getTankInfo(){
        return new FluidTankInfo[]{null};
    }

    public static class FluidTankInfo {
        public FluidAmount fluid;
    }

    public static class FluidAmount {
        public int amount;
    }

    public void manageFuel(){}

    /*
     * <h2>Inherited variables</h2>
     * these functions are overridden by classes that extend this so that way the values can be changed indirectly.
     */
    public float[][] bogieModelOffsets(){return null;}

    public ModelBase[] bogieModels(){return null;}

    public Bogie[] bogies(){
        if(bogieModels()==null){
            return null;
        }
        Bogie[] b = new Bogie[bogieModels().length];
        for(int i=0;i<bogieModels().length;i++){
            b[i]=new Bogie(bogieModels()[i], bogieModelOffsets()==null||bogieModelOffsets().length<=i?null:bogieModelOffsets()[i]);
        }
        return b;
    }

    /**defines the points that the entity uses for path-finding and rotation, with 0 being the entity center.
     * Usually the value where the front and back bogies are.
     * example:
     * return new float[]{-3.5f, 3.5f};*/
    public float[] rotationPoints(){return bogieLengthFromCenter();}

    @Deprecated
    public float[] bogieLengthFromCenter(){return new float[]{1,-1};}

    public float getPistonOffset(){return 0;}

    public float getRenderScale(){return 0.0625f;}

    public float getPlayerScale(){return 0.65f;}

    public float[][] modelOffsets(){return null;}

    public float[][] modelRotations(){return null;}

    public void registerSkins(){}

    public ModelBase[] getModel(){return null;}

    public float[][] getRiderOffsets(){return null;}

    public float[] getHitboxSize(){return new float[]{3,1.5f,0.21f};}

    public boolean isReinforced(){return false;}

    public int getInventoryRows(){return 0;}

    public int[] getTankCapacity(){return null;}

    public float getMaxFuel(){return 0;}

    public String[] setParticles(){return null;}

    public float[] getAnimationData(int id) {return null;}

    public float weightKg(){return 907.18474f;}

    public ItemStack[] getRecipe(){return null;}

    public String transportName(){return "Fugidsnot";}

    public String transportcountry(){return "Nowhere";}

    public String transportYear(){return "19 somethin'";}

    public String transportFuelType(){return null;}

    public float transportTopSpeed(){return 0;}

    public float transportTopSpeedReverse(){return getTypes().contains(DIESEL)?transportTopSpeed()*0.5f:transportTopSpeed()*0.75f;}

    public boolean isFictional(){return true;}

    public float transportTractiveEffort(){return 0;}

    public float transportMetricHorsePower(){return 0;}

    public String[] additionalItemText(){return null;}

    public List<TrainsInMotion.transportTypes> getTypes(){return TrainsInMotion.transportTypes.SLUG.singleton();}

    /**the item of this transport: TiM kept it in a static field of each class, 1.21 registers it with the entity type*/
    public Item getItem(){return TiMGenericRegistry.itemFor(getClass());}
}
