package fexcraft.tmt.slim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * What 1.12 kept in OpenGL state, for the TMT models: the matrix stack (GL11.glPushMatrix and friends, see the GL11
 * shim), the bound texture (Tessellator.bindTexture), the colour and whether lighting applies. A renderer calls
 * {@link #begin} before rendering a model and {@link #end} after.
 */
public final class RenderContext {
    public static PoseStack pose = new PoseStack();
    public static MultiBufferSource buffers;
    public static VertexConsumer consumer;
    public static ResourceLocation texture;
    public static int light = LightTexture.FULL_BRIGHT, overlay = OverlayTexture.NO_OVERLAY;
    public static boolean fullBright;
    public static float r = 1, g = 1, b = 1, a = 1;

    private RenderContext() {}

    public static void begin(PoseStack poseStack, MultiBufferSource source, int packedLight, ResourceLocation tex) {
        pose = poseStack;
        buffers = source;
        light = packedLight;
        overlay = OverlayTexture.NO_OVERLAY;
        fullBright = false;
        r = g = b = a = 1;
        bind(tex);
    }

    public static void end() {
        consumer = null;
        buffers = null;
    }

    /** switch texture: a new buffer of the entity render type for it */
    public static void bind(ResourceLocation tex) {
        texture = tex;
        if (buffers != null && tex != null) consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(tex));
    }

    static int lightNow() {
        return fullBright ? LightTexture.FULL_BRIGHT : light;
    }
}
