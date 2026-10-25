package ebf.tim.render;

import ebf.tim.models.Bogie;
import fexcraft.tmt.slim.ModelBase;

import java.util.ArrayList;
import java.util.List;

/**
 * what the renderer keeps per transport
 * todo: animated parts, cargo renders and particles from 1.12
 */
public class TransportRenderData {
    public ModelBase[] modelList = null;
    public Bogie[] bogies = null;
    public List<AnimationBase> animatedPart = new ArrayList<>();
    public float wheelPitch=0;
    public float lastWheelPitch =0;
    public long lastFrameTime=0;
    public boolean needsModelUpdate = true;
}
