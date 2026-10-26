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

public class EntityFreightWood2 extends GenericRailTransport {

    public EntityFreightWood2(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Freight Flat Logs";}
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
    @Override//Most flatcars weigh anywhere between 10 and 30 tons.
    public float weightKg(){return 15000f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelFreightWood()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, level()==null?0.3f:0.63f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, -90.0f, -180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/flatCartWood2.png","flatCartWood2", "description.flatCartWood2"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"flatCartWood2";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameIron.get(), 1),
                new ItemStack(Items.IRON_INGOT, 2), null, null, null, null, new ItemStack(Blocks.OAK_LOG, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{2.725f,1.4f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.675f, -0.75f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}