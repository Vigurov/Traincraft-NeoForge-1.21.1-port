package fexcraft.tmt.slim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * TMT's tessellator for 1.21: polygons go into the {@link RenderContext} vertex consumer (entity render types draw
 * quads, so triangles repeat their last vertex and bigger polygons are fanned into quads).
 * @Author EternalBlueFlame (1.12), ported
 */
public class Tessellator {

    public static Tessellator INSTANCE = new Tessellator();

    private static Float x, y, z;
    private final List<float[]> pending = new ArrayList<>();
    private int mode = GL11.GL_POLYGON;
    /**a normal for the next draw() (OBJ models know theirs), else it is worked out from each polygon*/
    private float[] nextNormal;

    public void setNormal(float x, float y, float z) {
        nextNormal = new float[]{x, y, z};
    }

    public static Tessellator getInstance() {
        return INSTANCE;
    }

    /** immediate-mode style drawing: vertices are collected until draw() */
    public void startDrawing(int mode) {
        x = null; y = null; z = null;
        this.mode = mode;
        pending.clear();
    }

    /**draws what was added since startDrawing, split up like OpenGL would for the mode*/
    public void draw() {
        List<float[]> p = pending;
        float[] n = nextNormal;
        nextNormal = null;
        if (n != null) {
            //the normal applies to the whole draw: one polygon per call in the OBJ loader
            if (p.size() >= 3) {
                if (mode == GL11.GL_QUADS || p.size() == 4 || p.size() == 3) emit(p, 1f, n[0], n[1], n[2]);
                else for (int i = 1; i + 1 < p.size(); i++) emit(List.of(p.get(0), p.get(i), p.get(i + 1)), 1f, n[0], n[1], n[2]);
            }
            pending.clear();
            return;
        }
        switch (mode) {
            case GL11.GL_QUADS:
                for (int i = 0; i + 3 < p.size(); i += 4) emit(p.subList(i, i + 4), 1f, 0, 0, 0);
                break;
            case GL11.GL_QUAD_STRIP:
                for (int i = 0; i + 3 < p.size(); i += 2) emit(List.of(p.get(i), p.get(i + 1), p.get(i + 3), p.get(i + 2)), 1f, 0, 0, 0);
                break;
            case GL11.GL_TRIANGLES:
                for (int i = 0; i + 2 < p.size(); i += 3) emit(p.subList(i, i + 3), 1f, 0, 0, 0);
                break;
            case GL11.GL_TRIANGLE_STRIP:
                for (int i = 0; i + 2 < p.size(); i++) emit(List.of(p.get(i), p.get(i + 1), p.get(i + 2)), 1f, 0, 0, 0);
                break;
            case GL11.GL_LINES: case GL11.GL_LINE_STRIP:
                break;
            default:
                if (p.size() >= 3) emit(p, 1f, 0, 0, 0);
        }
        pending.clear();
    }

    public void addVertex(float i, float j, float k) {
        addVertexWithUV(i, j, k, 0, 0);
    }

    public void addVertexWithUV(float i, float j, float k, float u, float v) {
        if (x != null) { i += x; j += y; k += z; }
        pending.add(new float[]{i, j, k, u, v});
    }

    public static void setTranslation(float xOffset, float yOffset, float zOffset) {
        x = xOffset; y = yOffset; z = zOffset;
    }

    public static void addTranslation(float xOffset, float yOffset, float zOffset) {
        x += xOffset; y += yOffset; z += zOffset;
    }

    public static void bindTexture(ResourceLocation uri) {
        TextureManager.bindTexture(uri);
    }

    public void drawTexturedVertsWithNormal(TexturedPolygon polygon, float scale) {
        List<TexturedVertex> v = polygon.vertices;
        if (v.size() < 3) return;
        Vec3f n = new Vec3f(v.get(1).vector3F.subtract(v.get(2).vector3F)).crossProduct(v.get(1).vector3F.subtract(v.get(0).vector3F)).normalize();
        List<float[]> pts = new ArrayList<>(v.size());
        for (TexturedVertex t : v) {
            float px = t.vector3F.xCoord, py = t.vector3F.yCoord, pz = t.vector3F.zCoord;
            if (x != null) { px += x; py += y; pz += z; }
            pts.add(new float[]{px * scale, py * scale, pz * scale, t.textureX, t.textureY});
        }
        emit(pts, 1f, n.xCoord, n.yCoord, n.zCoord);
    }

