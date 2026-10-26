package train.blocks.machines;

import ebf.tim.registry.TiMFluids;
import ebf.tim.registry.TiMItems;
import ebf.tim.registry.TiMOres;
import ebf.tim.utility.FluidStack;
import ebf.tim.utility.ItemStackSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import train.blocks.TCBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Traincraft machines, the 1.12 tiles in one class by kind:
 * <ul>
 * <li>distil: petroleum ore, oil, reeds, wheat or leaves burn into diesel / fuel oil (30 buckets tank) and sometimes
 * plastic; buckets and canisters in the container slot are filled from the tank</li>
 * <li>hearthfurnace: iron and graphite make steel</li>
 * <li>dieselgenerator: burns diesel from its tank into energy (FE) for the blocks next to it, while powered by redstone</li>
 * </ul>
 * Slot ids and positions are the 1.12 ones.
 */
public class MachineTile extends BlockEntity implements ItemStackSlot.Host {
    public final String kind;
    public final List<ItemStackSlot> inventory = new ArrayList<>();
    public FluidStack tank = null;
    public int burnTime, currentItemBurnTime, cookTime, cookDuration;
    public boolean powered;
    public final EnergyStorage energy = new EnergyStorage(320, 80);
    private final Random random = new Random();

    public MachineTile(BlockPos pos, BlockState state) {
        super(TCBlocks.MACHINE_TILE.get(), pos, state);
        kind = state.getBlock() instanceof MachineBlock m ? m.kind : "distil";
        switch (kind) {
            case "distil" -> {
                cookDuration = 400;
                inventory.add(new ItemStackSlot(this, 400, 56, 17).setCraftingInput(true).setOverlay(TCBlocks.ORE_PETROLEUM.get()));
                inventory.add(new ItemStackSlot(this, 401, 56, 53).setCraftingInput(true).setOverlay(Items.COAL));
                inventory.add(new ItemStackSlot(this, 402, 123, 8).setCraftingInput(true).setOverlay(Items.BUCKET));
                inventory.add(new ItemStackSlot(this, 403, 116, 60).setCraftingOutput(true));
                inventory.add(new ItemStackSlot(this, 404, 123, 33).setCraftingOutput(true));
            }
            case "hearthfurnace" -> {
                cookDuration = 600;
                inventory.add(new ItemStackSlot(this, 400, 56, 17).setCraftingInput(true).setOverlay(Items.IRON_INGOT));
                inventory.add(new ItemStackSlot(this, 401, 35, 17).setCraftingInput(true).setOverlay(TiMItems.graphite.get()));
                inventory.add(new ItemStackSlot(this, 402, 47, 53).setCraftingInput(true).setOverlay(Items.COAL));
                inventory.add(new ItemStackSlot(this, 403, 116, 35).setCraftingOutput(true));
            }
            default -> {
                inventory.add(new ItemStackSlot(this, 400, 56, 17));
                inventory.add(new ItemStackSlot(this, 401, 56, 53));
            }
        }
    }

    public ItemStackSlot slot(int id) {
        for (ItemStackSlot s : inventory) if (s.getSlotID() == id) return s;
        return null;
    }

    private boolean empty(int id) {
        return !slot(id).getHasStack();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        ItemStackSlot s = slot(slot);
        return s == null || !s.isCraftingOutput();
    }

    @Override
    public void markDirty() {
        setChanged();
    }

    public int tankCapacity() {
        return 30000;
    }

