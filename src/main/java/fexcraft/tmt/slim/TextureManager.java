package fexcraft.tmt.slim;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.21 texture binding for TMT models: binding means switching the render context's buffer.
 * todo: skin recolouring (the 1.12 bindTexture with colour lists) and block textures for TiM rails
 */
public final class TextureManager {
    public static Map<Object, int[]> ingotColors = new HashMap<>();

    private TextureManager() {}

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
}
