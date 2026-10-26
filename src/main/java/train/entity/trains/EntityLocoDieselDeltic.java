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

public class EntityLocoDieselDeltic extends EntityTrainCore {

    public EntityLocoDieselDeltic(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Diesel Deltic";}
    @Override
    public String transportcountry(){return "uk";}
    @Override
    public String transportYear(){return "1961-1978";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.DIESEL.singleton();
    }
    @Override
    public float weightKg(){return 100589f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelDeltic()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, -0.6f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, -90.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/Deltic.png","Deltic", "description.Deltic"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Deltic";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(TiMItems.controlPanel.get(), 2), new ItemStack(TiMItems.wheelSteel.get(), 3), new ItemStack(TiMItems.frameSteel.get(), 2),
                new ItemStack(TiMOres.ingotSteel.get(), 2), new ItemStack(TiMItems.chimneySteel.get(), 1), new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.smallElectricEngine.get(), 4), new ItemStack(TiMItems.mediumDieselEngine.get(), 4), new ItemStack(TiMItems.generator.get(), 3)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{-2.4f,1.7f, 0f},{2.4f,1.7f, 0.2f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{7.5f,2.7f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{2.4f, -2.4f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "diesel";}
    @Override
    public float transportMetricHorsePower(){return 1997;}
    @Override
    public float transportTopSpeed(){return 161;}
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