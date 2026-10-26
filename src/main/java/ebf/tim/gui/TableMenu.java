package ebf.tim.gui;

import ebf.tim.blocks.TileEntityStorage;
import ebf.tim.registry.TiMMenus;
import ebf.tim.utility.ItemStackSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * the menu of a TiM table: its slots at the 1.12 places, the result slots only give (taking a result uses up its
 * recipe), the player's inventory under them. Buttons 0/1 page the results back and forward.
 */
public class TableMenu extends AbstractContainerMenu {
    /** the rail table has slots above the 1.12 GUI origin: the image starts this much higher */
    public static final int OY = 12;
    public final TileEntityStorage table;
    public final int tableSlots;

    public static TableMenu fromNetwork(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        return new TableMenu(id, inv, (TileEntityStorage) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public TableMenu(int id, Inventory playerInv, TileEntityStorage table) {
        super(TiMMenus.TABLE.get(), id);
        this.table = table;
        Container c = new SlotsContainer(table.inventory, table,
                p -> !table.isRemoved() && p.distanceToSqr(table.getBlockPos().getCenter()) < 64);
        int i = 0;
        for (ItemStackSlot s : table.inventory) {
            if (s.isCraftingOutput()) {
                addSlot(new Slot(c, i++, s.xPos, s.yPos + OY) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                        if (!player.level().isClientSide) table.onCrafting(stack);
                        super.onTake(player, stack);
                    }
                });
            } else {
                addSlot(new Slot(c, i++, s.xPos, s.yPos + OY));
            }
        }
        tableSlots = i;
        //the number of result pages, for the page buttons
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return table.pages; }
            @Override public void set(int v) { table.pages = v; }
        });
        boolean assembly = table.isAssemblyTable();
        int hotbarY = (assembly ? 232 : 142) + OY, invY = (assembly ? 174 : 84) + OY;
        for (int iT = 0; iT < 9; iT++) {
            addSlot(new Slot(playerInv, iT, 8 + iT * 18, hotbarY));
        }
        for (int ir = 0; ir < 3; ir++) {
            for (int ic = 0; ic < 9; ic++) {
                addSlot(new Slot(playerInv, ((ir * 9) + ic) + 9, 8 + (ic * 18), invY + (ir * 18)));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < tableSlots) {
            //a result: one craft into the player inventory
            if (!moveItemStackTo(stack, tableSlots, slots.size(), true)) return ItemStack.EMPTY;
            if (table.inventory.get(index).isCraftingOutput()) {
                slot.onTake(player, copy);
                table.markDirty();
                return ItemStack.EMPTY;
            }
        } else {
            //into the input slots
            int firstOut = tableSlots;
            for (int s = 0; s < tableSlots; s++) if (table.inventory.get(s).isCraftingOutput()) { firstOut = s; break; }
            if (!moveItemStackTo(stack, 0, firstOut, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        table.changePage(id == 1);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return !table.isRemoved() && player.distanceToSqr(table.getBlockPos().getCenter()) < 64;
    }
}
