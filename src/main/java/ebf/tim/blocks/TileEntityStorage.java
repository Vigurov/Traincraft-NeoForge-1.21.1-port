package ebf.tim.blocks;

import ebf.tim.registry.TiMBlocks;
import ebf.tim.utility.ItemStackSlot;
import ebf.tim.utility.Recipe;
import ebf.tim.utility.RecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * The inventory of a TiM table: the TiM train table (3x3 in, 3x3 results), the rail table (rail material, ties,
 * ballast in, a rail out) and the Traincraft assembly tables (10 in, 8 results, 8 storage). Slot ids and positions
 * are the 1.12 ones. Results are listed in the output slots; taking one uses up its recipe.
 * @author Eternal Blue Flame
 */
public class TileEntityStorage extends BlockEntity implements ItemStackSlot.Host {
    public List<ItemStackSlot> inventory = new ArrayList<>();
    /** "traintable", "railtable", "traintabletier1".."3" */
    public final String table;
    public int outputPage = 1;
    public int pages = 1;
    private boolean updating = false;

    public TileEntityStorage(BlockPos pos, BlockState state) {
        super(TiMBlocks.TABLE_TILE.get(), pos, state);
        this.table = state.getBlock() instanceof BlockDynamic b ? b.table : "traintable";
        initInventory();
    }

    public boolean isAssemblyTable() {
        return table.startsWith("traintabletier");
    }

