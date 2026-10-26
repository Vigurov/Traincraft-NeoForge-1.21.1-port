package ebf.tim.utility;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A TiM table recipe: what it makes (one or more results) and, per input slot, the stacks that fit there
 * (any of them, at least that many). A null slot must stay empty.
 */
public class Recipe {
    /** All the items, whether it be transports, parts, etc. that this recipe can craft. */
    public List<ItemStack> result = new ArrayList<>();
    /** per input slot (reading order), the accepted stacks; shorter than the table: the rest must be empty */
    public final List<List<ItemStack>> input;

    public Recipe(List<ItemStack> results, List<List<ItemStack>> cost) {
        this.result = results;
        this.input = cost;
    }

    public Recipe(ItemStack results, List<List<ItemStack>> cost) {
        this(Collections.singletonList(results), cost);
    }

    public List<ItemStack> getresult(){return result;}

    public List<List<ItemStack>> getRecipeItems() {
        return input;
    }

    /**
     * The recipe checking when player is using the table.
     * @param stacks The stacks that the player has put in.
     * @return If the stacks parameter matches the recipe's input and can be crafted.
     */
    public boolean inputMatches(List<ItemStack> stacks){
        if (stacks.size() < input.size()) return false;
        int i=0;
        for(List<ItemStack> slot : input){
            if(slot!=null) {
                boolean isMatching = false;
                for (ItemStack s : slot) {
                    isMatching = compareItemsAndSize(s, stacks.get(i));
                    if (isMatching) break;
                }
                if (!isMatching) return false;
            } else if (stacks.get(i) != null && !stacks.get(i).isEmpty()) {
                return false;
            }
            i++;
        }
        //the slots past the recipe must be empty
        for (; i < stacks.size(); i++) {
            if (stacks.get(i) != null && !stacks.get(i).isEmpty()) return false;
        }
        return true;
    }

    /** the stack in the slot fits this ingredient (same item, at least as many) */
    public static boolean compareItemsAndSize(ItemStack target, ItemStack toCompare) {
        boolean tEmpty = target == null || target.isEmpty(), cEmpty = toCompare == null || toCompare.isEmpty();
        if(tEmpty || cEmpty) {
            return tEmpty && cEmpty;
        }
        return target.getItem() == toCompare.getItem() && target.getCount() <= toCompare.getCount();
    }
}
