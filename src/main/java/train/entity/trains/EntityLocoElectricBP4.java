package train.entity.trains;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.models.Bogie;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;
import train.render.models.ModelBP4Bogie;

import java.util.List;

public class EntityLocoElectricBP4 extends EntityTrainCore {

    public EntityLocoElectricBP4(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Electric BP4";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "2007";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.ELECTRIC.singleton();
    }
    @Override
    public float weightKg(){return 11500f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelBP4()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0f, 0f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/bp4_Green.png","Green", "description.bp4.Green")
                    .setBogieTextures(fexcraft.tmt.slim.TextureManager.loc(Info.resourceLocation, "textures/trains/bp4bogie.png")));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/bp4_Purple.png","Purple", "description.bp4.Purple")
                    .setBogieTextures(fexcraft.tmt.slim.TextureManager.loc(Info.resourceLocation, "textures/trains/bp4bogie_purple.png")));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Green";
    }


    //recipe


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{-1.7f,1.35f, 0.25f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{5.6f,2.4f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.5f, -1.6f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "electric";}
    @Override
    public float transportMetricHorsePower(){return 75;}
    @Override
    public float transportTopSpeed(){return 105;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{8650};}

    @Override
    public Bogie[] bogies(){
        return new Bogie[]{
                new Bogie(new ModelBP4Bogie(), -1.6f,-0.15f,0),
                new Bogie(new ModelBP4Bogie(), 1.5f,-0.15f,0)
        };
    }


    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}