package train.client;

import ebf.tim.entities.GenericRailTransport;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The player standing on a train turns with it on curves, every frame: turning their view once a tick (the
 * train's own steps) made the camera jump 20 times a second. Their position goes with the train in
 * GenericRailTransport.carryRiders, which the frame interpolation already smooths.
 */
public final class RoofRideClient {
    private RoofRideClient() {}

    private static float lastYaw = Float.NaN;
    /** all the turning given so far (the autotest compares it with the player's) */
    public static double turned;
    private static GenericRailTransport lastTrain;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(RoofRideClient::onFrame);
    }

    private static void onFrame(RenderFrameEvent.Pre e) {
        Minecraft mc = Minecraft.getInstance();
        GenericRailTransport t = GenericRailTransport.roofRide;
        if (mc.player == null || mc.level == null || t == null || t.isRemoved() || t.level() != mc.level
                || mc.level.getGameTime() - GenericRailTransport.roofRideTime > 1 || mc.isPaused()) {
            lastYaw = Float.NaN;
            lastTrain = null;
            return;
        }
        float yaw = Mth.rotLerp(e.getPartialTick().getGameTimeDeltaPartialTick(false), t.yRotO, t.getYRot());
        if (t == lastTrain && !Float.isNaN(lastYaw)) {
            float d = Mth.wrapDegrees(yaw - lastYaw);
            //a whole half turn at once is the train's yaw flipping end for end, not a turn
            if (Math.abs(d) < 30) {
                mc.player.setYRot(mc.player.getYRot() + d);
                turned += d;
                mc.player.yRotO += d;
                mc.player.setYHeadRot(mc.player.getYHeadRot() + d);
            }
        }
        lastYaw = yaw;
        lastTrain = t;
    }
}
