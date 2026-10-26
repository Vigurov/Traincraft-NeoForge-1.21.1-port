package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import train.blocks.catenary.CatenaryMastBlock;
import train.blocks.catenary.CatenaryMastTile;

/**
 * The catenary mast (TMT boxes, Traincraft's old look) and the spans to the masts it is joined to: the messenger
 * wire sagging between the cantilevers, the contact wire level under it on droppers, swinging a little from side
 * to side (the stagger) so a pantograph wears evenly.
 */
public class RenderCatenary implements BlockEntityRenderer<CatenaryMastTile> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/catenary_mast.png");
    private static final int TW = 256, TH = 128;
    /** the cantilever's reach (to the middle of the track beside the mast) and wire heights, in blocks */
    public static final double REACH = 1.0, CONTACT_Y = 3.03, MESSENGER_Y = 3.9;
    private static ModelRendererTurbo[] parts;

    public RenderCatenary(BlockEntityRendererProvider.Context c) {}

    private static ModelRendererTurbo box(int u, int v, float x, float y, float z, float w, float h, float d) {
        ModelRendererTurbo m = new ModelRendererTurbo("", u, v, TW, TH);
        m.addBox(x, y, z, w, h, d);
        return m;
    }

    /** in pixels, y up from the block's bottom, the cantilever along +x */
    private static ModelRendererTurbo[] build() {
        return new ModelRendererTurbo[]{
                box(64, 0, -4, 0, -4, 8, 3, 8),          //concrete foundation
                box(0, 0, -2, 3, -2, 4, 64, 4),          //mast
                box(0, 72, -3, 67, -3, 6, 2, 6),         //cap
                box(16, 0, -2.5f, 3, -2.5f, 5, 1, 5),    //base flange
                //upper cantilever (the messenger wire's): insulator, then the tube out over the track
                box(96, 0, 2, 59.5f, -1.5f, 5, 3, 3),
                box(0, 82, 7, 60, -1, 13, 2, 2),
                //lower cantilever (the contact wire's registration arm): insulator and tube
                box(96, 12, 2, 46.5f, -1.5f, 5, 3, 3),
                box(0, 88, 7, 47, -1, 13, 2, 2),
                //the stay between them at the far end, and the clamps that hold the wires over the track
                box(160, 0, 18, 49, -0.5f, 1, 11, 1),
                box(160, 16, 15.5f, 62, -1, 1, 1, 2),
                box(160, 20, 15.5f, 45.5f, -1, 1, 1.5f, 2),
                //brackets where the insulators meet the mast
                box(160, 24, 1.5f, 58.5f, -2, 1, 5, 4),
                box(160, 34, 1.5f, 45.5f, -2, 1, 5, 4),
                //number plate
                box(64, 16, 2, 20, -1.5f, 0.5f, 4, 3),
        };
    }

    private static float yaw(Direction d) {
        return switch (d) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    /** where a mast holds its wires, in the world */
    public static Vec3 anchor(BlockPos pos, BlockState state, double height) {
        Direction d = state.hasProperty(CatenaryMastBlock.FACING) ? state.getValue(CatenaryMastBlock.FACING) : Direction.EAST;
        return new Vec3(pos.getX() + 0.5 + d.getStepX() * REACH, pos.getY() + height, pos.getZ() + 0.5 + d.getStepZ() * REACH);
    }

    @Override
    public void render(CatenaryMastTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (parts == null) parts = build();
        BlockState state = tile.getBlockState();
        Direction facing = state.getValue(CatenaryMastBlock.FACING);
        RenderContext.begin(pose, buffers, light, TEX);
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5f, 0, 0.5f);
            GL11.glRotatef(yaw(facing), 0, 1, 0);
            for (ModelRendererTurbo p : parts) p.render(0.0625f);
            GL11.glPopMatrix();
        } finally {
            RenderContext.end();
        }
        //each span once, from the mast with the lower position
        if (tile.getLevel() == null) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        BlockPos pos = tile.getBlockPos();
        for (BlockPos o : tile.links) {
            if (o.asLong() < pos.asLong()) continue;
            BlockState os = tile.getLevel().getBlockState(o);
            if (!(os.getBlock() instanceof CatenaryMastBlock)) continue;
            int light2 = LevelRenderer.getLightColor(tile.getLevel(), o.above(3));
            drawSpan(pose, vc, pos, anchor(pos, state, 0), anchor(o, os, 0), light, light2);
        }
    }

    private static void drawSpan(PoseStack pose, VertexConsumer vc, BlockPos origin, Vec3 a, Vec3 b, int la, int lb) {
        Vec3 base = Vec3.atLowerCornerOf(origin);
        double len = a.distanceTo(b);
        int n = Math.max(4, (int) Math.ceil(len * 2));
        double sag = Math.min(0.45, 0.012 * len * len / 4 + 0.08);
        //the contact wire's stagger: from one side of the track's middle to the other, over a span
        double sx = b.z - a.z, sz = -(b.x - a.x), sl = Math.sqrt(sx * sx + sz * sz);
        sx = sl > 0 ? sx / sl * 0.12 : 0;
        sz = sl > 0 ? sz / sl * 0.12 : 0;
        Vec3 prevM = null, prevC = null;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double x = a.x + (b.x - a.x) * t, z = a.z + (b.z - a.z) * t, y0 = a.y + (b.y - a.y) * t;
            Vec3 m = new Vec3(x, y0 + MESSENGER_Y - 4 * sag * t * (1 - t), z).subtract(base);
            double st = 1 - 2 * t;
            Vec3 c = new Vec3(x + sx * st, y0 + CONTACT_Y, z + sz * st).subtract(base);
            int l = t < 0.5 ? la : lb;
            if (prevM != null) {
                wire(pose, vc, prevM, m, 0.035, 160, l);
                wire(pose, vc, prevC, c, 0.04, 128, l);
            }
            //droppers between them, not right at the masts
            if (i > 0 && i < n && i % 2 == 0) wire(pose, vc, c, m, 0.02, 160, l);
            prevM = m;
            prevC = c;
        }
    }

    /** a thin bar from p to q: two crossed strips, textured from the given column of the sheet */
    private static void wire(PoseStack pose, VertexConsumer vc, Vec3 p, Vec3 q, double w, int u, int light) {
        Vec3 d = q.subtract(p);
        if (d.lengthSqr() < 1e-8) return;
        Vec3 side = d.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1e-8) side = new Vec3(1, 0, 0);
        side = side.normalize().scale(w / 2);
        Vec3 up = d.cross(side).normalize().scale(w / 2);
        strip(pose, vc, p, q, side, u, light);
        strip(pose, vc, p, q, up, u, light);
    }

    private static void strip(PoseStack pose, VertexConsumer vc, Vec3 p, Vec3 q, Vec3 o, int u, int light) {
        Matrix4f m = pose.last().pose();
        float u0 = (u + 2) / (float) TW, u1 = (u + 6) / (float) TW, v0 = 2 / (float) TH, v1 = 6 / (float) TH;
        vertex(vc, m, p.add(o), u0, v0, light);
        vertex(vc, m, p.subtract(o), u1, v0, light);
        vertex(vc, m, q.subtract(o), u1, v1, light);
        vertex(vc, m, q.add(o), u0, v1, light);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, Vec3 v, float u, float tv, int light) {
        vc.addVertex(m, (float) v.x, (float) v.y, (float) v.z).setColor(255, 255, 255, 255).setUv(u, tv)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }

    @Override
    public AABB getRenderBoundingBox(CatenaryMastTile tile) {
        AABB box = new AABB(tile.getBlockPos()).inflate(1.5, 0, 1.5).expandTowards(0, 4.5, 0);
        for (BlockPos o : tile.links) box = box.minmax(new AABB(o).expandTowards(0, 4.5, 0));
        return box;
    }

    @Override
    public boolean shouldRenderOffScreen(CatenaryMastTile tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
