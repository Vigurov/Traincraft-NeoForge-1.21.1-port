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
    /** how far its wheels are moved across, to run on the rails (WheelFit); null until measured */
    public java.util.Map<fexcraft.tmt.slim.ModelRendererTurbo, Float> wheelShift = null;
    /** the model's bounds in its frame (x along, y down, z across): min x, y, z, max x, y, z */
    public float[] hull = null;
    /** the parts taken for its wheels (moved or not), and all its parts: for tests (tc.onlywheels) */
    public java.util.Set<fexcraft.tmt.slim.ModelRendererTurbo> wheelParts = new java.util.HashSet<>(), allParts = new java.util.HashSet<>();
    public List<AnimationBase> animatedPart = new ArrayList<>();
    public float wheelPitch=0;
    public float lastWheelPitch =0;
    public long lastFrameTime=0;
    public boolean needsModelUpdate = true;
}
