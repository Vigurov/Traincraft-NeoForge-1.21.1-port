package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Catenary wire: right click a mast, then another one (up to MAX_SPAN blocks away): a span is strung between them,
 * one wire used. Clicking on from the last mast to a new one carries the line on.
 */
public class CatenaryWireItem extends Item {
    public CatenaryWireItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        Level level = c.getLevel();
        BlockPos pos = c.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof CatenaryMastTile mast)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = c.getItemInHand();
        Player player = c.getPlayer();
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains("from")) {
            data.putLong("from", pos.asLong());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
            say(player, "message.traincraft.catenary.start");
            return InteractionResult.CONSUME;
        }
        BlockPos from = BlockPos.of(data.getLong("from"));
        data.remove("from");
        CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
        if (from.equals(pos)) {
            say(player, "message.traincraft.catenary.cancel");
            return InteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(from) instanceof CatenaryMastTile start)) {
            say(player, "message.traincraft.catenary.gone");
            return InteractionResult.CONSUME;
        }
        if (from.distSqr(pos) > (double) CatenaryMastTile.MAX_SPAN * CatenaryMastTile.MAX_SPAN || Math.abs(from.getY() - pos.getY()) > 4) {
            say(player, "message.traincraft.catenary.toofar");
            return InteractionResult.CONSUME;
        }
        if (mast.links.contains(from)) {
            say(player, "message.traincraft.catenary.already");
            return InteractionResult.CONSUME;
        }
        start.link(pos);
        mast.link(from);
        if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        //on from here: the next click on a new mast strings the next span
        if (!stack.isEmpty()) {
            data.putLong("from", pos.asLong());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
        }
        say(player, "message.traincraft.catenary.strung");
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        //held from a mast: it shines, like a lead in hand
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("from");
    }

    private static void say(Player p, String key) {
        if (p != null) p.displayClientMessage(Component.translatable(key), true);
    }
}
