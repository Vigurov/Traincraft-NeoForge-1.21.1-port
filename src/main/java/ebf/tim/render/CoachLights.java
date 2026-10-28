package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ebf.tim.entities.GenericRailTransport;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * The lamps of a coach at night. Real light in the world would light round the coach too; instead, while it is dark
 * where the coach is, the faces inside it (between its floor and ceiling, within its walls: WalkTable's measures,
 * the model's bounds) are drawn as lit by a lamp, and lamps are drawn on the ceiling down its middle, glowing. Its
 * outside stays as dark as it is.
 */
public final class CoachLights {
    private CoachLights() {}

    /** how bright inside: block light 13 of 15 */
    private static final int LAMP = 13;

    /** before the model: the inside to light, or null when it is light out or the coach is not known */
    public static float[] begin(GenericRailTransport t, org.joml.Matrix4f frame) {
        var w = t.walk();
        float[] hull = t.renderData.hull;
        //tests: -Dtc.cut=h leaves out the faces more than h over the rails (the roof off, to see the seats)
        RenderContext.cutY = Float.NaN;
        if (hull != null && System.getProperty("tc.cut") != null) {
            RenderContext.cutY = hull[4] - Float.parseFloat(System.getProperty("tc.cut"));
            RenderContext.interiorFrameInv = new org.joml.Matrix4f(frame).invert();
        }
        if (Boolean.getBoolean("tc.lightdebug") && t.tickCount % 40 == 0) System.out.println("[TCTEST] lights " + t.getClass().getSimpleName() + " walk " + (w != null) + " hull " + (hull != null)
                + " block " + LightTexture.block(RenderContext.light) + " sky " + LightTexture.sky(RenderContext.light) + " day " + (t.level() instanceof net.minecraft.client.multiplayer.ClientLevel cl ? cl.getSkyDarken(1f) : -1));
        if (w == null || hull == null || t.level() == null) return null;
        int block = LightTexture.block(RenderContext.light), sky = LightTexture.sky(RenderContext.light);
        //the daylight now (1 by day, 0.2 at night: what the client lights the world with)
        float day = t.level() instanceof net.minecraft.client.multiplayer.ClientLevel cl ? cl.getSkyDarken(1f) : 1f;
        float seen = Math.max(block, sky * day);
        if (seen >= 9) {
            if (RenderContext.cutY != RenderContext.cutY) RenderContext.interiorFrameInv = null;
            return null;
        }
        //the frame's y points down from the wheels, which are on the rails
        float bottom = hull[4];
        float zc = (hull[2] + hull[5]) / 2, half = (hull[5] - hull[2]) / 2;
        float[] box = {hull[0] + 0.12f, hull[3] - 0.12f, bottom - w.ceil() - 0.16f, bottom - w.floor() + 0.04f, zc - half + 0.035f, zc + half - 0.035f};
        RenderContext.interiorBox = box;
        RenderContext.interiorLight = LightTexture.pack(Math.max(block, LAMP), sky);
        RenderContext.interiorFrameInv = new org.joml.Matrix4f(frame).invert();
        return new float[]{w.minAlong(), w.maxAlong(), bottom - w.ceil() + 0.012f, zc};
    }

    /** after the model, its frame still on: the lamps on the ceiling, and the inside light off again */
    public static void end(float[] lamps) {
        RenderContext.cutY = Float.NaN;
        RenderContext.interiorBox = null;
        RenderContext.interiorLight = -1;
        RenderContext.interiorFrameInv = null;
        if (RenderContext.buffers == null) return;
        VertexConsumer c = RenderContext.buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        var w = RenderContext.white();
        float u = (w.getU0() + w.getU1()) / 2, v = (w.getV0() + w.getV1()) / 2;
        PoseStack.Pose p = RenderContext.pose.last();
        float from = lamps[0] + 0.4f, to = lamps[1] - 0.4f, y = lamps[2], zc = lamps[3], s = 0.09f;
        int n = Math.max(1, Math.round((to - from) / 1.4f) + 1);
        for (int i = 0; i < n; i++) {
            float x = n == 1 ? (from + to) / 2 : from + (to - from) * i / (n - 1);
            float[][] q = {{x - s, zc - s}, {x + s, zc - s}, {x + s, zc + s}, {x - s, zc + s}};
            for (float[] k : q) {
                c.addVertex(p, k[0], y, k[1]).setColor(255, 244, 205, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 1, 0);
            }
        }
    }
}
