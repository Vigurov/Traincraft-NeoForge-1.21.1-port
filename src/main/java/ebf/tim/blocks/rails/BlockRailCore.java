package ebf.tim.blocks.rails;

import ebf.tim.blocks.RailTileEntity;
import ebf.tim.entities.EntityBogie;
import ebf.tim.items.ItemRail;
import ebf.tim.registry.TiMBlocks;
import ebf.tim.utility.CommonUtil;
import ebf.tim.utility.XmlBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * The TiM rail: its shape (1.12 metadata, kept in the tile entity) comes from the rails around it, and it is drawn
 * from its rail, tie and ballast materials (see {@link ebf.tim.render.models.Model1x1Rail}).
 * The vanilla rail logic (RailState) does not run for it: TiM works the shapes out itself.
 * @author Eternal Blue Flame
 */
public class BlockRailCore extends RailBlock implements EntityBlock {
    private static final int[] matrixXZ = {0,-1,1,-2,2}, matrixY = {0,-1,+1};
    private static final VoxelShape FLAT = Block.box(0, 0, 0, 16, 2, 16), SLOPE = Block.box(0, 0, 0, 16, 8, 16);

    //750mm single gauge track, each entry in the array is another gauge the rails would support
    public final int[] gaugemm;
    //scales the model parts
    public final float renderScale;

