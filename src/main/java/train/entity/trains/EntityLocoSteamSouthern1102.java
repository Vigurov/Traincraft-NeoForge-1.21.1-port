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

public class EntityLocoSteamSouthern1102 extends EntityTrainCore {

    public EntityLocoSteamSouthern1102(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam Southern 1102";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1903-1935";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override
    public float weightKg(){return 124450f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelSouthern1102()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-1.5f, 0.7f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/Southern1102.png","Southern1102", "description.Southern1102"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Southern1102";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelIron.get(), 3), new ItemStack(TiMItems.frameIron.get(), 2),
                new ItemStack(Items.IRON_INGOT, 2), new ItemStack(TiMItems.chimneyIron.get(), 1), new ItemStack(TiMItems.cabinIron.get(), 1),
                new ItemStack(TiMItems.boilerIron.get(), 2), new ItemStack(TiMItems.fireboxIron.get(), 1), new ItemStack(Items.COAL, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{2.1f,1.5f, -0.3f},{2.1f,1.5f, 0.3f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{5f,2.4f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.3f, -2f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 1236;}
    @Override
    public float transportTopSpeed(){return 118;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{10000, 2000};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return true;
    }
    @Override
    public float getMaxFuel(){return 1;}

}