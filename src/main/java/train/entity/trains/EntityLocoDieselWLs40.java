package train.entity.trains;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.registry.TiMItems;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoDieselWLs40 extends EntityTrainCore {

    public EntityLocoDieselWLs40(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco WLs40";}
    @Override
    public String transportcountry(){return "poland";}
    @Override
    public String transportYear(){return "1948-1993";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.DIESEL.singleton();
    }
    @Override
    public float weightKg(){return 10000f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelWLs40()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{1.4f, 0.05f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/wls40.png","wls40", "description.wls40"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"wls40";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(TiMItems.controlPanel.get(), 1), new ItemStack(TiMItems.wheelSteel.get(), 4), new ItemStack(TiMItems.frameSteel.get(), 2),
                null, null, new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.hydraulicTransmission.get(), 1), new ItemStack(TiMItems.mediumDieselEngine.get(), 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1.4f,1.4f, 0.3f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{4.1f,2.4f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.4f, -0.65f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "diesel";}
    @Override
    public float transportMetricHorsePower(){return 60;}
    @Override
    public float transportTopSpeed(){return 17;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{3000};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}