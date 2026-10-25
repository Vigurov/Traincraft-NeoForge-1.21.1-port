package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ebf.tim.blocks.RailTileEntity;
import ebf.tim.blocks.rails.RailShapeCore;
import ebf.tim.render.models.Model1x1Rail;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

/**
 * Draws TiM rails. The geometry is built by the 1.12 rail models once per rail (1.12 compiled it into a display list)
 * and replayed every frame.
 */
public class RailRenderer implements BlockEntityRenderer<RailTileEntity> {

    public RailRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(RailTileEntity te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(te.renderCache instanceof Recorder)) {
            te.renderCache = build(te);
        }
        Recorder r = (Recorder) te.renderCache;
        if (r.size == 0) {
            return;
        }
        VertexConsumer c = buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose p = pose.last();
        float[] d = r.data;
        for (int i = 0; i < r.size; i += Recorder.STRIDE) {
            c.addVertex(p, d[i], d[i + 1], d[i + 2])
                    .setColor(d[i + 3], d[i + 4], d[i + 5], d[i + 6])
                    .setUv(d[i + 7], d[i + 8])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(p, d[i + 9], d[i + 10], d[i + 11]);
        }
    }

    private static Recorder build(RailTileEntity te) {
        Recorder rec = new Recorder();
        if (te.getLevel() == null || te.getData() == null || te.getData().isEmpty()) {
            return rec;
        }
        RailShapeCore route = new RailShapeCore().fromXML(te.getData());
        if (route.activePath == null || route.activePath.isEmpty()) {
            return rec;
        }
        RenderContext.begin(new PoseStack(), type -> rec, 0, InventoryMenu.BLOCK_ATLAS);
        try {
            Model1x1Rail.Model3DRail(te.getLevel(), te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ(), route);
        } finally {
            RenderContext.end();
        }
        return rec;
    }

    @Override
    public AABB getRenderBoundingBox(RailTileEntity te) {
        return new AABB(te.getBlockPos()).inflate(1);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    /** keeps the vertices the rail models emit: position, colour, uv, normal */
    static class Recorder implements VertexConsumer {
        static final int STRIDE = 12;
        float[] data = new float[STRIDE * 256];
        int size = 0;
        private int cur = -STRIDE;

        private void grow() {
            if (size + STRIDE > data.length) {
                data = java.util.Arrays.copyOf(data, data.length * 2);
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            grow();
            cur = size;
            size += STRIDE;
            data[cur] = x; data[cur + 1] = y; data[cur + 2] = z;
            data[cur + 3] = data[cur + 4] = data[cur + 5] = data[cur + 6] = 1;
            data[cur + 10] = 1;
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            data[cur + 3] = red / 255f; data[cur + 4] = green / 255f; data[cur + 5] = blue / 255f; data[cur + 6] = alpha / 255f;
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            data[cur + 7] = u; data[cur + 8] = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            data[cur + 9] = x; data[cur + 10] = y; data[cur + 11] = z;
            return this;
        }
    }
}
