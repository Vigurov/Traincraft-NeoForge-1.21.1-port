package ebf.tim.utility;

import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.Vec3d;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class HitboxDynamic {

    private float longest=0;
    public List<CollisionBox> interactionBoxes = new ArrayList<>();
    public CollisionBox front,back;

    public HitboxDynamic(float depth, float height, float width, GenericRailTransport entity){
        depth *=0.5f;
        width *=0.5f;
        if(Math.abs(depth)>longest){
            longest=Math.abs(depth);
        }
        if(Math.abs(width)>longest){
            longest=Math.abs(width);
        }
        depth*=2;width*=2;
        if(height!=-1f) {
            interactionBoxes = new ArrayList<>();
            for (float f = 0; f < depth - (width * 0.25f); f += width) {
                CollisionBox c = new CollisionBox((entity));
                interactionBoxes.add(c);
                if(front==null){
                    front=c;
                } else{
                    back=c;
                }
            }
            if(back==null){
                back=front;
            }
        }
    }

    public void position(double x, double y, double z, float pitch, float yaw){
        for(int i=0; i<interactionBoxes.size();i++){
            Vec3d part = CommonUtil.rotateDistance(
                    (interactionBoxes.get(0).host.getHitboxSize()[0]*0.5f)-
                    ((interactionBoxes.get(0).host.getHitboxSize()[0]/interactionBoxes.size())*(i+0.5f)),
                    -pitch, yaw).addVector(x,y,z);
            CollisionBox box = interactionBoxes.get(i);
            box.xo = box.getX(); box.yo = box.getY(); box.zo = box.getZ();
            box.xOld = box.xo; box.yOld = box.yo; box.zOld = box.zo;
            box.setPos(part.xCoord,part.yCoord,part.zCoord);
        }
    }

    /**
     * the entities touching the boxes: players and mobs, and other transports' boxes
     * (not passengers, and not the boxes of this consist)
     */
    public List<Entity> collidingEntities = new ArrayList<>();

    public void updateCollidingEntities(GenericRailTransport host){
        collidingEntities = new ArrayList<>();
        if(host==null || interactionBoxes.isEmpty()){return;}
        AABB area = new AABB(host.getX()-longest-0.5, host.getY()-0.5, host.getZ()-longest-0.5,
                host.getX()+longest+0.5, host.getY()+host.getHitboxSize()[1]+1.5, host.getZ()+longest+0.5);
        for (Entity obj : host.level().getEntities(host, area)) {
            //generally we only want to collide with mobs/players/other collision boxes
            //we dont collide with passenger entities, we collide with the thing they are on.
            if(obj instanceof GenericRailTransport || obj.isPassenger()) {
                continue;
            }
            //if it's another collision box, be sure it's not the current entity or a linked one
            if(obj instanceof CollisionBox){
                GenericRailTransport other = ((CollisionBox) obj).host;
                if(other==null || other==host){
                    continue;
                }
                //make sure not to interact with own consist.
                //we have to check against front and back link on top of entire consist,
                //    in case entire consist hasn't updated just yet
                if((host.frontLinkedID!=null && host.frontLinkedID==other.getId()) ||
                        (host.backLinkedID!=null && host.backLinkedID==other.getId())||
                        host.getConsist().contains(other)){
                    continue;
                }
            }
            if(containsEntity(obj)){
                this.collidingEntities.add(obj);
            }
        }
    }

    public boolean containsEntity(Entity e){
        for(CollisionBox box : interactionBoxes){
            if (e.getBoundingBox().intersects(box.getBoundingBox().inflate(0.2D, e instanceof Player?1.2D:0.2D, 0.2D)))
                return true;
        }
        return false;
    }
}
