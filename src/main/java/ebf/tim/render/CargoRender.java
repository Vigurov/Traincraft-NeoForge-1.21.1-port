package ebf.tim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.WalkShapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * The load of an open car as it is carried: one material (the one most of the slots hold), heaped. Bulk things
 * (sand, gravel, ore, coal, grain) as a heap with a ridge along the car, the fuller the higher, a full one a little
 * over its sides; the texture at its true size. Things that are no block show as the block they are kept in
 * (coal as coal block, wheat as hay, ingots as their metal's block); other goods ride in barrels.
 */
public final class CargoRender {
    private CargoRender() {}

    /** the heap's grid, in blocks */
    private static final float STEP = 0.25f;

    public static void render(GenericRailTransport t, float yaw, float pitch, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!t.showsItsLoad() || t.backBogie == null || t.inventory == null || t.inventory.isEmpty()) return;
        String ids = t.entityData().get(GenericRailTransport.CARGO_ITEMS);
        if (ids.isEmpty()) return;
        String[] list = ids.split(" ");
        //the material: what most slots hold
        Map<String, Integer> count = new HashMap<>();
        String main = list[0];
        for (String id : list) if (count.merge(id, 1, Integer::sum) > count.getOrDefault(main, 0)) main = id;
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(main));
        BlockState bulk = bulkOf(item);
        WalkShapes.Shape s = t.collisionHandler.shape;
        float fill = Math.min(1f, list.length / (float) t.inventory.size());
        pose.pushPose();
        pose.translate(0, t.backBogie.yOffset, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(-pitch));
        if (bulk != null) heap(s, fill, bulk, pose, buffers, light);
        else barrels(s, fill, pose, buffers, light);
        pose.popPose();
    }

    /** the block a thing is carried as, heaped, or null for goods (barrels) */
    private static BlockState bulkOf(Item item) {
        if (item instanceof BlockItem bi) {
            BlockState st = bi.getBlock().defaultBlockState();
            return st.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) ? st : null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        Block b = switch (path) {
            case "coal", "charcoal", "dust.coal" -> Blocks.COAL_BLOCK;
            case "wheat" -> Blocks.HAY_BLOCK;
            case "redstone" -> Blocks.REDSTONE_BLOCK;
            case "lapis_lazuli" -> Blocks.LAPIS_BLOCK;
            case "clay_ball" -> Blocks.CLAY;
            case "snowball" -> Blocks.SNOW_BLOCK;
            case "slime_ball" -> Blocks.SLIME_BLOCK;
            case "bone_meal", "bone" -> Blocks.BONE_BLOCK;
            case "dried_kelp" -> Blocks.DRIED_KELP_BLOCK;
            case "nether_wart" -> Blocks.NETHER_WART_BLOCK;
            case "ingot.steel" -> block("traincraft:blocksteel");
            case "ingot.aluminum" -> block("traincraft:blockaluminum");
            default -> null;
        };
        if (b != null) return b.defaultBlockState();
        //iron_ingot -> iron_block, raw_iron -> raw_iron_block, diamond -> diamond_block ...
        for (String name : new String[]{path.replace("_ingot", "") + "_block", path + "_block"}) {
            Block found = block(id.getNamespace() + ":" + name);
            if (found != null && found != Blocks.AIR) return found.defaultBlockState();
        }
        return null;
    }

    private static Block block(String id) {
        ResourceLocation r = ResourceLocation.tryParse(id);
        return r == null || !BuiltInRegistries.BLOCK.containsKey(r) ? null : BuiltInRegistries.BLOCK.get(r);
    }

    /** the load's top over the rails: level, the fuller the higher, up to just under the walls' top */
    private static float height(WalkShapes.Shape s, float fill, float x, float z, float halfLong, float halfWide) {
        float depth = s.wall() - s.floor() - 0.06f;
        return s.floor() + Math.max(0.02f, fill * depth);
    }

    private static void heap(WalkShapes.Shape s, float fill, BlockState st, PoseStack pose, MultiBufferSource buffers, int light) {
        float halfLong = s.halfInside(), halfWide = s.halfWidth() - s.thickness();
        int na = Math.max(1, Math.round(2 * halfLong / STEP)), nw = Math.max(1, Math.round(2 * halfWide / STEP));
        float da = 2 * halfLong / na, dw = 2 * halfWide / nw;
        TextureAtlasSprite tex = topOf(st);
        VertexConsumer c = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose p = pose.last();
        for (int i = 0; i < na; i++) {
            for (int j = 0; j < nw; j++) {
                float x0 = -halfLong + da * i, x1 = x0 + da, z0 = -halfWide + dw * j, z1 = z0 + dw;
                float h00 = height(s, fill, x0, z0, halfLong, halfWide), h10 = height(s, fill, x1, z0, halfLong, halfWide);
                float h11 = height(s, fill, x1, z1, halfLong, halfWide), h01 = height(s, fill, x0, z1, halfLong, halfWide);
                //the texture at its true size: one block's worth per block, wherever the cell lies in it
                float u0 = frac(x0), u1 = u0 + da, v0 = frac(z0), v1 = v0 + dw;
                float nx = (h00 - h10 + h01 - h11) / (2 * da), nz = (h00 - h01 + h10 - h11) / (2 * dw);
                float nl = (float) Math.sqrt(nx * nx + 1 + nz * nz);
                vertex(c, p, x0, h00, z0, tex.getU(u0), tex.getV(v0), nx / nl, 1 / nl, nz / nl, light);
                vertex(c, p, x0, h01, z1, tex.getU(u0), tex.getV(v1), nx / nl, 1 / nl, nz / nl, light);
                vertex(c, p, x1, h11, z1, tex.getU(u1), tex.getV(v1), nx / nl, 1 / nl, nz / nl, light);
                vertex(c, p, x1, h10, z0, tex.getU(u1), tex.getV(v0), nx / nl, 1 / nl, nz / nl, light);
            }
        }
    }

    /** the texture of the block's top (hay's ends, not its banded side), its particle one when it has no top */
    private static TextureAtlasSprite topOf(BlockState st) {
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(st);
        var quads = model.getQuads(st, net.minecraft.core.Direction.UP, net.minecraft.util.RandomSource.create(42));
        return quads.isEmpty() ? model.getParticleIcon(net.neoforged.neoforge.client.model.data.ModelData.EMPTY) : quads.get(0).getSprite();
    }

    private static float frac(float v) {
        float f = v - (float) Math.floor(v);
        //the cells divide a block evenly, so one never runs over a block's edge
        return f > 1 - 1e-4f ? 0 : f;
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose p, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        c.addVertex(p, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    /** goods: barrels standing on the floor, row by row, as many as the load is big */
    private static void barrels(WalkShapes.Shape s, float fill, PoseStack pose, MultiBufferSource buffers, int light) {
        float halfLong = s.halfInside(), halfWide = s.halfWidth() - s.thickness();
        float size = Math.min(0.5f, Math.min(halfWide, s.wall() - s.floor()));
        int na = Math.max(1, (int) (2 * halfLong / size)), nw = Math.max(1, (int) (2 * halfWide / size));
        int layers = Math.max(1, (int) ((s.wall() - s.floor() + size * 0.5f) / size));
        int total = na * nw * layers, n = Math.max(1, Math.round(total * fill));
        var blocks = Minecraft.getInstance().getBlockRenderer();
        BlockState barrel = Blocks.BARREL.defaultBlockState();
        float ox = -na * size / 2, oz = -nw * size / 2;
        for (int k = 0; k < n; k++) {
            int layer = k / (na * nw), rest = k % (na * nw), i = rest / nw, j = rest % nw;
            pose.pushPose();
            pose.translate(ox + i * size, s.floor() + layer * size, oz + j * size);
            pose.scale(size, size, size);
            blocks.renderSingleBlock(barrel, pose, buffers, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }
}
