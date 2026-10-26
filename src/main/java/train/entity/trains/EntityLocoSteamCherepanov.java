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

public class EntityLocoSteamCherepanov extends EntityTrainCore {

    public EntityLocoSteamCherepanov(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam Cherepanov";}
    @Override
    public String transportcountry(){return "russia";}
    @Override
    public String transportYear(){return "1833-1834";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override
    public float weightKg(){return 16380.7f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelLocoCherepanov()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-0.075f, level()==null?0.2f:0.6f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, -180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/locoCherepanov.png","locoCherepanov", "description.locoCherepanov"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"locoCherepanov";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 1),
                new ItemStack(Items.STICK, 1), new ItemStack(TiMItems.chimneyIron.get(), 1), null, new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxIron.get(), 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0.85f,1.35f, 0.1f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?2.8f:2.2f,2.7f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.1f, -0.6f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 60;}
    @Override
    public float transportTopSpeed(){return 30;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{3000, 600};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}