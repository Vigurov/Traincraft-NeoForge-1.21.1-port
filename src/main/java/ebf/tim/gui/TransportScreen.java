package ebf.tim.gui;

import ebf.tim.TrainsInMotion;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.networking.PacketInteract;
import ebf.tim.utility.FluidStack;
import ebf.tim.utility.ItemStackSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * <h1>Transport GUI</h1>
 * the inventory screen of trains and rollingstock: the 1.12 GUITransport layout and buttons.
 * @author Eternal Blue Flame
 */
public class TransportScreen extends AbstractContainerScreen<TransportMenu> {
    private static final ResourceLocation CHEST = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HOVER = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation FLAME = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");

    private final GenericRailTransport transport;
    /**the 1.12 GUI origin (guiLeft, guiTop)*/
    private int gx, gy;
    private int yCenter;
    private final List<Btn> buttons = new ArrayList<>();

    /** a 1.12 GUIButton: icon, hover text, tint when on, an interaction id to send */
    private record Btn(int x, int y, String icon, Supplier<String> hover, BooleanSupplier on, int[] onColor, Runnable click) {}

    public TransportScreen(TransportMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        transport = menu.transport;
        imageWidth = 290 + TransportMenu.OX;
        imageHeight = 190 + TransportMenu.OY;
    }

    private void send(int key) {
        PacketDistributor.sendToServer(new PacketInteract(key, transport.getId()));
    }

    private Btn toggle(int x, int y, String icon, String hover, GenericRailTransport.boolValues value, int key) {
        return new Btn(gx + x, gy + y, icon, () -> hover + "." + transport.getBoolean(value), () -> transport.getBoolean(value),
                value == GenericRailTransport.boolValues.LAMP ? new int[]{255, 255, 60} : new int[]{150, 255, 150}, () -> send(key));
    }

