package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;

import java.util.HashMap;
import java.util.Map;

/**
 * The wheels onto the rails. Traincraft's models were made by many hands, and their wheels stand at different
 * distances from the middle: most on the classic track's railheads (0.31..0.375 across), some inside them (DB cars,
 * 0.25..0.31), some outside (0.375..0.44). Each transport's wheels are found once, from its parts as drawn (low,
 * thin across, short along, all on one side), and moved across so their middle is where the right ones' is; the
 * body stays as it is. Less than 0.04 (under a pixel) is left; steam locomotives are left (their rods).
 */
public final class WheelFit {
    private WheelFit() {}

    /** where a wheel's middle is across in the models whose wheels are on the railheads */
    static final float WHEEL = 0.344f;

    public static void prepare(GenericRailTransport t) {
        if (t.renderData.wheelShift != null || t.frontBogie == null || t.backBogie == null) return;

        if (t.renderData.modelList == null) return;
        //a steam locomotive's wheels carry its rods and valve gear, which are other parts: they stay as made
        if (t.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)) {
            t.renderData.wheelShift = new HashMap<>();
            return;
        }
        Map<ModelRendererTurbo, Float> shift = new HashMap<>();
        t.renderData.wheelShift = shift;
        Map<ModelRendererTurbo, float[]> boxes = new HashMap<>();
        RenderContext.begin(new PoseStack(), null, 0, null);
        RenderContext.classify = boxes;
        RenderContext.classifyFrame = null;
        RenderWagon.measuring = true;
        try {
            RenderWagon.doRender(t, t.getYRot(), 0, t.backBogie.yOffset, false, null);
        } catch (RuntimeException e) {
            return;
        } finally {
            RenderWagon.measuring = false;
            RenderContext.classify = null;
            RenderContext.classifyFrame = null;
            RenderContext.end();
        }
        //the frame's y points down: the lowest point is the largest
        float bottom = -Float.MAX_VALUE;
        for (float[] b : boxes.values()) bottom = Math.max(bottom, b[4]);
        t.renderData.allParts.addAll(boxes.keySet());
        for (var e : boxes.entrySet()) {
            float[] b = e.getValue();
            if (Boolean.getBoolean("tc.wheeldebug") && e.getKey() != null && b[4] > bottom - 0.3f) {
                System.out.println(String.format(java.util.Locale.ROOT, "[TCTEST] wheelpart %s x %.2f..%.2f h %.2f..%.2f z %.3f..%.3f",
                        t.getClass().getSimpleName(), b[0], b[3], bottom - b[4], bottom - b[1], b[2], b[5]));
            }
            if (e.getKey() == null || b[4] < bottom - 0.08f) continue;
            float tall = b[4] - b[1], wide = b[5] - b[2], longer = b[3] - b[0], mid = (b[2] + b[5]) / 2;
            if (b[2] * b[5] <= 0 || wide > 0.25f || longer > 4f || tall > 0.9f) continue;
            float off = Math.abs(mid);
            if (off < 0.15f || off > 0.6f) continue;
            t.renderData.wheelParts.add(e.getKey());
            float dz = Math.signum(mid) * (WHEEL - off);
            if (Math.abs(dz) >= 0.04f && Math.abs(dz) <= 0.15f && !Boolean.getBoolean("tc.nowheelfit")) shift.put(e.getKey(), dz);
        }
        if (Boolean.getBoolean("tc.wheeldebug")) {
            java.util.TreeMap<Float, Integer> by = new java.util.TreeMap<>();
            for (float v : shift.values()) by.merge(Math.round(Math.abs(v) * 1000) / 1000f, 1, Integer::sum);
            System.out.println("[TCTEST] wheelfit " + t.getClass().getSimpleName() + " wheels " + t.renderData.wheelParts.size() + " moved " + by);
        }
    }
}
