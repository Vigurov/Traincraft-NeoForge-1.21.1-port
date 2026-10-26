package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import ebf.tim.registry.TiMOres;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityBUnitDD35 extends GenericRailTransport {

    public EntityBUnitDD35(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "DD35 B Unit";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1963-1981";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.B_UNIT.singleton();
    }
    @Override
    public float weightKg(){return 236766.145f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelDD35B()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.005f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/dd35b_Orange.png","Orange", "description.dd35b.Orange"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/dd35b_Black.png","Black", "description.dd35b.Black"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Orange";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelWood.get(), 8), new ItemStack(TiMItems.frameSteel.get(), 3),
                new ItemStack(TiMOres.ingotSteel.get(), 2), new ItemStack(TiMItems.chimneySteel.get(), 2), new ItemStack(TiMItems.cabinSteel.get(), 1),
                new ItemStack(TiMItems.smallElectricEngine.get(), 8), new ItemStack(TiMItems.mediumDieselEngine.get(), 11), new ItemStack(TiMItems.generator.get(), 4)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{null};}
    @Override
    public float[] getHitboxSize(){return new float[]{7f,2.37f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.925f, -1.925f};}
    @Override
    public int[] getTankCapacity(){return new int[]{getTankVolume()};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}
