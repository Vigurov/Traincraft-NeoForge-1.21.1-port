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
    private static final int TW = 512, TH = 128, CONCRETE = 0, TANK = 128, FINS = 192, PORCELAIN = 224, STEEL = 256, DARK = 384, GLASS = 416;
    private static ModelRendererTurbo[] parts, glass;
    private static final java.util.List<ModelRendererTurbo> GLASS_PARTS = new java.util.ArrayList<>();

    public RenderSubstation(BlockEntityRendererProvider.Context c) {}

    private static ModelRendererTurbo box(int u, int v, float x, float y, float z, float w, float h, float d) {
        ModelRendererTurbo m = new ModelRendererTurbo("", u, v, TW, TH);
        m.addBox(x, y, z, w, h, d);
        return m;
    }

    /**
     * in pixels, y up from the ground, from the middle of its 2 x 2 blocks: a concrete pad; the transformer with its
     * radiator fins, lid and conservator; three tall high voltage bushings for the power lines in, three low ones
     * out; a cabinet; and the steel gantry at the front the feeders leave from, on a string of glass discs
     */
    private static ModelRendererTurbo[] build() {
        java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>();
        GLASS_PARTS.clear();
        for (int x = -16; x < 16; x += 16) for (int z = -16; z < 16; z += 16) l.add(box(CONCRETE, 0, x, 0, z, 16, 2, 16));
        //transformer
        l.add(box(TANK, 0, -8, 2, -9, 16, 14, 10));
        l.add(box(TANK, 0, -8.5f, 16, -9.5f, 17, 1, 11));
        for (int i = 0; i < 6; i++) {
            float z = -8 + i * 1.6f;
            l.add(box(FINS, 0, -11.5f, 3.5f, z, 3.5f, 11, 0.8f));
            l.add(box(FINS, 0, 8, 3.5f, z, 3.5f, 11, 0.8f));
        }
        l.add(box(TANK, 0, -6, 19.5f, -1.5f, 12, 3, 3));
        l.add(box(STEEL, 0, -5, 17, -0.6f, 1, 2.5f, 1));
        l.add(box(STEEL, 0, 4, 17, -0.6f, 1, 2.5f, 1));
        //high voltage bushings in
        for (float x : SubstationTile.BUSHING_X) bushing(l, x, SubstationTile.BUSHING_Z, 17, SubstationTile.BUSHING_TOP - 0.8f);
        //low voltage bushings out, and the cabinet
        for (int x = -4; x <= 4; x += 4) bushing(l, x, -1.5f, 17, 22);
        l.add(box(STEEL, 0, 9, 2, 6, 6, 8, 6));
        //the gantry at the front, the feeder string hung under its beam
        float gz = SubstationTile.FEEDER_Z;
        l.add(box(STEEL, 0, -10.8f, 2, gz - 0.8f, 1.6f, 34, 1.6f));
        l.add(box(STEEL, 0, 9.2f, 2, gz - 0.8f, 1.6f, 34, 1.6f));
        l.add(box(STEEL, 0, -10.8f, 34, gz - 0.8f, 21.6f, 1.6f, 1.6f));
        l.add(box(STEEL, 0, -10.8f, 22, gz - 0.6f, 21.6f, 1.2f, 1.2f));
        float top = 34, bottom = SubstationTile.FEEDER_Y;
        float[] lens = {0.7f, 0.9f, 0.7f, 0.9f, 0.7f};
        float total = 0;
        for (float f : lens) total += f;
        float at = top;
        for (int i = 0; i < lens.length; i++) {
            float h = lens[i] * (top - bottom) / total;
            boolean disc = i % 2 == 1;
            ModelRendererTurbo m = disc ? box(GLASS, 0, -1.9f, at - h, gz - 1.9f, 3.8f, h, 3.8f) : box(DARK, 0, -0.55f, at - h, gz - 0.55f, 1.1f, h, 1.1f);
            (disc ? GLASS_PARTS : l).add(m);
            at -= h;
        }
        l.add(box(DARK, 0, -0.7f, bottom - 0.6f, gz - 0.7f, 1.4f, 0.8f, 1.4f));
        return l.toArray(new ModelRendererTurbo[0]);
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
            for (ModelRendererTurbo p : parts) {
                GL11.glPushMatrix();
                p.render(0.0625f);
                GL11.glPopMatrix();
            }
            RenderContext.consumer = buffers.getBuffer(RenderType.entityTranslucent(TEX));
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
        //the power lines in, from the generators to the bushings
        int i = 0;
        for (BlockPos g : tile.inputs) {
            Vec3 from = generatorPoint(tile, g);
            int lg = LevelRenderer.getLightColor(tile.getLevel(), g.above());
            RenderCatenary.cable(pose, vc, Vec3.atLowerCornerOf(pos), from, tile.inputPoint(i++), 0.045, lg, light);
        }
        for (BlockPos m : tile.feeders) {
            if (!(tile.getLevel().getBlockEntity(m) instanceof CatenaryMastTile mast)) continue;
            Vec3 a = tile.feederPoint(), b = mast.feederPoint();
            int lb = LevelRenderer.getLightColor(tile.getLevel(), m.above(4));
            RenderCatenary.cable(pose, vc, Vec3.atLowerCornerOf(pos), a, b, 0.05, light, lb);
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
