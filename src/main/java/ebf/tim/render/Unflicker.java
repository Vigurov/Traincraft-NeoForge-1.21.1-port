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
 * each such pair the smaller part is moved a hair off the plane, the way its face looks, so it is in front. And
 * the side walls are made thin. Each part's change: {move x, y, z} or {move x, y, z, outer z, squeeze across}.
 */
public final class Unflicker {
    private Unflicker() {}

    /** how far a part is moved off, in blocks: too little to see, enough for the depth buffer some way off */
    public static final float HAIR = 0.003f;
    /** how thick a side wall is made */
    public static final float THIN = 1 / 64f;

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
            planes.computeIfAbsent(ax + ":" + Math.round(d * 1000), k -> new ArrayList<>()).add(face);
        }
        //pairs in one plane (or within 0.002 of it: as bad), either way they look, overlapping: the smaller part off, its face's way;
        //again while any are still together (three in one plane)
        for (int round = 0; round < 6; round++) {
            boolean moved = false;
            for (var e : planes.entrySet()) {
                List<Face> l = new ArrayList<>(e.getValue());
                String[] k = e.getKey().split(":");
                for (int dk = 1; dk <= 2; dk++) {
                    List<Face> near = planes.get(k[0] + ":" + (Integer.parseInt(k[1]) + dk));
                    if (near != null) l.addAll(near);
                }
                for (int i = 0; i < l.size(); i++) for (int j = i + 1; j < l.size(); j++) {
                    Face a = l.get(i), b = l.get(j);
                    if (a.part == b.part || Math.abs(a.plane - b.plane) > 0.002f) continue;
                    if (Math.min(a.hi1, b.hi1) - Math.max(a.lo1, b.lo1) < 0.005f || Math.min(a.hi2, b.hi2) - Math.max(a.lo2, b.lo2) < 0.005f) continue;
                    Face mover = size(bounds.get(a.part)) <= size(bounds.get(b.part)) ? a : b;
                    Face other = mover == a ? b : a;
                    float[] m = moves.computeIfAbsent(mover.part, x -> new float[3]);
                    float[] o = moves.get(other.part);
                    float om = o == null ? 0 : o[mover.axis];
                    //already apart on that axis (as moved, from where they were)
                    float gap = (mover.plane + m[mover.axis]) - (other.plane + om);
                    if (gap * mover.sign >= HAIR * 0.9f) continue;
                    m[mover.axis] = other.plane + om + mover.sign * HAIR - mover.plane;
                    moved = true;
                }
            }
            if (!moved) break;
        }
        //the side walls: thin boxes the length of the car at its sides, made a 64th thick from their outside (a
        //sixteenth or more apart, inside and out, one sees the one from the other, a camera gets between them)
        float half = 0;
        for (float[] b : bounds.values()) half = Math.max(half, Math.max(-b[2], b[5]));
        //each wall: x from, to, its inside before and after
        List<float[]> left = new ArrayList<>(), right = new ArrayList<>();
        java.util.Set<ModelRendererTurbo> walls = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        for (var e : bounds.entrySet()) {
            float[] b = e.getValue();
            float tz = b[5] - b[2];
            boolean side = b[2] > half - 0.2f || b[5] < -(half - 0.2f);
            if (!side || tz < 0.03f || tz > 0.13f || b[3] - b[0] < 0.4f || b[4] - b[1] < 0.1f) continue;
            boolean onRight = b[2] > 0;
            float outer = onRight ? b[5] : b[2];
            float[] m = moves.getOrDefault(e.getKey(), new float[3]);
            moves.put(e.getKey(), new float[]{m[0], m[1], m[2], outer, THIN / tz});
            walls.add(e.getKey());
            if (onRight) right.add(new float[]{b[0], b[3], b[2], outer - THIN});
            else left.add(new float[]{b[0], b[3], b[5], outer + THIN});
        }
        //what met a wall's inside (the floor, the ceiling, seats, frames) reaches its new inside
        for (var e : bounds.entrySet()) {
            if (walls.contains(e.getKey())) continue;
            float[] b = e.getValue();
            float lo = b[2], hi = b[5], nlo = lo, nhi = hi;
            for (float[] w : left) if (Math.abs(lo - w[2]) < 0.012f && Math.min(b[3], w[1]) - Math.max(b[0], w[0]) > 0.01f) nlo = Math.min(nlo, w[3]);
            for (float[] w : right) if (Math.abs(hi - w[2]) < 0.012f && Math.min(b[3], w[1]) - Math.max(b[0], w[0]) > 0.01f) nhi = Math.max(nhi, w[3]);
            if (nlo == lo && nhi == hi) continue;
            float[] m = moves.getOrDefault(e.getKey(), new float[3]);
            if (hi - lo < 1e-4f) {
                //flat against it: moved there
                moves.put(e.getKey(), new float[]{m[0], m[1], m[2] + (nlo != lo ? nlo - lo : nhi - hi)});
                continue;
            }
            //stretched across: lo..hi onto nlo..nhi, as z' = p + (z - p) * f
            float f = (nhi - nlo) / (hi - lo);
            if (Math.abs(1 - f) < 1e-6f) continue;
            float p = (nlo - lo * f) / (1 - f);
            moves.put(e.getKey(), new float[]{m[0], m[1], m[2], p, f});
        }
        moves.values().removeIf(m -> m[0] == 0 && m[1] == 0 && m[2] == 0 && m.length < 5);
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
