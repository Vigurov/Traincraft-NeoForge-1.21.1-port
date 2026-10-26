package ebf.tim.utility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * One inventory slot of a transport or a TiM table: its id (TiM numbers them: 40+ cargo, 400 fuel, 401 water...),
 * its GUI position, an overlay icon for empty fuel/water slots, and the stack.
 * In 1.12 this was also the container slot with TiM's own click handling; in 1.21 the menus use vanilla slots on
 * top of these (see TransportMenu), so only the storage and the 1.12 helper calls are kept.
 */
public class ItemStackSlot {
    /**what owns the slot: decides what fits and is told about changes*/
    public interface Host {
        boolean isItemValidForSlot(int slot, ItemStack stack);
        void markDirty();
    }

    private final Host host;
    private int slotID;
    public int xPos, yPos;
    private ItemStack stack = ItemStack.EMPTY;
    private ItemStack overlay = ItemStack.EMPTY;
    private boolean craftingOutput = false, craftingInput = false;

    public ItemStackSlot(Host host, int slotID, int xPos, int yPos) {
        this.host = host;
        this.slotID = slotID;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public ItemStackSlot(Object host, int slotID, int xPos, int yPos) {
        this(host instanceof Host h ? h : null, slotID, xPos, yPos);
    }

    public ItemStackSlot setCoords(int x, int y) {
        xPos = x;
        yPos = y;
        return this;
    }

    public ItemStackSlot setSlotID(int slot) {
        slotID = slot;
        return this;
    }

    public ItemStackSlot setOverlay(ItemLike item) {
        overlay = item == null ? ItemStack.EMPTY : new ItemStack(item);
        return this;
    }

    public ItemStackSlot setOverlay(ItemStack s) {
        overlay = s == null ? ItemStack.EMPTY : s;
        return this;
    }

    public ItemStack getOverlay() {
        return overlay;
    }

    public ItemStackSlot setCraftingOutput(boolean craft) {
        craftingOutput = craft;
        return this;
    }

    public boolean isCraftingOutput() {
        return craftingOutput;
    }

    public ItemStackSlot setCraftingInput(boolean craft) {
        craftingInput = craft;
        return this;
    }

    public boolean isCraftingInput() {
        return craftingInput;
    }

    public int getSlotID() { return slotID; }

    /** null when empty, like 1.7 TiM: the models count cargo that way */
    public ItemStack getStack() { return stack.isEmpty() ? null : stack; }

    /** the stack, empty instead of null */
    public ItemStack getStackOrEmpty() { return stack; }

    public boolean getHasStack() { return !stack.isEmpty(); }

    public Item getItem() { return stack.getItem(); }

    public int getStackSize() { return stack.getCount(); }

    public void setStack(ItemStack s) { stack = s == null ? ItemStack.EMPTY : s; }

    public boolean setSlotContents(ItemStack s, List<ItemStackSlot> hostInventory) {
        setStack(s);
        if (host != null) host.markDirty();
        return true;
    }

    public boolean setSlotStackSize(int size) {
        if (size <= 0) {
            stack = ItemStack.EMPTY;
        } else {
            stack.setCount(size);
        }
        if (host != null) host.markDirty();
        return true;
    }

    public boolean isItemValid(ItemStack s) {
        if (s == null || s.isEmpty()) return true;
        return host == null || host.isItemValidForSlot(slotID, s);
    }

    /** takes amount off the stack; returns what is left */
    public ItemStack decrStackSize(int amount) {
        if (stack.isEmpty() || amount >= stack.getCount()) {
            setSlotContents(ItemStack.EMPTY, null);
        } else {
            stack.shrink(amount);
            if (host != null) host.markDirty();
        }
        return stack;
    }

    public boolean contentEquals(ItemStack other) {
        if (stack.isEmpty() || other == null || other.isEmpty()) {
            return stack.isEmpty() && (other == null || other.isEmpty());
        }
        return ItemStack.isSameItemSameComponents(other, stack);
    }

    /** puts as much of the stack in here as fits; returns the rest (empty when all went in) */
    public ItemStack mergeStack(ItemStack itemStack, List<ItemStackSlot> hostInventory, int inventoryType) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isItemValid(itemStack)) {
            if (!getHasStack()) {
                setSlotContents(itemStack.copy(), hostInventory);
                return ItemStack.EMPTY;
            } else if (contentEquals(itemStack) && getStackSize() < stack.getMaxStackSize()) {
                int room = stack.getMaxStackSize() - getStackSize();
                if (itemStack.getCount() <= room) {
                    setSlotStackSize(getStackSize() + itemStack.getCount());
                    return ItemStack.EMPTY;
                } else {
                    setSlotStackSize(stack.getMaxStackSize());
                    ItemStack rest = itemStack.copy();
                    rest.shrink(room);
                    return rest;
                }
            }
        }
        return itemStack;
    }
}
