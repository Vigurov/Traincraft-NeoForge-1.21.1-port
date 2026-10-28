package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityCabooseLogging extends GenericRailTransport {

    public EntityCabooseLogging(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }




    //I cannot find anything about this one xx Chiel
    //main stats
    @Override
    public String transportName(){return "Caboose Logging";}
    @Override
    public String transportcountry(){return "Us";}
    @Override
    public String transportYear(){return "1836";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.WORKCAR.singleton();
    }
    @Override
    public float weightKg(){return 8754.3327f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelCabooseLogging()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.63f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/cabLogging_Red.png","Red", "description.cabLogging.Red"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/cabLogging_Cyan.png","Cyan", "description.cabLogging.Cyan"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Red";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                new ItemStack(Items.IRON_INGOT, 1), null, null, null, null, new ItemStack(Blocks.CRAFTING_TABLE, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?3.25f:2.0f,2.4f,1.45f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.25f, -0.325f};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}
