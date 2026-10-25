package fexcraft.tmt.slim;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.21 texture binding for TMT models: binding means switching the render context's buffer.
 * todo: skin recolouring (the 1.12 bindTexture with colour lists)
 */
public final class TextureManager {
    /**the average colour of each rail material's item texture, for drawing TiM rails*/
    public static Map<ItemStack, int[]> ingotColors = new HashMap<>();

    private TextureManager() {}

    /**
     * a texture location from 1.12 code: those used mixed case names (Windows and old Minecraft didn't mind),
     * the files are lower case and 1.21 only takes lower case
     */
    public static ResourceLocation loc(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace.toLowerCase(java.util.Locale.ROOT),
                path.toLowerCase(java.util.Locale.ROOT).replace(' ', '_'));
    }

    public static ResourceLocation parse(String location) {
        return ResourceLocation.parse(location.toLowerCase(java.util.Locale.ROOT).replace(' ', '_'));
    }

    public static int bindTexture(ResourceLocation textureURI) {
        RenderContext.bind(textureURI);
        return 0;
    }

    public static void bindTexture(ResourceLocation textureURI, int[] skinColorsFrom, int[] skinColorsTo, List<Integer> colorsFrom, List<Integer> colorsTo) {
        RenderContext.bind(textureURI);
    }

    public static boolean textureExists(ResourceLocation textureURI) {
        return Minecraft.getInstance().getResourceManager().getResource(textureURI).isPresent();
    }

    /**
     * the average colour of an item's texture (the rail colour of a TiM rail made from it), worked out once per item:
     * same as the 1.7.10 version, transparent and near black pixels are skipped
     */
    public static int[] getIngotColor(ItemStack s) {
        for (Map.Entry<ItemStack, int[]> e : ingotColors.entrySet()) {
            if (e.getKey().getItem() == s.getItem()) {
                return e.getValue();
            }
        }
        int red = 0, green = 0, blue = 0, divisor = 0;
        try {
            TextureAtlasSprite sprite = Minecraft.getInstance().getItemRenderer().getModel(s, null, null, 0).getParticleIcon();
            ResourceLocation name = sprite.contents().name();
            ResourceLocation file = ResourceLocation.fromNamespaceAndPath(name.getNamespace(), "textures/" + name.getPath() + ".png");
            try (InputStream in = Minecraft.getInstance().getResourceManager().open(file); NativeImage img = NativeImage.read(in)) {
                for (int y = 0; y < img.getHeight(); y++) {
                    for (int x = 0; x < img.getWidth(); x++) {
                        int c = img.getPixelRGBA(x, y); //ABGR
                        int a = (c >> 24) & 0xFF, b = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, r = c & 0xFF;
                        if (a <= 128 || r + g + b <= 20) {
                            continue;
                        }
                        red += 25 + r;
                        green += 25 + g;
                        blue += 25 + b;
                        divisor++;
                    }
                }
            }
        } catch (Exception e) {
            divisor = 0;
        }
        int[] color = divisor == 0 ? new int[]{160, 160, 160}
                : new int[]{Math.min(255, red / divisor), Math.min(255, green / divisor), Math.min(255, blue / divisor)};
        ingotColors.put(s.copy(), color);
        return color;
    }

    /**the block texture of a side of the block the item places (ballast and ties), in the block atlas*/
    public static TextureAtlasSprite bindBlockTextureFromSide(int side, ItemStack b) {
        RenderContext.bind(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
        if (!(b.getItem() instanceof BlockItem bi)) {
            return Minecraft.getInstance().getItemRenderer().getModel(b, null, null, 0).getParticleIcon();
        }
        BlockState state = bi.getBlock().defaultBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        List<BakedQuad> quads = model.getQuads(state, Direction.from3DDataValue(side), RandomSource.create(0));
        if (quads.isEmpty()) {
            return model.getParticleIcon();
        }
        return quads.get(0).getSprite();
    }
}
