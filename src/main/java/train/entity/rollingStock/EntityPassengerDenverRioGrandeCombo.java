package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.ArrayList;
import java.util.List;

public class EntityPassengerDenverRioGrandeCombo extends GenericRailTransport {

    public EntityPassengerDenverRioGrandeCombo(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Passenger Denver Rio Grande Combo";}
    @Override//probably wrong, this is actually taken from:
    // https://www.historycolorado.org/sites/default/files/media/documents/2017/5ah3006.pdf
    public String transportcountry(){return "Us";}
    @Override
    public String transportYear(){return "1914-1960";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return getInventoryRowCount();}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        List<TrainsInMotion.transportTypes> l=new ArrayList<>();
        l.add(TrainsInMotion.transportTypes.PASSENGER);
        l.add(TrainsInMotion.transportTypes.FREIGHT);
        return l;
    }
    @Override//probably wrong, this is actually taken from:
    // https://www.historycolorado.org/sites/default/files/media/documents/2017/5ah3006.pdf
    public float weightKg(){return 58967f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelDRGCombo()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/drg_combo_Yellow.png","Yellow", "description.drg.combo.Yellow"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/drg_combo_Red.png","Red", "description.drg.combo.Red"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/drg_combo_Green.png","Green", "description.drg.combo.Green"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Yellow";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Blocks.CHEST, 4), new ItemStack(TiMItems.wheelIron.get(), 4), new ItemStack(TiMItems.frameWood.get(), 3),
                null, null, new ItemStack(TiMItems.cabinWood.get(), 3),
                null, null, new ItemStack(TiMItems.seatsWooden.get(), 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1.6f,1.5f, 0f},{0.2f,1.4f, 0f},{-1f,1.5f, 0f},{-2.2f,1.5f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{7.2f,2.35f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{2.15f, -2.05f};}



    //these only change in very specific use cases.
    @Override
    public float getMaxFuel(){return 1;}

}