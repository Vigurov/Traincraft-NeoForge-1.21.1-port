package fexcraft.tmt.slim;

import com.mojang.math.Axis;

/**
 * Stand-in for the fixed-function OpenGL calls the 1.12 TMT models make (there is no such GL in 1.21): the matrix
 * calls go to {@link RenderContext#pose}, colour to the context, state switches that have no meaning any more are
 * ignored. Models only change their import from org.lwjgl.opengl.GL11 to this class.
 */
public final class GL11 {
    public static final int GL_QUADS = 7, GL_QUAD_STRIP = 8, GL_TRIANGLES = 4, GL_TRIANGLE_STRIP = 5, GL_LINE_STRIP = 3, GL_POLYGON = 9, GL_TRIANGLE_FAN = 6, GL_LINES = 1,
            GL_CULL_FACE = 2884, GL_LIGHTING = 2896, GL_BLEND = 3042, GL_TEXTURE_2D = 3553, GL_ALPHA_TEST = 3008,
            GL_SRC_ALPHA = 770, GL_ONE_MINUS_SRC_ALPHA = 771, GL_COMPILE = 4864, GL_SMOOTH = 7425, GL_FLAT = 7424,
            GL_RESCALE_NORMAL = 32826, GL_NORMALIZE = 2977, GL_DEPTH_TEST = 2929, GL_COLOR_MATERIAL = 2903,
            GL_LIGHT0 = 16384, GL_LIGHT1 = 16385, GL_FRONT_AND_BACK = 1032, GL_AMBIENT_AND_DIFFUSE = 5634;

    private GL11() {}

    public static void glPushMatrix() { RenderContext.pose.pushPose(); RenderContext.depth++; }
    public static void glPopMatrix() { RenderContext.pose.popPose(); RenderContext.depth--; }
    public static void glTranslatef(float x, float y, float z) { RenderContext.pose.translate(x, y, z); }
    public static void glTranslated(double x, double y, double z) { RenderContext.pose.translate(x, y, z); }
    public static void glScalef(float x, float y, float z) { RenderContext.pose.scale(x, y, z); }
    public static void glScaled(double x, double y, double z) { RenderContext.pose.scale((float) x, (float) y, (float) z); }

    public static void glRotatef(float deg, float x, float y, float z) {
        if (deg == 0) return;
        if (x == 0 && y == 0 && z == 0) return;
        if (y == 0 && z == 0) RenderContext.pose.mulPose((x > 0 ? Axis.XP : Axis.XN).rotationDegrees(deg));
        else if (x == 0 && z == 0) RenderContext.pose.mulPose((y > 0 ? Axis.YP : Axis.YN).rotationDegrees(deg));
        else if (x == 0 && y == 0) RenderContext.pose.mulPose((z > 0 ? Axis.ZP : Axis.ZN).rotationDegrees(deg));
        else {
            float l = (float) Math.sqrt(x * x + y * y + z * z);
            RenderContext.pose.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(deg), x / l, y / l, z / l));
        }
    }
    public static void glRotated(double deg, double x, double y, double z) { glRotatef((float) deg, (float) x, (float) y, (float) z); }

    public static void glColor4f(float r, float g, float b, float a) { RenderContext.r = r; RenderContext.g = g; RenderContext.b = b; RenderContext.a = a; }
    public static void glColor3f(float r, float g, float b) { glColor4f(r, g, b, 1); }

    // lighting switches: models turn lighting off for lamps; untextured drawing uses a white texture
    public static void glDisable(int cap) {
        if (cap == GL_LIGHTING) RenderContext.fullBright = true;
        if (cap == GL_TEXTURE_2D) RenderContext.untextured = true;
    }
    public static void glEnable(int cap) {
        if (cap == GL_LIGHTING) RenderContext.fullBright = false;
        if (cap == GL_TEXTURE_2D) RenderContext.untextured = false;
    }

    // state that has no meaning in the 1.21 pipeline
    public static void glBlendFunc(int a, int b) {}
    public static void glShadeModel(int mode) {}
    public static void glNormal3f(float x, float y, float z) {}
    public static void glAlphaFunc(int f, float ref) {}
    public static void glDepthMask(boolean flag) {}
    public static void glColorMaterial(int face, int mode) {}
    public static void glLineWidth(float w) {}
    public static void glClear(int mask) {}
    public static final int GL_COLOR_BUFFER_BIT = 16384, GL_DEPTH_BUFFER_BIT = 256;
}
