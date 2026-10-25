package ebf.tim.utility;

import com.mojang.blaze3d.platform.InputConstants;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.networking.PacketInteract;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * <h2>Keybind management</h2>
 * The 1.12 TiM controls (EventManager): in the driver's seat W and S step the throttle, holding one of them
 * for two seconds goes to full power that way (TC style) until released, space holds the brake, H the horn.
 */
public class ClientKeyHandler {
    private static final String CATEGORY = "Trains in Motion";
    public static final KeyMapping KeyHorn = new KeyMapping("Use Horn/Whistle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping KeyLamp = new KeyMapping("Lamp Toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, CATEGORY);
    public static final KeyMapping KeyInventory = new KeyMapping("Open Train/rollingstock GUI", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    /**1.12 started the engine from the locomotive's GUI, which is not ported yet*/
    public static final KeyMapping KeyIgnition = new KeyMapping("Start/stop engine", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY);

    /**0 both, 1 TC only, 2 TiM only (1.12 client setting)*/
    public static int controls = 1;
    private static int holdTimer = 0;
    private static boolean forwardWas, backWas, jumpWas;

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(KeyHorn);
        event.register(KeyLamp);
        event.register(KeyInventory);
        event.register(KeyIgnition);
    }

    private static void send(int key, GenericRailTransport t) {
        PacketDistributor.sendToServer(new PacketInteract(key, t.getId()));
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || !(player.getVehicle() instanceof GenericRailTransport t)) {
            forwardWas = backWas = jumpWas = false;
            holdTimer = 0;
            return;
        }
        boolean forward = mc.options.keyUp.isDown(), back = mc.options.keyDown.isDown(), jump = mc.options.keyJump.isDown();

        while (KeyLamp.consumeClick()) {
            send(5, t);
        }
        while (KeyInventory.consumeClick()) {
            send(1, t);
        }
        boolean loco = t instanceof EntityTrainCore && t.isControlSeat(player);
        while (KeyIgnition.consumeClick()) {
            if (loco) send(8, t);
        }
        while (KeyHorn.consumeClick()) {
            if (loco) send(9, t);
        }
        if (loco) {
            //key presses: step the throttle (the yaw vector points front to back, so forward is negative)
            if (forward && !forwardWas) {
                if (holdTimer < 15 && controls != 1) {
                    send(2, t);
                }
                if (t.getAccelerator() > 6) {
                    send(16, t);
                }
            } else if (back && !backWas) {
                if (holdTimer < 15 && controls != 1) {
                    send(3, t);
                }
                if (t.getAccelerator() < -6) {
                    send(16, t);
                }
            }
            //hold space for the brake
            if (jump != jumpWas) {
                send(15, t);
            }

            //holding a key: full power that way
            if (forward || back) {
                if (controls == 1 && holdTimer < 40) {
                    holdTimer = 40;
                }
                if (controls != 2) {
                    if (holdTimer == 40) {
                        send(forward ? 12 : 11, t);
                        holdTimer++;
                    } else if (holdTimer < 40) {
                        holdTimer++;
                    }
                }
            } else {
                if (holdTimer > 40) {
                    send(controls != 1 ? 4 : 14, t);
                }
                holdTimer = 0;
            }
        }
        forwardWas = forward;
        backWas = back;
        jumpWas = jump;
    }
}
