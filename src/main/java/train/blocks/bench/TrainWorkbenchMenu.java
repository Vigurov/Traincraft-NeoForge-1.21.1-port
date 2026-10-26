package train.blocks.bench;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import train.blocks.TCBlocks;

/**
 * The train workbench: the vanilla crafting table's slots and logic (result 124,35, grid 30,17 like 1.12), but the
 * grid is the block's own item list, so whatever is left in it stays there.
 */
public class TrainWorkbenchMenu extends AbstractContainerMenu {
	private final CraftingContainer craftSlots;
	private final ResultContainer resultSlots = new ResultContainer();
	private final ContainerLevelAccess access;
	private final Player player;

	public TrainWorkbenchMenu(int id, Inventory inv, NonNullList<ItemStack> grid, BlockEntity tile, ContainerLevelAccess access) {
		super(TCBlocks.WORKBENCH_MENU.get(), id);
		this.access = access;
		this.player = inv.player;
		this.craftSlots = new TransientCraftingContainer(this, 3, 3, grid) {
			@Override
			public void setChanged() {
				if (tile != null) tile.setChanged();
			}
		};
		addSlot(new ResultSlot(player, craftSlots, resultSlots, 0, 124, 35));
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				addSlot(new Slot(craftSlots, j + i * 3, 30 + j * 18, 17 + i * 18));
			}
		}
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 9; i++) {
				addSlot(new Slot(inv, i + k * 9 + 9, 8 + i * 18, 84 + k * 18));
			}
		}
		for (int l = 0; l < 9; l++) {
			addSlot(new Slot(inv, l, 8 + l * 18, 142));
		}
		slotsChanged(craftSlots);
	}

	public static TrainWorkbenchMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
		return new TrainWorkbenchMenu(id, inv, NonNullList.withSize(9, ItemStack.EMPTY), null, ContainerLevelAccess.NULL);
	}

	/** CraftingMenu.slotChangedCraftingGrid */
	@Override
	public void slotsChanged(Container inventory) {
		access.execute((level, pos) -> {
			if (level.isClientSide || !(player instanceof ServerPlayer sp)) return;
			CraftingInput input = craftSlots.asCraftInput();
			ItemStack result = ItemStack.EMPTY;
			var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
			if (recipe.isPresent() && resultSlots.setRecipeUsed(level, sp, recipe.get())) {
				ItemStack r = recipe.get().value().assemble(input, level.registryAccess());
				if (r.isItemEnabled(level.enabledFeatures())) result = r;
			}
			resultSlots.setItem(0, result);
			setRemoteSlot(0, result);
			sp.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, result));
		});
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, TCBlocks.PART_TABLE.get());
	}

	/** CraftingMenu.quickMoveStack */
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			copy = stack.copy();
			if (index == 0) {
				access.execute((level, pos) -> stack.getItem().onCraftedBy(stack, level, player));
				if (!moveItemStackTo(stack, 10, 46, true)) return ItemStack.EMPTY;
				slot.onQuickCraft(stack, copy);
			} else if (index >= 10 && index < 46) {
				if (!moveItemStackTo(stack, 1, 10, false)) {
					if (index < 37) {
						if (!moveItemStackTo(stack, 37, 46, false)) return ItemStack.EMPTY;
					} else if (!moveItemStackTo(stack, 10, 37, false)) {
						return ItemStack.EMPTY;
					}
				}
			} else if (!moveItemStackTo(stack, 10, 46, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
			if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, stack);
			if (index == 0) player.drop(stack, false);
		}
		return copy;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
	}
}
