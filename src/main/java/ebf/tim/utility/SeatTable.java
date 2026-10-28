package ebf.tim.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The seats found in each coach's model (data/traincraft/transport_seats.json, made by the autotest's seatscan
 * from the models as drawn): for each, where along and across it is and the height of the seat's top over the
 * rails. Read on both sides, the server too.
 */
public final class SeatTable {
    private SeatTable() {}

    private static Map<String, float[][]> seats;

    /** the seats of the transport with that registry name (path), or null */
    public static float[][] of(String id) {
        if (seats == null) {
            seats = new HashMap<>();
            try (InputStream in = SeatTable.class.getClassLoader().getResourceAsStream("data/traincraft/transport_seats.json")) {
                if (in != null) {
                    JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    for (var e : o.entrySet()) {
                        var arr = e.getValue().getAsJsonArray();
                        float[][] s = new float[arr.size()][];
                        for (int i = 0; i < s.length; i++) {
                            var a = arr.get(i).getAsJsonArray();
                            s[i] = new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat(), a.size() > 3 ? a.get(3).getAsFloat() : 0, a.size() > 4 ? a.get(4).getAsFloat() : 9};
                        }
                        seats.put(e.getKey(), s);
                    }
                }
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger("traincraft").warn("could not read transport_seats.json", ex);
            }
        }
        return seats.get(id);
    }
}
