package ebf.tim.entities;


import ebf.tim.blocks.rails.BlockRailCore;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * <h1>Bogie Core</h1>
 * this controls the behavior of the bogies in trains and rollingstock.
 * In 1.12 this was a minecart entity of its own; in 1.21 it is part of the transport: the server moves it, the client
 * gets its position through the transport's synced data. The movement code is the 1.12 one.
 * @author Eternal Blue Flame
 */
public class EntityBogie {

    /**the transport this bogie belongs to*/
    public final GenericRailTransport host;
    /**defines if this is the front bogie of the transport*/
    public final boolean isFront;
    /**used to calculate the X/Y/Z velocity based on the direction the rail is facing, similar to how vanilla minecarts work.*/
    private static final double[][][] martix = new double[][][] {
            //straight
            {{0, -0.5}, {0, 0.5}, {0, -1}},
            {{ -0.5, 0}, {0.5, 0}, {-1, 0}},
            //slope
            {{ -0.5, 0}, {0.5, 0}, {-1, 0}},
            {{ -0.5, 0}, {0.5, 0}, {-1, 0}},
            {{0, -0.5}, {0, 0.5}, {0, -1}},
            {{0, -0.5}, {0, 0.5}, {0, -1}},
            //turns
            {{0, 0.5}, {0.5, 0}, {-0.5, 0.5}},
            {{0, 0.5}, { -0.5, 0}, {0.5, 0.5}},
            {{0, -0.5}, { -0.5, 0}, {0.5, -0.5}},
            {{0, -0.5}, {0.5, 0}, {-0.5, -0.5}}
    };

    /**cached values for the rail path and motion vectors
     * prevents need to generate a new variable multiple times per tick and reduces GC strain*/
    private double railPathX=0, railPathZ=0,motionSqrt,railPathX2, railPathZ2;
    private int railMetadata, xFloor=0,yFloor=0,zFloor=0;
    private Block blockNext, blockCurrent;
    float railmax;

    public double posX, posY, posZ, prevPosX, prevPosY, prevPosZ, motionX, motionZ;
    /**0/1 own motion x/z, 2/3 coupling pull x/z (reset every tick), 4/5 the cached direction*/
    public double[] velocity = new double[]{0,0,0,0,0,0};
    /**normally this variable exists already in 1.7, this additional declaration of it is support for 1.8.9+*/
    public float yOffset=0.125f;
    /**
     * the height of the top of the rails above the rail block's floor, measured from their geometry (autotest
     * measure=1): the renderer stands the model's wheels there. 1.12 used one guessed render lift per rail kind,
     * which left most trains hovering a little and raised them further off the rails
     */
    public static final float TIM_RAIL_Y = 0.125f, CLASSIC_TRACK_Y = 0.0858f, VANILLA_RAIL_Y = 0.0625f, GROUND_Y = 0f;
    /**what 1.12 kept in the minecart's entity data: the TiM rails read the last used rail shape from here*/
    public final CompoundTag entityData = new CompoundTag();

    public EntityBogie(GenericRailTransport host, double xPos, double yPos, double zPos, boolean front) {
        this.host = host;
        posX = prevPosX = xPos;
        posY = prevPosY = yPos;
        posZ = prevPosZ = zPos;
        isFront = front;
    }

    public Level getWorld(){return host.level();}

    /**detector rails and the floor cache, run by the host every server tick before it moves*/
    public void onUpdate() {
        //init stuff for detector/activator rails
        if(yFloor==0) {
            xFloor = CommonUtil.floorDouble(this.posX);
            yFloor = CommonUtil.floorDouble(this.posY);
            zFloor = CommonUtil.floorDouble(this.posZ);
            blockCurrent=CommonUtil.getBlockAt(getWorld(),xFloor,yFloor,zFloor);
        }
    }

    public void addVelocity(GenericRailTransport host, double speed){
        //cache rotation so it only has to be processed once per tick
        if(velocity[4]==0 && velocity[5]==0){
            Vec3f vec = CommonUtil.rotatePoint(new Vec3f(1,0,0),0,host.getYRot(),0);
            //scaled so |x|+|z| = 1: TiM measures speed that way, a unit vector at 45 degrees would be 41 % faster
            double l1 = Math.abs(vec.xCoord) + Math.abs(vec.zCoord);
            velocity[4]=l1 > 0 ? vec.xCoord / l1 : vec.xCoord;
            velocity[5]=l1 > 0 ? vec.zCoord / l1 : vec.zCoord;
        }

        velocity[0]+=speed*velocity[4];
        velocity[1]+=speed*velocity[5];
    }

