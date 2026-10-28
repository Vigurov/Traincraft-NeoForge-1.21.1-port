package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Faces of two parts lying in one plane and overlapping (a plate on a wall, two panels meeting in it) flicker: the
 * depth buffer cannot tell which is in front. Found once per transport from its model as drawn, in its frame; of
 * each such pair the smaller part is moved a hair off the plane, the way its face looks, so it is in front.
 */
public final class Unflicker {
    private Unflicker() {}

    /** how far a part is moved off, in blocks: too little to see, enough for the depth buffer some way off */
    public static final float HAIR = 0.003f;

    private record Face(ModelRendererTurbo part, int axis, int sign, float plane, float lo1, float hi1, float lo2, float hi2) {}

    public static void prepare(GenericRailTransport t) {
        if (t.renderData.unflicker != null || t.frontBogie == null || t.backBogie == null) return;
        if (Boolean.getBoolean("tc.flicker")) {
            t.renderData.unflicker = new HashMap<>();
            return;
        }
        List<Vector3f[]> faces = new ArrayList<>();
        List<ModelRendererTurbo> parts = new ArrayList<>();
        RenderContext.begin(new PoseStack(), null, 0, null);
        RenderContext.measure = new ArrayList<>();
        RenderContext.measureFaces = faces;
        RenderContext.measureFacePart = parts;
        RenderWagon.measuring = true;
        try {
            RenderWagon.doRender(t, t.getYRot(), 0, t.backBogie.yOffset, false, null);
        } catch (RuntimeException e) {
            t.renderData.unflicker = new HashMap<>();
            return;
        } finally {
            RenderWagon.measuring = false;
            RenderContext.measure = null;
            RenderContext.measureFaces = null;
            RenderContext.measureFacePart = null;
            RenderContext.end();
        }
        //(drawing it may have made its render data anew)
        t.renderData.unflicker = find(faces, parts, RenderContext.measuredFrameInv);
    }

    /** the moves for those faces (as measured; frameInv takes them into the frame) */
    public static Map<ModelRendererTurbo, float[]> find(List<Vector3f[]> faces, List<ModelRendererTurbo> parts, org.joml.Matrix4f frameInv) {
        Map<ModelRendererTurbo, float[]> moves = new HashMap<>();
        if (frameInv == null) return moves;
        //each part's size (its bounds' volume, a flat part by its area), and its faces turned along an axis
        Map<ModelRendererTurbo, float[]> bounds = new IdentityHashMap<>();
        Map<String, List<Face>> planes = new HashMap<>();
        for (int f = 0; f < faces.size() && f < parts.size(); f++) {
            Vector3f[] v = faces.get(f);
            ModelRendererTurbo part = parts.get(f);
            if (v.length < 3 || part == null) continue;
            Vector3f[] w = new Vector3f[v.length];
            for (int k = 0; k < v.length; k++) w[k] = frameInv.transformPosition(v[k], new Vector3f());
            float[] b = bounds.computeIfAbsent(part, k -> new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE});
            for (Vector3f p : w) {
                b[0] = Math.min(b[0], p.x); b[1] = Math.min(b[1], p.y); b[2] = Math.min(b[2], p.z);
                b[3] = Math.max(b[3], p.x); b[4] = Math.max(b[4], p.y); b[5] = Math.max(b[5], p.z);
            }
            Vector3f n = new Vector3f(w[1]).sub(w[0]).cross(new Vector3f(w[2]).sub(w[0]));
            if (n.lengthSquared() < 1e-14f) continue;
            n.normalize();
            int ax = Math.abs(n.x) > 0.999f ? 0 : Math.abs(n.y) > 0.999f ? 1 : Math.abs(n.z) > 0.999f ? 2 : -1;
            if (ax < 0) continue;
            int a1 = (ax + 1) % 3, a2 = (ax + 2) % 3;
            float lo1 = Float.MAX_VALUE, hi1 = -Float.MAX_VALUE, lo2 = Float.MAX_VALUE, hi2 = -Float.MAX_VALUE;
            for (Vector3f p : w) {
                lo1 = Math.min(lo1, p.get(a1)); hi1 = Math.max(hi1, p.get(a1));
                lo2 = Math.min(lo2, p.get(a2)); hi2 = Math.max(hi2, p.get(a2));
            }
            float d = w[0].get(ax);
            Face face = new Face(part, ax, n.get(ax) > 0 ? 1 : -1, d, lo1, hi1, lo2, hi2);
            planes.computeIfAbsent(ax + ":" + Math.round(d * 4000), k -> new ArrayList<>()).add(face);
        }
        //pairs in one plane (to a 4000th, either way they look), overlapping: the smaller part off, its face's way;
        //again while any are still together (three in one plane)
        for (int round = 0; round < 6; round++) {
            boolean moved = false;
            for (var e : planes.entrySet()) {
                List<Face> l = new ArrayList<>(e.getValue());
                String[] k = e.getKey().split(":");
                List<Face> near = planes.get(k[0] + ":" + (Integer.parseInt(k[1]) + 1));
                if (near != null) l.addAll(near);
                for (int i = 0; i < l.size(); i++) for (int j = i + 1; j < l.size(); j++) {
                    Face a = l.get(i), b = l.get(j);
                    if (a.part == b.part || Math.abs(a.plane - b.plane) > 0.0004f) continue;
                    if (Math.min(a.hi1, b.hi1) - Math.max(a.lo1, b.lo1) < 0.005f || Math.min(a.hi2, b.hi2) - Math.max(a.lo2, b.lo2) < 0.005f) continue;
                    Face mover = size(bounds.get(a.part)) <= size(bounds.get(b.part)) ? a : b;
                    Face other = mover == a ? b : a;
                    float[] m = moves.computeIfAbsent(mover.part, x -> new float[3]);
                    float[] o = moves.get(other.part);
                    float om = o == null ? 0 : o[mover.axis];
                    //already apart on that axis
                    if (Math.abs(m[mover.axis] - om) >= HAIR * 0.9f) continue;
                    m[mover.axis] = om + mover.sign * HAIR;
                    moved = true;
                }
            }
            if (!moved) break;
        }
        moves.values().removeIf(m -> m[0] == 0 && m[1] == 0 && m[2] == 0);
        return moves;
    }

    private static float size(float[] b) {
        float x = b[3] - b[0], y = b[4] - b[1], z = b[5] - b[2];
        float[] s = {x, y, z};
        java.util.Arrays.sort(s);
        //a flat part by its area
        return s[0] < 1e-3f ? s[1] * s[2] : x * y * z;
    }
}
