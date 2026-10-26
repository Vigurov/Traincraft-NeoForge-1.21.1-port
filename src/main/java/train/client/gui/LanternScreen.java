package train.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import train.blocks.lantern.LanternTile;
import train.blocks.lantern.PacketLantern;

/** the 1.12 lantern screen: a hex colour field, Done and Cancel */
public class LanternScreen extends Screen {
    private final LanternTile lantern;
    private EditBox colorField;
    private Button done;

    public LanternScreen(LanternTile lantern) {
        super(Component.translatable("block.traincraft.block.lantern"));
        this.lantern = lantern;
    }

    @Override
    protected void init() {
        colorField = new EditBox(font, width / 2 - 150, 60, 300, 20, Component.empty());
        colorField.setMaxLength(32767);
        colorField.setValue(lantern.getColorString());
        colorField.setResponder(s -> done.active = !s.trim().isEmpty());
        addRenderableWidget(colorField);
        setInitialFocus(colorField);
        done = addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> apply()).bounds(width / 2 - 100, height / 4 + 108, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(width / 2 - 100, height / 4 + 132, 200, 20).build());
    }

    private void apply() {
        String s = colorField.getValue().trim().replaceFirst("^#", "");
        if (s.length() == 6) {
            try {
                PacketDistributor.sendToServer(new PacketLantern(Integer.parseInt(s, 16), lantern.getBlockPos()));
            } catch (NumberFormatException ignored) {
            }
        }
        onClose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 257 || key == 335) {
            apply();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.fill(width / 2 - 150, 86, width / 2 - 130, 96, 0xFF000000 | lantern.getColor());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
