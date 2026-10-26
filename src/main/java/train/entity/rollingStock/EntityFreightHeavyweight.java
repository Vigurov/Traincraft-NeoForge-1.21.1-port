package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import ebf.tim.registry.TiMOres;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityFreightHeavyweight extends GenericRailTransport {

    public EntityFreightHeavyweight(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Heavyweight Mail Car";}
    @Override
    public String transportcountry(){return "us";}
    @Override//I think this is the rock island golden gate?
    public String transportYear(){return "1929-1969";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return getInventoryRowCount();}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.FREIGHT.singleton();
    }
    @Override//rough guess, heavyweight cars are designed to make use of extra mass for a smoother movement, average is around 67t
    public float weightKg(){return 55000f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelHeavyweight()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.0f, 0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/heavyweight_mailcar.png","heavyweight_mailcar", "description.heavyweight_mailcar"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"heavyweight_mailcar";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(TiMOres.ingotSteel.get(), 4), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameSteel.get(), 2),
                new ItemStack(TiMOres.ingotSteel.get(), 2), null, null, null, null, new ItemStack(Blocks.CHEST, 2)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{7.4f,2.5f,1.45f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.9f, -1.9f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}