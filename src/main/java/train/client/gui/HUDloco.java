package train.client.gui;

import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.FluidStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static ebf.tim.TrainsInMotion.transportTypes.STEAM;

/**
 * The locomotive HUD of TC 4.5 (HUDloco: locohud.png / loco_hud_steam.png) for whoever sits in the driver's seat:
 * fuel, for steam the water and the boiler heat, the speedometer needle and the speed. 1.12 only had a debug
 * text placeholder here.
 */
public class HUDloco {
    private static final ResourceLocation HUD = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/locohud.png"),
            HUD_STEAM = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/loco_hud_steam.png");

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null
                || !(mc.player.getVehicle() instanceof EntityTrainCore loco) || !loco.isControlSeat(mc.player)) {
            return;
        }
        int windowHeight = g.guiHeight() - 100;
        boolean steam = loco.getTypes().contains(STEAM);
        ResourceLocation tex = steam ? HUD_STEAM : HUD;
        //TC4 getSpeed: km/h
        double speed = Math.abs(loco.getVelocity()) * 72;

        g.blit(tex, 10, windowHeight, 0, 150, 137, 90);

        GenericRailTransport.FluidTankInfo[] tanks = loco.getTankInfo();
        if (steam) {
            //water: a black bar from the top hides the empty part (49 pixels)
            int water = amount(tanks, 0), cap = capacity(tanks, 0);
            int w = cap <= 0 ? 0 : Math.max(0, Math.min(49, water * 49 / cap));
            g.blit(tex, 70, windowHeight + 17, 190, 169 + w, 6, 49 - w);
            //boiler heat instead of TC4's overheating
            float heat = loco.entityData().get(GenericRailTransport.BOILER_HEAT) / 100f, max = Math.max(1, loco.getMaxFuel() * 750);
            int h = (int) Math.max(0, Math.min(49, heat * 49 / max));
            g.blit(tex, 56, windowHeight + 17, 176, 169 + h, 5, 49 - h);
            //red screen when the boiler is dry but burning (TC4: no water put in)
            if (water <= 1 && loco.entityData().get(GenericRailTransport.FUEL_CONSUMPTION) > 0) {
                g.fillGradient(0, 0, g.guiWidth(), g.guiHeight(), 1615855616, -1602211792);
            }
        }

        //speedometer needle, 280 km/h at the top
        int needle = (int) Math.min(49, speed * 49 / 280);
        if (steam) {
            g.blit(tex, 84, windowHeight + 37 - needle + 20, 177, 149, 16, 8);
        } else {
            g.blit(tex, 75, windowHeight + 37 - needle + 20, 163, 150, 30, 5);
        }

        //fuel: steam the fire left (0..18 from the firebox), the others their fuel tank; 70 pixels
        int fuel;
        if (steam) {
            fuel = loco.entityData().get(GenericRailTransport.FUEL_CONSUMPTION) * 70 / 18;
        } else {
            int cap = capacity(tanks, 0);
            fuel = cap <= 0 ? 0 : amount(tanks, 0) * 70 / cap;
        }
        fuel = Math.max(0, Math.min(70, fuel));
        if (steam) {
            g.blit(tex, 34, windowHeight + 17, 154, 170 + fuel, 9, 70 - fuel);
        } else {
            g.blit(tex, 28, windowHeight + 11, 148, 150 + fuel, 7, 70 - fuel);
        }

        int h = steam ? 15 : 13;
        g.drawString(mc.font, "Speed:", 106, windowHeight + 7 + h, 0xFFFFFF);
        g.drawString(mc.font, "  " + (int) speed, 106, windowHeight + 18 + h, 0xFFFFFF);
        g.drawString(mc.font, " Km/h", 106, windowHeight + 29 + h, 0xFFFFFF);
        if (steam) {
            float heat = loco.entityData().get(GenericRailTransport.BOILER_HEAT) / 100f;
            g.drawString(mc.font, "State: " + (heat < 100 ? "cold" : heat < 0.5f * loco.getMaxFuel() * 750 ? "warm" : "hot"), 50, windowHeight + 80, 0xFFFFFF);
        }
    }

    private static int amount(GenericRailTransport.FluidTankInfo[] tanks, int i) {
        if (tanks == null || tanks.length <= i || tanks[i] == null) return 0;
        FluidStack f = tanks[i].fluid;
        return f == null ? 0 : f.amount;
    }

    private static int capacity(GenericRailTransport.FluidTankInfo[] tanks, int i) {
        return tanks == null || tanks.length <= i || tanks[i] == null ? 0 : tanks[i].capacity;
    }
}
