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
 * wire sagging between the cantilevers, the contact wire level under it on droppers, in the clamps of the
 * registration arms.
 */
public class RenderCatenary implements BlockEntityRenderer<CatenaryMastTile> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/catenary_mast.png");
    public static final ResourceLocation TEXTURE = TEX;
    private static final int TW = 512, TH = 128;
    private static final double CONTACT_Y = train.blocks.catenary.CatenaryGeometry.CONTACT_Y, MESSENGER_Y = train.blocks.catenary.CatenaryGeometry.MESSENGER_Y;
    private static ModelRendererTurbo[] parts, glass, feederParts, feederGlass;

    public RenderCatenary(BlockEntityRendererProvider.Context c) {}

    /** texture columns: steel, concrete, insulator, copper, dark steel */
    private static final int STEEL = 0, CONCRETE = 256, INSULATOR = 320, COPPER = 352, DARK = 384, GLASS = 432;
    /** the steel-cored aluminium of feeders and power lines (bare, as overhead lines are) */
    public static final int ALUMINIUM = 416;

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

    /** the glass discs of the insulator strings, drawn see-through after the rest */
    private static final java.util.List<ModelRendererTurbo> GLASS_PARTS = new java.util.ArrayList<>();

    /**
     * a string of glass disc insulators (the Soviet tempered glass ones, bluish green) from (ax, ay) to (bx, by) in
     * the cantilever's plane: a pin, then a disc and a cap for each
     */
    private static void insulatorString(java.util.List<ModelRendererTurbo> l, float ax, float ay, float bx, float by, int discs) {
        float dx = bx - ax, dy = by - ay, len = (float) Math.sqrt(dx * dx + dy * dy), ux = dx / len, uy = dy / len;
        float[] lens = new float[discs * 2 + 1];
        for (int i = 0; i < lens.length; i++) lens[i] = i % 2 == 1 ? 0.9f : 0.7f;
        float total = 0;
        for (float f : lens) total += f;
        float k = len / total, at = 0;
        for (int i = 0; i < lens.length; i++) {
            float s0 = at, s1 = at + lens[i] * k;
            boolean disc = i % 2 == 1;
            ModelRendererTurbo m = bar(disc ? GLASS : DARK, disc ? 0 : 12, ax + ux * s0, ay + uy * s0, 0, ax + ux * s1, ay + uy * s1, 0, disc ? 3.8f : 1.1f);
            (disc ? GLASS_PARTS : l).add(m);
            at = s1;
        }
    }

    /** half the lattice mast's width at a height (pixels): it narrows towards the top, like a pylon */
    private static float half(float y) {
        return 4.5f - 2.3f * (y - 3) / 67f;
    }

    /**
     * in pixels, y up from the block's bottom, the cantilever along +x. The mast is a lattice of steel angles (four
     * corner posts, struts and zig-zag bracing on every face, as the old Soviet line masts); the cantilever a
     * triangle of a top tube and a strut with the wires hung from it on glass insulators
     */
    private static ModelRendererTurbo[] build() {
        java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>();
        l.add(box(CONCRETE, 0, -6, 0, -6, 12, 3, 12));
        float y0 = 3, y1 = 70;
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
        //cantilever, earthed with the mast (the Soviet 3 kV lines' "uninsulated cantilever"): the top tube out over
        //the track and the strut up to its far end, bolted to the mast; the insulators are where the wires hang
        float ty = 66f, sy = 54f;
        l.add(box(DARK, 16, half(ty) - 0.5f, ty - 1.5f, -1.6f, 1.2f, 3, 3.2f));
        l.add(bar(STEEL, 16, half(ty), ty, 0, 19.5f, ty, 0, 1.4f));
        l.add(box(DARK, 16, half(sy) - 0.5f, sy - 1.5f, -1.6f, 1.2f, 3, 3.2f));
        //the strut meets the tube short of the string, so the discs hang clear of it
        float strutEnd = 12.5f;
        l.add(bar(STEEL, 20, half(sy), sy, 0, strutEnd, ty - 0.5f, 0, 1.2f));
        //the messenger wire hangs under the tube's end on a string of three glass discs, in its clamp
        insulatorString(l, 16, ty - 0.7f, 16, 60.2f, 3);
        l.add(box(DARK, 0, 15.3f, 58.7f, -1, 1.4f, 1.5f, 2));
        //registration arm: from the strut through two glass discs, then down and out to the contact wire's clamp
        float fx0 = 8, fy0 = sy + (ty - 0.5f - sy) * (fx0 - half(sy)) / (strutEnd - half(sy));
        insulatorString(l, fx0, fy0, fx0 + 2.6f, fy0 - 2.6f, 2);
        l.add(bar(DARK, 4, fx0 + 2.6f, fy0 - 2.6f, 0, 16, 48.8f, 0, 0.7f));
        l.add(box(DARK, 8, 15.4f, 47.8f, -0.6f, 1.2f, 1.4f, 1.2f));
        return l.toArray(new ModelRendererTurbo[0]);
    }

    /**
     * where a feeder comes to the mast: a bracket out from its top on the side away from the track, the cable hung
     * under it on two glass discs (at CatenaryMastTile.feederPoint)
     */
    private static ModelRendererTurbo[] buildFeederBracket() {
        java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>();
        float y = 68.5f;
        l.add(bar(STEEL, 24, -half(y), y, 0, -10.6f, y, 0, 1.2f));
        l.add(bar(STEEL, 28, -half(y - 5), y - 5, 0, -8.5f, y - 0.4f, 0, 0.9f));
        insulatorString(l, -9.9f, y - 0.7f, -9.9f, 65.6f, 2);
        l.add(box(DARK, 24, -10.6f, 64.5f, -0.7f, 1.4f, 1.1f, 1.4f));
        return l.toArray(new ModelRendererTurbo[0]);
    }

    /** a cable sagging from a to b (world), drawn relative to origin */
    public static void cable(PoseStack pose, VertexConsumer vc, Vec3 origin, Vec3 a, Vec3 b, double w, int la, int lb) {
        cable(pose, vc, origin, a, b, w, la, lb, DARK);
    }

    public static void cable(PoseStack pose, VertexConsumer vc, Vec3 origin, Vec3 a, Vec3 b, double w, int la, int lb, int u) {
        double len = a.distanceTo(b);
        int n = Math.max(4, (int) Math.ceil(len * 2));
        double sag = Math.min(0.6, 0.015 * len * len / 4 + 0.1);
        Vec3 prev = null;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            Vec3 p = a.add(b.subtract(a).scale(t)).add(0, -4 * sag * t * (1 - t), 0).subtract(origin);
            if (prev != null) wire(pose, vc, prev, p, w, u, t < 0.5 ? la : lb);
            prev = p;
        }
    }

    private static float yaw(Direction d) {
        return switch (d) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static Vec3 anchor(BlockPos pos, BlockState state, double height) {
        return train.blocks.catenary.CatenaryGeometry.anchor(pos, state, height);
    }

    @Override
    public void render(CatenaryMastTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (parts == null) {
            GLASS_PARTS.clear();
            parts = build();
            glass = GLASS_PARTS.toArray(new ModelRendererTurbo[0]);
            GLASS_PARTS.clear();
            feederParts = buildFeederBracket();
            feederGlass = GLASS_PARTS.toArray(new ModelRendererTurbo[0]);
        }
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
            if (tile.feeder != null) {
                for (ModelRendererTurbo p : feederParts) {
                    GL11.glPushMatrix();
                    p.render(0.0625f);
                    GL11.glPopMatrix();
                }
            }
            //the glass, see-through
            RenderContext.consumer = buffers.getBuffer(RenderType.entityTranslucent(TEX));
            if (tile.feeder != null) {
                for (ModelRendererTurbo p : feederGlass) {
                    GL11.glPushMatrix();
                    p.render(0.0625f);
                    GL11.glPopMatrix();
                }
            }
            for (ModelRendererTurbo p : glass) {
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
        Vec3 prevM = null, prevC = null;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double x = a.x + (b.x - a.x) * t, z = a.z + (b.z - a.z) * t, y0 = a.y + (b.y - a.y) * t;
            Vec3 m = new Vec3(x, y0 + MESSENGER_Y - 4 * sag * t * (1 - t), z).subtract(base);
            //held in the registration arm's clamp at each mast
            Vec3 c = new Vec3(x, y0 + CONTACT_Y, z).subtract(base);
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
