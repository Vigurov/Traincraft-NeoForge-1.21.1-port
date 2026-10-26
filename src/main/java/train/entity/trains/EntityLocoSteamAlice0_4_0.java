package train.entity.trains;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.registry.TiMItems;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import train.library.Info;

import java.util.List;

public class EntityLocoSteamAlice0_4_0 extends EntityTrainCore {

    public EntityLocoSteamAlice0_4_0(EntityType<? extends GenericRailTransport> type, Level worldObj) {
        super(type, worldObj);
    }





    //main stats
    @Override
    public String transportName(){return "Loco Steam Alice";}
    @Override
    public String transportcountry(){return "wales";}
    @Override
    public String transportYear(){return "1902-1967";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override
    public float weightKg(){return 6096f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelLocoAlice040()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{1.2f, 0f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 0.0f, 0.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/0-4-0-loco-alice.png","0-4-0-loco-alice", "description.0-4-0-loco-alice"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"0-4-0-loco-alice";
    }


    //recipe
    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[]{
                new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(TiMItems.wheelIron.get(), 2), new ItemStack(TiMItems.frameWood.get(), 1),
                null, new ItemStack(TiMItems.chimneyIron.get(), 1), null, new ItemStack(TiMItems.boilerIron.get(), 1), new ItemStack(TiMItems.fireboxSteel.get(), 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1.3f,1.35f, -0.3f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{3.8f,2.4f,1.1f};}
    @Override
    public float[] rotationPoints() {return new float[]{0.3f, -0.6f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 200;}
    @Override
    public float transportTopSpeed(){return 32;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot();
    }
    @Override
    public int[] getTankCapacity(){return new int[]{3750, 750};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public float getMaxFuel(){return 1;}

}