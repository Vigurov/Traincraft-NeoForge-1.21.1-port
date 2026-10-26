package train.entity.trains;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.registry.TiMItems;
import ebf.tim.registry.TiMOres;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoDieselClass66 extends EntityTrainCore {

    public EntityLocoDieselClass66(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Diesel EWS Class 66";}
    @Override
    public String transportcountry(){return "uk";}
    @Override
    public String transportYear(){return "1998-2015";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.DIESEL.singleton();
    }
    @Override
    public float weightKg(){return 129647.5f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelEWSClass66()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.2f, -0.45f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, -90.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/Class66_Pink.png","Pink", "description.Class66.Pink"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/Class66_Green.png","Green", "description.Class66.Green"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/Class66_Red.png","Red", "description.Class66.Red"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Pink";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(TiMItems.controlPanel.get(), 2), new ItemStack(TiMItems.wheelSteel.get(), 3), new ItemStack(TiMItems.frameSteel.get(), 2),
                new ItemStack(TiMOres.ingotSteel.get(), 2), new ItemStack(TiMItems.chimneySteel.get(), 1), new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.smallElectricEngine.get(), 4), new ItemStack(TiMItems.mediumDieselEngine.get(), 4), new ItemStack(TiMItems.generator.get(), 2)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{-3,1.25f, 0.2f},{2.9f,1.25f, -0.15f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{7.4f,2.2f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{2.3f, -2.4f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "diesel";}
    @Override
    public float transportMetricHorsePower(){return 3678;}
    @Override
    public float transportTopSpeed(){return 121;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{6400};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return true;
    }
    @Override
    public float getMaxFuel(){return 1;}

}