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
    public float yOffset=0.425f;
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

        velocity[2]+=speed*velocity[4];
        velocity[3]+=speed*velocity[5];
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

            if(!(block instanceof BaseRailBlock)){
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
                this.yOffset=(block instanceof BlockRailCore?0.425f:0.3425f);
                loopVanilla(host, Math.abs(velocity[0])+Math.abs(velocity[1])+Math.abs(velocity[2])+Math.abs(velocity[3]), (BaseRailBlock) block);
            }
           velocity[2]=0;velocity[3]=0;
        }
    }

    private void moveOffRail(){
        //ploughing through the ground: a derailed train comes to a stop within a second or so
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
        return String.format("(%.2f,%.2f,%.2f v=%.3f,%.3f,%.3f,%.3f %s)", posX, posY, posZ, velocity[0], velocity[1], velocity[2], velocity[3],
                CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor).getClass().getSimpleName());
    }
}
