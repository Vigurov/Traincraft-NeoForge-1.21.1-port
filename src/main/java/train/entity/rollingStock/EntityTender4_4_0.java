package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityTender4_4_0 extends GenericRailTransport {

    public EntityTender4_4_0(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Tender 4-4-0";}
    @Override
    public String transportcountry(){return "us";}
    @Override
    public String transportYear(){return "1855";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return getInventoryRowCount();}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.TENDER.singleton();
    }
    @Override//NOTE: this is actually taken from the tender of the Sierra Railway 2-8-0 "Consolidation" #18, which has a very similar design
    public float weightKg(){return 27216f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelNormalSteamTender()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, level()==null?0.2f:0.63f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 90.0f, -180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_Black.png","Black", "description.4-4-0-loco.tender.Black"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_White.png","White", "description.4-4-0-loco.tender.White"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_Brown.png","Brown", "description.4-4-0-loco.tender.Brown"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_Blue.png","Blue", "description.4-4-0-loco.tender.Blue"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_Green.png","Green", "description.4-4-0-loco.tender.Green"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_Red.png","Red", "description.4-4-0-loco.tender.Red"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/4-4-0-loco_tender_Purple.png","Purple", "description.4-4-0-loco.tender.Purple"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Black";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameWood.get(), 1),
                new ItemStack(Items.STICK, 2), null, null, null, null, new ItemStack(Items.COAL, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?2.8f:2.625f,1.6f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.6f, -0.575f};}
    @Override
    public int[] getTankCapacity(){return new int[]{getTankVolume()};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}