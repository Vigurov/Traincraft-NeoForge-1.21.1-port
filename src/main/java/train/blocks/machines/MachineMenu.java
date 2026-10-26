package train.blocks.machines;

import ebf.tim.gui.SlotsContainer;
import ebf.tim.utility.ItemStackSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import train.blocks.TCBlocks;

/** the menu of a Traincraft machine: its slots at the 1.12 places, the player inventory below, the progress values */
public class MachineMenu extends AbstractContainerMenu {
    public final MachineTile machine;
    public final ContainerData data;
    private final int machineSlots;

    public static MachineMenu fromNetwork(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String kind = buf.readUtf();
        MachineTile te = inv.player.level().getBlockEntity(pos) instanceof MachineTile t ? t
                : new MachineTile(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("traincraft", "block." + kind)).defaultBlockState());
        return new MachineMenu(id, inv, te, new SimpleContainerData(7));
    }

    public MachineMenu(int id, Inventory inv, MachineTile machine) {
        this(id, inv, machine, machine.data);
    }

    private MachineMenu(int id, Inventory playerInv, MachineTile machine, ContainerData data) {
        super(TCBlocks.MACHINE_MENU.get(), id);
        this.machine = machine;
        this.data = data;
        Container c = new SlotsContainer(machine.inventory, machine, p -> p.distanceToSqr(machine.getBlockPos().getCenter()) < 64);
        int i = 0;
        for (ItemStackSlot s : machine.inventory) {
            addSlot(new Slot(c, i++, s.xPos, s.yPos) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(getContainerSlot(), stack);
                }
            });
        }
        machineSlots = i;
        for (int ir = 0; ir < 3; ir++) {
            for (int ic = 0; ic < 9; ic++) {
                addSlot(new Slot(playerInv, ic + ir * 9 + 9, 8 + ic * 18, 84 + ir * 18));
            }
        }
        for (int iT = 0; iT < 9; iT++) {
            addSlot(new Slot(playerInv, iT, 8 + iT * 18, 142));
        }
        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(machine.getBlockPos().getCenter()) < 64;
    }
}
