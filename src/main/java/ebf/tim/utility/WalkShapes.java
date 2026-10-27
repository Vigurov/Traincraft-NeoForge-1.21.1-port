package ebf.tim.utility;

import java.util.HashMap;
import java.util.Map;

/**
 * What players walk on in and on the cars that are open: a floor, and around it walls for the ones built like a
 * tub (open wagons, hoppers, a tender's coal space), so they can climb in and walk about inside. The heights are
 * over the rails' top, the widths from the middle, all measured from the models as drawn (the autotest's
 * shapecheck, which casts rays down through each body). The covered cars and the locomotives stay solid boxes.
 */
public final class WalkShapes {
    private WalkShapes() {}

    /**
     * floor and wall tops over the rails, half the width, wall thickness, half the inside length (0: no walls), half
     * the body's length (beyond the inside, up to it, solid as high as the walls: a hopper's sloping ends)
     */
    public record Shape(float floor, float wall, float halfWidth, float thickness, float halfInside, float halfBody) {
        public boolean tub() { return halfInside > 0; }
    }

    private static final Map<String, Shape> BY_ID = new HashMap<>();

    private static void tub(String id, float floor, float wall, float halfWidth, float thickness, float halfInside, float halfBody) {
        BY_ID.put(id, new Shape(floor, wall, halfWidth, thickness, halfInside, halfBody));
    }

    private static void flat(String id, float floor) {
        BY_ID.put(id, new Shape(floor, floor, 0, 0, 0, 0));
    }

    static {
        tub("freightflatcartlogsdb", 0.532f, 1.470f, 0.750f, 0.150f, 1.725f, 1.875f);
        tub("freightcartus", 0.470f, 1.095f, 0.563f, 0.080f, 1.350f, 1.875f);
        tub("freightgtngorewagon", 0.353f, 1.033f, 0.625f, 0.080f, 0.525f, 2.250f);
        tub("freighthopperus", 0.704f, 1.535f, 0.563f, 0.150f, 0.400f, 1.875f);
        tub("freightminecartyellow", 0.407f, 1.026f, 0.464f, 0.152f, 0.412f, 0.688f);
        tub("freightopenredbrown", 0.532f, 1.345f, 0.562f, 0.150f, 1.225f, 1.375f);
        tub("freightopenwagon", 0.550f, 1.487f, 0.563f, 0.080f, 1.362f, 1.813f);
        tub("tenderbr01", 0.937f, 1.585f, 0.688f, 0.275f, 0.500f, 1.550f);
        //the DB gondola's model sits off to one side, which the rays could not read: by eye, from its hitbox
        tub("freightgondoladb", 0.61f, 1.48f, 0.70f, 0.08f, 1.78f, 1.88f);
        flat("bambooflatcarpassenger", 0.364f);
        flat("freightbulkheadflatcart", 0.535f);
        flat("flatcartdb", 0.532f);
        flat("freightflatcartrailsdb", 0.532f);
        flat("flatcart", 0.467f);
        flat("flatcartsu", 0.595f);
        flat("flatcartus", 0.532f);
        flat("freightflatcartwoodus", 0.532f);
        flat("bambooflatcarfreight", 0.990f);
        flat("freightdepressedflatcar", 0.220f);
        flat("freighticewagon", 0.912f);
        flat("freightslatewagon", 0.912f);
        flat("freighttrailer", 0.470f);
        flat("freightflatlogs", 0.532f);
        flat("tenderadler", 0.950f);
        flat("propagandabritain", 0.598f);
        flat("propagandajapan", 0.598f);
        flat("propagandausa", 0.598f);
        flat("propagandaussr", 0.598f);
        flat("jukeboxcart", 0.532f);
    }

    /** the shape of the transport with that registry name (path), or null when it is a solid one */
    public static Shape of(String id) {
        return BY_ID.get(id);
    }
}
