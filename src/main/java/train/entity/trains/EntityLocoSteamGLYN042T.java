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

public class EntityLocoSteamGLYN042T extends EntityTrainCore {

    public EntityLocoSteamGLYN042T(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam glyn";}
    @Override
    public String transportcountry(){return "wales";}
    @Override
    public String transportYear(){return "1892-1936";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override//NOTE: this weight is actually from Sir Theodore, which was the predecessor to glyn, but with a nearly identical design
    public float weightKg(){return 12700f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.Model042TGLYN()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{1.3f, 0f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/0-4-2-loco-glyn.png","0-4-2-loco-glyn", "description.0-4-2-loco-glyn"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"0-4-2-loco-glyn";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelIron.get(), 3), new ItemStack(TiMItems.frameIron.get(), 1),
                null, new ItemStack(TiMItems.chimneyIron.get(), 1), new ItemStack(TiMItems.cabinWood.get(), 1),
                new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxSteel.get(), 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1.2f,1.3f, 0.4f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{4.1f,2.6f,1.5f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.9f, -0.75f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 600;}
    @Override
    public float transportTopSpeed(){return 32;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{3750, 750};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}