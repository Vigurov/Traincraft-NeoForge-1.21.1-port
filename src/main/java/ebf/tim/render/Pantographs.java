package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Electrics raise their pantographs under a contact wire: the model's own pantograph is left out and one is drawn in
 * its place, folded on the roof, raised with its collector up against the wire; it goes up and down in about a
 * second. Where each
 * locomotive's pantographs are was measured from its model (along it from its middle, the roof's height over the
 * rails, how far along the folded one reaches); the models only have them folded, as Traincraft had no wires.
 */
public final class Pantographs {
    private Pantographs() {}

    enum Kind { DIAMOND, SINGLE_ARM }

    /** one pantograph: along the locomotive from its middle, the roof's height under it, its folded length either way */
    record Panto(float x, float roof, float half, Kind kind, boolean red) {}

    private static final Map<String, Panto[]> DATA = new HashMap<>();

    static {
        put("EntityLocoElectricVL10", new Panto(-0.31f, 2.04f, 0.36f, Kind.DIAMOND, true));
        put("EntityLocoElectricE10_DB", new Panto(-1.94f, 2.02f, 0.56f, Kind.DIAMOND, true), new Panto(1.94f, 2.02f, 0.56f, Kind.DIAMOND, true));
        put("EntityLocoElectricBR_E69", new Panto(0f, 1.98f, 0.6f, Kind.DIAMOND, true));
        put("EntityLocoElectricBR185", new Panto(0.83f, 1.95f, 0.56f, Kind.SINGLE_ARM, false), new Panto(-0.67f, 1.95f, 0.56f, Kind.SINGLE_ARM, false));
        put("EntityLocoElectricCD151", new Panto(-1.42f, 2.12f, 0.36f, Kind.SINGLE_ARM, false), new Panto(1.36f, 2.12f, 0.36f, Kind.SINGLE_ARM, false));
        put("EntityLocoElectricClass85", new Panto(-1.0f, 2.0f, 0.5f, Kind.SINGLE_ARM, false));
        put("EntityLocoElectricE103", new Panto(-1.73f, 1.86f, 0.36f, Kind.SINGLE_ARM, true), new Panto(1.8f, 1.86f, 0.36f, Kind.SINGLE_ARM, true));
        put("EntityLocoElectricHighSpeedZeroED", new Panto(1.23f, 1.79f, 0.42f, Kind.SINGLE_ARM, true));
        put("EntityLocoElectricICE1", new Panto(2.58f, 1.95f, 0.4f, Kind.SINGLE_ARM, false));
    }

    private static void put(String cls, Panto... p) {
        DATA.put(cls, p);
    }

    /** per locomotive: how far up (0 down, 1 at the wire), the model's folded pantograph parts, the wheels' level */
    private static final class State {
        float lift;
        long last;
        Set<ModelRendererTurbo> hidden;
        float bottom;
    }

    private static final Map<GenericRailTransport, State> STATES = new WeakHashMap<>();

    static Panto[] of(GenericRailTransport t) {
        return DATA.get(t.getClass().getSimpleName());
    }

    /** before the model is drawn (in its frame): which parts to leave out; true when this frame only finds them */
    static boolean begin(GenericRailTransport t, Matrix4f frameInverse) {
        Panto[] ps = of(t);
        if (ps == null) return false;
        State s = STATES.computeIfAbsent(t, k -> new State());
        float target = t.entityData().get(GenericRailTransport.WIRE_HEIGHT) > 0 ? 1 : 0;
        long now = System.currentTimeMillis();
        float dt = s.last == 0 ? 0 : Math.min(0.2f, (now - s.last) / 1000f);
        s.last = now;
        //up or down in about a second
        s.lift += Math.copySign(Math.min(Math.abs(target - s.lift), dt * 1.1f), target - s.lift);
        //the model's own (folded) pantograph is never drawn: this one stands in for it, folded down on the roof too,
        //so it is the same one going up and down (it was swapped for the model's when down)
        if (s.hidden == null) {
            RenderContext.classify = new HashMap<>();
            RenderContext.classifyFrame = frameInverse;
            return true;
        }
        RenderContext.hidden = s.hidden;
        return false;
    }

