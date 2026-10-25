package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class PassengerIC4_DSB_FH extends GenericRailTransport {

    public PassengerIC4_DSB_FH(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Passenger IC4 DSB FH";}
    @Override
    public String transportcountry(){return "Denmark";}
    @Override
    public String transportYear(){return "2005-2013";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.PASSENGER.singleton();
    }
    @Override
    public float weightKg(){return 35000f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelLocoDieselIC4_DSB_FH()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-3.45f, -0.87f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,90f,0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/IC4_DSB_FH.png","IC4_DSB_FH", "description.IC4_DSB_FH"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"IC4_DSB_FH";
    }


    //recipe


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{7.5f,2.1f,1.525f};}
    @Override
    public float[] rotationPoints() {return new float[]{3.525f, -3.575f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}