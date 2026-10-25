package ebf.tim.utility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * One inventory slot of a transport. 1.12 TiM drew and clicked these itself; for now this is only the storage
 * (the transport GUIs come later), with the 1.12 calls the models and transports make.
 * todo: GUI position and slot logic from the 1.12 class
 */
public class ItemStackSlot {
    private final int slotID;
    public final int xPos, yPos;
    private ItemStack stack = ItemStack.EMPTY;
    public Item overlay;

    public ItemStackSlot(Object host, int slotID, int xPos, int yPos) {
        this.slotID = slotID;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public ItemStackSlot setOverlay(Item item) {
        overlay = item;
        return this;
    }

    public int getSlotID() { return slotID; }

    /** null when empty, like 1.7 TiM: the models count cargo that way */
    public ItemStack getStack() { return stack.isEmpty() ? null : stack; }

    public boolean getHasStack() { return !stack.isEmpty(); }

    public void setStack(ItemStack s) { stack = s == null ? ItemStack.EMPTY : s; }
}
