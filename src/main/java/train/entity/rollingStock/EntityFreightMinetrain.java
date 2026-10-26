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

public class EntityFreightMinetrain extends GenericRailTransport {

    public EntityFreightMinetrain(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Freight Minecart Yellow";}
    @Override
    public String transportcountry(){return null;}
    @Override
    public String transportYear(){return null;}
    @Override
    public boolean isFictional(){return true;}
    @Override
    public int getInventoryRows(){return getInventoryRowCount();}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.FREIGHT.singleton();
    }
    @Override//the average minecart weighs anywhere from 500lb to 1500lb, considering this one is a bit larger im guessing around 1400?
    public float weightKg(){return 625f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelMinetrain()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, level()==null?0.2f:0.63f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/minetrain.png","minetrain", "description.minetrain"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"minetrain";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(Items.IRON_INGOT, 2), new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameIron.get(), 1),
                new ItemStack(Items.IRON_INGOT, 2), null, null, null, null, new ItemStack(Blocks.CHEST, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?1.9f:1.4f,1.25f,1.0f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.3f, -0.3f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}