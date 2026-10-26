package ebf.tim.gui;

import ebf.tim.utility.ItemStackSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/** a vanilla Container over a list of TiM ItemStackSlots, so menus can use vanilla slots on them */
public class SlotsContainer implements Container {
    public final List<ItemStackSlot> slots;
    private final ItemStackSlot.Host host;
    private final Predicate<Player> valid;

    public SlotsContainer(List<ItemStackSlot> slots, ItemStackSlot.Host host, Predicate<Player> valid) {
        this.slots = slots;
        this.host = host;
        this.valid = valid;
    }

    @Override public int getContainerSize() { return slots.size(); }

    @Override public boolean isEmpty() {
        for (ItemStackSlot s : slots) if (s.getHasStack()) return false;
        return true;
    }

    @Override public ItemStack getItem(int i) { return slots.get(i).getStackOrEmpty(); }

    @Override public ItemStack removeItem(int i, int count) {
        ItemStack s = slots.get(i).getStackOrEmpty();
        if (s.isEmpty() || count <= 0) return ItemStack.EMPTY;
        ItemStack taken = s.split(count);
        if (s.isEmpty()) slots.get(i).setStack(ItemStack.EMPTY);
        setChanged();
        return taken;
    }

    @Override public ItemStack removeItemNoUpdate(int i) {
        ItemStack s = slots.get(i).getStackOrEmpty();
        slots.get(i).setStack(ItemStack.EMPTY);
        return s;
    }

    @Override public void setItem(int i, ItemStack stack) {
        slots.get(i).setStack(stack);
        setChanged();
    }

    @Override public boolean canPlaceItem(int i, ItemStack stack) {
        return slots.get(i).isItemValid(stack) && !slots.get(i).isCraftingOutput();
    }

    @Override public void setChanged() { if (host != null) host.markDirty(); }

    @Override public boolean stillValid(Player player) { return valid == null || valid.test(player); }

    @Override public void clearContent() {
        for (ItemStackSlot s : slots) s.setStack(ItemStack.EMPTY);
    }
}
