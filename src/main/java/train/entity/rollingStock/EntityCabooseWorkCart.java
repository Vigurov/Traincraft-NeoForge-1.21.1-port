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

public class EntityCabooseWorkCart extends GenericRailTransport {

    public EntityCabooseWorkCart(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //I cannot find anything about this one xx Chiel
    //main stats
    @Override
    public String transportName(){return "Work Caboose";}
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
    public float weightKg(){return 18597.287f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelWorkCaboose()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.57f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/workcaboose.png","workcaboose", "description.workcaboose"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"workcaboose";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameSteel.get(), 2),
                new ItemStack(TiMOres.ingotSteel.get(), 2), null, null, null, null, new ItemStack(Blocks.CRAFTING_TABLE, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?3.25f:2.85f,2.7f,1.3f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.76f, -0.85f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}
