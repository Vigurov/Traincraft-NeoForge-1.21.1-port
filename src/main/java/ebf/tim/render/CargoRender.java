package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.WalkShapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The load of an open car as it is: what lies in its slots, heaped in it as small cubes (blocks) or lying items,
 * the heap the higher the more slots are filled. Only its top shows over the walls, so only that is drawn.
 * Each slot's things take their share of the heap, in slot order from one end.
 */
public final class CargoRender {
    private CargoRender() {}

    /** the size of one lump of the load */
    private static final float CELL = 0.25f;

    public static void render(GenericRailTransport t, float yaw, float pitch, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!t.showsItsLoad() || t.backBogie == null || t.inventory == null || t.inventory.isEmpty()) return;
        String ids = t.entityData().get(GenericRailTransport.CARGO_ITEMS);
        if (ids.isEmpty()) return;
        String[] list = ids.split(" ");
        WalkShapes.Shape s = t.collisionHandler.shape;
        float halfLong = s.halfInside(), halfWide = s.halfWidth() - s.thickness();
        float depth = s.wall() - s.floor() - 0.04f;
        float level = depth * Math.min(1f, list.length / (float) t.inventory.size());
        int along = Math.max(1, (int) (2 * halfLong / CELL)), across = Math.max(1, (int) (2 * halfWide / CELL));
        float ca = 2 * halfLong / along, cw = 2 * halfWide / across;
        float h = Math.min(Math.min(ca, cw), level);
        float top = s.floor() + level - h;
        pose.pushPose();
        pose.translate(0, t.backBogie.yOffset, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(-pitch));
        var blocks = Minecraft.getInstance().getBlockRenderer();
        var items = Minecraft.getInstance().getItemRenderer();
        for (int i = 0; i < along; i++) {
            //the slot whose share this stretch of the heap is
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(list[Math.min(list.length - 1, i * list.length / along)]));
            for (int j = 0; j < across; j++) {
                float x = -halfLong + ca * i, z = -halfWide + cw * j;
                pose.pushPose();
                if (item instanceof BlockItem bi) {
                    pose.translate(x, top, z);
                    pose.scale(ca, h, cw);
                    blocks.renderSingleBlock(bi.getBlock().defaultBlockState(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
                } else {
                    //lying flat on the heap
                    pose.translate(x + ca / 2, s.floor() + level - 0.01f, z + cw / 2);
                    pose.mulPose(Axis.XP.rotationDegrees(90));
                    pose.mulPose(Axis.ZP.rotationDegrees((i * 37 + j * 71) % 360));
                    pose.scale(ca * 1.3f, cw * 1.3f, ca * 1.3f);
                    items.renderStatic(new ItemStack(item), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, t.level(), i * 31 + j);
                }
                pose.popPose();
            }
        }
        pose.popPose();
    }
}
