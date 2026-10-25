package train.entity.trains;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import train.library.Info;

import java.util.List;

public class EntityLocoDieselChME3 extends EntityTrainCore {

    public EntityLocoDieselChME3(EntityType<? extends GenericRailTransport> type, Level world) {
        super(type, world);
    }

    //main stats
    @Override
    public String transportName(){return "Loco Diesel ChME3";}
    @Override
    public String transportcountry(){return "cezch";}
    @Override
    public String transportYear(){return "1963-1994";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.DIESEL.singleton();
    }
    @Override
    public float weightKg(){return 125990f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelChME3()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.62f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/chme3.png","chme3", "description.chme3"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"chme3";
    }

    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0.65f,1.42f, 0.35f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{3.7f,2.4f,1.5f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.9f, -0.9f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "diesel";}
    @Override
    public float transportMetricHorsePower(){return 2383;}
    @Override
    public float transportTopSpeed(){return 95;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{5000};}

    public String[] setParticles(){return new String[]{
            "smoke ,0,-0.15,1.55,0,0,0,0"
    };}

    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}