    public void addLinking(GenericRailTransport host, double speed){
        //cache rotation so it only has to be processed once per tick
        if(velocity[4]==0 && velocity[5]==0){
            Vec3f vec = CommonUtil.rotatePoint(new Vec3f(1,0,0),0,host.getYRot(),0);
            //scaled so |x|+|z| = 1: TiM measures speed that way, a unit vector at 45 degrees would be 41 % faster
            double l1 = Math.abs(vec.xCoord) + Math.abs(vec.zCoord);
            velocity[4]=l1 > 0 ? vec.xCoord / l1 : vec.xCoord;
            velocity[5]=l1 > 0 ? vec.zCoord / l1 : vec.zCoord;
        }

        //the coupling's pull is a distance: a whole one along the train, at any angle (scaled like the speed it
        //came out 0.7 of it on diagonals, and coupled cars drifted apart there)
        double l = Math.abs(velocity[4]) + Math.abs(velocity[5]), e = Math.sqrt(velocity[4] * velocity[4] + velocity[5] * velocity[5]);
        velocity[2]+=speed*velocity[4] * (e > 0 ? l / e : 1);
        velocity[3]+=speed*velocity[5] * (e > 0 ? l / e : 1);
    }

    public void drag(GenericRailTransport host, double drag){
        velocity[0]*=drag;
        velocity[1]*=drag;
    }

    public void setVelocity(double x, double y, double z) {
        motionX=(((long)(x*1000000))*0.000001);
        motionZ=(((long)(z*1000000))*0.000001);
    }

