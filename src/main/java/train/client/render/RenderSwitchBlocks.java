package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import ebf.tim.blocks.TileSwitch;
import fexcraft.tmt.slim.GL11;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import train.blocks.signal.TileTCSignal;
import train.render.models.ModelSwitchStandOff;
import train.render.models.ModelSwitchStandOn;
import train.render.models.blocks.ModelBlockSignal;

/**
 * The high star switch stand and the Traincraft signal (1.12 TileSwitchStand / TileTCSignal renderModel, under the
 * TiM rail TESR's transform: block centre, the facing's turn, upside down). The items draw the same models.
 */
public class RenderSwitchBlocks implements BlockEntityRenderer<TileSwitch> {
    private static final ResourceLocation STAND_ON = tex("textures/models/switchstand_uv_draw_1.png"),
            STAND_OFF = tex("textures/models/switchstand_uv_draw_2.png"),
            SIGNAL_GREEN = tex("textures/trains/signal_suisse_green.png"),
            SIGNAL_RED = tex("textures/trains/signal_suisse_red.png");
    private static ModelBase standOn, standOff, signal;

    public RenderSwitchBlocks(BlockEntityRendererProvider.Context context) {}

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath("traincraft", path);
    }

    @Override
    public void render(TileSwitch tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        draw(tile instanceof TileTCSignal, tile.getStrength() > 0, tile.facing(), false, pose, buffers, light);
    }

    @Override
    public AABB getRenderBoundingBox(TileSwitch tile) {
        return new AABB(tile.getBlockPos()).inflate(1, 0, 1).expandTowards(0, 2, 0);
    }

    /** facing: the 1.12 number, -1 for none (items) */
    static void draw(boolean isSignal, boolean on, int facing, boolean item, PoseStack pose, MultiBufferSource buffers, int light) {
        if (standOn == null) {
            standOn = new ModelSwitchStandOn();
            standOff = new ModelSwitchStandOff();
            signal = new ModelBlockSignal();
        }
        RenderContext.begin(pose, buffers, light, isSignal ? (on ? SIGNAL_GREEN : SIGNAL_RED) : (on ? STAND_ON : STAND_OFF));
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5f, 0.5f, 0.5f);
            switch (facing) {
                case 0 -> GL11.glRotatef(180, 0, 1, 0);
                case 1 -> GL11.glRotatef(90, 0, 1, 0);
                case 2 -> GL11.glRotatef(270, 0, 1, 0);
                default -> {
                }
            }
            GL11.glRotatef(180, 1, 0, 0);
            if (isSignal) {
                GL11.glTranslatef(0, 0.5f, 0);
                GL11.glRotatef(180, 0, 0, 1);
                if (item) {
                    GL11.glScalef(0.65f, 0.65f, 0.65f);
                    GL11.glTranslatef(0, -0.5f, 0);
                    GL11.glRotatef(15, 0, 1, 0);
                } else {
                    GL11.glRotatef(270, 0, 1, 0);
                }
                //1.12 turned the lightmap off: the signal glows
                RenderContext.fullBright = true;
                signal.render(null, 0, 0, 0, 0, 0, 0.0625f);
            } else if (item) {
                GL11.glTranslated(-0.2, 0, 0);
                GL11.glScalef(0.65f, 0.65f, 0.65f);
                standOff.render();
            } else {
                GL11.glTranslated(0, -0.2f, 0);
                //the extra offset for the track height
                GL11.glTranslatef(0, ebf.tim.utility.ClientProxy.railSkin == 3 ? 0.09f : 0.01875f, 0);
                (on ? standOn : standOff).render();
            }
            GL11.glPopMatrix();
        } finally {
            RenderContext.end();
        }
    }

    /** the block items: the same models */
    public static class Items extends BlockEntityWithoutLevelRenderer {
        public Items() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            boolean isSignal = stack.getItem() == train.blocks.TCBlocks.SIGNAL.get().asItem();
            //the models are taller than a block: shrink them into the block the item transforms expect
            float k = isSignal ? 0.55f : 0.9f;
            pose.pushPose();
            pose.translate(0.5, 0.5, 0.5);
            pose.scale(k, k, k);
            //turned so the item views see the signal's face
            if (isSignal) pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            pose.translate(-0.5, -0.5, -0.5);
            draw(isSignal, false, -1, true, pose, buffers, light);
            pose.popPose();
        }
    }
}
