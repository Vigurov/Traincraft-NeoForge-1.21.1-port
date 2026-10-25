package traincompat;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** an IIcon backed by a block atlas sprite; the name is the 1.7.10 style "namespace:texture" */
public class SpriteIcon implements IIcon {
    public final TextureAtlasSprite sprite;
    private final String name;

    public SpriteIcon(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        this.name = sprite.contents().name().toString().replace(":block/", ":");
    }

    @Override public int getIconWidth() { return sprite.contents().width(); }
    @Override public int getIconHeight() { return sprite.contents().height(); }
    @Override public float getMinU() { return sprite.getU0(); }
    @Override public float getMaxU() { return sprite.getU1(); }
    @Override public float getInterpolatedU(double u) { return sprite.getU((float) u / 16f); }
    @Override public float getMinV() { return sprite.getV0(); }
    @Override public float getMaxV() { return sprite.getV1(); }
    @Override public float getInterpolatedV(double v) { return sprite.getV((float) v / 16f); }
    @Override public String getIconName() { return name; }
}
