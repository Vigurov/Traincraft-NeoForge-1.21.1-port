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

public class EntityLocoSteamSnowPlow extends EntityTrainCore {

    public EntityLocoSteamSnowPlow(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam Snow Plow";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1834-1884";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override//it's a small steam powered rotary engine in a small boxcar, i added the weight of the US boxcar and the adler, close enough
    public float weightKg(){return 122467f+11400f;}

    public String[] additionalItemText(){return new String[]{"Historically this was not self powered"};}
    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelSnowPlow()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.05f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        //more useful information may be found at https://www.midcontinent.org/equipment-roster/service-equipment/oregon-short-line-762/
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/train_snowplow.png","train_snowplow", "description.train_snowplow"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"train_snowplow";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameWood.get(), 4),
                null, new ItemStack(TiMItems.chimneyIron.get(), 1), new ItemStack(TiMItems.cabinWood.get(), 1),
                new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxIron.get(), 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{6.15f,2.6f,1.5f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.5f, -1.4f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";} //return "steam, snow plow"
    @Override
    public float transportMetricHorsePower(){return 200;}
    @Override
    public float transportTopSpeed(){return 20;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{6850};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}