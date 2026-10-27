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
        if (!t.showsItsLoad() || t.backBogie == null || t.frontBogie == null || t.inventory == null || t.inventory.isEmpty()) return;
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
        Grid g = GRIDS.get(t.getClass());
        if (g == null) return;
        //the model's along is from the back bogie to the front one; the yaw may point either way along it
        double r = Math.toRadians(yaw);
        double fx = t.frontBogie.posX - t.backBogie.posX, fz = t.frontBogie.posZ - t.backBogie.posZ;
        float sign = (fx * Math.cos(r) + fz * Math.sin(r)) >= 0 ? 1 : -1;
        pose.pushPose();
        pose.translate(0, t.backBogie.yOffset, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(-pitch));
        float k = sign * g.dir;
        pose.scale(k, 1, k);
        if (bulk != null) heap(g, s, fill, bulk, pose, buffers, light);
        else barrels(s, fill, pose, buffers, light);
        pose.popPose();
    }

    /**
     * the model seen from above, once per car type: the height of its first surface down every eighth of a block,
     * over the rails' top, in its own frame (x from the back bogie towards the front one times dir, z across),
     * every sixteenth of a block
     */
    private record Grid(float x0, float z0, float cell, int nx, int nz, float[] top, float dir) {
        float at(int i, int j) { return top[i * nz + j]; }
    }

    private static final Map<Class<?>, Grid> GRIDS = new HashMap<>();
    /** the cells the load covers, per car type and load height (cm) */
    private static final Map<String, boolean[]> COVER = new HashMap<>();

    /** measures the model of that car type once (before it is drawn: it renders the model into nothing) */
    public static void prepare(GenericRailTransport t) {
        if (!t.showsItsLoad() || GRIDS.containsKey(t.getClass())) return;
        GRIDS.put(t.getClass(), null);
        try {
            GenericRailTransport m = (GenericRailTransport) t.getType().create(t.level());
            if (m == null) return;
            m.setPos(0, 0, 0);
            float[] rp = m.rotationPoints();
            m.frontBogie = new ebf.tim.entities.EntityBogie(m, rp[1], 0, 0, true);
            m.backBogie = new ebf.tim.entities.EntityBogie(m, rp[0], 0, 0, false);
            float railY = 0.3f;
            m.frontBogie.yOffset = m.backBogie.yOffset = railY;
            m.renderData.needsModelUpdate = true;
            var verts = new java.util.ArrayList<org.joml.Vector3f>();
            var faces = new java.util.ArrayList<org.joml.Vector3f[]>();
            var up = new java.util.ArrayList<Float>();
            fexcraft.tmt.slim.RenderContext.begin(new PoseStack(), null, 0, null);
            fexcraft.tmt.slim.RenderContext.measure = verts;
            fexcraft.tmt.slim.RenderContext.measureFaces = faces;
            fexcraft.tmt.slim.RenderContext.measureFaceUp = up;
            RenderWagon.measuring = true;
            try {
                RenderWagon.doRender(m, m.getYRot(), 0, railY, false, null);
            } finally {
                RenderWagon.measuring = false;
                fexcraft.tmt.slim.RenderContext.measure = null;
                fexcraft.tmt.slim.RenderContext.measureFaces = null;
                fexcraft.tmt.slim.RenderContext.measureFaceUp = null;
                fexcraft.tmt.slim.RenderContext.end();
            }
            if (verts.isEmpty()) return;
            //only what one sees from above: a face turned down is not drawn from there (the DB gondola's floor
            //plate, turned over when it is empty, would keep the load off where it seems to be)
            if (up.size() == faces.size()) {
                var seen = new java.util.ArrayList<org.joml.Vector3f[]>();
                for (int f = 0; f < faces.size(); f++) if (up.get(f) > -0.01f) seen.add(faces.get(f));
                faces = seen;
            }
            float x0 = Float.MAX_VALUE, x1 = -Float.MAX_VALUE, z0 = Float.MAX_VALUE, z1 = -Float.MAX_VALUE;
            for (var v : verts) { x0 = Math.min(x0, v.x); x1 = Math.max(x1, v.x); z0 = Math.min(z0, v.z); z1 = Math.max(z1, v.z); }
            float cell = 0.0625f;
            //on the texture's eighths: the cells then never run over a repeat of it
            x0 = (float) Math.floor(x0 / cell) * cell;
            z0 = (float) Math.floor(z0 / cell) * cell;
            int nx = (int) Math.ceil((x1 - x0) / cell), nz = (int) Math.ceil((z1 - z0) / cell);
            float[] top = new float[nx * nz];
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < nz; j++) {
                    float h = rayDown(faces, x0 + cell * (i + 0.5f), z0 + cell * (j + 0.5f));
                    top[i * nz + j] = h == -Float.MAX_VALUE ? Float.NaN : h - railY;
                }
            }
            GRIDS.put(t.getClass(), new Grid(x0, z0, cell, nx, nz, top, rp[1] - rp[0] >= 0 ? 1 : -1));
            if (Boolean.getBoolean("tc.cargodebug")) {
                StringBuilder b = new StringBuilder("[TCTEST] cargogrid " + t.getClass().getSimpleName() + " x0 " + x0 + " z0 " + z0 + " " + nx + "x" + nz + "\n");
                for (int j = 0; j < nz; j++) {
                    for (int i = 0; i < nx; i++) {
                        float h = top[i * nz + j];
                        b.append(Float.isNaN(h) ? "  . " : String.format(java.util.Locale.ROOT, "%4.0f", h * 100));
                    }
                    b.append("\n");
                }
                System.out.println(b);
            }
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger("traincraft").warn("could not measure " + t.getClass().getSimpleName() + " for its load", e);
        }
    }

    /** the height of the first face a vertical ray down at (x, z) meets, or -MAX when none */
    private static float rayDown(java.util.List<org.joml.Vector3f[]> faces, float x, float z) {
        float best = -Float.MAX_VALUE;
        for (var f : faces) {
            for (int i = 1; i + 1 < f.length; i++) {
                var a = f[0]; var b = f[i]; var c = f[i + 1];
                float d = (b.z - c.z) * (a.x - c.x) + (c.x - b.x) * (a.z - c.z);
                if (Math.abs(d) < 1e-7f) continue;
                float l1 = ((b.z - c.z) * (x - c.x) + (c.x - b.x) * (z - c.z)) / d;
                float l2 = ((c.z - a.z) * (x - c.x) + (a.x - c.x) * (z - c.z)) / d;
                float l3 = 1 - l1 - l2;
                if (l1 < -1e-4f || l2 < -1e-4f || l3 < -1e-4f) continue;
                best = Math.max(best, l1 * a.y + l2 * b.y + l3 * c.y);
            }
        }
        return best;
    }

    /**
     * the cells the load at height y covers: from the middle of the car outwards over all that is lower than it,
     * stopping at what is higher (the walls), so it fills whatever the inside's shape is and never runs outside
     */
    private static boolean[] cover(Grid g, float y) {
        return COVER.computeIfAbsent(System.identityHashCode(g) + "@" + Math.round(y * 100), key -> {
            boolean[] in = new boolean[g.nx() * g.nz()];
            java.util.ArrayDeque<int[]> todo = new java.util.ArrayDeque<>();
            //seeds: the low cells in the middle third of the car's length, on its middle line
            int jm = Math.round((0 - g.z0()) / g.cell() - 0.5f);
            for (int i = g.nx() / 3; i < 2 * g.nx() / 3; i++) {
                for (int dj = -1; dj <= 1; dj++) {
                    int j = jm + dj;
                    if (j >= 0 && j < g.nz() && low(g, i, j, y)) todo.add(new int[]{i, j});
                }
            }
            while (!todo.isEmpty()) {
                int[] q = todo.poll();
                int idx = q[0] * g.nz() + q[1];
                if (in[idx]) continue;
                in[idx] = true;
                int[][] next = {{q[0] + 1, q[1]}, {q[0] - 1, q[1]}, {q[0], q[1] + 1}, {q[0], q[1] - 1}};
                for (int[] n : next) {
                    if (n[0] < 0 || n[1] < 0 || n[0] >= g.nx() || n[1] >= g.nz()) continue;
                    if (!in[n[0] * g.nz() + n[1]] && low(g, n[0], n[1], y)) todo.add(n);
                }
            }
            //one cell on into the walls round it: a cell only partly under a wall counted as the wall, and left a
            //gap along it; under the wall the load's edge is hidden (walls are thicker than a cell)
            boolean[] out = in.clone();
            for (int i = 0; i < g.nx(); i++) {
                for (int j = 0; j < g.nz(); j++) {
                    if (!in[i * g.nz() + j]) continue;
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            int a = i + di, b = j + dj;
                            if (a < 0 || b < 0 || a >= g.nx() || b >= g.nz()) continue;
                            float h = g.at(a, b);
                            if (!Float.isNaN(h) && h >= y - 0.01f) out[a * g.nz() + b] = true;
                        }
                    }
                }
            }
            return out;
        });
    }

    private static boolean low(Grid g, int i, int j, float y) {
        float h = g.at(i, j);
        return !Float.isNaN(h) && h < y - 0.01f;
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

    private static void heap(Grid g, WalkShapes.Shape s, float fill, BlockState st, PoseStack pose, MultiBufferSource buffers, int light) {
        float y = s.floor() + Math.max(0.02f, fill * (s.wall() - s.floor() - 0.06f));
        boolean[] in = cover(g, y);
        TextureAtlasSprite tex = topOf(st);
        //coal's block is smooth tiles at its own size: twice as fine it reads as loose lumps
        float grain = st.is(Blocks.COAL_BLOCK) ? 2 : 1;
        VertexConsumer c = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose p = pose.last();
        for (int i = 0; i < g.nx(); i++) {
            for (int j = 0; j < g.nz(); j++) {
                if (!in[i * g.nz() + j]) continue;
                float x0 = g.x0() + g.cell() * i, x1 = x0 + g.cell(), z0 = g.z0() + g.cell() * j, z1 = z0 + g.cell();
                float u0 = tile(x0, x1, grain), u1 = u0 + g.cell() * grain, v0 = tile(z0, z1, grain), v1 = v0 + g.cell() * grain;
                vertex(c, p, x0, y, z0, tex.getU(u0), tex.getV(v0), 0, 1, 0, light);
                vertex(c, p, x0, y, z1, tex.getU(u0), tex.getV(v1), 0, 1, 0, light);
                vertex(c, p, x1, y, z1, tex.getU(u1), tex.getV(v1), 0, 1, 0, light);
                vertex(c, p, x1, y, z0, tex.getU(u1), tex.getV(v0), 0, 1, 0, light);
            }
        }
    }

    /** from a to b: both ends and every multiple of step between them */
    private static float[] lines(float a, float b, float step) {
        java.util.List<Float> l = new java.util.ArrayList<>();
        l.add(a);
        for (float v = (float) (Math.floor(a / step) + 1) * step; v < b - 1e-4f; v += step) if (v > a + 1e-4f) l.add(v);
        l.add(b);
        float[] r = new float[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = l.get(i);
        return r;
    }

    /** where in its texture repeat a cell from a to b (inside one repeat) starts, 0..1 */
    private static float tile(float a, float b, float grain) {
        float mid = (a + b) / 2 * grain;
        return a * grain - (float) Math.floor(mid);
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
