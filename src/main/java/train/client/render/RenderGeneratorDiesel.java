package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import train.blocks.machines.MachineTile;
import train.render.models.blocks.ModelGeneratorDiesel;

/** the diesel generator, drawn with its 1.12 model (the other machines are plain block models) */
public class RenderGeneratorDiesel implements BlockEntityRenderer<MachineTile> {
    private static final ModelGeneratorDiesel modelGenerator = new ModelGeneratorDiesel((float) (1.0 / 16.0));
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/generator_diesel.png");

    public RenderGeneratorDiesel(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(MachineTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!tile.kind.equals("dieselgenerator")) return;
        RenderContext.begin(pose, buffers, light, texture);
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5F, 0.0F, 0.5F);
            //1.12 facing: 0 south, 1 west, 2 north, 3 east
            int facing = tile.getBlockState().getValue(HorizontalDirectionalBlock.FACING).get2DDataValue();
            modelGenerator.render(0.0625F, facing);
            GL11.glPopMatrix();
        } finally {
            RenderContext.end();
        }
    }
}
