package traincompat;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wavefront .obj model drawn like Forge 1.7.10 did: per group (g/o), triangles and quads, texture v flipped,
 * normals from the file or from the face. Each group is compiled into a display list on first draw.
 */
public class WavefrontObject implements IModelCustom {
    private static final class Face {
        float[][] v, t, n;
    }

    private static final class Group {
        final List<Face> faces = new ArrayList<>();
    }

    private final Map<String, Group> groups = new LinkedHashMap<>();

    public WavefrontObject(String name, BufferedReader in) throws IOException {
        List<float[]> v = new ArrayList<>(), t = new ArrayList<>(), n = new ArrayList<>();
        Group current = null;
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            String[] p = line.split("\\s+");
            switch (p[0]) {
                case "v":
                    v.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    break;
                case "vt":
                    t.add(new float[]{Float.parseFloat(p[1]), 1 - (p.length > 2 ? Float.parseFloat(p[2]) : 0)});
                    break;
                case "vn":
                    n.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                    break;
                case "g":
                case "o":
                    current = groups.computeIfAbsent(p.length > 1 ? p[1] : "default", k -> new Group());
                    break;
                case "f": {
                    if (current == null) {
                        current = groups.computeIfAbsent("default", k -> new Group());
                    }
                    int count = p.length - 1;
                    // split polygons into triangles fanning from the first vertex; quads stay quads
                    List<int[]> polys = new ArrayList<>();
                    if (count <= 4) {
                        int[] all = new int[count];
                        for (int i = 0; i < count; i++) all[i] = i;
                        polys.add(all);
                    } else {
                        for (int i = 1; i + 1 < count; i++) polys.add(new int[]{0, i, i + 1});
                    }
                    for (int[] poly : polys) {
                        Face f = new Face();
                        f.v = new float[poly.length][];
                        f.t = new float[poly.length][];
                        f.n = new float[poly.length][];
                        for (int i = 0; i < poly.length; i++) {
                            String[] idx = p[poly[i] + 1].split("/");
                            f.v[i] = v.get(index(idx[0], v.size()));
                            if (idx.length > 1 && !idx[1].isEmpty()) f.t[i] = t.get(index(idx[1], t.size()));
                            if (idx.length > 2 && !idx[2].isEmpty()) f.n[i] = n.get(index(idx[2], n.size()));
                        }
                        current.faces.add(f);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private static int index(String s, int size) {
        int i = Integer.parseInt(s);
        return i < 0 ? size + i : i - 1;
    }

    private static float[] faceNormal(Face f) {
        float[] a = f.v[0], b = f.v[1], c = f.v[2];
        float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
        float vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
        float x = uy * vz - uz * vy, y = uz * vx - ux * vz, z = ux * vy - uy * vx;
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        return l == 0 ? new float[]{0, 1, 0} : new float[]{x / l, y / l, z / l};
    }

    /** draws a group: it used to be a display list, now the faces go through the TMT tessellator into the
     * render context (whose pose and texture the track renderer sets) */
    private static void draw(Group g) {
        fexcraft.tmt.slim.Tessellator t = fexcraft.tmt.slim.Tessellator.getInstance();
        for (Face f : g.faces) {
            t.startDrawing(f.v.length == 4 ? fexcraft.tmt.slim.GL11.GL_QUADS : fexcraft.tmt.slim.GL11.GL_TRIANGLES);
            for (int i = 0; i < f.v.length; i++) {
                float u = f.t[i] != null ? f.t[i][0] : 0, v = f.t[i] != null ? f.t[i][1] : 0;
                t.addVertexWithUV(f.v[i][0], f.v[i][1], f.v[i][2], u, v);
            }
            t.draw();
        }
    }

    @Override public String getType() { return "obj"; }

    @Override
    public void renderAll() {
        for (Group g : groups.values()) draw(g);
    }

    @Override
    public void renderOnly(String... names) {
        for (String s : names) {
            Group g = groups.get(s);
            if (g != null) draw(g);
        }
    }

    @Override
    public void renderPart(String name) {
        renderOnly(name);
    }

    @Override
    public void renderAllExcept(String... excluded) {
        Set<String> ex = new HashSet<>(Arrays.asList(excluded));
        for (Map.Entry<String, Group> e : groups.entrySet()) {
            if (!ex.contains(e.getKey())) draw(e.getValue());
        }
    }
}
