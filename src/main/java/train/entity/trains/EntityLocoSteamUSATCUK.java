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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoSteamUSATCUK extends EntityTrainCore {

    public EntityLocoSteamUSATCUK(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam USATCUK";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1942–1944";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override
    public float weightKg(){return 45654f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelUSATCUK()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-0.95f, 0.375f, -0.8375f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/USATCUK.png","USATCUK", "description.USATCUK"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"USATCUK";
    }


    @Override
    public String getCraftingTable() {
        return "traintabletier2";
    }

    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelIron.get(), 3), new ItemStack(TiMItems.frameSteel.get(), 2), new ItemStack(Items.BLACK_DYE, 8),
                new ItemStack(TiMOres.ingotSteel.get(), 2), new ItemStack(TiMItems.chimneySteel.get(), 1), new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxIron.get(), 1), new ItemStack(Items.COAL, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1.1f,1.2f, -0.25f},{1.1f,1.2f, 0.25f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{3.9f,2.1f,1.3f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.7f, -0.8f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 197;}
    @Override
    public float transportTractiveEffort(){return 21630;}
    @Override
    public float transportTopSpeed(){return 75;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{4542, 600};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return true;
    }
    @Override
    public float getMaxFuel(){return 1;}

}