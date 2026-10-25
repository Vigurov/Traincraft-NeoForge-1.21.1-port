package ebf.tim.models;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.Vec3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bogie {
    public float rotationYaw;
    public ModelBase bogieModel;
    public float[] offset = new float[]{0,0},prevPos = null, position = new float[]{0,0}, rotation=new float[]{0,0,0};
    public List<Bogie> subBogies=new ArrayList<>();

    public Bogie(ModelBase model, @Nullable float[] offset){
        this.bogieModel = model;
        if(offset!=null) {
            this.offset = offset;
        }
    }

    public Bogie(ModelBase model, float offset){
        this.bogieModel = model;
        this.offset = new float[]{offset,0,0};
    }

    public Bogie(ModelBase model, float offsetX, float offsetY, float offsetZ){
        this.bogieModel = model;
        this.offset = new float[]{offsetX,offsetY,offsetZ};
    }

    public Bogie(ModelBase model, float offsetX, float offsetY, float offsetZ, Bogie[] bogies){
        this.bogieModel = model;
        this.offset = new float[]{offsetX,offsetY,offsetZ};
        Collections.addAll(this.subBogies, bogies);
    }

    public Bogie(ModelBase model, @Nullable float[] offset, Bogie[] bogies){
        this.bogieModel = model;
        if(offset!=null) {
            this.offset = offset;
        }
        Collections.addAll(this.subBogies, bogies);
    }

    public Bogie addSubBogie(ModelBase model, float offsetX, float offsetY, float offsetZ){
        subBogies.add(new Bogie(model,offsetX,offsetY,offsetZ));
        return this;
    }

    public Bogie addSubBogie(ModelBase model, float[] offset){
        subBogies.add(new Bogie(model,offset));
        return this;
    }

    public Bogie addSubBogie(Bogie sub){
        subBogies.add(sub);
        return this;
    }

    public Bogie setRotation(float x, float y, float z){
        rotation[0]=x;
        rotation[1]=y;
        rotation[2]=z;
        return this;
    }

    public void updateRotation(GenericRailTransport entity){
        if(prevPos!=position && prevPos!=null && position!=null&&
                Math.abs(Math.abs(position[0])+Math.abs(position[1])-Math.abs(prevPos[0])-Math.abs(prevPos[1]))>0.35f
        ) {
            rotationYaw = CommonUtil.atan2degreesf(prevPos[1] - position[1], prevPos[0] - position[0]);
            if(Math.abs(entity.getYRot()+180)-Math.abs(rotationYaw+180)>90 ||
                    Math.abs(entity.getYRot()+180)-Math.abs(rotationYaw+180)<-90){
                rotationYaw-=180;
            }
            for(Bogie b : subBogies){
                b.updateRotation(entity);
            }
            prevPos = position;
        } else if(prevPos==null){
            rotationYaw=entity.getYRot();
            prevPos = position;
        }
    }

    public void updatePosition(GenericRailTransport entity, Vec3f prevOffset){
        if(prevOffset==null){
            prevOffset= CommonUtil.rotatePoint(new Vec3f(offset[0],0,offset[1]),0,entity.getYRot(),0);
        } else {
            prevOffset.add(CommonUtil.rotatePoint(new Vec3f(offset[0],0,offset[1]),0,entity.getYRot(),0));
        }
        position=new float[]{(float)entity.getX()+prevOffset.xCoord, (float)entity.getZ()+prevOffset.zCoord};
        for(Bogie b : subBogies){
            b.updatePosition(entity,prevOffset);
        }
    }
}