    /** after a frame that only measured the parts: the folded pantographs' ones */
    static void classified(GenericRailTransport t) {
        State s = STATES.get(t);
        Map<ModelRendererTurbo, float[]> boxes = RenderContext.classify;
        RenderContext.classify = null;
        RenderContext.classifyFrame = null;
        if (s == null || boxes == null) return;
        float bottom = -Float.MAX_VALUE;
        for (float[] b : boxes.values()) bottom = Math.max(bottom, b[4]);
        s.bottom = bottom;
        Set<ModelRendererTurbo> hide = new HashSet<>();
        //the frame's y points down: a height h over the wheels is bottom - h
        for (var e : boxes.entrySet()) {
            if (e.getKey() == null) continue;
            float[] b = e.getValue();
            for (Panto p : of(t)) {
                boolean along = b[0] >= p.x() - p.half() - 0.05f && b[3] <= p.x() + p.half() + 0.05f;
                boolean overRoof = b[4] <= bottom - (p.roof() - 0.08f) && b[1] >= bottom - (p.roof() + 0.95f);
                if (along && overRoof) hide.add(e.getKey());
            }
        }
        s.hidden = hide;
        if (debug) {
            float roof = of(t)[0].roof();
            float topMost = Float.MAX_VALUE;
            for (float[] b : boxes.values()) topMost = Math.min(topMost, b[1]);
            System.out.println(String.format("[TCTEST] panto %s classified %d parts (%d without a part), bottom %.2f, height %.2f, hidden %d",
                    t.getClass().getSimpleName(), boxes.size(), boxes.containsKey(null) ? 1 : 0, bottom, bottom - topMost, hide.size()));
            for (var e : boxes.entrySet()) {
                float[] b = e.getValue();
                if (b[4] > bottom - (roof - 0.3f)) continue;
                System.out.println(String.format("[TCTEST] panto %s part x %.2f..%.2f h %.2f..%.2f hidden=%s", t.getClass().getSimpleName(),
                        b[0], b[3], bottom - b[4], bottom - b[1], hide.contains(e.getKey())));
            }
        }
    }

    /** the autotest's pantolog=1: the parts over the roof, hidden or not */
    public static boolean debug;

    static void end() {
        RenderContext.hidden = null;
    }

    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/catenary_mast.png");
    private static final int TW = 512, TH = 128, RED = 464, DARK = 496;

