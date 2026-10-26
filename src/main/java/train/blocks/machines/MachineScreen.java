package train.blocks.machines;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** the 1.12 machine GUIs: their pictures, fire, progress arrow and tank gauge */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private final ResourceLocation gui;
    private final String name;

    public MachineScreen(MachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        String kind = menu.machine.kind;
        gui = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/" + switch (kind) {
            case "distil" -> "gui_distillation_tower2.png";
            case "hearthfurnace" -> "gui_open_hearth_furnace.png";
            default -> "gui_generator_diesel.png";
        });
        name = switch (kind) {
            case "distil" -> "Distillation tower";
            case "hearthfurnace" -> "Open Hearth Furnace";
            default -> "Diesel Generator";
        };
    }

    private int burnScaled(int i) {
        int cur = menu.data.get(1) == 0 ? Math.max(1, menu.data.get(3)) : menu.data.get(1);
        return menu.data.get(0) * i / cur;
    }

    private int cookScaled(int i) {
        return menu.data.get(3) == 0 ? 0 : menu.data.get(2) * i / menu.data.get(3);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int j = leftPos, k = topPos;
        g.blit(gui, j, k, 0, 0, imageWidth, imageHeight);
        String kind = menu.machine.kind;
        if (!kind.equals("hearthfurnace")) {
            //the tank
            int liqui = Math.abs(menu.data.get(4) * 50 / 30000);
            g.blit(gui, j + 145, k + 57 - liqui, 177, 107 - liqui, 18, liqui);
        }
        if (menu.data.get(0) > 0) {
            int l = burnScaled(12);
            g.blit(gui, j + 56, k + 36 + 12 - l, 176, 12 - l, 14, l + 2);
            if (kind.equals("hearthfurnace")) {
                g.blit(gui, j + 36, k + 36 + 12 - l, 176, 12 - l, 14, l + 2);
            }
        }
        if (kind.equals("distil")) {
            g.blit(gui, j + 87, k + 36, 184, 15, cookScaled(22), 41);
        } else if (kind.equals("hearthfurnace")) {
            g.blit(gui, j + 79, k + 34, 176, 14, cookScaled(24) + 1, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, name, 8, 6, 0x404040, false);
        g.drawString(font, Component.translatable("container.inventory"), 8, imageHeight - 96 + 2, 0x404040, false);
        if (menu.machine.kind.equals("distil") && menu.machine.inventory.get(0).getHasStack()) {
            int chance = MachineTile.plasticChance(menu.machine.inventory.get(0).getStackOrEmpty());
            if (chance != 0) g.drawString(font, (100 / chance) + "%", 79, 70, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (!menu.machine.kind.equals("hearthfurnace") && mouseX >= leftPos + 143 && mouseX <= leftPos + 164 && mouseY >= topPos + 5 && mouseY <= topPos + 57) {
            String fluid = menu.data.get(4) == 0 ? "empty" : menu.data.get(5) == 1 ? "Fuel Oil" : "Diesel";
            g.renderComponentTooltip(font, java.util.List.of(Component.literal(fluid), Component.literal(menu.data.get(4) + "/30000")), mouseX, mouseY);
        }
        renderTooltip(g, mouseX, mouseY);
    }
}
