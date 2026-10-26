package ebf.tim.gui;

import ebf.tim.blocks.TileEntityStorage;
import ebf.tim.utility.ItemStackSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * <h1>Train crafting table GUI</h1>
 * the 1.12 GUITrainTable: the Traincraft assembly tables use their own pictures (iron, steel and advanced age),
 * the TiM train table the crafting table one, the rail table a plain panel with labels.
 * @author Eternal Blue Flame
 * @author ComputerButter
 */
public class TableScreen extends AbstractContainerScreen<TableMenu> {
    private static final ResourceLocation[] ASSEMBLY = {
            ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/gui_tieri_ironage.png"),
            ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/gui_tierii_steelage.png"),
            ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/gui_tieriii_advancedage.png")};
    private static final ResourceLocation CRAFTING = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final ResourceLocation CHEST = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HOVER = ResourceLocation.withDefaultNamespace("widget/button_highlighted");

    private final TileEntityStorage table;
    private int gx, gy;

    public TableScreen(TableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        table = menu.table;
        imageWidth = 176;
        imageHeight = (table.isAssemblyTable() ? 256 : 166) + TableMenu.OY;
    }

    @Override
    protected void init() {
        super.init();
        gx = leftPos;
        gy = topPos + TableMenu.OY;
    }

    /** the page buttons of the TiM table (only when there is more than one page) */
    private int[][] pageButtons() {
        return table.isAssemblyTable() ? new int[][]{{gx + 145, gy + 126}, {gx + 145, gy + 144}} : new int[][]{{gx + 105, gy + 34}, {gx + 141, gy + 34}};
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (table.isAssemblyTable()) {
            int tier = table.table.charAt(table.table.length() - 1) - '1';
            g.blit(ASSEMBLY[Math.max(0, Math.min(2, tier))], gx, gy, 0, 0, 176, 256);
        } else if (table.table.equals("railtable")) {
            //a panel behind the rail slots
            g.fill(gx - 18, gy - 10, gx + 176, gy + 68, 0xFF555555);
            g.fill(gx - 17, gy - 9, gx + 175, gy + 67, 0xFFC6C6C6);
            drawSlots(g);
            g.blit(CHEST, gx, gy + 64, 0, 0, 176, 16);
            g.blit(CHEST, gx, gy + 78, 0, 134, 176, 88);
        } else {
            g.blit(CRAFTING, gx, gy, 0, 0, 176, 166);
            drawSlots(g);
        }
    }

    /** slot backgrounds, and a faint picture of what goes in the empty ones */
    private void drawSlots(GuiGraphics g) {
        for (ItemStackSlot s : table.inventory) {
            g.blit(CHEST, gx + s.xPos - 1, gy + s.yPos - 1, 7, 17, 18, 18);
            if (!s.getHasStack() && !s.getOverlay().isEmpty()) {
                g.renderFakeItem(s.getOverlay(), gx + s.xPos, gy + s.yPos);
                g.fill(gx + s.xPos, gy + s.yPos, gx + s.xPos + 16, gy + s.yPos + 16, 300, 0x88C6C6C6);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int oy = TableMenu.OY;
        if (table.table.equals("railtable")) {
            g.drawString(font, "Rail", 8, 2 + oy, 0xFFFFFF);
            g.drawString(font, "Ties", 6, 24 + oy, 0xFFFFFF);
            g.drawString(font, "Ballast", -8, 41 + oy, 0xFFFFFF);
            g.drawString(font, "Old Shape", 108, 18 + oy, 0xFFFFFF);
            g.drawString(font, "Output", 116, 53 + oy, 0xFFFFFF);
            g.drawString(font, Component.translatable("container.inventory"), 8, 72 + oy, 0x404040, false);
        } else if (table.isAssemblyTable()) {
            g.drawString(font, title, 8, 5 + oy, 12241200, false);
            g.drawString(font, Component.translatable("container.inventory"), 8, 256 - 92 + oy, 0x404040, false);
            g.drawString(font, Component.translatable("container.storage"), 8, 118 + oy, 0x404040, false);
            g.drawString(font, Component.translatable("container.output"), 90, 118 + oy, 12241200, false);
        } else {
            g.drawString(font, title, 28, 6 + oy, 0x404040, false);
            g.drawString(font, Component.translatable("container.inventory"), 8, 166 - 96 + 2 + oy, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (table.pages > 1) {
            String[] labels = {"<<", ">>"};
            int[][] b = pageButtons();
            for (int i = 0; i < 2; i++) {
                boolean hover = TransportScreen.isIn(mouseX, mouseY, b[i][0], b[i][1], 18, 18);
                g.blitSprite(hover ? BUTTON_HOVER : BUTTON, b[i][0], b[i][1], 18, 18);
                g.drawCenteredString(font, labels[i], b[i][0] + 9, b[i][1] + 5, 0xE0E0E0);
            }
        }
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (table.pages > 1) {
            int[][] b = pageButtons();
            for (int i = 0; i < 2; i++) {
                if (TransportScreen.isIn((int) mouseX, (int) mouseY, b[i][0], b[i][1], 18, 18)) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
