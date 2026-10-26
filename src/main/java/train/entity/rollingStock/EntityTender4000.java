package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import ebf.tim.registry.TiMOres;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityTender4000 extends GenericRailTransport {

    public EntityTender4000(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "4000GallonTender";}
    @Override
    public String transportcountry(){return "uk";}
    @Override
    public String transportYear(){return "1926-1944";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return getInventoryRowCount();}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.TENDER.singleton();
    }
    @Override
    public float weightKg(){return 47445.7f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.Model4000GallonTender()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{-4.1f, 0.005f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/GWR_Tender_Green.png","Green", "description.GWR.Tender.Green"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/GWR_Tender_Blue.png","Blue", "description.GWR.Tender.Blue"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/GWR_Tender_Red.png","Red", "description.GWR.Tender.Red"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/GWR_Tender_Lime.png","Lime", "description.GWR.Tender.Lime"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Green";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(TiMOres.ingotSteel.get(), 6), new ItemStack(TiMItems.wheelWood.get(), 3), new ItemStack(TiMItems.frameSteel.get(), 3),
                new ItemStack(TiMOres.ingotSteel.get(), 2), null, null, null, null, new ItemStack(Items.COAL, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{3.925f,2.0f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.0f, -1.25f};}
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