    /** the raised pantographs, in the model's frame (y down from the wheels' level) */
    static void draw(GenericRailTransport t) {
        Panto[] ps = of(t);
        State s = ps == null ? null : STATES.get(t);
        if (s == null || s.hidden == null || RenderContext.buffers == null) return;
        float wire = t.entityData().get(GenericRailTransport.WIRE_HEIGHT);
        if (wire <= 0) wire = 3.03f;
        VertexConsumer vc = RenderContext.buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        PoseStack pose = RenderContext.pose;
        for (Panto p : ps) {
            int u = p.red() ? RED : DARK;
            float roof = p.roof();
            //the collector's height: from just over the roof up to the underside of the wire
            float top = roof + 0.18f + s.lift * (wire - 0.03f - roof - 0.18f);
            float y0 = s.bottom - roof, yt = s.bottom - top;
            float x = p.x();
            //base frame on the roof, on its insulators
            for (float z : new float[]{-0.24f, 0.24f}) bar(pose, vc, x - 0.32f, y0, z, x + 0.32f, y0, z, 0.05f, DARK, t);
            bar(pose, vc, x - 0.32f, y0, -0.24f, x - 0.32f, y0, 0.24f, 0.05f, DARK, t);
            bar(pose, vc, x + 0.32f, y0, -0.24f, x + 0.32f, y0, 0.24f, 0.05f, DARK, t);
            if (p.kind() == Kind.DIAMOND) {
                //a rhombus each side: from the frame's middle up to the knees and in again to the collector
                float arm = 0.58f, d = top - roof, w = d >= 2 * arm ? 0.02f : (float) Math.sqrt(arm * arm - d * d / 4);
                float ym = s.bottom - (roof + d / 2);
                for (float z : new float[]{-0.22f, 0.22f}) {
                    bar(pose, vc, x, y0, z, x - w, ym, z, 0.05f, u, t);
                    bar(pose, vc, x, y0, z, x + w, ym, z, 0.05f, u, t);
                    bar(pose, vc, x - w, ym, z, x, yt, z, 0.05f, u, t);
                    bar(pose, vc, x + w, ym, z, x, yt, z, 0.05f, u, t);
                }
                bar(pose, vc, x - w, ym, -0.22f, x - w, ym, 0.22f, 0.03f, u, t);
                bar(pose, vc, x + w, ym, -0.22f, x + w, ym, 0.22f, 0.03f, u, t);
            } else {
                //one arm: from the hinge at the frame's end up to the knee and on to the collector. Its two halves
                //are as long as each other; folded they lie along the roof, the collector at the far end of the
                //frame, and rising it comes back over the frame
                float arm = 0.62f, hx = x - 0.5f, hy = roof + 0.04f;
                float dy = top - hy, reach = 1.8f * arm;
                float dx = (float) Math.sqrt(Math.max(0.0025f, reach * reach - dy * dy));
                float cx = hx + dx;
                float dist = (float) Math.sqrt(dx * dx + dy * dy), half = dist / 2;
                float len = Math.max(arm, half + 0.01f), hgt = (float) Math.sqrt(Math.max(0, len * len - half * half));
                float mx = hx + dx / 2, my = hy + dy / 2;
                //the knee above the line from hinge to collector
                float kx = mx - dy / dist * hgt, ky = my + dx / dist * hgt;
                for (float z : new float[]{-0.14f, 0.14f}) bar(pose, vc, hx, s.bottom - hy, z, kx, s.bottom - ky, z * 0.4f, 0.055f, u, t);
                bar(pose, vc, kx, s.bottom - ky, 0, cx, yt, 0, 0.05f, u, t);
                x = cx;
            }
            //the collector: two strips across, with horns bent down at the ends
            for (float dxs : new float[]{-0.07f, 0.07f}) {
                bar(pose, vc, x + dxs, yt - 0.02f, -0.48f, x + dxs, yt - 0.02f, 0.48f, 0.035f, DARK, t);
                bar(pose, vc, x + dxs, yt - 0.02f, 0.48f, x + dxs, yt + 0.06f, 0.6f, 0.03f, DARK, t);
                bar(pose, vc, x + dxs, yt - 0.02f, -0.48f, x + dxs, yt + 0.06f, -0.6f, 0.03f, DARK, t);
            }
            bar(pose, vc, x - 0.07f, yt, 0, x + 0.07f, yt, 0, 0.04f, DARK, t);
        }
    }

    /** a bar of square section from a to b: its four sides */
    private static void bar(PoseStack pose, VertexConsumer vc, float ax, float ay, float az, float bx, float by, float bz, float w, int u,
                            GenericRailTransport t) {
        Vector3f d = new Vector3f(bx - ax, by - ay, bz - az);
        if (d.lengthSquared() < 1e-8f) return;
        Vector3f s1 = new Vector3f(d).cross(Math.abs(d.y) > 0.9f * d.length() ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0)).normalize(w / 2);
        Vector3f s2 = new Vector3f(d).cross(s1).normalize(w / 2);
        Vector3f a = new Vector3f(ax, ay, az), b = new Vector3f(bx, by, bz);
        quad(pose, vc, a, b, s1, s2, u);
        quad(pose, vc, a, b, s2, new Vector3f(s1).negate(), u);
        quad(pose, vc, a, b, new Vector3f(s1).negate(), new Vector3f(s2).negate(), u);
        quad(pose, vc, a, b, new Vector3f(s2).negate(), s1, u);
    }

    private static void quad(PoseStack pose, VertexConsumer vc, Vector3f a, Vector3f b, Vector3f o1, Vector3f o2, int u) {
        Matrix4f m = pose.last().pose();
        float u0 = (u + 2) / (float) TW, u1 = (u + 8) / (float) TW, v0 = 2 / (float) TH, v1 = 10 / (float) TH;
        Vector3f n = new Vector3f(o1).add(o2).normalize();
        vertex(vc, m, pose, new Vector3f(a).add(o1), u0, v0, n);
        vertex(vc, m, pose, new Vector3f(b).add(o1), u1, v0, n);
        vertex(vc, m, pose, new Vector3f(b).add(o2), u1, v1, n);
        vertex(vc, m, pose, new Vector3f(a).add(o2), u0, v1, n);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, PoseStack pose, Vector3f p, float u, float v, Vector3f n) {
        Vector3f nn = pose.last().normal().transform(new Vector3f(n));
        vc.addVertex(m, p.x, p.y, p.z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(RenderContext.light).setNormal(nn.x, nn.y, nn.z);
    }
}
