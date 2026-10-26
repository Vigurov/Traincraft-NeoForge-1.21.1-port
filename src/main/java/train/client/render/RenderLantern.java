package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import train.blocks.lantern.LanternTile;
import train.library.Info;

/** the lantern OBJ, tinted with the lantern's colour (1.12 ModelLantern) */
public class RenderLantern implements BlockEntityRenderer<LanternTile> {
    private static ModelRendererTurbo model;
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/lantern_uv_draw_2.png");

    public RenderLantern(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(LanternTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (model == null) {
            model = new ModelRendererTurbo((String) null);
            model.addObj(Info.getModelAddress("lantern.obj"));
        }
        RenderContext.begin(pose, buffers, light, texture);
        try {
            GL11.glPushMatrix();
            GL11.glTranslated(0.5, 0.5, 0.5);
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            GL11.glRotatef(90, 1, 0, 0);
            int j = tile.getColor();
            GL11.glColor4f((j >> 16 & 255) / 255f, (j >> 8 & 255) / 255f, (j & 255) / 255f, 1);
            model.render(1);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glPopMatrix();
        } finally {
            RenderContext.end();
        }
    }
}
