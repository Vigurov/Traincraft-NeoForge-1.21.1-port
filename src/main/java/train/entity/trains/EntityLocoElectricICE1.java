package train.entity.trains;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.registry.TiMItems;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoElectricICE1 extends EntityTrainCore {

    public EntityLocoElectricICE1(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Electric ICE 1";}
    @Override
    public String transportcountry(){return "germany";}
    @Override
    public String transportYear(){return "1989-2008";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.ELECTRIC.singleton();
    }
    @Override
    public float weightKg(){return 849415f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelICE1()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{3.2f, 0f, -0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/ICE1_Engine.png","ICE1_Engine", "description.ICE1_Engine"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"ICE1_Engine";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(TiMItems.controlPanel.get(), 2), new ItemStack(TiMItems.wheelSteel.get(), 2), new ItemStack(TiMItems.frameSteel.get(), 2),
                null, null, new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.transformer.get(), 4), new ItemStack(TiMItems.smallElectricEngine.get(), 4), new ItemStack(Items.REDSTONE, 4)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{-2.4f,1.1f, -0.2f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{7.8f,2.1f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{2.4f, -1.5f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "electric";}
    @Override
    public float transportMetricHorsePower(){return 6539;}
    @Override
    public float transportTopSpeed(){return 280;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    public int[] getTankCapacity(){return new int[]{2250};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return true;
    }
    @Override
    public float getMaxFuel(){return 1;}

}