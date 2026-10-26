package train.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import train.blocks.bench.TrainWorkbenchMenu;

/** the train workbench screen (1.12 GuiTrainCraftingBlock): Traincraft's crafting_table.png */
public class GuiTrainCraftingBlock extends AbstractContainerScreen<TrainWorkbenchMenu> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/crafting_table.png");

	public GuiTrainCraftingBlock(TrainWorkbenchMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		titleLabelX = 8;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		renderTooltip(g, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
		g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
