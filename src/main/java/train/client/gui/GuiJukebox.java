package train.client.gui;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.networking.PacketInteract;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import train.client.JukeboxClient;
import train.core.network.PacketSetJukeboxStreamingUrl;
import train.entity.rollingStock.EntityJukeBoxCart;

/**
 * The jukebox screen (1.12 GuiJukebox): the stream link, Play/Pause, Paste, Clear, this player's volume and the
 * lock.
 */
public class GuiJukebox extends Screen {
	private static final int W = 352, H = 120;
	private final EntityJukeBoxCart jukebox;
	private EditBox streamTextBox;
	private Button lock;

	public GuiJukebox(EntityJukeBoxCart jukebox) {
		super(Component.translatable("item.traincraft.jukeboxcart"));
		this.jukebox = jukebox;
	}

	@Override
	protected void init() {
		int cx = width / 2, cy = height / 2;
		streamTextBox = new EditBox(font, cx - W / 2 + 10, cy - H / 2 + 50, W - 20, 16, Component.empty());
		streamTextBox.setMaxLength(1000);
		streamTextBox.setValue(jukebox.getStreamURL());
		addRenderableWidget(streamTextBox);
		setInitialFocus(streamTextBox);
		addRenderableWidget(Button.builder(Component.literal("Play/Pause"), b -> send(!jukebox.isPlaying())).bounds(cx - 45, cy + 30, 90, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Paste"), b -> streamTextBox.setValue(minecraft.keyboardHandler.getClipboard().trim()))
				.bounds(cx - 45 - 120, cy + 30, 90, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Clear"), b -> streamTextBox.setValue("")).bounds(cx - 45 + 120, cy + 30, 90, 20).build());
		addRenderableWidget(Button.builder(Component.literal("+"), b -> jukebox.volume = Math.min(1, jukebox.volume + 0.1f)).bounds(cx - 70, cy + 30, 20, 20).build());
		addRenderableWidget(Button.builder(Component.literal("-"), b -> jukebox.volume = Math.max(0, jukebox.volume - 0.1f)).bounds(cx + 50, cy + 30, 20, 20).build());
		lock = addRenderableWidget(Button.builder(lockText(), b -> PacketDistributor.sendToServer(new PacketInteract(6, jukebox.getId())))
				.bounds(cx - W / 2, cy - H / 2 - 12, 60, 12).build());
	}

	private Component lockText() {
		return Component.literal(jukebox.getBoolean(GenericRailTransport.boolValues.LOCKED) ? "Locked" : "Unlocked");
	}

	private void send(boolean play) {
		PacketDistributor.sendToServer(new PacketSetJukeboxStreamingUrl(jukebox.getId(), streamTextBox.getValue().trim(), play));
	}

	@Override
	public void tick() {
		lock.setMessage(lockText());
		if (jukebox.isRemoved()) onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		int x0 = width / 2 - W / 2, y0 = height / 2 - H / 2;
		g.drawString(font, "Paste the link below. (Only .m3u and .pls radio streams)", x0 + 10, height / 2 - 30, 0xff0e0e0e, false);
		g.drawString(font, "Volume: " + (int) Math.ceil(jukebox.volume * 100), width / 2 - 26, height / 2 + 18, 0xff0e0e0e, false);
		String state = jukebox.isPlaying() ? "Playing" : "Stopped";
		String error = JukeboxClient.error(jukebox);
		if (jukebox.isPlaying() && error != null) state = "Can't play: " + error;
		g.drawString(font, font.plainSubstrByWidth(state, W - 20), x0 + 10, y0 + 8, error != null && jukebox.isPlaying() ? 0xffaa0000 : 0xff0e0e0e, false);
	}

	@Override
	public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(g, mouseX, mouseY, partialTick);
		int x0 = width / 2 - W / 2, y0 = height / 2 - H / 2;
		//1.12 drew a light grey panel with a dark frame
		g.fill(x0 - 2, y0 - 2, x0 + W + 2, y0 + H + 2, 0xff373737);
		g.fill(x0, y0, x0 + W, y0 + H, 0xffc6c6c6);
	}
}
