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

public class EntityPropagandaUSSR extends GenericRailTransport {

    public EntityPropagandaUSSR(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Propaganda USSR";}
    @Override
    public String transportcountry(){return "Su";}
    @Override
    public String transportYear(){return null;}
    @Override
    public boolean isFictional(){return true;}
    @Override
    public int getInventoryRows(){return 0;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.OTHER.singleton();
    }
    @Override//Most flatcars weigh anywhere between 10 and 30 tons.
    public float weightKg(){return 15000f;}
    @Override
    public String[] additionalItemText(){return new String[]{"We can learn from history, but we can also deceive ourselves","when we selectively take evidence from the past to justify what we have already made up our minds to do.", "- Margaret MacMillan"};}


    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelPropagandaCar()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{1.1f, 0.0025f, 0.075f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/propaganda_USSR_Blue.png","Blue", "description.propaganda.USSR.Blue"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/propaganda_USSR_White.png","White", "description.propaganda.USSR.White"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/propaganda_USSR_Red.png","Red", "description.propaganda.USSR.Red"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Blue";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameWood.get(), 2),
                null, null, null, null, null, new ItemStack(Items.PAINTING, 2)        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return null;}
    @Override
    public float[] getHitboxSize(){return new float[]{7.6f,2.95f,1.4f};}
    @Override
    public float[] rotationPoints() {return new float[]{2.7f, -2.625f};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}