    public BlockRailCore(Properties properties){
        super(properties);
        gaugemm=new int[]{750};
        renderScale=1;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RailTileEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter world, BlockPos pos){return true;}

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return 0.4f;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int meta = getTileEntityMeta(world, pos.getX(), pos.getY(), pos.getZ());
        return meta >1 && meta <6 ? SLOPE : FLAT;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        //the ties underfoot, as low as the rail tops
        return Block.box(0, 0, 0, 16, 2, 16);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @Nullable AbstractMinecart cart) {
        int meta = getTileEntityMeta(world, pos.getX(), pos.getY(), pos.getZ());
        return RailShape.values()[Math.max(0, meta)];
    }

    /**
     * the shape for a bogie, as 1.12 metadata: 1.12 getRailDirection with the cart. The cart remembers the last
     * shape it used, which picks the way through crossings and parallel switches.
     */
    public int getRailMeta(Level world, BlockPos pos, @Nullable EntityBogie cart) {
        int x = pos.getX(), y=pos.getY(),z=pos.getZ();
        if(!(world.getBlockEntity(pos) instanceof RailTileEntity)){
            return 0;
        }
        int meta = ((RailTileEntity) world.getBlockEntity(pos)).getMeta();
        if (cart == null){
            return meta;
        }
        CompoundTag data = cart.entityData;
        //first be sure the key exists, and create it if it doesn't, that way we be sure we don't crash. Also if it doesn't exist we can just return the base meta unchanged.
        if (!data.contains("tim.lastusedrail.meta")){
            data.putInt("tim.lastusedrail.meta",meta);
            return meta;
        }
        int last = data.getInt("tim.lastusedrail.meta");
        boolean changed;
        switch (meta) {
            //add support for intersections
            case 0: {
                //only part that theoretically works.
                if (last == 1) {
                    meta = 1;
                }
                changed = true;
                break;
            }
            case 1: {
                if (last == 0) {
                    meta = 0;
                }
                changed = true;
                break;
            }
            case 6:{
                if(getTileEntityMeta(world,x+1,y,z)==9 && cart.motionZ>0 && last==0){
                    return 0;//this already worked fine, but make it smoother
                } else if(getTileEntityMeta(world,x+1,y,z)==7){
                    if(last==0) {
                        return 0;//cover parallel entering from wrong end on straight
                    } else if(last==7){
                        return 9;//cover parallel off shape
                    }
                } else if(getTileEntityMeta(world,x,y,z+1)==8 && cart.motionX>0 && last==1){
                    return 1;//this already worked fine, but make it smoother
                } else if (getTileEntityMeta(world,x,y,z+1)==9) {
                    if (last == 1) {
                        return 1;//cover parallel entering from wrong end on straight
                    } else if (last == 9) {
                        return 7;//cover parallel off shape
                    }
                }
                changed = true;
                break;
            }
            case 7:{
                if(getTileEntityMeta(world,x-1,y,z)==9 && cart.motionX>0 && last==0){
                    return 0;//this already worked fine, but make it smoother
                } else if(getTileEntityMeta(world,x-1,y,z)==8){
                    if(last==0) {
                        return 0;//cover parallel entering from wrong end on straight
                    } else if(last==9){
                        return 6;//cover parallel off shape
                    }
                } else if(getTileEntityMeta(world,x,y,z+1)==9 && cart.motionX<0 && last==1){
                    return 1;//this already worked fine, but make it smoother
                } else if (getTileEntityMeta(world,x,y,z+1)==8) {
                    if (last == 1) {
                        return 1;//cover parallel entering from wrong end on straight
                    } else if (last == 7) {
                        return 6;//cover parallel off shape
                    }
                } else if(getTileEntityMeta(world,x-1,y,z)==6) {
                    if (last == 0) {
                        return 0;//cover parallel entering from wrong end on straight
                    } else if (last == 6) {
                        return 8;//cover parallel off shape
                    }
                }
                changed = true;
                break;
            }
            case 8:{
                if(getTileEntityMeta(world,x-1,y,z)==6 && cart.motionZ<0 && last==0){
                    return 0;//this already worked fine, but make it smoother
                } else if(getTileEntityMeta(world,x-1,y,z)==9){
                    if(last==0) {
                        return 0;//cover parallel entering from wrong end on straight
                    } else if(last==9){
                        return 7;//cover parallel off shape
                    }
                } else if(getTileEntityMeta(world,x,y,z-1)==6 && cart.motionX<0 && last==1){
                    return 1;//this already worked fine, but make it smoother
                } else if (getTileEntityMeta(world,x,y,z-1)==7) {
                    if (last == 1) {
                        return 1;//cover parallel entering from wrong end on straight
                    } else if (last == 7) {
                        return 9;//cover parallel off shape
                    }
                }
                changed = true;
                break;
            }
            case 9:{
                if(getTileEntityMeta(world,x+1,y,z)==6 && cart.motionZ>0 && last==0){
                    return 0;//this already worked fine, but make it smoother
                } else if(getTileEntityMeta(world,x+1,y,z)==7){
                    if(last==0 && cart.motionZ<0) {
                        return 0;//cover parallel entering from wrong end on straight
                    } else if(last==8){
                        return 6;//cover parallel off shape
                    }
                } else if(getTileEntityMeta(world,x,y,z-1)==7 && cart.motionX>0 && last==1){
                    return 1;//this already worked fine, but make it smoother
                } else if (getTileEntityMeta(world,x,y,z-1)==6) {
                    if (last == 1) {
                        return 1;//cover parallel entering from wrong end on straight
                    } else if (last == 6) {
                        return 8;//cover parallel off shape
                    }
                } else if(getTileEntityMeta(world,x+1,y,z)==8 && getTileEntityMeta(world,x+1,y,z-1)==6) {
                    if (last == 0) {
                        return 0;//cover parallel entering from wrong end on straight
                    } else if (last == 9) {
                        return 6;//cover parallel off shape
                    }
                }
                changed = true;
                break;
            }
            default: {
                changed = false;
            }
        }
        //note changes in the entity so we can keep track of whether or not this is an intersection
        if (changed){
            data.putInt("tim.lastusedrail.meta",meta);
            data.putInt("tim.lastusedrail.x",x);
            data.putInt("tim.lastusedrail.z",z);
        }
        return meta;
    }

    public static int getTileEntityMeta(BlockGetter w, int x, int y, int z){
        if(w.getBlockEntity(new BlockPos(x,y,z)) instanceof RailTileEntity rail){
            return rail.getMeta();
        }
        return -1;
    }

    /*
     * <h2>placing, neighbours and breaking</h2>
     * no vanilla RailState: the shapes are TiM's
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            updateNearbyShapes(level, pos.getX(),pos.getY(),pos.getZ());
        }
    }

    @Override
    protected BlockState updateState(BlockState state, Level level, BlockPos pos, boolean movedByPiston) {
        return state;
    }

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block neighborBlock) {}

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if(!level.isClientSide && !(level.getBlockState(fromPos).getBlock() instanceof BaseRailBlock)) {
            updateNearbyShapes(level,pos.getX(),pos.getY(),pos.getZ());
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
            if (!level.isClientSide) {
                updateNearbyShapes(level, pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative() && level.getBlockEntity(pos) instanceof RailTileEntity rail
                && level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOBLOCKDROPS)) {
            rail.dropItem();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        if(world.getBlockEntity(pos) instanceof RailTileEntity rail) {
            XmlBuilder xml = rail.getData();
            return ItemRail.setStackData(
                    new ItemStack(TiMBlocks.railItem()), xml.getItemStack("rail"),
                    xml.getItemStack("ballast"), xml.getItemStack("ties"), xml.getItemStack("wires"));
        }
        return ItemStack.EMPTY;
    }

    public static void updateNearbyShapes(Level world, int xCoord, int yCoord, int zCoord){
        //update all the other nearby rails.
        BlockEntity te;
        BlockPos pos;
        for(int x : matrixXZ){
            for(int z : matrixXZ){
                for(int y : matrixY){
                    pos=new BlockPos(x+xCoord,y+yCoord,z+zCoord);
                    te=world.getBlockEntity(pos);
                    if(te instanceof RailTileEntity && ((RailTileEntity) te).getData()!=null){
                        new RailData(world,xCoord+x,yCoord+y,zCoord+z).rebuildRailMeta();
                        RailShapeCore.processPoints(x+xCoord,y+yCoord, z+zCoord,world,
                                getShape(world, x+xCoord,y+yCoord, z+zCoord),((RailTileEntity) te).getData());
                    }
                }
            }
        }
    }

    //override this to provide a different gauge
    public void updateShape(int xPos, int yPos, int zPos, Level worldObj, ItemStack rail, @Nullable ItemStack ties, @Nullable ItemStack ballast, @Nullable ItemStack wires){
        RailShapeCore.processPoints(xPos, yPos, zPos, getShape(worldObj, xPos, yPos, zPos),gaugemm, renderScale, worldObj, rail, ties, ballast, wires);
    }

    public static RailSimpleShape getShape(Level worldObj, int xPos, int yPos, int zPos){
        BlockEntity te= worldObj.getBlockEntity(new BlockPos(xPos,yPos,zPos));
        if(!(te instanceof RailTileEntity)){
            return null;
        }
        switch (((RailTileEntity)te).getMeta()){
            //Z straight
            case 0: {
                return RailVanillaShapes.vanillaZStraight(worldObj, xPos, yPos, zPos);
            }
            //X straight
            case 1: {
                return RailVanillaShapes.vanillaXStraight(worldObj, xPos, yPos, zPos);
            }
            //curves
            case 9: {
                return RailVanillaShapes.vanillaCurve9(worldObj, xPos, yPos, zPos);
            }
            case 8: {
                return RailVanillaShapes.vanillaCurve8(worldObj, xPos, yPos, zPos);
            }
            case 7: {
                return RailVanillaShapes.vanillaCurve7(worldObj, xPos, yPos, zPos);
            }
            case 6: {
                return RailVanillaShapes.vanillaCurve6(worldObj, xPos, yPos, zPos);
            }
            //Z slopes
            case 5 :{
                return RailVanillaShapes.vanillaSlopeZ5(worldObj, xPos, yPos, zPos);
            }
            case 4 :{
                return RailVanillaShapes.vanillaSlopeZ4(worldObj, xPos, yPos, zPos);
            }
            //X slopes
            case 2 :{
                return RailVanillaShapes.vanillaSlopeX2(worldObj, xPos, yPos, zPos);
            }
            case 3 :{
                return RailVanillaShapes.vanillaSlopeX3(worldObj, xPos, yPos, zPos);
            }
        }
        return null;
    }

    /**
     * checks an array of blocks for a specific class and meta to check if the shape should be made.
     * @return boolean for if the shape can be made
     */
    public static boolean checkBlockMeta(Level world, int xCoord, int yCoord, int zCoord, int ... meta){
        if (!world.hasChunk(xCoord>>4, zCoord>>4) ||
                !(CommonUtil.getBlockAt(world, xCoord,yCoord,zCoord) instanceof BaseRailBlock)){
            return false;
        }else {
            int block = getRailMeta(world, xCoord, yCoord, zCoord,null);
            for (int m : meta) {
                if (m==block){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Simplifies getting rail metadata for a block, returns -1 is the block is not a rail.
     */
    public static int getRailMeta(Level world, int x, int y, int z, @Nullable EntityBogie cart){
        if(!(CommonUtil.getBlockAt(world,x,y,z) instanceof BaseRailBlock)){
            return -1;
        }
        return CommonUtil.getRailMeta(world, cart,x,y,z);
    }

    public static int[] getNearbyMeta(Level world, int xCoord, int yCoord, int zCoord){
        int[] meta = new int[9];
        int i=0;
        for(int z=-1;z<2;z++){
            for(int x=-1;x<2;x++){
                meta[i]= getRailMeta(world, xCoord+x,yCoord,zCoord+z,null);
                i++;
            }
        }
        return meta;
    }
}
