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
    private static final int TW = 512, TH = 128;
    /** the cantilever's reach (to the middle of the track beside the mast) and wire heights, in blocks */
    public static final double REACH = 1.0, CONTACT_Y = 3.03, MESSENGER_Y = 3.98;
    private static ModelRendererTurbo[] parts;

    public RenderCatenary(BlockEntityRendererProvider.Context c) {}

    /** texture columns: steel, concrete, insulator, copper, dark steel, plate */
    private static final int STEEL = 0, CONCRETE = 256, INSULATOR = 288, COPPER = 320, DARK = 352, PLATE = 384;

    private static ModelRendererTurbo box(int u, int v, float x, float y, float z, float w, float h, float d) {
        ModelRendererTurbo m = new ModelRendererTurbo("", u, v, TW, TH);
        m.addBox(x, y, z, w, h, d);
        return m;
    }

    /** a bar of square section t from a to b (pixels), turned to lie along the line between them */
    private static ModelRendererTurbo bar(int u, int v, float ax, float ay, float az, float bx, float by, float bz, float t) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float hl = (float) Math.sqrt(dx * dx + dz * dz), len = (float) Math.sqrt(hl * hl + dy * dy);
        ModelRendererTurbo m = new ModelRendererTurbo("", u, v, TW, TH);
        m.addBox(0, -t / 2, -t / 2, len, t, t);
        m.rotationPointX = ax;
        m.rotationPointY = ay;
        m.rotationPointZ = az;
        m.rotateAngleY = (float) Math.toDegrees(Math.atan2(-dz, dx));
        m.rotateAngleZ = (float) Math.toDegrees(Math.atan2(dy, hl));
        return m;
    }

    /** half the lattice mast's width at a height (pixels): it narrows towards the top, like a pylon */
    private static float half(float y) {
        return 4.5f - 2.3f * (y - 3) / 64f;
    }

    /**
     * in pixels, y up from the block's bottom, the cantilever along +x. The mast is a lattice of steel angles (four
     * corner posts, struts and zig-zag bracing on every face, as the old Soviet line masts); the cantilever a
     * triangle of a top tube and a strut, insulated at the mast, with the registration arm for the contact wire
     */
    private static ModelRendererTurbo[] build() {
        java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>();
        l.add(box(CONCRETE, 0, -6, 0, -6, 12, 3, 12));
        float y0 = 3, y1 = 67;
        int[][] corners = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        for (int[] c : corners) {
            l.add(bar(STEEL, 0, c[0] * half(y0), y0, c[1] * half(y0), c[0] * half(y1), y1, c[1] * half(y1), 1.3f));
        }
        //struts and bracing, panel by panel up the four faces
        int panels = 8;
        for (int k = 0; k <= panels; k++) {
            float y = y0 + (y1 - y0) * k / panels, h = half(y);
            for (int f = 0; f < 4; f++) {
                int[] c0 = corners[f], c1 = corners[(f + 1) % 4];
                l.add(bar(STEEL, 4, c0[0] * h, y, c0[1] * h, c1[0] * h, y, c1[1] * h, 0.7f));
                if (k == panels) continue;
                float yn = y0 + (y1 - y0) * (k + 1) / panels, hn = half(yn);
                boolean flip = (k + f) % 2 == 0;
                int[] from = flip ? c0 : c1, to = flip ? c1 : c0;
                l.add(bar(STEEL, 8, from[0] * h, y, from[1] * h, to[0] * hn, yn, to[1] * hn, 0.6f));
            }
        }
        l.add(box(STEEL, 12, -2.8f, y1, -2.8f, 5.6f, 1.2f, 5.6f));
        //cantilever: insulators at the mast, the top tube out over the track, the strut up to its far end
        float ty = 61.5f, sy = 51.5f, mx = half(ty);
        l.add(bar(INSULATOR, 0, mx, ty, 0, mx + 4.5f, ty, 0, 2.6f));
        l.add(bar(STEEL, 16, mx + 4.5f, ty, 0, 19.5f, ty, 0, 1.4f));
        l.add(bar(INSULATOR, 4, half(sy), sy, 0, half(sy) + 3.6f, sy + 2.2f, 0, 2.4f));
        l.add(bar(STEEL, 20, half(sy) + 3.6f, sy + 2.2f, 0, 17.5f, ty - 0.5f, 0, 1.2f));
        //the messenger's saddle on the tube, over the middle of the track
        l.add(box(DARK, 0, 15, ty + 0.6f, -1, 2, 1.2f, 2));
        //registration arm: from the strut down and out to the contact wire, with its own small insulator
        l.add(bar(INSULATOR, 8, 9, 55.5f, 0, 11, 53.5f, 0, 1.8f));
        l.add(bar(DARK, 4, 11, 53.5f, 0, 16, 48.8f, 0, 0.7f));
        l.add(box(DARK, 8, 15.4f, 47.8f, -0.6f, 1.2f, 1.4f, 1.2f));
        //number plate on the mast, facing the track
        l.add(box(PLATE, 0, half(24) + 0.1f, 22, -1.5f, 0.4f, 4, 3));
        return l.toArray(new ModelRendererTurbo[0]);
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
            for (ModelRendererTurbo p : parts) {
                //TMT leaves a part's rotation on the matrix: each part in its own
                GL11.glPushMatrix();
                p.render(0.0625f);
                GL11.glPopMatrix();
            }
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
                wire(pose, vc, prevM, m, 0.035, DARK, l);
                wire(pose, vc, prevC, c, 0.04, COPPER, l);
            }
            //droppers between them, not right at the masts
            if (i > 0 && i < n && i % 2 == 0) wire(pose, vc, c, m, 0.02, DARK, l);
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
