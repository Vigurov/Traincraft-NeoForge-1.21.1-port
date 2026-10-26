package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import train.blocks.BridgePillarBlock;
import train.library.Info;

/** the bridge pillar OBJ with the slope track texture (1.12 ModelBridgePillar) */
public class RenderBridgePillar implements BlockEntityRenderer<BridgePillarBlock.Tile> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/track_slope.png");
    private static ModelRendererTurbo model;

    public RenderBridgePillar(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BridgePillarBlock.Tile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (model == null) {
            model = new ModelRendererTurbo((String) null);
            model.addObj(Info.getModelAddress("bridge_block.obj"));
        }
        RenderContext.begin(pose, buffers, light, TEX);
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5f, 0.45f, 1.0f);
            GL11.glRotatef(180, 0, 0, 1);
            model.render(1);
            GL11.glPopMatrix();
        } finally {
            RenderContext.end();
        }
    }
}
