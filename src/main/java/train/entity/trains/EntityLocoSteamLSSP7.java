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

public class EntityLocoSteamLSSP7 extends EntityTrainCore {

    public EntityLocoSteamLSSP7(EntityType<? extends GenericRailTransport> type, Level world) {
        super(type, world);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam LSSP7";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1924-1929";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override//weight unknown, rough guess based on the 40 ton two truck variant https://nelsonslocomotive.com/Heisler/GettingStarted/GettingStarted.htm
    public float weightKg(){return 30000f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelLSSP7()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-0.3f, level()==null?0.1f:0.36f, -0.825f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/LSSP7.png","LSSP7", "description.LSSP7"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"LSSP7";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                null, null, new ItemStack(TiMItems.cabinIron.get(), 1),
                new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxIron.get(), 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0.65f,1.25f, 0.3f}};}
    @Override
    public float[] getHitboxSize(){return level()==null?new float[]{2.8f,2.2f,1.3f}:new float[]{2.2f,2.2f,1.3f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.3f, -0.55f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 250;}
    @Override
    public float transportTopSpeed(){return 45;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{5000, 1000};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}