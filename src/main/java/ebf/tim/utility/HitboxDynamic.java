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
    /** what players walk on in an open car (WalkShapes): its floor and walls; empty for the solid ones */
    public List<WalkBox> walkBoxes = new ArrayList<>();
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
        walkShape(entity);
    }

    /** the floor and walls of an open car, from its measured shape; its own boxes then stop only clicks and cars */
    private void walkShape(GenericRailTransport entity) {
        var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        WalkShapes.Shape s = type == null ? null : WalkShapes.of(type.getPath());
        if (s == null || interactionBoxes.isEmpty()) return;
        for (CollisionBox b : interactionBoxes) b.solid = false;
        float length = entity.getHitboxSize()[0];
        //the floor: squares across the body, along all of it
        float fw = s.tub() ? 2 * s.halfWidth() : entity.getHitboxSize()[2];
        float fl = s.tub() ? 2 * (s.halfInside() + s.thickness()) : length;
        int n = (int) Math.ceil(fl / fw);
        for (int i = 0; i < n; i++) {
            walkBoxes.add(new WalkBox(entity, -fl / 2 + fl / n * (i + 0.5f), 0, 0, fw, s.floor()));
        }
        if (!s.tub()) return;
        //the walls: small squares (a long thin box would grow to a square across when the car stands at an angle),
        //their inner faces where the model's are
        float t = 0.2f, h = s.wall() - s.floor();
        float sideAt = s.halfWidth() - s.thickness() + t / 2, endAt = s.halfInside() + t / 2;
        int ns = (int) Math.ceil((2 * endAt + t) / t);
        for (int i = 0; i <= ns; i++) {
            float a = -endAt - t / 2 + (2 * endAt + t) * i / ns;
            for (float sd : new float[]{-sideAt, sideAt}) walkBoxes.add(new WalkBox(entity, a, sd, s.floor(), t, h));
        }
        int ne = (int) Math.ceil(2 * sideAt / t);
        for (int i = 1; i < ne; i++) {
            float c = -sideAt + 2 * sideAt * i / ne;
            for (float e : new float[]{-endAt, endAt}) walkBoxes.add(new WalkBox(entity, e, c, s.floor(), t, h));
        }
        //beyond the end walls up to the body's ends (a hopper's sloping ends, closed end parts): solid as high as the
        //walls, or one would jump the end wall into the model and fall through it
        float from = endAt + t / 2, len = s.halfBody() - from;
        if (len > 0.05f) {
            //squares no longer than the stretch (into the inside they would close it), as many across as cover it
            int nl = (int) Math.ceil(len / 0.4f);
            float cell = len / nl;
            int nw = (int) Math.ceil(fw / cell);
            for (int i = 0; i < nl; i++) {
                float a = from + cell * (i + 0.5f);
                for (int j = 0; j < nw; j++) {
                    float c = -fw / 2 + fw / nw * (j + 0.5f);
                    for (float sg : new float[]{-1, 1}) walkBoxes.add(new WalkBox(entity, sg * a, c, 0, cell, s.wall()));
                }
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
        if (walkBoxes.isEmpty()) return;
        //over the rails' top, along and across the car, tilted with it on a slope
        GenericRailTransport host = walkBoxes.get(0).host;
        double rail = y + host.entityData().get(GenericRailTransport.BOGIE_Y_OFFSET);
        double r = Math.toRadians(yaw), ux = Math.cos(r), uz = Math.sin(r), climb = Math.sin(Math.toRadians(-pitch));
        for (WalkBox b : walkBoxes) {
            b.xo = b.getX(); b.yo = b.getY(); b.zo = b.getZ();
            b.xOld = b.xo; b.yOld = b.yo; b.zOld = b.zo;
            b.setPos(x + b.along * ux - b.side * uz, rail + b.bottom + b.along * climb, z + b.along * uz + b.side * ux);
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
            if(obj instanceof GenericRailTransport || obj instanceof WalkBox || obj.isPassenger()) {
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
