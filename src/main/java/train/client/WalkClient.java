package train.client;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.networking.PacketWalk;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Walking inside a coach, on the player's own client: the movement keys move them along its aisle (the server
 * checks it and sees to the doors), drawn where they are at once; and a standing rider drawn standing.
 */
public final class WalkClient {
    private WalkClient() {}

    private static int car = -1;
    private static float[] local;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(WalkClient::tick);
        NeoForge.EVENT_BUS.addListener((RenderLivingEvent.Pre<?, ?> e) -> GenericRailTransport.renderingRider = e.getEntity());
        NeoForge.EVENT_BUS.addListener((RenderLivingEvent.Post<?, ?> e) -> GenericRailTransport.renderingRider = null);
        GenericRailTransport.localStand = (t, e) -> e == Minecraft.getInstance().player && t.getId() == car ? local : null;
    }

    private static void tick(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || mc.isPaused() || !(p.getVehicle() instanceof GenericRailTransport t) || !t.isStanding(p) || t.walk() == null) {
            local = null;
            car = -1;
            return;
        }
        float[] server = t.standOf(p);
        //new in this car, or put somewhere else by the server: from where the server has them
        if (local == null || car != t.getId() || Math.hypot(server[0] - local[0], server[1] - local[1]) > 1.0) {
            local = server.clone();
            car = t.getId();
        }
        float fwd = p.input.forwardImpulse, str = p.input.leftImpulse;
        if (fwd == 0 && str == 0) return;
        double yaw = Math.toRadians(p.getYRot());
        double mx = str * Math.cos(yaw) - fwd * Math.sin(yaw), mz = fwd * Math.cos(yaw) + str * Math.sin(yaw);
        double len = Math.hypot(mx, mz);
        //as fast as the shrunk rider walks
        double speed = (mc.options.keySprint.isDown() ? 0.2 : 0.14) * GenericRailTransport.SEATED_SCALE;
        mx *= speed / len;
        mz *= speed / len;
        Vec3 u = t.alongDir(), v = t.acrossDir();
        float da = (float) (mx * u.x + mz * u.z), dc = (float) (mx * v.x + mz * v.z);
        var w = t.walk();
        float a = local[0], c = local[1];
        //each way by itself, so one slides along a wall; along, a little to the side if needed (the aisles are
        //narrow: one keeps to their middle)
        //(or onto the middle of a cell across: a stair one cell wide)
        float mid = w.z0() + w.c() * ((float) Math.floor((c - w.z0()) / w.c()) + 0.5f);
        java.util.List<Float> tries = new java.util.ArrayList<>(java.util.List.of(c, c - 0.03f, c + 0.03f, mid, mid - w.c(), mid + w.c(), c - 0.06f, c + 0.06f, c - 0.1f, c + 0.1f));
        final float from = c;
        tries.sort((x, y) -> Float.compare(Math.abs(x - from), Math.abs(y - from)));
        for (float to : tries) {
            if (Math.abs(to - from) <= 0.1f && w.ok(a + da, to)) {
                a += da;
                c = to;
                break;
            }
        }
        if (w.ok(a, c + dc)) c += dc;
        if (a == local[0] && c == local[1]) {
            //at an end of the aisle, the server takes one through its door
            if (w.end(a + da) != 0) PacketDistributor.sendToServer(new PacketWalk(t.getId(), a + Math.signum(da) * 0.01f, c));
            return;
        }
        local[0] = a;
        local[1] = c;
        PacketDistributor.sendToServer(new PacketWalk(t.getId(), a, c));
    }
}
