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

public class EntityTankLava extends GenericRailTransport {

    public EntityTankLava(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Tank Lava";}
    @Override
    public String transportcountry(){return "Uk";}
    @Override
    public String transportYear(){return "1958";}
    @Override
    public boolean isFictional(){return true;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.TANKER.singleton();
    }
    @Override//tanker cars usually weigh anywhere around 40000lb to 180000lb.
    // given the limited capacity, but extra reinforcement, 70000-ish 'seems legit'.
    public float weightKg(){return 32000f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelWatertransp()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.63f, 0.0f}};}
@Override
    public float[][] modelRotations(){return new float[][]{{0f,180f,180f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/lavacar_Empty.png","Empty", "description.lavacar.Empty"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/lavacar_Full.png","Full", "description.lavacar.Full"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Empty";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Items.IRON_INGOT, 6), new ItemStack(TiMItems.wheelWood.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                new ItemStack(Items.STICK, 2), null, null, null, null, new ItemStack(Items.LAVA_BUCKET, 1)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{3.725f,2.1f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{1.05f, -1.075f};}
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