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

public class EntityFreightCartSmall extends GenericRailTransport {

    public EntityFreightCartSmall(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Freight Cart Small";}
    @Override
    public String transportcountry(){return "Us";}
    @Override
    public String transportYear(){return "1890";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return getInventoryRowCount();}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.FREIGHT.singleton();
    }
    @Override
    public float weightKg(){return 20502.375f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelSmallFreightCart()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.38f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/freightCartSmall.png","freightCartSmall", "description.freightCartSmall"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"freightCartSmall";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                new ItemStack(Items.STICK, 2), null, null, null, null, new ItemStack(Blocks.CHEST, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{3.125f,2.1f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.9f, -0.75f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}