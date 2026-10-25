package traincompat;

/** the bit of LWJGL 2's Vector2f the track placement uses */
public class Vector2f {
    public float x, y;

    public Vector2f() {}

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
