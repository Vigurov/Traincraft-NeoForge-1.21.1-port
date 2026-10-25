package ebf.tim.utility;

import ebf.tim.entities.EntityBogie;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.Vec3d;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <h1>utilities</h1>
 * used for misc utility functions
 *
 * @author Eternal Blue Flame
 * @author Zora No Densha
 */
public class CommonUtil {

    /**converts a degrees float to radians*/
    public static final float radianF = (float) Math.PI / 180.0f;
    /**converts a radians double to degrees*/
    public static final double degreesD = 180.0d / Math.PI;
    public static final float degreesF = (float) (180.0d / Math.PI);

    /**redirect shorthand that typecasts doubles to ints
     * @see #isRailBlockAt(Level, int, int, int) */
    public static boolean isRailBlockAt(Level world, double x, double y, double z) {
        return isRailBlockAt(world,floorDouble(x), floorDouble(y),floorDouble(z));
    }

    public static Block getBlockAt(Level world, double x, double y, double z){
        return world.getBlockState(new BlockPos(floorDouble(x), floorDouble(y),floorDouble(z))).getBlock();
    }

    public static Block getBlockAt(Level world, int x, int y, int z){
        return world.getBlockState(new BlockPos(x,y,z)).getBlock();
    }

    public static boolean isAir(Level world, int x, int y, int z){
        return world.getBlockState(new BlockPos(x,y,z)).isAir();
    }

    /**the rail shape as a 1.12 metadata value (RailShape keeps the same order), 0 when it isn't a rail*/
    public static int getRailMeta(Level w, EntityBogie cart, int x, int y, int z){
        BlockPos pos = new BlockPos(x,y,z);
        BlockState state = w.getBlockState(pos);
        if(state.getBlock() instanceof ebf.tim.blocks.rails.BlockRailCore){
            return ((ebf.tim.blocks.rails.BlockRailCore) state.getBlock()).getRailMeta(w, pos, cart);
        }
        if(state.getBlock() instanceof BaseRailBlock){
            return ((BaseRailBlock)state.getBlock()).getRailDirection(state, w, pos, null).ordinal();
        }
        return 0;
    }

    public static float getMaxRailSpeed(Level world, BaseRailBlock rail, GenericRailTransport host, double x, double y, double z){
        BlockPos pos = new BlockPos(floorDouble(x), floorDouble(y),floorDouble(z));
        //the rail asks the minecart (vanilla: is it in water); there is none, so rails that need one get the 1.12 default
        try {
            return rail.getRailMaxSpeed(world.getBlockState(pos), world, pos, null);
        } catch (NullPointerException e) {
            return 0.4f;
        }
    }

    public static int floorDouble(double value){
        return value < (int)value ? ((int)value) - 1 : (int)value;
    }

    /**
     * <h2>Vanilla Track  detection Overrrides</h2>
     * a modified version of vanilla track detection so that way it's more efficient and can support rails from other mods.
     */
    public static boolean isRailBlockAt(Level world, int x, int y, int z) {
        Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        //classic Traincraft 4 track pieces count as rails too
        return b instanceof BaseRailBlock || isClassicTrack(b);
    }

    /** the axis of a classic track piece like a TiM rail meta: 0 = along Z, 1 = along X (its facing, from the piece's start) */
    public static int classicTrackAxis(Level world, int x, int y, int z) {
        net.minecraft.world.level.block.entity.BlockEntity te = world.getBlockEntity(new BlockPos(x, y, z));
        if (te instanceof train.common.tile.TileTCRailGag g && g.originX.size() > 0) {
            te = world.getBlockEntity(new BlockPos(g.originX.get(0), g.originY.get(0), g.originZ.get(0)));
        }
        return te instanceof train.common.tile.TileTCRail rail ? (rail.getBlockMeta() & 1) : 0;
    }

    /** a classic Traincraft 4 track piece (the track block itself or one of its filler blocks) */
    public static boolean isClassicTrack(Block b) {
        return b instanceof train.common.blocks.BlockTCRail || b instanceof train.common.blocks.BlockTCRailGag;
    }

