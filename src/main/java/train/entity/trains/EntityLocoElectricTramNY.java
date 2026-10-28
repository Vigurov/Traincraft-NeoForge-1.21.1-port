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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoElectricTramNY extends EntityTrainCore {

    public EntityLocoElectricTramNY(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Electric Tram NY";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1986-2011";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.ELECTRIC.singleton();
    }
    @Override//NOTE: this is actually from the PA5, but the design and materials are very similar
    public float weightKg(){return 29029.9f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelTramNY()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.62f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/locoTramNY.png","locoTramNY", "description.locoTramNY"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"locoTramNY";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(TiMItems.controlPanel.get(), 1), new ItemStack(TiMItems.wheelSteel.get(), 2), new ItemStack(TiMItems.frameSteel.get(), 1),
                new ItemStack(TiMOres.ingotSteel.get(), 1), null, new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.transformer.get(), 2), new ItemStack(TiMItems.smallElectricEngine.get(), 2), new ItemStack(Items.REDSTONE, 4)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{-1.9f,1.2f, 0.45f},{-0.7f,1.2f, -0.3f},{0.9f,1.2f, 0.3f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{4.4f,2.3f,1.5f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.25f, -1.25f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "electric";}
    @Override
    public float transportMetricHorsePower(){return 1327;}
    @Override
    public float transportTopSpeed(){return 89;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    public int[] getTankCapacity(){return new int[]{2250};}


    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}