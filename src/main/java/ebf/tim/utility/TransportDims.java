package ebf.tim.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ebf.tim.entities.GenericRailTransport;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The measured size of every transport's model (data/traincraft/transport_dims.json, made by the autotest's
 * measure=1, which draws each model into nothing and collects its vertices): the lowest point, as a + s * the
 * height the renderer is given, and how far the model reaches to its front and its back from the centre.
 * The renderer stands the wheels on the rail with it; the couplings keep the ends this far apart.
 */
public final class TransportDims {
    private TransportDims() {}

    public record Dims(float a, float s, float front, float back) {}

    private static Map<String, Dims> byId;

    private static synchronized Map<String, Dims> all() {
        if (byId == null) {
            byId = new HashMap<>();
            InputStream in = TransportDims.class.getClassLoader().getResourceAsStream("data/traincraft/transport_dims.json");
            if (in != null) {
                try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                    for (var e : o.entrySet()) {
                        if (e.getKey().startsWith("_")) continue;
                        JsonObject d = e.getValue().getAsJsonObject();
                        byId.put(e.getKey(), new Dims(d.get("a").getAsFloat(), d.get("s").getAsFloat(), d.get("front").getAsFloat(), d.get("back").getAsFloat()));
                    }
                } catch (Exception ex) {
                    org.slf4j.LoggerFactory.getLogger("traincraft").warn("could not read transport_dims.json", ex);
                }
            }
        }
        return byId;
    }

    /** null if the transport was not measured */
    public static Dims of(GenericRailTransport t) {
        return all().get(BuiltInRegistries.ENTITY_TYPE.getKey(t.getType()).getPath());
    }
}
