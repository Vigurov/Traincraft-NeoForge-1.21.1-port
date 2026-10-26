package train.entity.rollingStock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMItems;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityPassengerBamboo extends GenericRailTransport {

    public EntityPassengerBamboo(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Bamboo Flatcar Passenger";}
    @Override
    public String transportcountry(){return "cambodia";}
    @Override
    public String transportYear(){return "1980s";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.PASSENGER.singleton();
    }
    @Override//the weight is a rough estimate based on size and the average weight of bamboo. 25kg engine
    public float weightKg(){return 67f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelBambooTrainPassenger()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.13f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Red.png","Red", "description.passenger.bamboo.Red"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Blue.png","Blue", "description.passenger.bamboo.Blue"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Black.png","Black", "description.passenger.bamboo.Black"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Yellow.png","Yellow", "description.passenger.bamboo.Yellow"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Magenta.png","Magenta", "description.passenger.bamboo.Magenta"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Cyan.png","Cyan", "description.passenger.bamboo.Cyan"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Pink.png","Pink", "description.passenger.bamboo.Pink"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_LightGrey.png","LightGrey", "description.passenger.bamboo.LightGrey"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Green.png","Green", "description.passenger.bamboo.Green"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_White.png","White", "description.passenger.bamboo.White"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_LightBlue.png","LightBlue", "description.passenger.bamboo.LightBlue"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Lime.png","Lime", "description.passenger.bamboo.Lime"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Brown.png","Brown", "description.passenger.bamboo.Brown"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Purple.png","Purple", "description.passenger.bamboo.Purple"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Orange.png","Orange", "description.passenger.bamboo.Orange"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/passenger_bamboo_Grey.png","Grey", "description.passenger.bamboo.Grey"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Red";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                null, new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 1),
                null, null, null, null, null, null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{-0.5f,0.9f, 0f},{0.5f,0.9f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{3.05f,0.8f,1.65f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.95f, -1.0f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return true;
    }
    @Override
    public float getMaxFuel(){return 1;}

}