    private void initInventory() {
        int s = 400;
        if (table.equals("traintable")) {
            //inventory grid (left grid)
            for (int l = 0; l < 3; ++l) {
                for (int i1 = 0; i1 < 3; ++i1) {
                    inventory.add(new ItemStackSlot(this, s++, 30 + i1 * 18, 17 + l * 18).setCraftingInput(true));
                }
            }
            //the results (right hand grid)
            for (int l = 0; l < 3; ++l) {
                for (int i1 = 0; i1 < 3; ++i1) {
                    inventory.add(new ItemStackSlot(this, s++, 106 + i1 * 18, 17 + l * 18).setCraftingOutput(true));
                }
            }
        } else if (table.equals("railtable")) {
            inventory.add(new ItemStackSlot(this, 400, 30, -2).setCraftingInput(true).setOverlay(Items.IRON_INGOT)); //ingot
            inventory.add(new ItemStackSlot(this, 401, 30, 18).setCraftingInput(true).setOverlay(Blocks.OAK_PLANKS)); //ties
            inventory.add(new ItemStackSlot(this, 402, 30, 37).setCraftingInput(true).setOverlay(Blocks.GRAVEL)); //ballast
            inventory.add(new ItemStackSlot(this, 403, 50, 7).setCraftingInput(true)); //wires
            inventory.add(new ItemStackSlot(this, 404, 50, 27).setCraftingInput(true)); //augment slot
            inventory.add(new ItemStackSlot(this, 405, 124, -2).setCraftingInput(true).setOverlay(Blocks.RAIL)); //old shape input
            inventory.add(new ItemStackSlot(this, 406, 124, 33).setCraftingOutput(true)); //output
        } else {
            //the assembly table crafting slots (400-409)
            int[][] in = {{25, 27}, {79, 27}, {115, 27}, {145, 27}, {25, 61}, {79, 61}, {115, 61}, {43, 93}, {79, 93}, {145, 93}};
            for (int[] p : in) {
                inventory.add(new ItemStackSlot(this, s++, p[0], p[1]).setCraftingInput(true));
            }
            //the results (410-417)
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 2; ++j) {
                    inventory.add(new ItemStackSlot(this, 410 + (j * 4 + i), 92 + i * 18, 128 + j * 18).setCraftingOutput(true));
                }
            }
            //storage
            for (int i = 0; i < 2; ++i) {
                for (int j = 0; j < 4; ++j) {
                    inventory.add(new ItemStackSlot(this, 36 + (j + i * 4), 8 + j * 18, 128 + i * 18));
                }
            }
        }
    }

    public ItemStackSlot getSlotIndexByID(int id) {
        for (ItemStackSlot slot : inventory) {
            if (slot.getSlotID() == id) return slot;
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        ItemStackSlot s = getSlotIndexByID(slot);
        return s == null || !s.isCraftingOutput();
    }

    @Override
    public void markDirty() {
        if (!updating) {
            updating = true;
            try {
                onCraftMatrixChanged();
            } finally {
                updating = false;
            }
        }
        setChanged();
    }

    /** the stacks in the input slots, in order */
    private ItemStack[] inputs() {
        List<ItemStack> l = new ArrayList<>();
        for (ItemStackSlot s : inventory) {
            if (s.isCraftingInput()) l.add(s.getStackOrEmpty());
        }
        return l.toArray(new ItemStack[0]);
    }

    private List<ItemStackSlot> outputs() {
        List<ItemStackSlot> l = new ArrayList<>();
        for (ItemStackSlot s : inventory) {
            if (s.isCraftingOutput()) l.add(s);
        }
        if (isAssemblyTable()) {
            //1.12 filled them by id: 410, 411...
            l.sort((a, b) -> Integer.compare(a.getSlotID(), b.getSlotID()));
        }
        return l;
    }

    /** lists what the inputs make in the output slots (server side; the menu syncs it) */
    public void onCraftMatrixChanged() {
        if (level == null || level.isClientSide) return;
        List<ItemStackSlot> out = outputs();
        if (table.equals("railtable")) {
            out.get(0).setStack(RecipeManager.railRecipe(getSlotIndexByID(400).getStackOrEmpty(),
                    getSlotIndexByID(401).getStackOrEmpty(), getSlotIndexByID(402).getStackOrEmpty()));
            return;
        }
        List<ItemStack> results = RecipeManager.getResult(inputs(), table);
        int n = out.size();
        pages = results == null ? 1 : Math.max(1, (results.size() + n - 1) / n);
        if (outputPage > pages) outputPage = pages;
        for (int i = 0; i < n; i++) {
            int r = i + (outputPage - 1) * n;
            out.get(i).setStack(results != null && r < results.size() ? results.get(r) : ItemStack.EMPTY);
        }
    }

    public void changePage(boolean next) {
        outputPage = Math.max(1, Math.min(pages, outputPage + (next ? 1 : -1)));
        markDirty();
    }

    /** a result was taken: use up the inputs of its recipe (the rail table: one of each input) */
    public void onCrafting(ItemStack taken) {
        if (table.equals("railtable")) {
            for (int i = 0; i < 6; i++) {
                ItemStackSlot s = getSlotIndexByID(400 + i);
                if (s != null && s.getHasStack()) s.getStackOrEmpty().shrink(1);
            }
        } else {
            Recipe r = RecipeManager.getRecipe(taken, table);
            if (r != null) {
                List<ItemStackSlot> in = new ArrayList<>();
                for (ItemStackSlot s : inventory) if (s.isCraftingInput()) in.add(s);
                for (int i = 0; i < r.input.size() && i < in.size(); i++) {
                    List<ItemStack> ing = r.input.get(i);
                    if (ing == null) continue;
                    ItemStack have = in.get(i).getStackOrEmpty();
                    for (ItemStack s : ing) {
                        if (Recipe.compareItemsAndSize(s, have)) {
                            have.shrink(s.getCount());
                            break;
                        }
                    }
                }
            }
        }
        for (ItemStackSlot s : inventory) {
            if (s.getHasStack() && s.getStackOrEmpty().isEmpty()) s.setStack(ItemStack.EMPTY);
        }
        markDirty();
    }

    public void dropInventory() {
        if (level == null) return;
        for (ItemStackSlot s : inventory) {
            if (s.getHasStack() && !s.isCraftingOutput()) {
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
            if (slot.getHasStack() && !slot.isCraftingOutput()) {
                CompoundTag it = (CompoundTag) slot.getStackOrEmpty().save(registries);
                it.putInt("Slot", slot.getSlotID());
                inv.add(it);
            }
        }
        tag.put("inventory", inv);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ListTag inv = tag.getList("inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < inv.size(); i++) {
            CompoundTag it = inv.getCompound(i);
            ItemStackSlot slot = getSlotIndexByID(it.getInt("Slot"));
            if (slot != null) slot.setStack(ItemStack.parse(registries, it).orElse(ItemStack.EMPTY));
        }
    }
}
