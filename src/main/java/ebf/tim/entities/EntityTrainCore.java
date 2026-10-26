package ebf.tim.entities;

import ebf.tim.utility.CommonUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static ebf.tim.TrainsInMotion.transportTypes.*;

/**
 * <h1>Train core</h1>
 * this is the management core for all trains.
 * @author Eternal Blue Flame
 */
public class EntityTrainCore extends GenericRailTransport {

    /**manages the items for burnHeat, and the burnHeat itself.*/
    public ebf.tim.utility.FuelHandler fuelHandler = new ebf.tim.utility.FuelHandler();
    /**defines the speed percentage the user is attempting to apply.*/
    public int accelerator =0;

    public EntityTrainCore(EntityType<? extends GenericRailTransport> type, Level world){
        super(type, world);
    }

    /**loads the entity's save file*/
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        accelerator = tag.getInt("accelerator");
        headlights.load(tag);
    }

    /**the light blocks ahead while the lamps are on*/
    public final ebf.tim.utility.Headlights headlights = new ebf.tim.utility.Headlights();

    @Override
    public void remove(RemovalReason reason) {
        headlights.clear(level());
        super.remove(reason);
    }

    /**saves the entity to server world*/
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("accelerator", getAccelerator());
        headlights.save(tag);
    }

    @Override
    public void initInventorySlots(){
        super.initInventorySlots();
        inventory.add(fuelSlot());
        //see super method's to-do
        if(getTypes().contains(STEAM)){
            inventory.add(waterSlot());
        }
    }

    //gets the throttle position as a percentage with 1 as max and -1 as max reverse
    public float getAcceleratorPercentage(){
        switch (Math.abs(getAccelerator())){
            case 1:{return Math.copySign(0.1f,getAccelerator());}
            case 2:{return Math.copySign(0.25f,getAccelerator());}
            case 3:{return Math.copySign(0.5f,getAccelerator());}
            case 4:{return Math.copySign(0.7f,getAccelerator());}
            case 5:{return Math.copySign(0.875f,getAccelerator());}
            case 6: case 7:{return Math.copySign(1f,getAccelerator());}
            default:{return 0;}
        }
    }

    /**
     * <h2>Calculate speed increase rate</h2>
     * this has a direct relation to GenericRailTransport#applyDrag
     */
    public void calculateAcceleration(){
        //core accel speed from TC4
        float accel=0.0025f* getAcceleratorPercentage();
        //buff to match engine type
        if(getTypes().contains(STEAM)){
            accel*=0.35f;
        } else if(getTypes().contains(DIESEL)){
            accel*=0.5f;
        } else if(getTypes().contains(ELECTRIC)){
            accel*=0.7f;
        }
        //scale based on power and velocity
        cachedVectors[2].xCoord=accel* (10000f * (float)Math.pow(8f,((2.35f * -(0.3557/getPower()))-2.35f)));
        //if speed is greater than top speed from km/h to m/s divided by 20 to get per tick
        if (Math.abs(frontBogie.velocity[0])+Math.abs(frontBogie.velocity[1]) >
                unRatio(accelerator>0?transportTopSpeed():transportTopSpeedReverse())) {
            cachedVectors[2].xCoord=0;
        }
        //off the rails the wheels have nothing to push against
        if(getBoolean(boolValues.DERAILED)) {
            cachedVectors[2].xCoord = 0;
        }
    }

    //TC4 uses this to scale movement speeds. documented as:
    //speed *= 10;// convert in ms
    //speed *= 6;// applying ratio
    //speed *= 3.6;// convert in km/h
    //unratio needs a +=1 because it always rounds down.
    public static float ratio(float val){return  ((val*6f)*36f);}
    public static float unRatio(float val){return  (((val+1)*0.16667f)*0.027778f);}

    /**a method to interface getting the accelerator value
     * this is intended for external use like collisions that need to see if the train is in gear from a superclass cast*/
    @Override
    public int getAccelerator(){
        return level()==null || !level().isClientSide?accelerator:entityData().get(ACCELERATOR);
    }

    /**
     * <h2> on entity update</h2>
     * first we have to manage our speed by updating our motion vector and applying it to the bogies.
     * after that we let the super handle things.
     */
    @Override
    public void tick() {
        if(!level().isClientSide && backBogie != null && frontBogie != null) {
            cachedVectors[2].xCoord = 0;
            //twice a second, re-calculate the speed.
            if (getAccelerator()!=0 && getBoolean(boolValues.RUNNING)) {
                if(Math.abs(getAccelerator())!=8 && tickCount % 10 == 0) {
                    calculateAcceleration();
                }
            } else {
                accelerator = 0;
                entityData().set(ACCELERATOR, getAccelerator());
            }
            if(getAccelerator()==0 && getBoolean(boolValues.BRAKE) && getVelocity()==0){
                backBogie.setVelocity(0,0,0);
                frontBogie.setVelocity(0,0,0);
            } else if(getAccelerator()!=0){
                appendMovement(cachedVectors[2].xCoord);
            }
        }
        super.tick();
        if (!level().isClientSide) {
            runningSounds();
            headlights.tick(this);
        } else {
            train.render.TrainSmoke.tick(this);
        }
        if(hornDelay >0) {
            hornDelay--;
        }
    }

    private int soundPosition = 0;
    private train.library.EnumSounds soundRecord;
    private boolean soundLooked = false;

    /**the TC4 sound set of this locomotive class (1.12's fallback for transports without their own sounds)*/
    public train.library.EnumSounds getSoundRecord() {
        if (!soundLooked) {
            soundLooked = true;
            for (train.library.EnumSounds s : train.library.EnumSounds.values()) {
                if (s.getEntityClass() == getClass()) {
                    soundRecord = s;
                    break;
                }
            }
        }
        return soundRecord;
    }

    private void playSound(String name, float volume, float pitch) {
        if (name == null || name.isEmpty()) return;
        SoundEvent sound = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(train.library.Info.resourceLocation, name));
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
    }

    /**
     * TC 4.5 Locomotive: while the engine runs, the idle sound when stopped and the running sound when moving,
     * each played again once its length (ticks) is over; locomotives whose sound changes with speed repeat it
     * sooner the faster they go. Quiet while the horn blows. 1.12 TiM never played these.
     */
    private void runningSounds() {
        train.library.EnumSounds s = getSoundRecord();
        if (s == null || !getBoolean(boolValues.RUNNING) || hornDelay != 0) {
            return;
        }
        double speed = getVelocity();
        if (soundPosition == 0) {
            if (speed < 0.01D) {
                playSound(s.getIdleString(), s.getIdleVolume(), 0.001F);
                soundPosition = s.getIdleSoundLenght();
            } else if (s.getSoundChangeWithSpeed()) {
                if (speed < 0.06D) {
                    playSound(s.getRunString(), s.getRunVolume(), 0.4F - 0.3F);
                    soundPosition = s.getRunSoundLenght();
                } else if (speed < 0.2D) {
                    playSound(s.getRunString(), s.getRunVolume(), 0.4F - 0.1F);
                    soundPosition = s.getRunSoundLenght() / 2;
                } else {
                    playSound(s.getRunString(), s.getRunVolume(), 0.4F);
                    soundPosition = s.getRunSoundLenght() / 3;
                }
            } else {
                playSound(s.getRunString(), s.getRunVolume(), 0.4F);
                soundPosition = s.getRunSoundLenght();
            }
        }
        if (soundPosition > 0) {
            soundPosition--;
        }
    }

    @Override
    public void manageFuel(){
        if(getTypes().contains(STEAM)) {
            fuelHandler.manageSteam(this);
        }
        if(getTypes().contains(DIESEL)){
            ebf.tim.utility.FuelHandler.manageDieselFuel(this);
        }
        if(getTypes().contains(ELECTRIC)){
            ebf.tim.utility.FuelHandler.manageElectricFuel(this);
        }
    }

    /**the fuel type to display for the transport.*/
    @Override
    public String transportFuelType(){
            if(getTypes().contains(STEAM)){
                return "Steam";
            }
            if (getTypes().contains(DIESEL)){
                return "Diesel";
            }
            if(getTypes().contains(ELECTRIC)){
                return "Electric";
            }
            return "Magic";
    }

    private int hornDelay =0;
    public void soundHorn() {
        if(hornDelay!=0 || level().isClientSide){
            return;
        }
        if(getHorn()!=null){
            SoundEvent sound = SoundEvent.createVariableRangeEvent(getHorn());
            level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, getHornVolume(), getHornPitch());
            hornDelay = 65;
            return;
        }
        //fallback for the TC4 sounds
        train.library.EnumSounds s = getSoundRecord();
        if (s != null && !s.getHornString().isEmpty()) {
            playSound(s.getHornString(), s.getHornVolume(), 1.0F);
            hornDelay = 65;
        }
    }

    @Override
    public boolean interact(Player player, boolean isFront, boolean isBack, int key) {
        if (!super.interact(player, isFront, isBack, key)){
            switch (key){
                case 8:{ //toggle ignition
                    setBoolean(boolValues.RUNNING, !getBoolean(boolValues.RUNNING));
                    updateLinks();
                    return true;
                }case 17:{ //the bell (TC 4.5 Locomotive.soundWhistle, key B); it shares the horn's pause
                    if(hornDelay ==0 && !level().isClientSide){
                        playSound("bell", 0.5F, 1F);
                        hornDelay = 65;
                    }
                    return true;
                }case 9:{ //plays a sound on all clients within hearing distance
                    if(hornDelay ==0){
                        soundHorn();
                    }
                    return true;
                }case 2:{ //decrease speed
                    if (getBoolean(boolValues.RUNNING)) {
                        //if a linked transport is running, dont update
                        if(consistLeadID!=null && consistLeadID!=getId()){
                            return true;
                        }
                        if(accelerator<=-6){
                            accelerator=-6;
                        } else {
                            accelerator--;
                        }
                        updateLinks();
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    return true;
                }case 3:{ //increase speed
                    if (getBoolean(boolValues.RUNNING)) {
                        //if a linked transport is running, dont update
                        if(consistLeadID!=null && consistLeadID!=getId()){
                            return true;
                        }
                        if(accelerator>=6){
                            accelerator=6;
                        } else {
                            accelerator++;
                        }
                        updateLinks();
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    return true;
                } case 16:{//reset speed
                    if (getBoolean(boolValues.RUNNING)) {
                        //if a linked transport is running, dont update
                        if (consistLeadID != null && consistLeadID != getId()) {
                            return true;
                        }
                        accelerator = 0;
                        updateLinks();
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    return true;
                } case 11:{//TC control forward
                    if(getBoolean(boolValues.RUNNING)) {
                        accelerator = 7;
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    updateLinks();
                    return true;
                }case 12:{//TC control reverse
                    if(getBoolean(boolValues.RUNNING)) {
                        accelerator = -7;
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    updateLinks();
                    return true;
                }case 4: {//TC control, keep speed
                    if(getBoolean(boolValues.RUNNING)) {
                        accelerator = (int)Math.copySign(8,accelerator);
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    updateLinks();
                    return true;
                }case 14: {//TC control, keep speed
                    if(getBoolean(boolValues.RUNNING)) {
                        accelerator = 0;
                        entityData().set(ACCELERATOR, accelerator);
                    }
                    updateLinks();
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * <h2>Inherited variables</h2>
     * these functions are overridden by classes that extend this so that way the values can be changed indirectly.
     */
    /**gets the resource location for the horn sound*/
    public ResourceLocation getHorn(){return null;}
    public float getHornVolume(){return 1.0f;}
    public float getHornPitch(){return 1.0f;}
    /**gets the resource location for the running/chugging sound*/
    public ResourceLocation getRunningSound(){return null;}
    public float getRunningVolume(){return 1.0f;}
    public float getRunningPitch(){return 1.0f;}
    public ResourceLocation getBellSound(){return null;}
    public float getBellVolume(){return 1.0f;}
    public float getBellPitch(){return 1.0f;}
}
