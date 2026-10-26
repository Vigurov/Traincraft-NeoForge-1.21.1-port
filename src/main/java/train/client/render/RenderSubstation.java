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
    private static final int TW = 256, TH = 128, CONCRETE = 0, TANK = 64, FINS = 128, PORCELAIN = 160, STEEL = 192, DARK = 224;
    private static ModelRendererTurbo[] parts;

    public RenderSubstation(BlockEntityRendererProvider.Context c) {}

    private static ModelRendererTurbo box(int u, int v, float x, float y, float z, float w, float h, float d) {
        ModelRendererTurbo m = new ModelRendererTurbo("", u, v, TW, TH);
        m.addBox(x, y, z, w, h, d);
        return m;
    }

    /** in pixels, y up from the block's bottom */
    private static ModelRendererTurbo[] build() {
        java.util.List<ModelRendererTurbo> l = new java.util.ArrayList<>();
        l.add(box(CONCRETE, 0, -8, 0, -8, 16, 2, 16));
        //the tank and its lid
        l.add(box(TANK, 0, -5, 2, -3.5f, 10, 9, 7));
        l.add(box(TANK, 20, -5.5f, 11, -4, 11, 1, 8));
        //radiator fins on both long sides
        for (int i = 0; i < 5; i++) {
            float x = -4.2f + i * 2;
            l.add(box(FINS, 0, x, 3, 3.5f, 0.8f, 7.5f, 3));
            l.add(box(FINS, 12, x, 3, -6.5f, 0.8f, 7.5f, 3));
        }
        //conservator tank on its legs
        l.add(box(TANK, 40, -4.5f, 14, -1.3f, 9, 2.6f, 2.6f));
        l.add(box(STEEL, 0, -3.5f, 12, -0.5f, 1, 2, 1));
        l.add(box(STEEL, 0, 2.5f, 12, -0.5f, 1, 2, 1));
        //three high voltage bushings in, and the tall one out to the feeders
        for (int i = -1; i <= 1; i++) bushing(l, i * 3, -2.2f, 12, 17);
        bushing(l, 0, 2.2f, 12, 23.5f);
        l.add(box(DARK, 0, -0.6f, 23.5f, 1.6f, 1.2f, 1.6f, 1.2f));
        //cabinet
        l.add(box(STEEL, 10, 5.5f, 2, -2, 2.2f, 6, 4));
        return l.toArray(new ModelRendererTurbo[0]);
    }

    /** a porcelain bushing: a core with sheds up it */
    private static void bushing(java.util.List<ModelRendererTurbo> l, float x, float z, float from, float to) {
        l.add(box(PORCELAIN, 0, x - 0.5f, from, z - 0.5f, 1, to - from, 1));
        for (float y = from + 1; y < to - 0.6f; y += 1.5f) l.add(box(PORCELAIN, 8, x - 1.1f, y, z - 1.1f, 2.2f, 0.6f, 2.2f));
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
        if (parts == null) parts = build();
        RenderContext.begin(pose, buffers, light, TEX);
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5f, 0, 0.5f);
            GL11.glRotatef(yaw(tile.getBlockState().getValue(SubstationBlock.FACING)), 0, 1, 0);
            for (ModelRendererTurbo p : parts) {
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
        for (BlockPos m : tile.feeders) {
            if (!(tile.getLevel().getBlockEntity(m) instanceof CatenaryMastTile mast)) continue;
            Vec3 a = tile.feederPoint(), b = mast.feederPoint();
            int lb = LevelRenderer.getLightColor(tile.getLevel(), m.above(4));
            RenderCatenary.cable(pose, vc, Vec3.atLowerCornerOf(pos), a, b, 0.05, light, lb);
        }
    }

    @Override
    public AABB getRenderBoundingBox(SubstationTile tile) {
        AABB box = new AABB(tile.getBlockPos()).expandTowards(0, 1, 0);
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
