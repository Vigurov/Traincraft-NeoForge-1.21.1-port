package traincompat;

/** 1.7.10 block/item icon: in 1.21 a sprite of the block atlas */
public interface IIcon {
    int getIconWidth();
    int getIconHeight();
    float getMinU();
    float getMaxU();
    float getInterpolatedU(double u);
    float getMinV();
    float getMaxV();
    float getInterpolatedV(double v);
    String getIconName();
}
