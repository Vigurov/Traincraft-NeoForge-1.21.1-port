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
        List<Window> found = new ArrayList<>();
        //the sides: looking along z (from +z and from -z), over x (along) and y (up)
        look(faces, 2, 0, x0, x1, 0, y1, +1, z1, found);
        look(faces, 2, 0, x0, x1, 0, y1, -1, z0, found);
        //the ends: looking along x, over z and y
        look(faces, 0, 2, z0, z1, 0, y1, +1, x1, found);
        look(faces, 0, 2, z0, z1, 0, y1, -1, x0, found);
        //one pane per window: the same window is found in a wall's outer and inner face, and from both sides; the
        //outermost one is kept
        float cx = (x0 + x1) / 2, cz = (z0 + z1) / 2;
        //the wall a window is in: of its planes, the largest (a stripe or rail standing out of the wall is a small
        //plane, and a pane in it would float in front of the wall)
        found.sort((p, q) -> q.size() != p.size() ? Integer.compare(q.size(), p.size())
                : Float.compare(Math.abs(q.at() - (q.a() == 0 ? cx : cz)), Math.abs(p.at() - (p.a() == 0 ? cx : cz))));
        List<Pane> panes = new ArrayList<>();
        List<Window> kept = new ArrayList<>();
        for (Window w : found) {
            boolean dup = false;
            for (Window k : kept) {
                //the same window: the same outline, on the same side of the car
                boolean side = Math.signum(w.at() - (w.a() == 0 ? cx : cz)) == Math.signum(k.at() - (k.a() == 0 ? cx : cz));
                if (k.a() == w.a() && side && w.overlap(k) > 0.5f) { dup = true; break; }
            }
            if (dup) continue;
            kept.add(w);
            panes.addAll(w.panes());
        }
        if (Boolean.getBoolean("tc.glassdebug")) System.out.println("[TCTEST] glass " + t.getClass().getSimpleName() + " panes " + panes.size() + " windows " + kept.size());
        return new Panes(panes, rp[1] - rp[0] >= 0 ? 1 : -1);
    }

    /**
     * looks through the model along axis a (0 x, 2 z), at every pixel of the other horizontal axis b and of height,
     * and keeps every surface it passes (not see-through texture there) by its depth. The walls are the depths with
     * much surface; in each wall's plane a window is a hole: empty, closed all round by the wall at least two pixels
     * thick (a rail or grille has thin bars), of a window's size, above the frame. What stands in front of a wall
     * (a hood, rails) does not matter: each wall is looked at by itself
     */
    private static void look(List<Face> faces, int a, int b, float b0, float b1, float h0, float h1, int sign, float from, List<Window> out) {
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
        //every surface on every ray, by depth in 64ths of a block
        float step = 1 / 64f;
        List<int[]> hits = new ArrayList<>();
        Map<Integer, Integer> histogram = new java.util.TreeMap<>();
        for (int i = 0; i < nb; i++) {
            for (int j = 0; j < nh; j++) {
                List<Integer> l = bucket.get(i * nh + j);
                if (l == null) continue;
                float pb = b0 + CELL * (i + 0.5f), ph = h0 + CELL * (j + 0.5f);
                java.util.Set<Integer> here = new java.util.TreeSet<>();
                allSeen(faces, l, a, b, pb, ph, sign, from, step, here);
                for (int d : here) {
                    hits.add(new int[]{i * nh + j, d});
                    histogram.merge(d, 1, Integer::sum);
                }
            }
        }
        if (System.getProperty("tc.glassmap") != null) {
            //tests: the depth of the first surface on each ray, as a grey picture (white near, black none)
            try (java.io.PrintWriter w = new java.io.PrintWriter(System.getProperty("tc.glassmap") + "/axis" + a + (sign > 0 ? "p" : "m") + ".pgm")) {
                int[] first = new int[nb * nh];
                java.util.Arrays.fill(first, Integer.MAX_VALUE);
                for (int[] h : hits) first[h[0]] = Math.min(first[h[0]], h[1]);
                w.println("P2 " + nb + " " + nh + " 255");
                for (int j = nh - 1; j >= 0; j--) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 0; i < nb; i++) row.append(first[i * nh + j] == Integer.MAX_VALUE ? 0 : Math.max(20, 255 - first[i * nh + j] * 2)).append(' ');
                    w.println(row);
                }
            } catch (java.io.IOException ignored) {
            }
        }
        //the walls: depths with a large flat surface
        List<Integer> walls = new ArrayList<>();
        for (var e : histogram.entrySet()) {
            int d = e.getKey(), c = e.getValue();
            if (c < 40) continue;
            if (c < histogram.getOrDefault(d - 1, 0) || c < histogram.getOrDefault(d + 1, 0)) continue;
            if (!walls.isEmpty() && d - walls.get(walls.size() - 1) <= 1) continue;
            walls.add(d);
        }
        if (Boolean.getBoolean("tc.glassdebug")) System.out.println("[TCTEST] walls axis " + a + ": " + walls.stream().map(w -> String.format(java.util.Locale.ROOT, "%.2f(%d)", from - sign * (w + 0.5f) * step, histogram.get(w))).toList());
        //each wall's plane, taking what is up to 10 steps behind it as the same wall (a frame inset, a slanting
        //windscreen's); and what lies just behind that, to 20 steps (0.3 blocks): a window shows the inside further
        //back, a door or panel set into the wall is right there
        for (int wall : walls) {
            boolean[] in = new boolean[nb * nh], close = new boolean[nb * nh], front = new boolean[nb * nh];
            int[] frame = new int[nb * nh];
            java.util.Arrays.fill(frame, Integer.MIN_VALUE);
            for (int[] h : hits) {
                int dd = h[1] - wall;
                //the wall, a frame a little inset, and whatever stands close in front (a roof over the windscreen)
                if (dd >= -20 && dd <= 10) in[h[0]] = true;
                //the wall itself here (a slanting frame's depth changes across it)
                if (dd >= -4 && dd <= 10 && (frame[h[0]] == Integer.MIN_VALUE || Math.abs(dd) < Math.abs(frame[h[0]] - wall))) frame[h[0]] = h[1];
                if (dd >= 11 && dd <= 20) close[h[0]] = true;
                //anything solid between the eye and the wall: the window is not seen from here
                if (dd < -1) front[h[0]] = true;
            }
            //the pane set into the wall by a third of a pixel: what of it is off the window hides inside the wall,
            //only what the opening shows is seen
            holes(in, close, front, frame, step, from, nb, nh, b0, h0, a, b, from - sign * (wall + 1.5f) * step, sign, histogram.get(wall), out);
        }
    }

    /** a window found in a wall's plane: its outline (cells) and its panes */
    private record Window(int a, float at, int size, float bLo, float bHi, float hLo, float hHi, List<Pane> panes) {
        float overlap(Window o) {
            float wb = Math.min(bHi, o.bHi) - Math.max(bLo, o.bLo), wh = Math.min(hHi, o.hHi) - Math.max(hLo, o.hLo);
            if (wb <= 0 || wh <= 0) return 0;
            float area = Math.min((bHi - bLo) * (hHi - hLo), (o.bHi - o.bLo) * (o.hHi - o.hLo));
            return wb * wh / area;
        }
    }

    /** the holes of one wall's plane that are windows */
    private static void holes(boolean[] in, boolean[] close, boolean[] front, int[] frame, float step, float from, int nb, int nh, float b0, float h0, int a, int b, float at, int sign, int size, List<Window> out) {
        boolean[] seen = new boolean[nb * nh];
        int[] queue = new int[nb * nh];
        for (int s = 0; s < nb * nh; s++) {
            if (in[s] || seen[s]) continue;
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
                    if (!in[k] && !seen[k]) { seen[k] = true; queue[n++] = k; }
                }
            }
            int wi = maxI - minI + 1, hj = maxJ - minJ + 1;
            boolean dbg = Boolean.getBoolean("tc.glassdebug") && n >= 6 && n < 2000;
            //a door: much taller than wide and tall as a man (its glass, if any, is the model's own)
            boolean door = hj > 2.2f * wi && hj * CELL > 0.6f;
            if (!closed || door || wi < 3 || hj < 3 || wi > 30 || hj > 30 || Math.max(wi, hj) > 6 * Math.min(wi, hj)
                    || h0 + CELL * minJ < 0.45f || n < 0.5f * wi * hj) {
                if (dbg) System.out.println(String.format(java.util.Locale.ROOT, "[TCTEST] hole axis %d at %.2f b %.2f..%.2f h %.2f..%.2f n %d: shape closed=" + closed + "", a, at, b0 + CELL * minI, b0 + CELL * (maxI + 1), h0 + CELL * minJ, h0 + CELL * (maxJ + 1), n));
                continue;
            }
            //the wall round it two pixels thick, nearly whole (a rail's gaps have one-pixel bars round them)
            boolean[] patch = new boolean[nb * nh];
            for (int q = 0; q < n; q++) patch[queue[q]] = true;
            int ring = 0, wall = 0;
            for (int i = minI - 2; i <= maxI + 2; i++) for (int j = minJ - 2; j <= maxJ + 2; j++) {
                if (i < 0 || j < 0 || i >= nb || j >= nh) { ring++; continue; }
                int k = i * nh + j;
                if (patch[k]) continue;
                boolean near = false;
                for (int di = -2; di <= 2 && !near; di++) for (int dj = -2; dj <= 2 && !near; dj++) {
                    int ii = i + di, jj = j + dj;
                    if (ii >= 0 && jj >= 0 && ii < nb && jj < nh && patch[ii * nh + jj]) near = true;
                }
                if (!near) continue;
                ring++;
                if (in[k]) wall++;
            }
            int shallow = 0, hidden = 0;
            for (int q = 0; q < n; q++) {
                if (close[queue[q]]) shallow++;
                if (front[queue[q]]) hidden++;
            }
            if (hidden > 0.25f * n) {
                if (dbg) System.out.println(String.format(java.util.Locale.ROOT, "[TCTEST] hole axis %d at %.2f b %.2f..%.2f h %.2f..%.2f n %d: behind something %d", a, at, b0 + CELL * minI, b0 + CELL * (maxI + 1), h0 + CELL * minJ, h0 + CELL * (maxJ + 1), n, hidden));
                continue;
            }
            if (shallow > 0.3f * n) {
                if (dbg) System.out.println(String.format(java.util.Locale.ROOT, "[TCTEST] hole axis %d at %.2f b %.2f..%.2f h %.2f..%.2f n %d: inset %d", a, at, b0 + CELL * minI, b0 + CELL * (maxI + 1), h0 + CELL * minJ, h0 + CELL * (maxJ + 1), n, shallow));
                continue;
            }
            if (wall < 0.8f * ring) {
                if (dbg) System.out.println(String.format(java.util.Locale.ROOT, "[TCTEST] hole axis %d at %.2f b %.2f..%.2f h %.2f..%.2f n %d: ring %d/%d", a, at, b0 + CELL * minI, b0 + CELL * (maxI + 1), h0 + CELL * minJ, h0 + CELL * (maxJ + 1), n, wall, ring));
                continue;
            }
            if (dbg) System.out.println(String.format(java.util.Locale.ROOT, "[TCTEST] hole axis %d at %.2f b %.2f..%.2f h %.2f..%.2f n %d: GLASS", a, at, b0 + CELL * minI, b0 + CELL * (maxI + 1), h0 + CELL * minJ, h0 + CELL * (maxJ + 1), n));
            //the window's slope from its frame: a plane depth = c0 + c1 * i + c2 * j fitted to the wall round it (a
            //windscreen leans back), the pane in it, set a third of a pixel into the wall
            double[][] m = new double[3][3];
            double[] r = new double[3];
            int fitted = 0;
            for (int i = minI - 2; i <= maxI + 2; i++) for (int j = minJ - 2; j <= maxJ + 2; j++) {
                if (i < 0 || j < 0 || i >= nb || j >= nh) continue;
                int k = i * nh + j;
                if (patch[k] || frame[k] == Integer.MIN_VALUE) continue;
                double[] x = {1, i, j};
                for (int u = 0; u < 3; u++) { r[u] += x[u] * frame[k]; for (int v = 0; v < 3; v++) m[u][v] += x[u] * x[v]; }
                fitted++;
            }
            double[] c = fitted >= 6 ? solve(m, r) : null;
            List<Pane> panes = new ArrayList<>();
            //the glass: where the window shows deep inside (not over a slanting corner or anything just behind)
            for (int q = 0; q < n; q++) {
                int cell = queue[q], i = cell / nh, j = cell % nh;
                if (close[cell] || front[cell]) continue;
                float pb0 = b0 + CELL * i, pb1 = pb0 + CELL, ph0 = h0 + CELL * j, ph1 = ph0 + CELL;
                //one pane of old Minecraft glass over the whole window, its frame round the window's outline
                float u0 = (i - minI) / (float) wi, u1 = (i - minI + 1) / (float) wi;
                float v0 = 1 - (j - minJ) / (float) hj, v1 = 1 - (j - minJ + 1) / (float) hj;
                if (c == null) {
                    panes.add(pane(a, b, at, pb0, pb1, ph0, ph1, sign, u0, v0, u1, v1));
                } else {
                    //each corner at the plane's depth there
                    float d00 = depthAt(c, i, j, from, step, sign), d10 = depthAt(c, i + 1, j, from, step, sign);
                    float d11 = depthAt(c, i + 1, j + 1, from, step, sign), d01 = depthAt(c, i, j + 1, from, step, sign);
                    panes.add(new Pane(point(a, b, d00, pb0, ph0), point(a, b, d10, pb1, ph0), point(a, b, d11, pb1, ph1), point(a, b, d01, pb0, ph1),
                            a == 0 ? sign : 0, a == 2 ? sign : 0, u0, v0, u1, v1));
                }
            }
            out.add(new Window(a, at, size, b0 + CELL * minI, b0 + CELL * (maxI + 1), h0 + CELL * minJ, h0 + CELL * (maxJ + 1), panes));
        }
    }

    /** the depths (in steps from the outermost surface) of every face the eye passes and cannot see through */
    private static void allSeen(List<Face> faces, List<Integer> which, int a, int b, float pb, float ph, int sign, float from, float step, java.util.Set<Integer> out) {
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
                if (f.tex() != null) {
                    float[] uv = f.uv();
                    float u = l1 * uv[0] + l2 * uv[2 * i] + l3 * uv[2 * (i + 1)];
                    float w = l1 * uv[1] + l2 * uv[2 * i + 1] + l3 * uv[2 * (i + 1) + 1];
                    if (f.tex().clearAt(u, w)) break;
                }
                float depth = sign * (from - (l1 * comp(p, a) + l2 * comp(q, a) + l3 * comp(r, a)));
                out.add((int) Math.floor(depth / step));
                break;
            }
        }
    }

    /** the fitted plane's position along the eye's axis at a cell corner (i, j), a third of a pixel into the wall */
    private static float depthAt(double[] c, int i, int j, float from, float step, int sign) {
        double steps = c[0] + c[1] * (i - 0.5) + c[2] * (j - 0.5) + 1.5;
        return (float) (from - sign * steps * step);
    }

    /** a 3x3 linear system, or null when it has no single answer */
    private static double[] solve(double[][] m, double[] r) {
        double[][] a = {m[0].clone(), m[1].clone(), m[2].clone()};
        double[] b = r.clone();
        for (int col = 0; col < 3; col++) {
            int piv = col;
            for (int row = col + 1; row < 3; row++) if (Math.abs(a[row][col]) > Math.abs(a[piv][col])) piv = row;
            if (Math.abs(a[piv][col]) < 1e-9) return null;
            double[] t = a[col]; a[col] = a[piv]; a[piv] = t;
            double tb = b[col]; b[col] = b[piv]; b[piv] = tb;
            for (int row = 0; row < 3; row++) {
                if (row == col) continue;
                double f = a[row][col] / a[col][col];
                for (int k = col; k < 3; k++) a[row][k] -= f * a[col][k];
                b[row] -= f * b[col];
            }
        }
        return new double[]{b[0] / a[0][0], b[1] / a[1][1], b[2] / a[2][2]};
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

    /** the glass of old Minecraft (before 1.14): clear with streaks across (its white frame is not used) */
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
