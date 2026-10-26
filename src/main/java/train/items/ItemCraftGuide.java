package train.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** the TiM recipe book (1.12 ebf.tim.items.ItemCraftGuide): right click opens GUICraftBook */
public class ItemCraftGuide extends Item {
    /** set by the client: opens the book screen */
    public static Runnable openScreen = () -> {};

    public ItemCraftGuide(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            openScreen.run();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
