package ebf.tim.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Glass in the windows, as Immersive Railroading has it. Traincraft's windows are openings: no faces there, or
 * see-through texture. Each model is looked at once from its sides and ends, a pixel at a time, as one sees it
 * (through see-through texture); an opening is a patch where the eye goes deep into the model, closed all round by
 * a flat wall (a rail or grille has gaps round its bars, so it is none), of a window's size, over the frame. Each
 * one gets a pane of old Minecraft glass, flush with the wall round it.
 */
public final class GlassPanes {
    private GlassPanes() {}

    /** a pane: its corners in the transport's frame (x along times dir, height over the rails, z across) */
    private record Pane(Vector3f a, Vector3f b, Vector3f c, Vector3f d, float nx, float nz, float u0, float v0, float u1, float v1) {}

    private record Panes(List<Pane> panes, float dir) {}

    private static final Map<Class<?>, Panes> BY_CLASS = new HashMap<>();
    private static final float CELL = 1 / 16f;
    /** off with -Dtc.noglass=true */
    private static final boolean ON = !Boolean.getBoolean("tc.noglass");

    public static void prepare(GenericRailTransport t) {
        if (!ON || BY_CLASS.containsKey(t.getClass()) || t.frontBogie == null) return;
        BY_CLASS.put(t.getClass(), null);
        try {
            BY_CLASS.put(t.getClass(), scan(t));
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger("traincraft").warn("no glass for " + t.getClass().getSimpleName(), e);
        }
    }

    /** a face as seen: corners, texture coordinates, and its texture's pixels (null: untextured, solid) */
    private record Face(Vector3f[] v, float[] uv, Tex tex) {}

    private record Tex(int w, int h, boolean[] clear) {
        boolean clearAt(float u, float v) {
            int x = Math.floorMod((int) Math.floor(u * w), w), y = Math.floorMod((int) Math.floor(v * h), h);
            return clear[y * w + x];
        }
    }

    private static final Map<ResourceLocation, Tex> TEXTURES = new HashMap<>();

