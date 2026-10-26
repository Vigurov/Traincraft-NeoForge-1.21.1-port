package train.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * The items of the blocks drawn by a block entity renderer (the windmill, the water wheel, the diesel generator,
 * the lantern, the bridge pillar, the catenary mast, the substation) show that model, in the inventory, in the hand
 * and on the ground: a block entity of the block, in no world, drawn by its renderer and shrunk into one block.
 * The size of each model is measured once, by drawing it into nothing.
 */
public class BlockItemModels extends BlockEntityWithoutLevelRenderer {

    private final Map<Item, BlockEntity> tiles = new HashMap<>();
    private final Map<Item, float[]> bounds = new HashMap<>();

    public BlockItemModels() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockEntity tile = tile(stack.getItem());
        if (tile == null) return;
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
        if (renderer == null) return;
        float[] b = bounds.computeIfAbsent(stack.getItem(), i -> measure(tile));
        //tall and thin ones (the mast, the windmill) may stand a little above the block, or they would be specks
        float size = Math.max(Math.max(b[3] - b[0], b[5] - b[2]), (b[4] - b[1]) * 0.7f);
        if (size <= 0) return;
        float k = 1 / size;
        pose.pushPose();
        //the middle of the model to the middle of the block the item transforms expect
        pose.translate(0.5, 0.5, 0.5);
        pose.scale(k, k, k);
        pose.translate(-(b[0] + b[3]) / 2, -(b[1] + b[4]) / 2, -(b[2] + b[5]) / 2);
        renderer.render(tile, 0, pose, buffers, light, overlay);
        pose.popPose();
    }

    private BlockEntity tile(Item item) {
        return tiles.computeIfAbsent(item, i -> {
            if (!(i instanceof BlockItem bi) || !(bi.getBlock() instanceof EntityBlock eb)) return null;
            return eb.newBlockEntity(BlockPos.ZERO, bi.getBlock().defaultBlockState());
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static float[] measure(BlockEntity tile) {
        Bounds b = new Bounds();
        var renderer = (net.minecraft.client.renderer.blockentity.BlockEntityRenderer) Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
        try {
            renderer.render(tile, 0, new PoseStack(), (MultiBufferSource) t -> b, 0xF000F0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        } catch (RuntimeException e) {
            return new float[]{0, 0, 0, 1, 1, 1};
        }
        return b.min[0] > b.max[0] ? new float[]{0, 0, 0, 1, 1, 1}
                : new float[]{b.min[0], b.min[1], b.min[2], b.max[0], b.max[1], b.max[2]};
    }

    /** takes the vertices and keeps how far they reach */
    private static class Bounds implements VertexConsumer {
        final float[] min = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        final float[] max = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            min[0] = Math.min(min[0], x); min[1] = Math.min(min[1], y); min[2] = Math.min(min[2], z);
            max[0] = Math.max(max[0], x); max[1] = Math.max(max[1], y); max[2] = Math.max(max[2], z);
            return this;
        }

        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