    @Override
    protected void init() {
        super.init();
        gx = leftPos + TransportMenu.OX;
        gy = topPos + TransportMenu.OY;
        yCenter = (int) ((11 - transport.getInventoryRows()) * 0.5f) * 18;
        buttons.clear();
        //generic to all
        if (minecraft.player.getName().getString().equals(transport.getOwnerName())) {
            buttons.add(new Btn(gx + 112, gy + 166, "crowbar", () -> "gui.unlink", () -> false, null, () -> send(13)));
        }
        buttons.add(toggle(130, 166, "transportkey", "gui.locked", GenericRailTransport.boolValues.LOCKED, 6));
        buttons.add(new Btn(gx + 148, gy + 166, null,
                () -> "gui.coupler." + (transport.getBoolean(GenericRailTransport.boolValues.COUPLINGFRONT) || transport.getBoolean(GenericRailTransport.boolValues.COUPLINGBACK)),
                () -> transport.getBoolean(GenericRailTransport.boolValues.COUPLINGFRONT) || transport.getBoolean(GenericRailTransport.boolValues.COUPLINGBACK),
                new int[]{150, 255, 150}, () -> send(7)));
        buttons.add(toggle(238, 166, null, "gui.lamp", GenericRailTransport.boolValues.LAMP, 5));
        buttons.add(toggle(220, 166, "guibrake", "gui.brake", GenericRailTransport.boolValues.BRAKE, 15));
        //train specific
        if (transport instanceof EntityTrainCore) {
            if (minecraft.player.isCreative()) {
                buttons.add(toggle(184, 166, null, "gui.creative", GenericRailTransport.boolValues.CREATIVE, 10));
            }
            if (transport.getTypes() != null && !transport.getTypes().contains(TrainsInMotion.transportTypes.STEAM)) {
                buttons.add(toggle(202, 166, null, "gui.running", GenericRailTransport.boolValues.RUNNING, 8));
            }
            buttons.add(new Btn(gx + 256, gy + 166, "guiwhistle", () -> "gui.horn", () -> false, null, () -> send(9)));
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        //the cargo grid, a chest without lid
        int rows = transport.getInventoryRows();
        if (rows > 0) {
            g.blit(CHEST, gx - 105, gy - 37 + yCenter, 0, 0, 176, 17);
            for (int i = 0; i < rows; i++) {
                g.blit(CHEST, gx - 105, i * 18 + (gy - 20) + yCenter, 0, 17, 176, 18);
            }
            g.blit(CHEST, gx - 105, rows * 18 + (gy - 20) + yCenter, 0, 215, 176, 7);
        }
        //the player inventory
        g.blit(CHEST, gx + 105, gy + 64, 0, 0, 176, 16);
        g.blit(CHEST, gx + 105, gy + 70, 0, 126, 176, 96);
        //fuel, water and tanker slots, with a faint picture of what goes in
        for (ItemStackSlot s : transport.inventory) {
            if (s.getSlotID() >= 400) {
                g.blit(CHEST, gx + s.xPos - 1, gy + s.yPos - 1, 7, 17, 18, 18);
                if (!s.getHasStack() && !s.getOverlay().isEmpty()) {
                    g.renderFakeItem(s.getOverlay(), gx + s.xPos, gy + s.yPos);
                    g.fill(gx + s.xPos, gy + s.yPos, gx + s.xPos + 16, gy + s.yPos + 16, 300, 0x88C6C6C6);
                }
            }
        }
        if (transport instanceof EntityTrainCore) {
            //the fire: how much of the burning item is left
            int burn = transport.entityData().get(GenericRailTransport.FUEL_CONSUMPTION);
            if (burn > 0) {
                int h = Math.min(14, (int) (burn / 18f * 14) + 1);
                g.blitSprite(FLAME, 14, 14, 0, 14 - h, gx + 114, gy + 15 - h, 14, h);
            }
        }
        renderTanks(g);
        //the buttons
        for (Btn b : buttons) {
            boolean hover = isIn(mouseX, mouseY, b.x, b.y, 18, 18);
            if (b.on.getAsBoolean() && b.onColor != null) {
                g.setColor(b.onColor[0] / 255f, b.onColor[1] / 255f, b.onColor[2] / 255f, 1);
            }
            g.blitSprite(hover ? BUTTON_HOVER : BUTTON, b.x, b.y, 18, 18);
            g.setColor(1, 1, 1, 1);
            if (b.icon != null) {
                g.blit(ResourceLocation.fromNamespaceAndPath("trainsinmotion", "textures/items/" + b.icon + ".png"), b.x + 1, b.y + 1, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    /** the fluid tanks: a bar each, filled with the fluid's texture */
    private void renderTanks(GuiGraphics g) {
        if (transport.getTankCapacity() == null) return;
        GenericRailTransport.FluidTankInfo[] tanks = transport.getTankInfo();
        for (int i = 0; i < tanks.length && i < transport.getTankCapacity().length; i++) {
            int x = gx + 186, y = gy + 40 + (-20 * i);
            g.fill(x - 1, y - 1, x + 91, y + 17, 0xFF373737);
            g.fill(x, y, x + 90, y + 16, 0xFF8B8B8B);
            FluidStack f = tanks[i] == null ? null : tanks[i].fluid;
            if (f != null && f.amount > 0 && tanks[i].capacity > 0) {
                int w = (int) Math.min(90, 90f * f.amount / tanks[i].capacity);
                var ext = IClientFluidTypeExtensions.of(f.getFluid());
                var sprite = minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture());
                int tint = ext.getTintColor();
                g.setColor((tint >> 16 & 255) / 255f, (tint >> 8 & 255) / 255f, (tint & 255) / 255f, 1);
                for (int dx = 0; dx < w; dx += 16) {
                    g.blit(x + dx, y, 0, Math.min(16, w - dx), 16, sprite);
                }
                g.setColor(1, 1, 1, 1);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        //drawn in render, at the 1.12 places
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(font, transport.getItem() == null ? transport.transportName() : transport.getItem().getDescription().getString(), gx - 95, gy - 30 + yCenter - (transport.getInventoryRows() > 0 ? 0 : 0), 0xFFFFFF);
        g.drawString(font, Component.translatable("container.inventory"), gx + 113, gy + 72, 0x404040, false);
        if (transport.getTypes().contains(TrainsInMotion.transportTypes.STEAM)) {
            g.drawString(font, "boiler heat: " + Math.round(transport.entityData().get(GenericRailTransport.BOILER_HEAT) / 100f) + "°C", gx + 140, gy + 20, 0xFFFFFF);
        }
        //tooltips
        for (Btn b : buttons) {
            if (isIn(mouseX, mouseY, b.x, b.y, 18, 18)) {
                g.renderTooltip(font, Component.translatable(b.hover.get()), mouseX, mouseY);
            }
        }
        if (transport.getTankCapacity() != null) {
            GenericRailTransport.FluidTankInfo[] tanks = transport.getTankInfo();
            for (int i = 0; i < tanks.length && i < transport.getTankCapacity().length; i++) {
                if (isIn(mouseX, mouseY, gx + 186, gy + 40 + (-20 * i), 90, 18)) {
                    FluidStack f = tanks[i] == null ? null : tanks[i].fluid;
                    String name = f != null && f.amount > 0 ? f.getLocalizedName()
                            : transport.getTankFilters() != null && transport.getTankFilters().length > i ? transport.getTankFilters()[i][0] : "";
                    g.renderTooltip(font, Component.literal(name + " " + (f == null ? 0 : f.amount) + "mb/" + tanks[i].capacity + "mb"), mouseX, mouseY);
                }
            }
        }
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Btn b : buttons) {
            if (isIn((int) mouseX, (int) mouseY, b.x, b.y, 18, 18)) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1));
                b.click.run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** checks if mouse cursor is within a specified rectangle */
    public static boolean isIn(int mouseX, int mouseY, int x, int y, int width, int height) {
        return (mouseY >= y && mouseY <= y + height) && (mouseX >= x && mouseX <= x + width);
    }
}
