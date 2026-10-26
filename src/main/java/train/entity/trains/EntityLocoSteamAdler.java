package train.entity.trains;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.registry.TiMItems;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoSteamAdler extends EntityTrainCore {

    public EntityLocoSteamAdler(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam Adler";}
    @Override
    public String transportcountry(){return "germany";}
    @Override
    public String transportYear(){return "1835";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override
    public float weightKg(){return 11400f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelLocoSteamAdler()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.1f, level()==null?-1.2f:-0.9f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, -90.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/locoAdler.png","locoAdler", "description.locoAdler"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"locoAdler";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(TiMItems.wheelWood.get(), 3), new ItemStack(TiMItems.frameWood.get(), 2),
                new ItemStack(Items.IRON_INGOT, 2), new ItemStack(TiMItems.chimneyIron.get(), 1), null, new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxIron.get(), 1), new ItemStack(Items.GOLD_INGOT, 2)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1,1.4f, 0.2f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{2.5f,2.1f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.9f, -0.6f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 200;}
    @Override
    public float transportTopSpeed(){return 64.3f;}
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