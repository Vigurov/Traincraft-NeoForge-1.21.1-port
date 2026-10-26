package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import train.blocks.machines.MachineTile;
import train.library.Info;
import train.render.models.blocks.ModelGeneratorDiesel;

/**
 * the machines that are models: the diesel generator (1.12 ModelGeneratorDiesel), the wind mill and the water wheel
 * (their OBJs, tinted and turning as in 1.12); the other machines are plain block models
 */
public class RenderGeneratorDiesel implements BlockEntityRenderer<MachineTile> {
    private static final ModelGeneratorDiesel modelGenerator = new ModelGeneratorDiesel((float) (1.0 / 16.0));
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/generator_diesel.png");
    private static final ResourceLocation WHEEL_TEX = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/models/water_wheel_uv.png");
    private static ModelRendererTurbo windMill, windMillWheel, waterWheel;

    public RenderGeneratorDiesel(BlockEntityRendererProvider.Context context) {}

    private static ModelRendererTurbo obj(String name) {
        ModelRendererTurbo m = new ModelRendererTurbo((String) null);
        m.addObj(Info.getModelAddress(name));
        return m;
    }

    private static void color(int j) {
        GL11.glColor4f((j >> 16 & 255) / 255f, (j >> 8 & 255) / 255f, (j & 255) / 255f, 1);
    }

    @Override
    public void render(MachineTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        switch (tile.kind) {
            case "dieselgenerator", "windmill", "waterwheel" -> {}
            default -> { return; }
        }
        //1.12 facing: 0 south, 1 west, 2 north, 3 east
        int facing = tile.getBlockState().getValue(HorizontalDirectionalBlock.FACING).get2DDataValue();
        float time = (tile.getLevel() == null ? 0 : tile.getLevel().getGameTime()) + partialTick;
        if (tile.kind.equals("dieselgenerator")) {
            RenderContext.begin(pose, buffers, light, texture);
            try {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.5F, 0.0F, 0.5F);
                modelGenerator.render(0.0625F, facing);
                GL11.glPopMatrix();
            } finally {
                RenderContext.end();
            }
            return;
        }
        if (windMill == null) {
            windMill = obj("wind_mill.obj");
            windMillWheel = obj("wind_mill_wheel.obj");
            waterWheel = obj("water_wheel.obj");
        }
        RenderContext.begin(pose, buffers, light, WHEEL_TEX);
        try {
            if (tile.kind.equals("windmill")) {
                float yaw = switch (facing) { case 1 -> 180; case 0 -> -90; case 2 -> 90; default -> 0; };
                GL11.glPushMatrix();
                GL11.glTranslated(0.5, 0.5, 0.5);
                GL11.glRotatef(90, 0, 0, 1);
                color(0x331D14);
                GL11.glScalef(0.5f, 0.5f, 0.5f);
                GL11.glRotatef(yaw, 0, 1, 0);
                windMill.render(1);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated(0.5, 1.5, 0.5);
                GL11.glRotatef(90, 0, 0, 1);
                color(0x7A7674);
                GL11.glScalef(0.45f, 0.45f, 0.45f);
                GL11.glRotatef(yaw, 0, 1, 0);
                //1.12 turned about 40 degrees a second, faster in rain and thunder
                float speed = tile.getLevel() != null && tile.getLevel().isThundering() ? 7.5f : tile.getLevel() != null && tile.getLevel().isRaining() ? 4.5f : 1;
                GL11.glRotatef(time * 2f * speed, 0F, 1F, 0F);
                windMillWheel.render(1);
                GL11.glPopMatrix();
            } else {
                GL11.glPushMatrix();
                GL11.glTranslated(0.5, 0.5, 0.5);
                GL11.glRotatef(90, 0, 0, 1);
                color(0x331D14);
                switch (tile.waterDir) {
                    case 3 -> GL11.glRotatef(90, 1, 0, 0);
                    case 1 -> GL11.glRotatef(-90, 1, 0, 0);
                    case 2 -> GL11.glRotatef(180, 1, 0, 0);
                    default -> {}
                }
                if (tile.waterDir >= 0) {
                    GL11.glRotatef(-time * 1.7f, 0F, 1F, 0F);
                }
                GL11.glScalef(0.25f, 0.5f, 0.25f);
                waterWheel.render(1);
                GL11.glPopMatrix();
            }
            GL11.glColor4f(1, 1, 1, 1);
        } finally {
            RenderContext.end();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(MachineTile tile) {
        return tile.kind.equals("windmill");
    }
}