    /**
     * <h2> movement management</h2>
     * this is modified movement from the minecart, should be more efficient, and reliable, but generally does the same thing, minus ability to collide.
     */
    public void minecartMove(GenericRailTransport host) {
        //server only
        if(!getWorld().isClientSide) {

            //reset rotation
            velocity[4]=0;velocity[5]=0;
            //update old position, add the gravity, and get the block below this,
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            Block block = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
            if (!CommonUtil.isClassicTrack(block) && lastTrack != null) {
                //a diagonal runs through block corners: right at one the bogie can be a hair inside an empty block
                //beside the track, which derailed it for a tick
                int nx = posX - xFloor < 0.05 ? -1 : posX - xFloor > 0.95 ? 1 : 0;
                int nz = posZ - zFloor < 0.05 ? -1 : posZ - zFloor > 0.95 ? 1 : 0;
                for (int[] o : new int[][]{{nx, 0}, {0, nz}, {nx, nz}}) {
                    if ((o[0] != 0 || o[1] != 0) && CommonUtil.isClassicTrack(CommonUtil.getBlockAt(getWorld(), xFloor + o[0], yFloor, zFloor + o[1]))) {
                        xFloor += o[0];
                        zFloor += o[1];
                        block = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
                        break;
                    }
                }
            }

            if(CommonUtil.isClassicTrack(block)){
                //classic Traincraft 4 pieces: follow their geometry (arcs, slopes, switches)
                this.yOffset=CLASSIC_TRACK_Y;
                moveOnTCRail(xFloor, yFloor, zFloor, block);
                updateFloor();
                velocity[2]=0;velocity[3]=0;
                return;
            }
            if(!(block instanceof BaseRailBlock)){
                if(CommonUtil.isClassicTrack(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor+1, zFloor))
                        || CommonUtil.isClassicTrack(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor-1, zFloor))) {
                    //stepped onto a classic piece one block up or down (the end of a slope)
                    yFloor += CommonUtil.isClassicTrack(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor+1, zFloor)) ? 1 : -1;
                    posY = yFloor;
                    moveOnTCRail(xFloor, yFloor, zFloor, CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor));
                    updateFloor();
                    velocity[2]=0;velocity[3]=0;
                    return;
                }
                if(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor+1, zFloor) instanceof BaseRailBlock) {
                    prevPosY=posY;
                    yFloor++;
                    //rails sit on whole blocks: land on the rail's height
                    posY=yFloor;
                    block = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
                } else if(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor-1, zFloor) instanceof BaseRailBlock) {
                    prevPosY=posY;
                    yFloor--;
                    posY=yFloor;
                    block = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
                } else if(CommonUtil.isAir(getWorld(), xFloor, yFloor, zFloor)) {
                    moveOffRail();
                }
            }

            //update on normal rails
            if (block instanceof BaseRailBlock) {
                this.yOffset=(block instanceof BlockRailCore?TIM_RAIL_Y:VANILLA_RAIL_Y);
                loopVanilla(host, Math.abs(velocity[0])+Math.abs(velocity[1])+Math.abs(velocity[2])+Math.abs(velocity[3]), (BaseRailBlock) block);
            }
           velocity[2]=0;velocity[3]=0;
        }
    }

    /*
     * <h1>Classic Traincraft 4 tracks</h1>
     * ported from the TC 4.5 bogie (same velocity layout as TiM: 0/1 own motion x/z, 2/3 coupling pull x/z).
     */
    public train.common.tile.TileTCRail lastTrack = null;

    private void updateFloor(){
        xFloor = CommonUtil.floorDouble(this.posX);
        yFloor = CommonUtil.floorDouble(this.posY);
        zFloor = CommonUtil.floorDouble(this.posZ);
        blockCurrent = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
    }

    private void moveOnTCRail(int i, int j, int k, Block l) {
        prevPosX=posX;
        prevPosY=posY;
        prevPosZ=posZ;
        net.minecraft.world.level.block.entity.BlockEntity te = CommonUtil.getTileEntity(getWorld(), i, j, k);
        if(l instanceof train.common.blocks.BlockTCRail && te instanceof train.common.tile.TileTCRail) {
            if(!train.common.items.TCRailTypes.isCrossingTrack((train.common.tile.TileTCRail) te) && !train.common.items.TCRailTypes.isDiagonalCrossingTrack((train.common.tile.TileTCRail) te)) {
                lastTrack = (train.common.tile.TileTCRail) te;
            }
        } else if(l instanceof train.common.blocks.BlockTCRailGag && te instanceof train.common.tile.TileTCRailGag
                && (lastTrack==null || !CommonUtil.getTiles(getWorld(),i,j,k).contains(lastTrack))){
            train.common.tile.TileTCRailGag tileGag = (train.common.tile.TileTCRailGag) te;
            if(tileGag.originX.size()>0) {
                net.minecraft.world.level.block.entity.BlockEntity o = CommonUtil.getTileEntity(getWorld(), tileGag.originX.get(0), tileGag.originY.get(0), tileGag.originZ.get(0));
                if (o instanceof train.common.tile.TileTCRail) {
                    lastTrack = (train.common.tile.TileTCRail) o;
                }
            }
        }
        if (lastTrack == null || lastTrack.isRemoved()) {
            //no piece to follow: keep going straight
            setPositionRelative(velocity[0]+velocity[2], 0, velocity[1]+velocity[3]);
            return;
        }

        if (train.common.items.TCRailTypes.isSwitchTrack(lastTrack)) {
            //facing the switch (coming from its entry) its setting picks the way; trailing through it (coming back from
            //one of its two ways) the bogie stays on the way it is on. TC decided both by a table that had the two
            //directions along x the wrong way round: a switch set straight sent trains along its curve, which only
            //looked right because the next rail pulled them back
            int sm = lastTrack.getBlockMeta();
            double fx = sm == 3 ? 1 : sm == 1 ? -1 : 0, fz = sm == 0 ? 1 : sm == 2 ? -1 : 0;
            boolean facing = (velocity[0] + velocity[2]) * fx + (velocity[1] + velocity[3]) * fz > 0;
            boolean goStraight = facing ? !lastTrack.getSwitchState() : closerToStraight(lastTrack, fx, fz);
            if (goStraight) {
                moveOnTCStraight(j, fx != 0 ? 1 : 2);
            } else {
                moveOnTC90TurnRail(j, lastTrack.r, lastTrack.cx, lastTrack.cz);
            }
        } else if (lastTrack.getSwitchState() && train.common.items.TCRailTypes.isTurnTrack(lastTrack) && lastTrack.r > 0
                && closerToStraight(lastTrack, lastTrack.getFacing() == 3 ? 1 : lastTrack.getFacing() == 1 ? -1 : 0,
                lastTrack.getFacing() == 0 ? 1 : lastTrack.getFacing() == 2 ? -1 : 0)) {
            //a straight rail a switch turned into part of its curve: whoever is on the straight way stays on it
            moveOnTCStraight(j, lastTrack.getFacing() == 1 || lastTrack.getFacing() == 3 ? 1 : 2);
        } else if (train.common.items.TCRailTypes.isStraightTrack(lastTrack)) {
            moveOnTCStraight(j, lastTrack.getBlockMeta());
        } else if (train.common.items.TCRailTypes.isTurnTrack(lastTrack)) {
            if (lastTrack.getRailType() == train.common.items.TCRailTypes.RailTypes.DIAGONALTURN && past45(lastTrack)) {
                //a 45 degree turn ends there: on along the diagonal it leads to (the arc went on to 90 degrees, off
                //the rails)
                moveOn45Exit(j, lastTrack);
            } else {
                moveOnTC90TurnRail(j, lastTrack.r, lastTrack.cx, lastTrack.cz);
            }
        } else if (train.common.items.TCRailTypes.isCrossingTrack(lastTrack)) {
            moveOnTCTwoWaysCrossing(j);
        } else if (train.common.items.TCRailTypes.isSlopeTrack(lastTrack)) {
            moveOnTCSlope(j, lastTrack.getBlockPos().getX(), lastTrack.getBlockPos().getZ(), lastTrack.slopeAngle, lastTrack.getBlockMeta());
        } else if (train.common.items.TCRailTypes.isCurvedSlopeTrack(lastTrack)) {
            moveOnTCCurvedSlope(j, lastTrack.r, lastTrack.cx, lastTrack.cz, lastTrack.getBlockPos().getX(), lastTrack.getBlockPos().getZ(), lastTrack.getBlockMeta(), lastTrack.slopeAngle);
        } else if (train.common.items.TCRailTypes.isDiagonalTrack(lastTrack) || train.common.items.TCRailTypes.isDiagonalCrossingTrack(lastTrack)) {
            moveOnTCDiagonal(j);
        } else {
            setPositionRelative(velocity[0]+velocity[2], 0, velocity[1]+velocity[3]);
        }
        //TiM rails treat "last rail ran along Z, this one along X" as a crossing and keep going straight:
        //remember the axis we are really moving along, so a curve hands over to a TiM rail the right way
        entityData.putInt("tim.lastusedrail.meta",
                Math.abs(velocity[0]+velocity[2]) > Math.abs(velocity[1]+velocity[3]) ? 1 : 0);
    }

    /*
     * On the classic pieces the bogie keeps its own speed (velocity 0/1) and direction; the coupling and
     * bogie-spacing pull (velocity 2/3) only counts with its part along the rail, signed. TC 4.5 added the sizes
     * of both, so a pull backwards made the bogie run faster forwards, or turned it round.
     */
    private void moveOnTCDiagonal(int j) {
        //the diagonal's direction from our own motion (the pull could point across it)
        double ox = velocity[0], oz = velocity[1];
        if (Math.abs(ox) + Math.abs(oz) < 1.0E-9) {
            ox = velocity[2];
            oz = velocity[3];
        }
        double sx = Math.copySign(1, ox), sz = Math.copySign(1, oz);
        railPathX = 0.5 * sx;
        railPathZ = 0.5 * sz;
        motionSqrt = Math.abs(velocity[0]) + Math.abs(velocity[1]);
        velocity[0] = motionSqrt * railPathX;
        velocity[1] = motionSqrt * railPathZ;
        //the speed is the distance moved, at any angle (TiM's |x|+|z| measure moved trains 0.7 as fast on diagonals,
        //and a train going from a curve onto one was braked by its own front)
        double along = motionSqrt + DIAG * (velocity[2] * sx + velocity[3] * sz);
        //onto the nearest diagonal through block centres, then along it. The one through this block's centre
        //(TC) is another line when the bogie is a hair into a block beside it, as at the corners the line runs
        //through, and it was thrown sideways and back
        if (sx * sz < 0) {
            double shift = (Math.round(posX + posZ) - (posX + posZ)) * 0.5;
            posX += shift;
            posZ += shift;
        } else {
            double shift = (Math.round(posX - posZ) - (posX - posZ)) * 0.5;
            posX += shift;
            posZ -= shift;
        }
        posY = j;
        setPositionRelative(DIAG * sx * along, 0, DIAG * sz * along);
    }

    private static final double DIAG = Math.sqrt(0.5);

    private void moveOnTCStraight(int j, int meta) {
        boolean alongZ = meta == 2 || meta == 0;
        double own = alongZ ? velocity[1] : velocity[0];
        double pull = alongZ ? velocity[3] : velocity[2];
        motionSqrt = Math.abs(velocity[0]) + Math.abs(velocity[1]);
        //coming off a curve the motion can still be partly across: then the larger part says which way
        double dir = Math.copySign(1, own != 0 ? own : motionSqrt > 0 ? velocity[0] + velocity[1] : pull);
        if (alongZ) {
            posX = xFloor + 0.5;
            railPathX = 0;
            railPathZ = dir;
            velocity[0] = 0;
            velocity[1] = dir * motionSqrt;
        } else {
            posZ = zFloor + 0.5;
            railPathX = dir;
            railPathZ = 0;
            velocity[0] = dir * motionSqrt;
            velocity[1] = 0;
        }
        double along = dir * motionSqrt + pull;
        posY = j;
        if (alongZ) {
            setPositionRelative(0, 0, along);
        } else {
            setPositionRelative(along, 0, 0);
        }
    }

    /** the rise of a slope piece at the bogie's position: its angle times the distance from the low end, at most a block */
    private double slopeRise(double slopeAngle, int tilex, int tilez, int meta){
        railPathX = tilex - posX;
        railPathZ = tilez - posZ;
        if (meta == 2) {
            railPathZ += 1;
            railPathX += 0.5;
        } else if (meta == 0) {
            railPathX += 0.5;
        } else if (meta == 1) {
            railPathX += 1;
            railPathZ += 0.5;
        } else if (meta == 3) {
            railPathZ += 0.5;
        }
        //TC slope angles give 0.8 of a block over the piece (0.13 x 6, 0.0666 x 12, 0.0444 x 18): scale to a whole block,
        //so the top meets the track one block up
        return Math.min(1, slopeAngle * Math.sqrt(railPathX * railPathX + railPathZ * railPathZ) / 0.8);
    }

    private void moveOnTCCurvedSlope(int j, double radius, double startX, double startZ, int tilex, int tilez, int meta, double slopeAngle) {
        moveOnTC90TurnRail(j,radius,startX,startZ);
        posY = j + slopeRise(slopeAngle, tilex, tilez, meta);
    }

    private void moveOnTCTwoWaysCrossing(int j) {
        //straight on along whichever line our own motion follows
        double ox = velocity[0], oz = velocity[1];
        if (Math.abs(ox) + Math.abs(oz) < 1.0E-9) {
            ox = velocity[2];
            oz = velocity[3];
        }
        moveOnTCStraight(j, Math.abs(ox) < Math.abs(oz) ? 0 : 1);
    }

    private void moveOnTCSlope(int j, int tilex, int tilez, double slopeAngle, int meta) {
        moveOnTCStraight(j, meta);
        posY = j + slopeRise(slopeAngle, tilex, tilez, meta);
    }

    /**
     * past the end of a 45 degree turn: along the straight line the arc runs into at 45 degrees (the diagonal its
     * model and the next diagonal piece lie on), kept on that line. The diagonal through the block's centre is a
     * different line on the blocks around the corner the line passes through.
     */
    private void moveOn45Exit(int j, train.common.tile.TileTCRail t) {
        int l = t.getFacing();
        double fx = l == 3 ? 1 : l == 1 ? -1 : 0, fz = l == 0 ? 1 : l == 2 ? -1 : 0;
        double rx = -fz, rz = fx;
        if (rx * (t.getBlockPos().getX() + 0.5 - t.cx) + rz * (t.getBlockPos().getZ() + 0.5 - t.cz) < 0) {
            rx = -rx;
            rz = -rz;
        }
        //the 45 degree point, and the line's direction there
        double s2 = Math.sqrt(0.5);
        double px = t.cx + t.r * (rx + fx) * s2, pz = t.cz + t.r * (rz + fz) * s2;
        double dx = (fx - rx) * s2, dz = (fz - rz) * s2;
        //onto the nearest diagonal through block centres, where the diagonal pieces lie (a 45 degree turn's own
        //line is a few hundredths off it, enough to clip the empty block at the corner it crosses)
        double c = dx * dz < 0 ? px + pz : px - pz, shift = (Math.round(c) - c) * 0.5;
        px += shift;
        pz += dx * dz < 0 ? shift : -shift;
        //the way our own motion goes along it
        double own = velocity[0] * dx + velocity[1] * dz;
        if (own < 0 || (own == 0 && velocity[2] * dx + velocity[3] * dz < 0)) {
            dx = -dx;
            dz = -dz;
        }
        double ux = Math.copySign(1, dx), uz = Math.copySign(1, dz);
        railPathX = 0.5 * ux;
        railPathZ = 0.5 * uz;
        motionSqrt = Math.abs(velocity[0]) + Math.abs(velocity[1]);
        velocity[0] = motionSqrt * railPathX;
        velocity[1] = motionSqrt * railPathZ;
        double along = motionSqrt + DIAG * (velocity[2] * ux + velocity[3] * uz);
        double tt = (posX - px) * dx + (posZ - pz) * dz;
        posX = px + tt * dx;
        posZ = pz + tt * dz;
        posY = j;
        setPositionRelative(DIAG * ux * along, 0, DIAG * uz * along);
    }

    /** nearer the straight way through this rail (its centre line along fx, fz) than its curve */
    private boolean closerToStraight(train.common.tile.TileTCRail t, double fx, double fz) {
        double cx = t.getBlockPos().getX() + 0.5, cz = t.getBlockPos().getZ() + 0.5;
        double toLine = Math.abs((posX - cx) * -fz + (posZ - cz) * fx);
        double toArc = Math.abs(Math.sqrt((posX - t.cx) * (posX - t.cx) + (posZ - t.cz) * (posZ - t.cz)) - t.r);
        return t.r <= 0 || toLine <= toArc;
    }

    /** on a 45 degree turn's arc, further round than 45 degrees from where it starts (the side its rail is on) */
    private boolean past45(train.common.tile.TileTCRail t) {
        int l = t.getFacing();
        double fx = l == 3 ? 1 : l == 1 ? -1 : 0, fz = l == 0 ? 1 : l == 2 ? -1 : 0;
        //the radius at the start is square to the way the turn sets off, on the side of the turn's rail
        double rx = -fz, rz = fx;
        double tx = t.getBlockPos().getX() + 0.5 - t.cx, tz = t.getBlockPos().getZ() + 0.5 - t.cz;
        if (rx * tx + rz * tz < 0) {
            rx = -rx;
            rz = -rz;
        }
        double px = posX - t.cx, pz = posZ - t.cz, pl = Math.sqrt(px * px + pz * pz);
        return pl > 1e-6 && (px * rx + pz * rz) / pl < Math.cos(Math.PI / 4);
    }

    private void moveOnTC90TurnRail(int j, double radius, double startX, double startZ){
        double controlX = posX - startX;
        double controlZ = posZ - startZ;
        double controlNorm = Math.sqrt(controlX * controlX + controlZ * controlZ);
        double xMotion=velocity[0]+velocity[2];
        double zMotion=velocity[1]+velocity[3];
        double normCpx = controlX / controlNorm;
        double normCpz = controlZ / controlNorm;
        railPathX = (posX + xMotion) - startX;
        railPathZ = (posZ + zMotion) - startZ;
        //the arc's tangent here, and which way along it our own motion goes
        double tx = -normCpz, tz = normCpx;
        double own = velocity[0] * tx + velocity[1] * tz;
        if (own < 0) {
            tx = -tx;
            tz = -tz;
        }
        //TiM measures speed as |x|+|z| (exact on its straight rails); keep that measure on the arc too, or every
        //curve <-> straight joint changes the speed (wagons then ran into the locomotive)
        double l1 = Math.abs(tx) + Math.abs(tz);
        motionSqrt = Math.abs(velocity[0]) + Math.abs(velocity[1]);
        //the coupling and bogie-spacing pull counts only with its share along the rail: its part across the rail
        //(towards the arc's centre) was taken as speed along it, which shook trains on curves and set a train
        //standing at the start of one rolling by itself
        double pull = velocity[2] * tx + velocity[3] * tz;
        double step = motionSqrt + pull;
        //onto the arc, then along its tangent
        setPosition(startX + (normCpx * radius), j, startZ + (normCpz * radius));
        setPositionRelative(tx * step, 0.0D, tz * step);
        velocity[0] = motionSqrt * tx / l1;
        velocity[1] = motionSqrt * tz / l1;
    }

    private void moveOffRail(){
        //ploughing through the ground: a derailed train comes to a stop within a second or so
        yOffset = GROUND_Y;
        velocity[0]*=0.85;
        velocity[1]*=0.85;
        railPathZ=0;railPathX=0;
        //broken or absurd speeds would make the loop below run (almost) forever and freeze the server
        for (int i = 0; i < 4; i++) {
            if (Double.isNaN(velocity[i]) || Double.isInfinite(velocity[i])) {
                velocity[i] = 0;
            }
            velocity[i] = Math.max(-2, Math.min(2, velocity[i]));
        }
        //split the move into steps of at most 0.35 so falling is checked on the way; each step is a share of the move,
        //adding the whole vector every step overshot the link correction and set derailed cars shaking
        double dx = velocity[0]+velocity[2], dz = velocity[1]+velocity[3];
        double moveLength = Math.abs(dx) + Math.abs(dz);
        if (moveLength > 2) {
            dx *= 2 / moveLength;
            dz *= 2 / moveLength;
            moveLength = 2;
        }
        int steps = (int) Math.ceil(moveLength / 0.35);
        for (int step = 0; step < steps; step++) {
            posX+=dx/steps;
            posZ+=dz/steps;

            //update Y position and check for collision
            if(xFloor!=CommonUtil.floorDouble(this.posX) || zFloor!=CommonUtil.floorDouble(posZ)) {
                xFloor = CommonUtil.floorDouble(this.posX);
                yFloor = CommonUtil.floorDouble(this.posY);
                zFloor = CommonUtil.floorDouble(this.posZ);
                //fall
                if (CommonUtil.isAir(getWorld(), xFloor, yFloor - 1, zFloor)) {
                    posY--;
                }
            }
        }
    }

    private void loopVanilla(GenericRailTransport host, double moveLength, BaseRailBlock block){

        //try to adhere to limiter track
        railmax = CommonUtil.getMaxRailSpeed(getWorld(), block, host, xFloor, yFloor, zFloor);
        if(railmax!=0.4f){
            moveLength=Math.min(moveLength,railmax);
        }
        railMetadata = CommonUtil.getRailMeta(getWorld(), this, xFloor, yFloor, zFloor);
        //actually move
        while (moveLength>0) {
            moveBogieVanilla(Math.min(0.3, moveLength));
            moveLength -= 0.3;

            //update the last used block to the one we just used, if it's actually different.
            if(xFloor!=CommonUtil.floorDouble(this.posX) || zFloor != CommonUtil.floorDouble(this.posZ)) {
                xFloor = CommonUtil.floorDouble(this.posX);
                yFloor = CommonUtil.floorDouble(this.posY);
                zFloor = CommonUtil.floorDouble(this.posZ);
                blockCurrent=CommonUtil.getBlockAt(getWorld(),xFloor,yFloor,zFloor);
                //check for collisions and skip update
                for (int i = 1; i < host.getHitboxSize()[1] - 1; i++) {
                    if (!CommonUtil.isAir(getWorld(), xFloor, yFloor + i, zFloor)) {
                        host.backBogie.motionX=0;
                        host.backBogie.motionZ=0;
                        host.frontBogie.motionX=0;
                        host.frontBogie.motionZ=0;
                        return;
                    }
                }
                //a classic piece just above or below (a classic slope): leave the height to it, the TiM stair step
                //would drop the bogie a whole block for one tick (the train jerked at the top of a classic slope)
                if(!(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor) instanceof BaseRailBlock)
                        && (CommonUtil.isClassicTrack(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor-1, zFloor))
                        || CommonUtil.isClassicTrack(CommonUtil.getBlockAt(getWorld(), xFloor, yFloor+1, zFloor)))) {
                    return;
                }
                //handle slope movement before other interactions
                if(!CommonUtil.isRailBlockAt(getWorld(), xFloor, yFloor, zFloor)){
                    this.prevPosY =posY;
                    if(CommonUtil.isRailBlockAt(getWorld(), xFloor, yFloor+1, zFloor)){
                        posY++;
                    } else if (CommonUtil.isRailBlockAt(getWorld(), xFloor, yFloor-1, zFloor)) {
                        posY--;
                    }
                    yFloor = CommonUtil.floorDouble(this.posY);
                }

                blockNext = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
                //entering a classic Traincraft 4 piece: the rest of this tick's movement follows its geometry
                if (CommonUtil.isClassicTrack(blockNext)) {
                    return;
                }
                //now loop this again for the next increment of movement, if there is one
                if (blockNext instanceof BaseRailBlock) {
                    block = (BaseRailBlock) blockNext;
                    //do the rail functions.
                    activateRail(new BlockPos(xFloor, yFloor, zFloor));
                    //get the direction of the rail from it's metadata
                    railMetadata = CommonUtil.getRailMeta(getWorld(), this, xFloor, yFloor, zFloor);
                }
            }
        }
    }

    /**detector rails see the train (1.12 called onMinecartPass and set the detector's meta)*/
    private void activateRail(BlockPos pos){
        BlockState state = getWorld().getBlockState(pos);
        if(state.getBlock() == Blocks.DETECTOR_RAIL && !state.getValue(BlockStateProperties.POWERED)){
            getWorld().setBlock(pos, state.setValue(BlockStateProperties.POWERED, true), 3);
            getWorld().updateNeighborsAt(pos, state.getBlock());
            getWorld().updateNeighborsAt(pos.below(), state.getBlock());
            getWorld().scheduleTick(pos, state.getBlock(), 20);
        }
    }


    private void moveBogieVanilla(double currentMotion){
        if(Math.abs(currentMotion)<0.000001){return;}
        //figure out the current rail's direction
        railPathX = (martix[railMetadata][2][0]);
        railPathZ = (martix[railMetadata][2][1]);

        //cover moving reverse of track direction using the rotation from the closed loop rather than the full motion
        if((velocity[0]+velocity[2]) * railPathX + (velocity[1]+velocity[3]) * railPathZ <= 0.0D) {
            railPathX = -railPathX;
            railPathZ = -railPathZ;
        }

        setPositionRelative((currentMotion * railPathX), 0, (currentMotion * railPathZ));

        motionSqrt = Math.abs(velocity[0])+Math.abs(velocity[1]);
        velocity[0] = (float)(motionSqrt * railPathX);
        velocity[1] = (float)(motionSqrt * railPathZ);

        motionSqrt = Math.abs(velocity[2])+Math.abs(velocity[3]);
        velocity[2] = (float)(motionSqrt * railPathX);
        velocity[3] = (float)(motionSqrt * railPathZ);

        //define the rail path again, to center the transport.
        railPathX2 = xFloor + 0.5D + martix[railMetadata][0][0];
        railPathZ2 = zFloor + 0.5D + martix[railMetadata][0][1];
        railPathX = (xFloor + 0.5D + martix[railMetadata][1][0]) - railPathX2;
        railPathZ = (zFloor + 0.5D + martix[railMetadata][1][1]) - railPathZ2;

        //based on the path direction, try to center the bogie on the track
        if (railPathX == 0.0D) {
            motionSqrt = this.posZ - zFloor;
        } else if (railPathZ == 0.0D) {
            motionSqrt = this.posX - xFloor;
        } else {
            motionSqrt = ((this.posX - railPathX2) * railPathX + (this.posZ - railPathZ2) * railPathZ) * 2.0D;
        }
        //do the centering movement
        setPosition((railPathX2 + railPathX * motionSqrt), posY, (railPathZ2 + railPathZ * motionSqrt));
    }

    public void setPosition(double x, double y, double z) {
        posX=x;
        posY=y;
        posZ=z;
    }

    public void setPositionRelative(double x, double y, double z) {
        posX+=((int)(x*10000))*0.0001;
        if(y!=0) {//usually we won't be changing this, so this is more efficient
            posY += ((int) (y * 10000)) * 0.0001;
        }
        posZ+=((int)(z*10000))*0.0001;
    }

    public String debugState(){
        return String.format("(%.2f,%.2f,%.2f yOff=%.4f v=%.3f,%.3f,%.3f,%.3f %s)", posX, posY, posZ, yOffset, velocity[0], velocity[1], velocity[2], velocity[3],
                CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor).getClass().getSimpleName()
                + (lastTrack == null ? "" : " last=" + lastTrack.getTrackName() + "/" + lastTrack.getBlockMeta() + (lastTrack.isRemoved() ? " REMOVED" : "")));
    }
}