    /**
     * the part's matrix changed as Unflicker says, in the transport's frame: moved, and (a side wall) squeezed across
     * toward its outside
     */
    private static org.joml.Matrix4f unflickered(float[] u, org.joml.Matrix4f m) {
        org.joml.Matrix4f f = RenderContext.unflickerFrame;
        org.joml.Matrix4f inFrame = new org.joml.Matrix4f().translation(u[0], u[1], u[2]);
        if (u.length > 4) inFrame.translate(0, 0, u[3]).scale(1, 1, u[4]).translate(0, 0, -u[3]);
        return new org.joml.Matrix4f(f).mul(inFrame).mul(new org.joml.Matrix4f(f).invert()).mul(m);
    }

    /** quads as they are, triangles with the last vertex twice, larger polygons as a fan of quads */
    private static void emit(List<float[]> pts, float unused, float nx, float ny, float nz) {
        if (RenderContext.classify != null) {
            org.joml.Matrix4f m = new org.joml.Matrix4f(RenderContext.classifyFrame).mul(RenderContext.pose.last().pose());
            float[] b = RenderContext.classify.computeIfAbsent(RenderContext.currentPart, k -> new float[]{
                    Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE});
            for (float[] p : pts) {
                org.joml.Vector3f v = m.transformPosition(p[0], p[1], p[2], new org.joml.Vector3f());
                b[0] = Math.min(b[0], v.x); b[1] = Math.min(b[1], v.y); b[2] = Math.min(b[2], v.z);
                b[3] = Math.max(b[3], v.x); b[4] = Math.max(b[4], v.y); b[5] = Math.max(b[5], v.z);
            }
            return;
        }
        if (RenderContext.measure != null) {
            org.joml.Matrix4f m = RenderContext.pose.last().pose();
            if (RenderContext.unflickerMeasure && RenderContext.unflicker != null && RenderContext.unflickerFrame != null) {
                float[] u = RenderContext.unflicker.get(RenderContext.currentPart);
                if (u != null) m = unflickered(u, m);
            }
            org.joml.Vector3f[] face = new org.joml.Vector3f[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                float[] p = pts.get(i);
                face[i] = m.transformPosition(p[0], p[1], p[2], new org.joml.Vector3f());
                RenderContext.measure.add(face[i]);
            }
            if (RenderContext.measureFaces != null) RenderContext.measureFaces.add(face);
            if (RenderContext.measureFacePart != null) RenderContext.measureFacePart.add(RenderContext.currentPart);
            if (RenderContext.measureFaceUV != null) {
                float[] uv = new float[pts.size() * 2];
                for (int i = 0; i < pts.size(); i++) { uv[2 * i] = pts.get(i)[3]; uv[2 * i + 1] = pts.get(i)[4]; }
                RenderContext.measureFaceUV.add(uv);
                RenderContext.measureFaceTex.add(RenderContext.untextured ? null : RenderContext.texture);
            }
            if (RenderContext.measureFaceUp != null) {
                float mx = nx, my = ny, mz = nz;
                if (mx == 0 && my == 0 && mz == 0 && pts.size() >= 3) {
                    Vec3f a = new Vec3f(pts.get(0)[0], pts.get(0)[1], pts.get(0)[2]);
                    Vec3f b = new Vec3f(pts.get(1)[0], pts.get(1)[1], pts.get(1)[2]);
                    Vec3f d = new Vec3f(pts.get(2)[0], pts.get(2)[1], pts.get(2)[2]);
                    Vec3f n = new Vec3f(b.subtract(d)).crossProduct(b.subtract(a)).normalize();
                    mx = n.xCoord; my = n.yCoord; mz = n.zCoord;
                }
                RenderContext.measureFaceUp.add(m.transformDirection(mx, my, mz, new org.joml.Vector3f()).normalize().y);
            }
            return;
        }
        VertexConsumer c = RenderContext.consumerNow();
        if (c == null) return;
        PoseStack.Pose pose = RenderContext.pose.last();
        if (RenderContext.unflicker != null && RenderContext.unflickerFrame != null) {
            float[] u = RenderContext.unflicker.get(RenderContext.currentPart);
            if (u != null) {
                PoseStack moved = new PoseStack();
                moved.last().pose().set(unflickered(u, pose.pose()));
                moved.last().normal().set(pose.normal());
                pose = moved.last();
            }
        }
        if (RenderContext.shiftZ != null && RenderContext.shiftFrame != null) {
            Float dz = RenderContext.shiftZ.get(RenderContext.currentPart);
            if (dz != null) {
                //across the transport, whatever turns the part has of its own
                org.joml.Vector3f d = RenderContext.shiftFrame.transformDirection(0, 0, dz, new org.joml.Vector3f());
                PoseStack moved = new PoseStack();
                moved.last().pose().set(new org.joml.Matrix4f().translation(d).mul(pose.pose()));
                moved.last().normal().set(pose.normal());
                pose = moved.last();
            }
        }
        if (nx == 0 && ny == 0 && nz == 0) {
            Vec3f a = new Vec3f(pts.get(0)[0], pts.get(0)[1], pts.get(0)[2]);
            Vec3f b = new Vec3f(pts.get(1)[0], pts.get(1)[1], pts.get(1)[2]);
            Vec3f d = new Vec3f(pts.get(2)[0], pts.get(2)[1], pts.get(2)[2]);
            Vec3f n = new Vec3f(b.subtract(d)).crossProduct(b.subtract(a)).normalize();
            nx = n.xCoord; ny = n.yCoord; nz = n.zCoord;
        }
        RenderContext.faceLight = -1;
        if ((RenderContext.interiorBox != null || RenderContext.cutY == RenderContext.cutY) && RenderContext.interiorFrameInv != null) {
            float cx = 0, cy = 0, cz = 0;
            for (float[] p : pts) { cx += p[0]; cy += p[1]; cz += p[2]; }
            org.joml.Vector3f m = new org.joml.Matrix4f(RenderContext.interiorFrameInv).mul(pose.pose())
                    .transformPosition(cx / pts.size(), cy / pts.size(), cz / pts.size(), new org.joml.Vector3f());
            if (m.y < RenderContext.cutY) return;
            float[] b = RenderContext.interiorBox;
            if (b != null && m.x > b[0] && m.x < b[1] && m.y > b[2] && m.y < b[3] && m.z > b[4] && m.z < b[5]) RenderContext.faceLight = RenderContext.interiorLight;
        }
        if (pts.size() == 4) {
            for (float[] p : pts) put(c, pose, p, nx, ny, nz);
        } else if (pts.size() == 3) {
            for (float[] p : pts) put(c, pose, p, nx, ny, nz);
            put(c, pose, pts.get(2), nx, ny, nz);
        } else {
            for (int i = 1; i + 1 < pts.size(); i += 2) {
                put(c, pose, pts.get(0), nx, ny, nz);
                put(c, pose, pts.get(i), nx, ny, nz);
                put(c, pose, pts.get(i + 1), nx, ny, nz);
                put(c, pose, pts.get(Math.min(i + 2, pts.size() - 1)), nx, ny, nz);
            }
        }
    }

    private static void put(VertexConsumer c, PoseStack.Pose pose, float[] p, float nx, float ny, float nz) {
        float u = p[3], v = p[4];
        if (RenderContext.untextured) {
            var w = RenderContext.white();
            u = (w.getU0() + w.getU1()) * 0.5f;
            v = (w.getV0() + w.getV1()) * 0.5f;
        }
        c.addVertex(pose, p[0], p[1], p[2])
                .setColor(RenderContext.r, RenderContext.g, RenderContext.b, RenderContext.a)
                .setUv(u, v)
                .setOverlay(RenderContext.overlay)
                .setLight(RenderContext.lightNow())
                .setNormal(pose, nx, ny, nz);
    }

    /** kept for callers of the 1.12 API; normals are computed per polygon now */
    public static void setNormal(Vec3f vec0, Vec3f vec1, Vec3f vec2) {}
}
