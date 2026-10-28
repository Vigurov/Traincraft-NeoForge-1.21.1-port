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
    /**the transport being drawn has its lamps on: its "lamp" parts glow (TC 4.5 ModelRenderHelper)*/
    public static boolean lampsOn;
    /**when set, polygons are not drawn: their vertices (in the pose stack's base space) are collected here*/
    public static java.util.List<org.joml.Vector3f> measure;
    /** while measuring: the faces too, each its corners (tests: what a vertical ray down meets) */
    public static java.util.List<org.joml.Vector3f[]> measureFaces;
    /** while measuring: for each face, the y of its normal as drawn (below 0: turned down, unseen from above) */
    public static java.util.List<Float> measureFaceUp;
    /** while measuring: each face's texture coordinates (u, v per corner) and texture (null untextured) */
    public static java.util.List<float[]> measureFaceUV;
    public static java.util.List<ResourceLocation> measureFaceTex;
    /**
     * parts moved a hair (in the transport's frame) off others lying in the same plane, which would flicker; and the
     * frame (tests: applied while measuring too, with unflickerMeasure)
     */
    public static java.util.Map<ModelRendererTurbo, float[]> unflicker;
    public static org.joml.Matrix4f unflickerFrame;
    public static boolean unflickerMeasure;
    /** while measuring: the transport's frame, inverted (set by RenderWagon) */
    public static org.joml.Matrix4f measuredFrameInv;
    /** while measuring: the part of each face */
    public static java.util.List<ModelRendererTurbo> measureFacePart;
    /** parts not drawn (an electric's folded pantograph while it is raised) */
    public static java.util.Set<ModelRendererTurbo> hidden;
    /** parts moved across the transport (its wheels onto the rails), and the transport's frame they are moved in */
    public static java.util.Map<ModelRendererTurbo, Float> shiftZ;
    public static org.joml.Matrix4f shiftFrame;
    /**
     * the inside of a coach lit (its lamps on at night): the faces whose middle is in this box of the transport's
     * frame (x0, x1 along; y0, y1 with y down; z0, z1 across) get this light; the frame's inverse
     */
    public static float[] interiorBox;
    public static int interiorLight = -1;
    public static org.joml.Matrix4f interiorFrameInv;
    /** tests: faces whose middle is over this y of the interior frame (y down) are not drawn; NaN: all are */
    public static float cutY = Float.NaN;
    /** the light for the face being drawn (an inside one lit), or -1 */
    static int faceLight = -1;
    /** the part being drawn */
    public static ModelRendererTurbo currentPart;
    /**
     * set: nothing is drawn, each part's bounds are kept instead (min x, y, z, max x, y, z), in the frame whose
     * inverse is classifyFrame
     */
    public static java.util.Map<ModelRendererTurbo, float[]> classify;
    public static org.joml.Matrix4f classifyFrame;
    /**GL_TEXTURE_2D disabled: draw with the vertex colour on a white part of the block atlas*/
    public static boolean untextured;
    private static ResourceLocation boundBeforeUntextured;
    public static float r = 1, g = 1, b = 1, a = 1;
    /**GL11.glPushMatrix depth, to unwind after a model fails*/
    public static int depth;

    private RenderContext() {}

    public static void begin(PoseStack poseStack, MultiBufferSource source, int packedLight, ResourceLocation tex) {
        pose = poseStack;
        buffers = source;
        light = packedLight;
        overlay = OverlayTexture.NO_OVERLAY;
        fullBright = false;
        untextured = false;
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
        boundBeforeUntextured = null;
        if (buffers != null && tex != null) consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(tex));
    }

    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath("trainsinmotion", "block/white");
    private static net.minecraft.client.renderer.texture.TextureAtlasSprite white;

    /**the white sprite in the block atlas, for untextured drawing*/
    public static net.minecraft.client.renderer.texture.TextureAtlasSprite white() {
        if (white == null || white.atlasLocation() == null) {
            white = net.minecraft.client.Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(WHITE);
        }
        return white;
    }

    /**the consumer for the next polygon: the block atlas while untextured*/
    static VertexConsumer consumerNow() {
        if (untextured && !net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS.equals(texture)) {
            ResourceLocation before = texture;
            bind(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
            boundBeforeUntextured = before;
        } else if (!untextured && boundBeforeUntextured != null) {
            ResourceLocation before = boundBeforeUntextured;
            boundBeforeUntextured = null;
            bind(before);
        }
        return consumer;
    }

    static int lightNow() {
        if (faceLight >= 0 && !fullBright) return faceLight;
        return fullBright ? LightTexture.FULL_BRIGHT : light;
    }
}
