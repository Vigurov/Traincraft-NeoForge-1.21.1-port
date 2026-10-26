package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import train.blocks.catenary.CatenaryMastTile;
import train.blocks.catenary.SubstationBlock;
import train.blocks.catenary.SubstationTile;

/**
 * The traction substation (TMT boxes in Traincraft's old look): a transformer on a concrete pad, its radiator
 * fins, conservator tank and brown porcelain bushings, a cabinet; and the feeder cables from its tall outgoing
 * bushing to the masts it feeds.
 */
public class RenderSubstation implements BlockEntityRenderer<SubstationTile> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/substation.png");
    private static final int TW = 512, TH = 128, CONCRETE = 0, TANK = 128, FINS = 192, PORCELAIN = 224, STEEL = 256, DARK = 384, GLASS = 416, WALL = 448;
    private static ModelRendererTurbo[] parts, glass;
    private static final java.util.List<ModelRendererTurbo> GLASS_PARTS = new java.util.ArrayList<>();

    public RenderSubstation(BlockEntityRendererProvider.Context c) {}

    private static ModelRendererTurbo box(int u, int v, float x, float y, float z, float w, float h, float d) {
        ModelRendererTurbo m = new ModelRendererTurbo("", u, v, TW, TH);
        m.addBox(x, y, z, w, h, d);
        return m;
    }

    /** a string of glass discs from top down to bottom at x, z (pixels): pin, disc, cap, disc ... */
    private static void string(java.util.List<ModelRendererTurbo> l, java.util.List<ModelRendererTurbo> g, float x, float z, float top, float bottom, int discs) {
        float[] lens = new float[discs * 2 + 1];
        float total = 0;
        for (int i = 0; i < lens.length; i++) total += lens[i] = i % 2 == 1 ? 0.9f : 0.7f;
        float at = top;
        for (int i = 0; i < lens.length; i++) {
            float h = lens[i] * (top - bottom) / total;
            if (i % 2 == 1) g.add(box(GLASS, 0, x - 1.6f, at - h, z - 1.6f, 3.2f, h, 3.2f));
            else l.add(box(DARK, 0, x - 0.5f, at - h, z - 0.5f, 1, h, 1));
            at -= h;
        }
    }

    /**
     * in pixels, y up from the ground, from the middle of its 2 x 2 blocks, the front (+z) where the feeders leave:
     * the incoming gantry at the back, its busbar on glass strings and the conductors down to the transformer's
     * bushings; the transformer with its fins and conservator; the rectifier hall (a 3 kV DC substation turns the
     * transformer's current into the line's); and the outgoing gantry at the front the feeders leave from
     */
    private static ModelRendererTurbo[] build() {
        java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>();
        GLASS_PARTS.clear();
        for (int x = -16; x < 16; x += 16) for (int z = -16; z < 16; z += 16) l.add(box(CONCRETE, 0, x, 0, z, 16, 2, 16));
        //incoming gantry: posts, beam (the lines' bays are drawn on it as many as there are), busbar under it
        float iz = SubstationTile.IN_Z;
        l.add(box(STEEL, 0, -15.4f, 2, iz - 0.8f, 1.6f, 31, 1.6f));
        l.add(box(STEEL, 0, 13.8f, 2, iz - 0.8f, 1.6f, 31, 1.6f));
        l.add(box(STEEL, 0, -15.4f, 31.4f, iz - 0.8f, 30.8f, 1.6f, 1.6f));
        l.add(box(STEEL, 0, -15.4f, 18, iz - 0.6f, 30.8f, 1.2f, 1.2f));
        for (float x : new float[]{-10, 0, 10}) string(l, GLASS_PARTS, x, iz, 31.4f, 28.4f, 2);
        l.add(box(DARK, 0, -14, 27.7f, iz - 0.35f, 28, 0.7f, 0.7f));
        //transformer
        l.add(box(TANK, 0, -13, 2, -8, 18, 15, 12));
        l.add(box(TANK, 0, -13.5f, 17, -8.5f, 19, 1, 13));
        for (int i = 0; i < 6; i++) l.add(box(FINS, 0, -15.6f, 3.5f, -7.2f + i * 1.8f, 2.6f, 12, 0.8f));
        for (int i = 0; i < 8; i++) l.add(box(FINS, 0, -12 + i * 2, 3.5f, 4, 0.8f, 12, 2.6f));
        l.add(box(TANK, 0, -11, 20.5f, -1.8f, 14, 3.5f, 3.5f));
        l.add(box(STEEL, 0, -10, 18, -0.6f, 1, 2.5f, 1));
        l.add(box(STEEL, 0, 1, 18, -0.6f, 1, 2.5f, 1));
        for (float x : new float[]{-10, -4, 2}) {
            bushing(l, x, -6, 18, 26.5f);
            //down from the busbar to it
            l.add(box(DARK, 0, x - 0.3f, 27.3f, iz, 0.6f, 0.6f, -6 - iz + 0.3f));
            l.add(box(DARK, 0, x - 0.3f, 27.3f, -6.3f, 0.6f, 0.6f, 0.6f));
        }
        for (float x : new float[]{-8, -2}) bushing(l, x, 1.5f, 18, 22);
        //rectifier hall, and the conductor to it from the transformer
        l.add(box(WALL, 0, 6, 2, -9, 9.5f, 26, 18));
        l.add(box(CONCRETE, 0, 5.5f, 28, -9.5f, 10.5f, 1.2f, 19));
        l.add(box(DARK, 0, 8.5f, 2, 8.9f, 4, 9, 0.4f));
        for (float z : new float[]{-6, 1}) l.add(box(DARK, 0, 5.7f, 15, z, 0.4f, 5, 3.5f));
        l.add(box(DARK, 0, -2.3f, 22.3f, 1.2f, 8.3f, 0.6f, 0.6f));
        //outgoing gantry at the front, the feeder string under its beam
        float gz = SubstationTile.FEEDER_Z;
        l.add(box(STEEL, 0, -9.8f, 2, gz - 0.8f, 1.6f, 30, 1.6f));
        l.add(box(STEEL, 0, 8.2f, 2, gz - 0.8f, 1.6f, 30, 1.6f));
        l.add(box(STEEL, 0, -9.8f, 30, gz - 0.8f, 19.6f, 1.6f, 1.6f));
        string(l, GLASS_PARTS, SubstationTile.FEEDER_X, gz, 30, SubstationTile.FEEDER_Y + 0.6f, 2);
        l.add(box(DARK, 0, -0.7f, SubstationTile.FEEDER_Y - 0.2f, gz - 0.7f, 1.4f, 0.8f, 1.4f));
        return l.toArray(new ModelRendererTurbo[0]);
    }

    /** the bays on the incoming gantry's beam for n lines: a pin and a glass disc for each */
    private static final java.util.Map<Integer, ModelRendererTurbo[][]> BAYS = new java.util.HashMap<>();

    private static ModelRendererTurbo[][] bays(int n) {
        return BAYS.computeIfAbsent(n, k -> {
            java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>(), g = new java.util.ArrayList<>();
            for (int i = 0; i < k; i++) {
                float x = SubstationTile.inputX(i, k), z = SubstationTile.IN_Z;
                l.add(box(DARK, 0, x - 0.4f, 33, z - 0.4f, 0.8f, 0.5f, 0.8f));
                g.add(box(GLASS, 0, x - 1.3f, 33.5f, z - 1.3f, 2.6f, 0.7f, 2.6f));
                l.add(box(DARK, 0, x - 0.4f, 34.2f, z - 0.4f, 0.8f, SubstationTile.IN_Y - 34.2f, 0.8f));
            }
            return new ModelRendererTurbo[][]{l.toArray(new ModelRendererTurbo[0]), g.toArray(new ModelRendererTurbo[0])};
        });
    }

    /** a porcelain bushing: a core with sheds up it */
    private static void bushing(java.util.List<ModelRendererTurbo> l, float x, float z, float from, float to) {
        l.add(box(PORCELAIN, 0, x - 0.6f, from, z - 0.6f, 1.2f, to - from, 1.2f));
        for (float y = from + 1; y < to - 0.6f; y += 1.6f) l.add(box(PORCELAIN, 8, x - 1.3f, y, z - 1.3f, 2.6f, 0.7f, 2.6f));
        l.add(box(DARK, 0, x - 0.6f, to, z - 0.6f, 1.2f, 0.8f, 1.2f));
    }

    private static float yaw(Direction d) {
        return switch (d) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    @Override
    public void render(SubstationTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (parts == null) {
            parts = build();
            glass = GLASS_PARTS.toArray(new ModelRendererTurbo[0]);
        }
        RenderContext.begin(pose, buffers, light, TEX);
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(1, 0, 1);
            GL11.glRotatef(yaw(tile.getBlockState().getValue(SubstationBlock.FACING)), 0, 1, 0);
            ModelRendererTurbo[][] bay = bays(tile.inputs.size());
            for (ModelRendererTurbo[] set : new ModelRendererTurbo[][]{parts, bay[0]}) {
                for (ModelRendererTurbo p : set) {
                    GL11.glPushMatrix();
                    p.render(0.0625f);
                    GL11.glPopMatrix();
                }
            }
            RenderContext.consumer = buffers.getBuffer(RenderType.entityTranslucent(TEX));
            for (ModelRendererTurbo p : bay[1]) {
                GL11.glPushMatrix();
                p.render(0.0625f);
                GL11.glPopMatrix();
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
        if (tile.getLevel() == null) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(RenderCatenary.TEXTURE));
        BlockPos pos = tile.getBlockPos();
        //from the rectifier hall up to the outgoing gantry's string
        RenderCatenary.cable(pose, vc, Vec3.atLowerCornerOf(pos), tile.hallPoint(), tile.feederPoint(), 0.045, light, light, RenderCatenary.ALUMINIUM);
        //the power lines in, from the generators to their bays on the incoming gantry
        int i = 0;
        for (BlockPos g : tile.inputs) {
            Vec3 from = generatorPoint(tile, g);
            int lg = LevelRenderer.getLightColor(tile.getLevel(), g.above());
            RenderCatenary.cable(pose, vc, Vec3.atLowerCornerOf(pos), from, tile.inputPoint(i++, tile.inputs.size()), 0.045, lg, light, RenderCatenary.ALUMINIUM);
        }
        for (BlockPos m : tile.feeders) {
            if (!(tile.getLevel().getBlockEntity(m) instanceof CatenaryMastTile mast)) continue;
            Vec3 a = tile.feederPoint(), b = mast.feederPoint();
            int lb = LevelRenderer.getLightColor(tile.getLevel(), m.above(4));
            RenderCatenary.cable(pose, vc, Vec3.atLowerCornerOf(pos), a, b, 0.05, light, lb, RenderCatenary.ALUMINIUM);
        }
    }

    /** where a power line leaves a generator: a windmill's tower under its wheel, anything else its top */
    private static Vec3 generatorPoint(SubstationTile tile, BlockPos g) {
        boolean windmill = tile.getLevel().getBlockEntity(g) instanceof train.blocks.machines.MachineTile m && m.kind.equals("windmill");
        return new Vec3(g.getX() + 0.5, g.getY() + (windmill ? 1.25 : 1.0), g.getZ() + 0.5);
    }

    @Override
    public AABB getRenderBoundingBox(SubstationTile tile) {
        AABB box = new AABB(tile.getBlockPos()).expandTowards(1, 2.3, 1);
        for (BlockPos g : tile.inputs) box = box.minmax(new AABB(g).expandTowards(0, 1, 0));
        for (BlockPos m : tile.feeders) box = box.minmax(new AABB(m).expandTowards(0, 4.5, 0));
        return box;
    }

    @Override
    public boolean shouldRenderOffScreen(SubstationTile tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
