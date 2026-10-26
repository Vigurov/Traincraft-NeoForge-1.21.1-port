package ebf.tim.gui;

import ebf.tim.entities.GenericRailTransport;
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
 * <h2>inventory menu of trains and rollingstock</h2>
 * The 1.12 layout (TransportSlotManager / GUITransport): the cargo grid on the left, the player inventory on the
 * right, fuel and water slots above it. Positions are the 1.12 ones; the screen's image starts {@link #OX},
 * {@link #OY} up and left of the 1.12 GUI origin so everything is inside it.
 */
public class TransportMenu extends AbstractContainerMenu {
    public static final int OX = 105, OY = 40;
    public final GenericRailTransport transport;
    public final int transportSlots;

    public static TransportMenu fromNetwork(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        return new TransportMenu(id, inv, (GenericRailTransport) inv.player.level().getEntity(buf.readVarInt()));
    }

    public TransportMenu(int id, Inventory playerInv, GenericRailTransport transport) {
        super(TiMMenus.TRANSPORT.get(), id);
        this.transport = transport;
        Container c = new SlotsContainer(transport.inventory, transport, transport::stillValid);
        int i = 0;
        for (ItemStackSlot s : transport.inventory) {
            addSlot(new Slot(c, i++, s.xPos + OX, s.yPos + OY) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(getContainerSlot(), stack);
                }
            });
        }
        transportSlots = i;
        //player toolbar
        for (int iT = 0; iT < 9; iT++) {
            addSlot(new Slot(playerInv, iT, 113 + (iT * 18) + OX, 142 + OY));
        }
        //player inventory
        for (int ir = 0; ir < 3; ir++) {
            for (int ic = 0; ic < 9; ic++) {
                addSlot(new Slot(playerInv, ((ir * 9) + ic) + 9, 113 + (ic * 18) + OX, 84 + (ir * 18) + OY));
            }
        }
    }

    /** shift click: player inventory to the transport (fuel/water slots first, as their filters allow) and back */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < transportSlots) {
            if (!moveItemStackTo(stack, transportSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            //the special slots (fuel 400+) are after the cargo: try them first
            int special = transportSlots;
            for (int s = 0; s < transportSlots; s++) {
                if (transport.inventory.get(s).getSlotID() >= 400) { special = s; break; }
            }
            if (!moveItemStackTo(stack, special, transportSlots, false) && !moveItemStackTo(stack, 0, special, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return transport != null && transport.stillValid(player);
    }
}
