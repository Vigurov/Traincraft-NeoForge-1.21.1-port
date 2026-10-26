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

public class EntityPassengerAdler extends GenericRailTransport {

    public EntityPassengerAdler(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Passenger Adler";}
    @Override
    public String transportcountry(){return "germany";}
    @Override
    public String transportYear(){return "1835";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.PASSENGER.singleton();
    }
    @Override//this is kinda sketchy, it's documented:
    // During a trial run the adler hauled one railway wagon with a payload of 8,000 German pounds
    //but no weight of the third or second class passenger cars was documented, so we're assuming the payload is a similar weight.
    public float weightKg(){return 4480.7f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelPassengerAdler()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.05f, level()==null?-1.2f:-0.85f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{180.0f, -90.0f, -180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passengerAdler.png","passengerAdler", "description.passengerAdler"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"passengerAdler";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Items.IRON_INGOT, 1), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                new ItemStack(Items.IRON_INGOT, 1), null, new ItemStack(TiMItems.cabinWood.get(), 1),
                null, null, new ItemStack(TiMItems.seatsWooden.get(), 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0.1f,0.9f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{level()==null?2.6f:2.2f,1.4f,1.0f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.375f, -0.325f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return true;
    }
    @Override
    public float getMaxFuel(){return 1;}

}