    private static Tex tex(ResourceLocation r) {
        if (r == null) return null;
        return TEXTURES.computeIfAbsent(r, k -> {
            try (InputStream in = Minecraft.getInstance().getResourceManager().open(k); NativeImage img = NativeImage.read(in)) {
                int w = img.getWidth(), h = img.getHeight();
                boolean[] c = new boolean[w * h];
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) c[y * w + x] = ((img.getPixelRGBA(x, y) >>> 24) & 0xFF) < 16;
                return new Tex(w, h, c);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static Panes scan(GenericRailTransport t) {
        GenericRailTransport m = (GenericRailTransport) t.getType().create(t.level());
        if (m == null) return null;
        m.setPos(0, 0, 0);
        float[] rp = m.rotationPoints();
        m.frontBogie = new ebf.tim.entities.EntityBogie(m, rp[1], 0, 0, true);
        m.backBogie = new ebf.tim.entities.EntityBogie(m, rp[0], 0, 0, false);
        float railY = 0.3f;
        m.frontBogie.yOffset = m.backBogie.yOffset = railY;
        m.renderData.needsModelUpdate = true;
        var verts = new ArrayList<Vector3f>();
        var corners = new ArrayList<Vector3f[]>();
        var uvs = new ArrayList<float[]>();
        var texs = new ArrayList<ResourceLocation>();
        RenderContext.begin(new PoseStack(), null, 0, null);
        RenderContext.measure = verts;
        RenderContext.measureFaces = corners;
        RenderContext.measureFaceUV = uvs;
        RenderContext.measureFaceTex = texs;
        RenderWagon.measuring = true;
        try {
            RenderWagon.doRender(m, m.getYRot(), 0, railY, false, null);
        } finally {
            RenderWagon.measuring = false;
            RenderContext.measure = null;
            RenderContext.measureFaces = null;
            RenderContext.measureFaceUV = null;
            RenderContext.measureFaceTex = null;
            RenderContext.end();
        }
        if (verts.isEmpty() || uvs.size() != corners.size()) return null;
        List<Face> faces = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            Vector3f[] c = corners.get(i);
            for (Vector3f v : c) v.y -= railY;
            faces.add(new Face(c, uvs.get(i), tex(texs.get(i))));
        }
        float x0 = Float.MAX_VALUE, x1 = -Float.MAX_VALUE, y1 = -Float.MAX_VALUE, z0 = Float.MAX_VALUE, z1 = -Float.MAX_VALUE;
        for (Face f : faces) for (Vector3f v : f.v()) {
            x0 = Math.min(x0, v.x); x1 = Math.max(x1, v.x); y1 = Math.max(y1, v.y); z0 = Math.min(z0, v.z); z1 = Math.max(z1, v.z);
        }
        List<Pane> panes = new ArrayList<>();
        //the sides: looking along z (from +z and from -z), over x (along) and y (up)
        look(faces, 2, 0, x0, x1, 0, y1, +1, z1, panes);
        look(faces, 2, 0, x0, x1, 0, y1, -1, z0, panes);
        //the ends: looking along x, over z and y
        look(faces, 0, 2, z0, z1, 0, y1, +1, x1, panes);
        look(faces, 0, 2, z0, z1, 0, y1, -1, x0, panes);
        if (Boolean.getBoolean("tc.glassdebug")) System.out.println("[TCTEST] glass " + t.getClass().getSimpleName() + " panes " + panes.size());
        return new Panes(panes, rp[1] - rp[0] >= 0 ? 1 : -1);
    }

    /**
     * looks along axis a (0 x, 2 z) from the side sign*infinity at every pixel of the other horizontal axis b and
     * of height, records how deep the eye goes from the outermost surface (from), finds the openings and glazes them
     */
    private static void look(List<Face> faces, int a, int b, float b0, float b1, float h0, float h1, int sign, float from, List<Pane> out) {
        int nb = (int) Math.ceil((b1 - b0) / CELL), nh = (int) Math.ceil((h1 - h0) / CELL);
        if (nb < 4 || nh < 4) return;
        //the faces by the cells their outline covers, so each ray only tries those
        List<List<Integer>> bucket = new ArrayList<>(nb * nh);
        for (int i = 0; i < nb * nh; i++) bucket.add(null);
        for (int f = 0; f < faces.size(); f++) {
            Vector3f[] v = faces.get(f).v();
            float lb = Float.MAX_VALUE, hb = -Float.MAX_VALUE, lh = Float.MAX_VALUE, hh = -Float.MAX_VALUE;
            for (Vector3f p : v) { float pb = comp(p, b); lb = Math.min(lb, pb); hb = Math.max(hb, pb); lh = Math.min(lh, p.y); hh = Math.max(hh, p.y); }
            int ib0 = Math.max(0, (int) Math.floor((lb - b0) / CELL)), ib1 = Math.min(nb - 1, (int) Math.floor((hb - b0) / CELL));
            int ih0 = Math.max(0, (int) Math.floor((lh - h0) / CELL)), ih1 = Math.min(nh - 1, (int) Math.floor((hh - h0) / CELL));
            for (int i = ib0; i <= ib1; i++) for (int j = ih0; j <= ih1; j++) {
                List<Integer> l = bucket.get(i * nh + j);
                if (l == null) bucket.set(i * nh + j, l = new ArrayList<>());
                l.add(f);
            }
        }
        float[] depth = new float[nb * nh];
        for (int i = 0; i < nb; i++) {
            for (int j = 0; j < nh; j++) {
                float pb = b0 + CELL * (i + 0.5f), ph = h0 + CELL * (j + 0.5f);
                List<Integer> l = bucket.get(i * nh + j);
                depth[i * nh + j] = l == null ? Float.POSITIVE_INFINITY : firstSeen(faces, l, a, b, pb, ph, sign, from);
            }
        }
        //a deep cell: deeper than the surface near it (the shallowest within 3 pixels) by more than a frame's depth
        boolean[] deep = new boolean[nb * nh];
        for (int i = 0; i < nb; i++) for (int j = 0; j < nh; j++) {
            float near = Float.POSITIVE_INFINITY;
            for (int di = -3; di <= 3; di++) for (int dj = -3; dj <= 3; dj++) {
                int ii = i + di, jj = j + dj;
                if (ii < 0 || jj < 0 || ii >= nb || jj >= nh) continue;
                near = Math.min(near, depth[ii * nh + jj]);
            }
            deep[i * nh + j] = near != Float.POSITIVE_INFINITY && depth[i * nh + j] > near + 0.14f;
        }
        boolean[] seen = new boolean[nb * nh];
        int[] queue = new int[nb * nh];
        for (int s = 0; s < nb * nh; s++) {
            if (!deep[s] || seen[s]) continue;
            int n = 0, head = 0, minI = nb, maxI = 0, minJ = nh, maxJ = 0;
            boolean closed = true;
            queue[n++] = s;
            seen[s] = true;
            while (head < n) {
                int c = queue[head++], i = c / nh, j = c % nh;
                minI = Math.min(minI, i); maxI = Math.max(maxI, i); minJ = Math.min(minJ, j); maxJ = Math.max(maxJ, j);
                if (i == 0 || j == 0 || i == nb - 1 || j == nh - 1) closed = false;
                int[][] next = {{i + 1, j}, {i - 1, j}, {i, j + 1}, {i, j - 1}};
                for (int[] q : next) {
                    if (q[0] < 0 || q[1] < 0 || q[0] >= nb || q[1] >= nh) continue;
                    int k = q[0] * nh + q[1];
                    if (deep[k] && !seen[k]) { seen[k] = true; queue[n++] = k; }
                }
            }
            int wi = maxI - minI + 1, hj = maxJ - minJ + 1;
            //a window: 3..28 pixels each way, not a slit, over the frame (half a block up), filling its outline
            if (!closed || wi < 3 || hj < 3 || wi > 28 || hj > 28 || Math.max(wi, hj) > 5 * Math.min(wi, hj)
                    || h0 + CELL * minJ < 0.5f || n < 0.55f * wi * hj) continue;
            //the wall round it: the ring 1 to 2 pixels outside it, nearly all at one depth
            List<Float> ring = new ArrayList<>();
            int ringAll = 0;
            for (int i = minI - 2; i <= maxI + 2; i++) for (int j = minJ - 2; j <= maxJ + 2; j++) {
                if (i < 0 || j < 0 || i >= nb || j >= nh) continue;
                int k = i * nh + j;
                if (deep[k] && inPatch(queue, n, k)) continue;
                boolean border = i < minI || i > maxI || j < minJ || j > maxJ;
                if (!border) continue;
                ringAll++;
                if (depth[k] != Float.POSITIVE_INFINITY) ring.add(depth[k]);
            }
            if (ring.size() < 0.9f * ringAll) continue;
            ring.sort(null);
            float wall = ring.get(ring.size() / 2);
            int flat = 0;
            for (float d : ring) if (Math.abs(d - wall) < 0.07f) flat++;
            if (flat < 0.75f * ring.size()) continue;
            //the pane: its pixels, at the wall's depth, a hair inside
            float at = from - sign * (wall + 0.03f);
            //one whole pane of glass over each window, its frame round the window's outline
            for (int q = 0; q < n; q++) {
                int c = queue[q], i = c / nh, j = c % nh;
                float pb0 = b0 + CELL * i, pb1 = pb0 + CELL, ph0 = h0 + CELL * j, ph1 = ph0 + CELL;
                float u0 = (i - minI) / (float) wi, u1 = (i - minI + 1) / (float) wi;
                float v0 = 1 - (j - minJ) / (float) hj, v1 = 1 - (j - minJ + 1) / (float) hj;
                out.add(pane(a, b, at, pb0, pb1, ph0, ph1, sign, u0, v0, u1, v1));
            }
        }
    }

    private static boolean inPatch(int[] queue, int n, int k) {
        for (int q = 0; q < n; q++) if (queue[q] == k) return true;
        return false;
    }

    private static Pane pane(int a, int b, float at, float pb0, float pb1, float ph0, float ph1, int sign, float u0, float v0, float u1, float v1) {
        Vector3f p00 = point(a, b, at, pb0, ph0), p10 = point(a, b, at, pb1, ph0), p11 = point(a, b, at, pb1, ph1), p01 = point(a, b, at, pb0, ph1);
        return new Pane(p00, p10, p11, p01, a == 0 ? sign : 0, a == 2 ? sign : 0, u0, v0, u1, v1);
    }

    private static Vector3f point(int a, int b, float at, float pb, float ph) {
        Vector3f v = new Vector3f(0, ph, 0);
        if (a == 0) { v.x = at; v.z = pb; } else { v.z = at; v.x = pb; }
        return v;
    }

    private static float comp(Vector3f v, int axis) {
        return axis == 0 ? v.x : axis == 1 ? v.y : v.z;
    }

    /** how deep from the outermost surface the eye meets the first face it cannot see through */
    private static float firstSeen(List<Face> faces, List<Integer> which, int a, int b, float pb, float ph, int sign, float from) {
        float best = Float.POSITIVE_INFINITY;
        for (int fi : which) {
            Face f = faces.get(fi);
            Vector3f[] v = f.v();
            for (int i = 1; i + 1 < v.length; i++) {
                Vector3f p = v[0], q = v[i], r = v[i + 1];
                float pB = comp(p, b), qB = comp(q, b), rB = comp(r, b);
                float d = (qB - rB) * (p.y - r.y) - (q.y - r.y) * (pB - rB);
                if (Math.abs(d) < 1e-8f) continue;
                float l1 = ((qB - rB) * (ph - r.y) - (q.y - r.y) * (pb - rB)) / d;
                float l2 = ((rB - pB) * (ph - r.y) + (p.y - r.y) * (pb - rB)) / d;
                float l3 = 1 - l1 - l2;
                if (l1 < -1e-4f || l2 < -1e-4f || l3 < -1e-4f) continue;
                float depth = sign * (from - (l1 * comp(p, a) + l2 * comp(q, a) + l3 * comp(r, a)));
                if (depth >= best || depth < -0.01f) continue;
                if (f.tex() != null) {
                    float[] uv = f.uv();
                    float u = l1 * uv[0] + l2 * uv[2 * i] + l3 * uv[2 * (i + 1)];
                    float w = l1 * uv[1] + l2 * uv[2 * i + 1] + l3 * uv[2 * (i + 1) + 1];
                    if (f.tex().clearAt(u, w)) continue;
                }
                best = depth;
            }
        }
        return best;
    }

    /** the glass of old Minecraft (before 1.14): clear, a white frame and streaks across */
    private static final ResourceLocation GLASS = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/window_glass.png");

    public static void render(GenericRailTransport t, float yaw, float pitch, PoseStack pose, MultiBufferSource buffers, int light) {
        Panes p = BY_CLASS.get(t.getClass());
        if (p == null || p.panes().isEmpty() || t.backBogie == null || t.frontBogie == null) return;
        double r = Math.toRadians(yaw);
        double fx = t.frontBogie.posX - t.backBogie.posX, fz = t.frontBogie.posZ - t.backBogie.posZ;
        float sign = (fx * Math.cos(r) + fz * Math.sin(r)) >= 0 ? 1 : -1;
        pose.pushPose();
        pose.translate(0, t.backBogie.yOffset, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(-pitch));
        float k = sign * p.dir();
        pose.scale(k, 1, k);
        VertexConsumer c = buffers.getBuffer(RenderType.entityTranslucent(GLASS));
        PoseStack.Pose last = pose.last();
        for (Pane q : p.panes()) {
            vertex(c, last, q.a(), q.u0(), q.v0(), q, light);
            vertex(c, last, q.b(), q.u1(), q.v0(), q, light);
            vertex(c, last, q.c(), q.u1(), q.v1(), q, light);
            vertex(c, last, q.d(), q.u0(), q.v1(), q, light);
        }
        pose.popPose();
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose p, Vector3f v, float u, float w, Pane q, int light) {
        c.addVertex(p, v.x, v.y, v.z).setColor(255, 255, 255, 255).setUv(u, w).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, q.nx(), 0, q.nz());
    }
}
