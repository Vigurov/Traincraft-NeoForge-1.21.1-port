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

public class EntityLocoElectricMinetrain extends EntityTrainCore {

    public EntityLocoElectricMinetrain(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Electric Minetrain";}
    @Override
    public String transportcountry(){return null;}
    @Override
    public String transportYear(){return null;}
    @Override
    public boolean isFictional(){return true;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.ELECTRIC.singleton();
    }
    @Override//the average minecart weighs anywhere from 500lb to 1500lb, considering this one hauls them im guessing probably double max?
    public float weightKg(){return 1360f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelLocoMinetrain()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-0.2f, level()==null?0.4f:0.62f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/locoMinetrain.png","locoMinetrain", "description.locoMinetrain"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"locoMinetrain";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Items.IRON_INGOT, 2), new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameIron.get(), 1),
                new ItemStack(Items.IRON_INGOT, 1), null, new ItemStack(TiMItems.controlPanel.get(), 1),
                new ItemStack(TiMItems.transformer.get(), 1), new ItemStack(TiMItems.smallElectricEngine.get(), 2), new ItemStack(Items.REDSTONE, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0.4f,1.225f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?2.3f:1.9f,1.6f,0.8f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.05f, -0.5f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "electric";}
    @Override
    public float transportMetricHorsePower(){return 500;}
    @Override
    public float transportTopSpeed(){return 40;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    public int[] getTankCapacity(){return new int[]{2250};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}