    // ---- helpers the classic (TC 4) track code uses, 1.7.10 style (x, y, z and metadata)
    public static net.minecraft.world.phys.AABB createAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){
        return new net.minecraft.world.phys.AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static net.minecraft.world.level.block.entity.BlockEntity getTileEntity(net.minecraft.world.level.BlockGetter world, double x, double y, double z){
        return getTileEntity(world, floorDouble(x), floorDouble(y), floorDouble(z));
    }

    public static net.minecraft.world.level.block.entity.BlockEntity getTileEntity(net.minecraft.world.level.BlockGetter world, int x, int y, int z){
        return world.getBlockEntity(new BlockPos(x, y, z));
    }

    /** the classic tracks a block belongs to: the track itself, or every track a gag block points at */
    public static java.util.List<net.minecraft.world.level.block.entity.BlockEntity> getTiles(Level w, int x, int y, int z){
        java.util.List<net.minecraft.world.level.block.entity.BlockEntity> tiles = new java.util.ArrayList<>();
        net.minecraft.world.level.block.entity.BlockEntity t = w.getBlockEntity(new BlockPos(x, y, z));
        if (t instanceof train.common.tile.TileTCRailGag gag) {
            for (int i = 0; i < gag.originX.size(); i++) {
                tiles.add(w.getBlockEntity(new BlockPos(gag.originX.get(i), gag.originY.get(i), gag.originZ.get(i))));
            }
        } else if (t != null) {
            tiles.add(t);
        }
        return tiles;
    }

    /** the 1.7.10 metadata of a block: the "meta" property of the classic track blocks */
    private static BlockState withMeta(BlockState state, int meta) {
        for (var p : state.getProperties()) {
            if (p.getName().equals("meta") && p instanceof net.minecraft.world.level.block.state.properties.IntegerProperty ip) {
                return state.setValue(ip, Math.max(0, Math.min(15, meta)));
            }
        }
        return state;
    }

    public static boolean setBlock(Level w, int x, int y, int z, Block b, int meta, int flags){
        return w.setBlock(new BlockPos(x, y, z), withMeta(b.defaultBlockState(), meta), flags);
    }

    public static boolean setBlock(Level w, int x, int y, int z, Block b, int meta){
        return setBlock(w, x, y, z, b, meta, 3);
    }

    public static boolean setBlock(Level w, int x, int y, int z, Block b){
        return setBlock(w, x, y, z, b, 0, 3);
    }

    public static boolean setBlockMeta(Level w, int x, int y, int z, int meta, int flags){
        BlockPos pos = new BlockPos(x, y, z);
        return w.setBlock(pos, withMeta(w.getBlockState(pos), meta), flags);
    }

    public static boolean setBlockMeta(Level w, int x, int y, int z, int meta){
        return setBlockMeta(w, x, y, z, meta, 3);
    }

    /** 1.7.10 world.markBlockForUpdate: send the block (and its tile) to the clients again */
    public static void markBlockForUpdate(Level w, int x, int y, int z){
        if (w == null) return;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = w.getBlockState(pos);
        w.sendBlockUpdated(pos, state, state, 3);
    }

    /** the facing a block was placed with: the metadata of the classic track blocks, else a horizontal facing */
    public static int getBlockFacing(net.minecraft.world.level.BlockGetter w, int x, int y, int z){
        if (w == null) return 0;
        BlockState state = w.getBlockState(new BlockPos(x,y,z));
        for (var p : state.getProperties()) {
            if (p.getName().equals("meta") && p instanceof net.minecraft.world.level.block.state.properties.IntegerProperty ip) {
                return state.getValue(ip);
            }
        }
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING).get3DDataValue();
        }
        return 0;
    }

    public static String translate(String text){
        return net.minecraft.locale.Language.getInstance().getOrDefault(text);
    }

    public static boolean stringContains(String s1, String... s2){
        for(String s3 : s2) {
            if (stringContains(s1,s3)){
                return true;
            }
        }
        return false;
    }

    public static boolean stringContains(String s1, String s2){
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.contains(s2);
    }

    public static float[] rotatePointF(float x, float y, float z, float pitch, float yaw, float roll) {
        float[] xyz = new float[]{x,y,z};
        float sin, cos;
        //rotate pitch
        if (pitch != 0.0F) {
            pitch *= radianF;
            cos = Mth.cos(pitch);
            sin = Mth.sin(pitch);

            xyz[0] = (y * sin) + (x * cos);
            xyz[1] = (y * cos) - (x * sin);
        }
        //rotate yaw
        if (yaw != 0.0F) {
            yaw *= radianF;
            cos = Mth.cos(yaw);
            sin = Mth.sin(yaw);

            xyz[0] = (x * cos) - (z * sin);
            xyz[2] = (x * sin) + (z * cos);
        }
        //rotate roll
        if (roll != 0.0F) {
            roll *=  radianF;
            cos = Mth.cos(roll);
            sin = Mth.sin(roll);

            xyz[1] = (z * cos) - (y * sin);
            xyz[2] = (z * sin) + (y * cos);
        }

        return xyz;
    }

    public static float calculatePitch(double yBack, double yFront, double distance){
            double yDiff = yFront - yBack;
            return (float) ((yDiff / distance) * -CommonUtil.degreesD);
    }

    /**
     * replacement for system atan2 function.
     * uses a lookup list of 1024 float entries (4kb roughly).
     * performance is measured at over 15 times more efficient.
     * @return angle in radians
     */
    public static float atan2f(double x, double z) {
        if (z < 0.0d) {
            if (x < 0.0d) {
                //0.03225806451 is (1.0D / (ATAN2_SQRT - 1))
                double invDiv = 1.0D / (((-z < -x) ? -x : -z) * 0.03225806451);
                return (atan2[(int)(-x * invDiv) * ATAN2_SQRT + (int)(-z * invDiv)] - 3.141592653f);
            } else {
                double invDiv = 1.0D / (((-z < x) ? x : -z) * 0.03225806451);
                return (atan2[(int)(x * invDiv) * ATAN2_SQRT + (int)(-z * invDiv)] - 3.141592653f)*-1;
            }
        } else {
            if (x < 0.0d) {
                double invDiv = 1.0D / (((z < -x) ? -x : z) * 0.03225806451);
                return (atan2[(int)(-x * invDiv) * ATAN2_SQRT + (int)(z * invDiv)]) * -1;
            }
            double invDiv = 1.0D / (((z < x) ? x : z) * 0.03225806451);
            return (atan2[(int)(x * invDiv) * ATAN2_SQRT + (int)(z * invDiv)]);
        }
    }

    public static float atan2degreesf(double x, double y){
        return atan2f(x,y)*57.295779514f;
    }

    private static final int ATAN2_SQRT = (int) Math.sqrt(1024);//32
    private static final float[] atan2 = new float[1024];

    static {
        for (int i = 0; i < ATAN2_SQRT; i++) {
            for (int j = 0; j < ATAN2_SQRT; j++) {
                atan2[j * ATAN2_SQRT + i] = (float) Math.atan2((float) j / ATAN2_SQRT, (float) i / ATAN2_SQRT);
            }
        }
    }

    public static double[] rotatePoint(double f, double pitch, double yaw) {
        double cos;
        double sin;
        double[] xyz = new double[]{f,0,0};
        //rotate pitch
        if (pitch != 0.0F) {
            pitch *= Math.PI / 180.0f;
            cos = Math.cos(pitch);
            sin = Math.sin(pitch);
            xyz[0] = (f * cos);
            xyz[1] = -(f * sin);
        }
        //rotate yaw
        if (yaw != 0.0F) {
            yaw *= Math.PI / 180.0f;
            cos = Math.cos(yaw);
            sin = Math.sin(yaw);
            xyz[0] = (f * cos);
            xyz[2] = (f * sin);
        }
        return xyz;
    }

    public static Vec3f rotatePoint(Vec3f f, float pitch, float yaw, float roll) {
        return new Vec3f(f.xCoord, f.yCoord, f.zCoord).rotatePoint(pitch, yaw, roll);
    }

    public static Vec3d rotateDistance(double distance, float pitch, float yaw) {
        Vec3d xyz = new Vec3d(distance, 0,0);
        //rotate pitch
        if (pitch != 0.0F) {
            pitch *= radianF;
            xyz.xCoord = distance * Math.cos(pitch);
            xyz.yCoord = distance * Math.sin(pitch);
        }
        //rotate yaw
        if (yaw != 0.0F) {
            yaw *= radianF;
            xyz.xCoord = (distance * Mth.cos(yaw));
            xyz.zCoord = (distance * Mth.sin(yaw));
        }
        return xyz;
    }

    public static float wrapAngleTo180(float value) {
        return Mth.wrapDegrees(value);
    }

    public static void sendChat(Player p, String s){
        p.sendSystemMessage(Component.literal(s));
    }

    /**
     * <h2>place on rail</h2>
     * spawns the transport on the rail at the given position, if the straight rail there is long enough.
     */
    public static boolean placeOnRail(GenericRailTransport entity, Player playerEntity, ItemStack stack, Level worldObj, int posX, int posY, int posZ) {

        //be sure there is a rail at the location
        if (isRailBlockAt(worldObj, posX,posY,posZ) && !worldObj.isClientSide) {
            //define the direction of the track
            int railMeta=isClassicTrack(getBlockAt(worldObj, posX, posY, posZ)) ? classicTrackAxis(worldObj, posX, posY, posZ)
                    : CommonUtil.getRailMeta(worldObj, null,posX,posY,posZ);
            //define the angle between the player and the track.
            // this is more reliable than player direction because player goes from -360 to 360 for no real reason.
            float rotation =atan2degreesf(posX-playerEntity.getX(), posZ-playerEntity.getZ());
            double x = posX + 0.5D, y = posY, z = posZ + 0.5D;

            if(railMeta==0){
                //check if the train fits on the track
                if (!isRailBlockAt(worldObj, posX, posY, posZ + Math.floor(entity.rotationPoints()[0]+ 1.0f ))
                        || !isRailBlockAt(worldObj, posX, posY, posZ + Math.floor(entity.rotationPoints()[1]- 1.0f ))) {
                    sendChat(playerEntity, "Place on a straight piece of track that is of sufficient length");
                    return false;
                }
                //decide the rotation based on placement direction
                //the math for this is a bit more complicated because the angles are awkward
                if((rotation+270)%360>180){
                    entity.setYRot(270);
                } else {
                    entity.setYRot(90);
                }
                x+=0.03125f;

            } else if (railMeta==1){
                //check if the train fits on the track
                if (!CommonUtil.isRailBlockAt(worldObj, posX - floorDouble(entity.rotationPoints()[0]+ 1.0f ), posY, posZ)
                        || !CommonUtil.isRailBlockAt(worldObj, posX - floorDouble(entity.rotationPoints()[1]- 1.0f ), posY, posZ)) {
                    sendChat(playerEntity, "Place on a straight piece of track that is of sufficient length");
                    return false;
                }

                //decide the rotation based on placement direction
                if(rotation>0){
                    entity.setYRot(180);
                } else {
                    entity.setYRot(0);
                }
                z+=0.03125f;
            } else {
                sendChat(playerEntity, "Place on a straight piece of track that is of sufficient length");
                return false;
            }
            entity.setPos(x, y, z);
            entity.yRotO = entity.getYRot();
            //actually place the entity
            worldObj.addFreshEntity(entity);
            entity.entityFirstInit(stack);
            //force first update to spawn entity in faster
            entity.tick();
            return true;
        }
        return false;
    }
}
