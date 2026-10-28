package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import train.library.Info;

import java.util.List;

/** A luxury sleeping car of the early 1900s, made for this port (ModelPassengerLuxSleeper) */
public class EntityPassengerLuxSleeper extends GenericRailTransport {

    public EntityPassengerLuxSleeper(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }

    @Override
    public String transportName(){return "Luxury Sleeping Car";}
    @Override
    public String transportcountry(){return "Europe";}
    @Override
    public String transportYear(){return "1905-1925";}
    @Override
    public boolean isFictional(){return true;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.PASSENGER.singleton();
    }
    @Override
    public float weightKg(){return 52000f;}

    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelPassengerLuxSleeper()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.63f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, -180.0f, -180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_lux_sleeper.png","Navy", "description.passenger.luxsleeper.navy"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Navy";
    }

    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Blocks.DARK_OAK_PLANKS, 6), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                new ItemStack(Items.GOLD_INGOT, 2), new ItemStack(Blocks.RED_WOOL, 2), new ItemStack(TiMItems.cabinWood.get(), 1),
                null, null, new ItemStack(TiMItems.seatsWooden.get(), 2)};
    }

    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{6.6f,2.3f,1.5f};}
    @Override
    public float[] rotationPoints() {return new float[]{2.125f, -2.125f};}

    @Override
    public float getMaxFuel(){return 1;}
}