    public int tankAmount() {
        return tank == null ? 0 : tank.amount;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    /*
     * <h2>the tank</h2>
     */
    /** puts the fluid in if it all fits; returns what did not fit (0: all of it went in) */
    private int fill(FluidStack f, boolean doFill) {
        if (f == null) return 0;
        if (tank != null && tank.amount > 0 && tank.getFluid() != f.getFluid()) return f.amount;
        int room = tankCapacity() - tankAmount();
        if (f.amount > room) return f.amount - room;
        if (doFill) tank = new FluidStack(f.getFluid(), tankAmount() + f.amount);
        return 0;
    }

    private void drain(int amount) {
        if (tank != null) tank = new FluidStack(tank.getFluid(), Math.max(0, tank.amount - amount));
    }

    /*
     * <h2>ticking</h2>
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineTile te) {
        switch (te.kind) {
            case "distil" -> te.tickDistil();
            case "hearthfurnace" -> te.tickHearth();
            default -> te.tickGenerator();
        }
        boolean lit = te.kind.equals("dieselgenerator") ? te.burnTime > 0 : te.isBurning();
        if (state.hasProperty(MachineBlock.LIT) && state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), 3);
        }
    }

    private int tickCount;

    private void takeFuel(int slotId) {
        ItemStackSlot fuel = slot(slotId);
        currentItemBurnTime = burnTime = fuel.getStackOrEmpty().getBurnTime(RecipeType.SMELTING);
        if (burnTime > 0) {
            ItemStack remainder = fuel.getStackOrEmpty().getCraftingRemainingItem();
            if (!remainder.isEmpty() && fuel.getStackSize() == 1) {
                fuel.setStack(remainder);
            } else {
                fuel.decrStackSize(1);
            }
        }
    }

    private void tickDistil() {
        tickCount++;
        boolean was = burnTime > 0;
        if (burnTime == 0 && canDistil()) {
            takeFuel(401);
        }
        if (isBurning() && canDistil()) {
            cookTime++;
            if (cookTime == cookDuration) {
                cookTime = 0;
                distil();
            }
        } else {
            cookTime = 0;
        }
        if (tickCount % 8 == 0 && !empty(402)) {
            ItemStack in = slot(402).getStackOrEmpty();
            if (in.getItem() == TiMItems.emptyCanister.get()) {
                if (tank != null && tank.getName().contains("diesel") && tank.amount >= 1000) {
                    placeInInvent(new ItemStack(TiMItems.dieselCanister.get()));
                }
            } else if (in.getItem() == Items.BUCKET) {
                if (tank != null && tank.amount >= 1000 && tank.getFluid().getBucket() != Items.AIR) {
                    placeInInvent(new ItemStack(tank.getFluid().getBucket()));
                }
            } else {
                //a filled container: empty it into the tank
                FluidStack f = FluidStack.contained(in);
                if (f != null && fill(f, false) == 0) {
                    fill(f, true);
                    ItemStack out = in.getCraftingRemainingItem();
                    if (empty(404)) {
                        slot(404).setStack(out);
                    } else if (slot(404).getItem() == out.getItem()) {
                        slot(404).getStackOrEmpty().grow(1);
                    }
                    slot(402).decrStackSize(1);
                }
            }
        }
        if (burnTime > 0) burnTime--;
        if (was != (burnTime > 0)) setChanged();
    }

    private void placeInInvent(ItemStack filled) {
        if (empty(404)) {
            slot(404).setStack(filled);
        } else if (ItemStack.isSameItemSameComponents(slot(404).getStackOrEmpty(), filled)
                && slot(404).getStackSize() + filled.getCount() <= filled.getMaxStackSize()) {
            slot(404).getStackOrEmpty().grow(filled.getCount());
        } else {
            return;
        }
        slot(402).decrStackSize(1);
        drain(1000);
        setChanged();
    }

    /** what the input distils into: diesel or fuel oil, in mB */
    private FluidStack distilResult(ItemStack item) {
        if (item.is(Items.SUGAR_CANE) || item.is(Items.WHEAT) || item.is(ItemTags.LEAVES)) {
            return new FluidStack(TiMFluids.fluidDiesel, 1000);
        } else if (item.is(TCBlocks.ORE_PETROLEUM.get().asItem())) {
            return new FluidStack(TiMFluids.fluidDiesel, 2000);
        } else if (isOil(item)) {
            return new FluidStack(TiMFluids.fluidfueloil, 2000);
        }
        return null;
    }

    public static boolean isOil(ItemStack s) {
        FluidStack f = FluidStack.contained(s);
        return f != null && (f.getName().contains("diesel") || f.getName().contains("oil"));
    }

    /** one in this many distillations also gives plastic */
    public static int plasticChance(ItemStack item) {
        if (item.is(TCBlocks.ORE_PETROLEUM.get().asItem()) || isOil(item)) {
            return 1;
        } else if (item.is(Items.SUGAR_CANE) || item.is(Items.WHEAT)) {
            return 4;
        } else if (item.is(ItemTags.LEAVES)) {
            return 6;
        }
        return 0;
    }

    private ItemStack plasticResult(ItemStack item) {
        if (item.is(Items.SUGAR_CANE) || isOil(item)) {
            return new ItemStack(TiMOres.dustPlastic.get(), 2);
        }
        return new ItemStack(TiMOres.dustPlastic.get(), 1);
    }

    private boolean canDistil() {
        if (empty(400) || (slot(403).getStackSize() == 64) || (slot(404).getStackSize() == 64)) {
            return false;
        }
        FluidStack r = distilResult(slot(400).getStackOrEmpty());
        return r != null && fill(r, false) == 0;
    }

    private void distil() {
        if (!canDistil()) return;
        ItemStack input = slot(400).getStackOrEmpty();
        fill(distilResult(input), true);
        int chance = plasticChance(input);
        if (chance > 0 && random.nextInt(chance) == 0) {
            //an oil bucket gives its bucket back instead
            ItemStack out = isOil(input) && input.getItem() != Items.BUCKET && input.getCraftingRemainingItem().is(Items.BUCKET)
                    ? new ItemStack(Items.BUCKET) : plasticResult(input);
            if (empty(403)) {
                slot(403).setStack(out);
            } else if (slot(403).getItem() == out.getItem()) {
                slot(403).getStackOrEmpty().grow(out.getCount());
            }
        }
        ItemStack remainder = input.getCraftingRemainingItem();
        if (!remainder.isEmpty() && !isOil(input)) {
            slot(400).setStack(remainder);
        } else {
            slot(400).decrStackSize(1);
        }
        setChanged();
    }

    private void tickHearth() {
        boolean was = burnTime > 0;
        if (burnTime > 0) burnTime--;
        if (burnTime == 0 && canSmeltSteel()) {
            takeFuel(402);
        }
        if (isBurning() && canSmeltSteel()) {
            cookTime++;
            if (cookTime == cookDuration) {
                cookTime = 0;
                if (empty(403)) {
                    slot(403).setStack(new ItemStack(TiMOres.ingotSteel.get()));
                } else if (slot(403).getStackSize() < 64) {
                    slot(403).getStackOrEmpty().grow(1);
                }
                slot(400).decrStackSize(1);
                slot(401).decrStackSize(1);
                //1.12: the first one takes 600 ticks, every one after 1000
                cookDuration = 1000;
                setChanged();
            }
        } else {
            cookTime = 0;
        }
        if (was != (burnTime > 0)) setChanged();
    }

    private boolean canSmeltSteel() {
        if (empty(400) || !slot(400).getStackOrEmpty().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.parse("c:ingots/iron")))) {
            return false;
        }
        if (!empty(403) && (slot(403).getItem() != TiMOres.ingotSteel.get() || slot(403).getStackSize() >= 64)) {
            return false;
        }
        return slot(401).getItem() == TiMItems.graphite.get();
    }

    private void tickGenerator() {
        if (!empty(400)) {
            FluidStack f = FluidStack.contained(slot(400).getStackOrEmpty());
            if (f != null && f.amount > 0 && (tank == null || tank.amount == 0 || tank.getFluid() == f.getFluid())
                    && tankAmount() + f.amount < tankCapacity()) {
                ItemStackSlot out = slot(401);
                ItemStack empty = slot(400).getStackOrEmpty().getCraftingRemainingItem();
                if (!out.getHasStack() || (out.getItem() == empty.getItem() && out.getStackSize() < out.getStackOrEmpty().getMaxStackSize())) {
                    if (!out.getHasStack()) out.setStack(empty);
                    else out.getStackOrEmpty().grow(1);
                    fill(f, true);
                    slot(400).decrStackSize(1);
                    setChanged();
                }
            }
        }
        int energyProduced = 70;
        if (burnTime > 0) {
            burnTime--;
            energy.receiveEnergy(energyProduced, false);
        }
        if (powered && energyProduced <= energy.getMaxEnergyStored() - energy.getEnergyStored()) {
            int fuelUsed = 50;
            if (burnTime <= 0 && tankAmount() >= fuelUsed) {
                burnTime = 8;
                drain(fuelUsed);
                setChanged();
            }
        }
        if (energy.getEnergyStored() > 0) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                IEnergyStorage other = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(side), side.getOpposite());
                if (other != null && other.canReceive()) {
                    int got = other.receiveEnergy(Math.min(energy.getMaxEnergyStored(), energy.getEnergyStored()), false);
                    energy.extractEnergy(got, false);
                }
            }
        }
    }

    /** what the generator offers to cables and machines: energy can be taken, not put in */
    public final IEnergyStorage energyOut = new IEnergyStorage() {
        @Override public int receiveEnergy(int toReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int toExtract, boolean simulate) { return energy.extractEnergy(toExtract, simulate); }
        @Override public int getEnergyStored() { return energy.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }
        @Override public boolean canExtract() { return energy.getEnergyStored() > 0; }
        @Override public boolean canReceive() { return false; }
    };

    /** for the GUI: burn, item burn time, cook, cook duration, tank amount, tank is fuel oil */
    public final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> burnTime;
                case 1 -> currentItemBurnTime;
                case 2 -> cookTime;
                case 3 -> cookDuration;
                case 4 -> tankAmount();
                case 5 -> tank != null && tank.getFluid() == TiMFluids.fluidfueloil.get() ? 1 : 0;
                default -> energy.getEnergyStored();
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return 7; }
    };

    public void dropInventory() {
        for (ItemStackSlot s : inventory) {
            if (s.getHasStack()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), s.getStackOrEmpty());
                s.setStack(ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag inv = new ListTag();
        for (ItemStackSlot slot : inventory) {
            if (slot.getHasStack()) {
                CompoundTag it = (CompoundTag) slot.getStackOrEmpty().save(registries);
                it.putInt("Slot", slot.getSlotID());
                inv.add(it);
            }
        }
        tag.put("inventory", inv);
        if (tank != null) tag.put("tank", tank.save());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("CookTime", cookTime);
        tag.putInt("ItemBurnTime", currentItemBurnTime);
        tag.putInt("CookDuration", cookDuration);
        tag.putBoolean("Powered", powered);
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ListTag inv = tag.getList("inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < inv.size(); i++) {
            CompoundTag it = inv.getCompound(i);
            ItemStackSlot s = slot(it.getInt("Slot"));
            if (s != null) s.setStack(ItemStack.parse(registries, it).orElse(ItemStack.EMPTY));
        }
        tank = tag.contains("tank") ? FluidStack.load(tag.getCompound("tank")) : null;
        burnTime = tag.getInt("BurnTime");
        cookTime = tag.getInt("CookTime");
        currentItemBurnTime = tag.getInt("ItemBurnTime");
        if (tag.contains("CookDuration")) cookDuration = tag.getInt("CookDuration");
        powered = tag.getBoolean("Powered");
        energy.receiveEnergy(tag.getInt("energy"), false);
    }
}
