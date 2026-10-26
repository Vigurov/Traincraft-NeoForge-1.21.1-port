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

public class EntityCaboose3 extends GenericRailTransport {

    public EntityCaboose3(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Caboose Black";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1863";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.WORKCAR.singleton();
    }
    @Override
    public float weightKg(){return 15739.655f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelCaboose3()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.63f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, -90.0f, -180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/caboose3.png","caboose3", "description.caboose3"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"caboose3";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameIron.get(), 2),
                new ItemStack(Items.IRON_INGOT, 2), null, new ItemStack(TiMItems.cabinWood.get(), 1),
                null, null, new ItemStack(TiMItems.seatsWooden.get(), 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?3f:2.75f,2.3f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.6f, -0.7f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}
