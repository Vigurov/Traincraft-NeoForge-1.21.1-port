package ebf.tim.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Where one can walk inside each coach (data/traincraft/transport_walk.json, made by the autotest's seatscan from
 * the models as drawn): its floor seen from above, a sixteenth of a block a cell, the walls and seats left out, and
 * the ceiling's height. The ends of the walkable part are where its vestibules are: the doors out, and to the next
 * coach. Read on both sides.
 */
public final class WalkTable {
    private WalkTable() {}

    /** half the width of a player as they walk the aisle (they are wider, but the models are small) */
    private static final float RADIUS = 0.12f;
    /** across: the aisles of the models are as narrow as two pixels, a door one (within half a cell) */
    private static final float SIDE = 0.03f;

    public record Walk(float x0, float z0, float c, int nx, int nz, float floor, float ceil, String cells, float minAlong, float maxAlong) {
        char at(float along, float across) {
            int i = (int) Math.floor((along - x0) / c), j = (int) Math.floor((across - z0) / c);
            if (i < 0 || j < 0 || i >= nx || j >= nz) return '0';
            return cells.charAt(i * nz + j);
        }

        /** whether one can stand there */
        public boolean ok(float along, float across) {
            return at(along, across) != '0' && at(along + RADIUS, across) != '0' && at(along - RADIUS, across) != '0'
                    && at(along, across + SIDE) != '0' && at(along, across - SIDE) != '0';
        }

        /** the nearest place one can stand to that one, within 1.5, or null */
        public float[] nearest(float along, float across) {
            float[] best = null;
            float bd = 1.5f * 1.5f;
            for (int i = 0; i < nx; i++) for (int j = 0; j < nz; j++) {
                float a = x0 + c * (i + 0.5f), z = z0 + c * (j + 0.5f);
                float d = (a - along) * (a - along) + (z - across) * (z - across);
                if (d < bd && ok(a, z)) { bd = d; best = new float[]{a, z}; }
            }
            return best;
        }

        /** the nearest place in the aisle (where one can walk along both ways) to that one, else the nearest at all */
        public float[] nearestAisle(float along, float across) {
            float[] best = null;
            float bd = 2.5f * 2.5f;
            for (int i = 0; i < nx; i++) for (int j = 0; j < nz; j++) {
                float a = x0 + c * (i + 0.5f), z = z0 + c * (j + 0.5f);
                float d = (a - along) * (a - along) + (z - across) * (z - across);
                if (d < bd && ok(a, z) && ok(a + 0.5f, z) && ok(a - 0.5f, z)) { bd = d; best = new float[]{a, z}; }
            }
            return best != null ? best : nearest(along, across);
        }

        /** at the end of the walkable part: +1 the front, -1 the back, 0 not */
        public int end(float along) {
            return along > maxAlong - RADIUS - 0.2f ? 1 : along < minAlong + RADIUS + 0.2f ? -1 : 0;
        }
    }

    private static Map<String, Walk> walks;

    /** the walkable inside of the transport with that registry name (path), or null */
    public static Walk of(String id) {
        if (walks == null) {
            walks = new HashMap<>();
            try (InputStream in = WalkTable.class.getClassLoader().getResourceAsStream("data/traincraft/transport_walk.json")) {
                if (in != null) {
                    JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    for (var e : o.entrySet()) {
                        JsonObject w = e.getValue().getAsJsonObject();
                        float x0 = w.get("x0").getAsFloat(), c = w.get("c").getAsFloat();
                        int nx = w.get("nx").getAsInt(), nz = w.get("nz").getAsInt();
                        String cells = w.get("walk").getAsString();
                        float lo = Float.MAX_VALUE, hi = -Float.MAX_VALUE;
                        for (int k = 0; k < cells.length(); k++) {
                            if (cells.charAt(k) == '0') continue;
                            float a = x0 + c * (k / nz + 0.5f);
                            lo = Math.min(lo, a);
                            hi = Math.max(hi, a);
                        }
                        walks.put(e.getKey(), new Walk(x0, w.get("z0").getAsFloat(), c, nx, nz, w.get("floor").getAsFloat(),
                                w.get("ceil").getAsFloat(), cells, lo, hi));
                    }
                }
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger("traincraft").warn("could not read transport_walk.json", ex);
            }
        }
        return walks.get(id);
    }
}
