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
 * Feeder cable: right click a substation and then a mast (or the mast first): the cable is strung from the
 * substation to a bracket at the mast's top, and the substation feeds the line there. A mast takes one feeder.
 */
public class FeederCableItem extends Item {
    public static final int MAX_LENGTH = 24;

    public FeederCableItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        Level level = c.getLevel();
        BlockPos pos = c.getClickedPos();
        boolean sub = level.getBlockEntity(pos) instanceof SubstationTile, mast = level.getBlockEntity(pos) instanceof CatenaryMastTile;
        if (!sub && !mast) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = c.getItemInHand();
        Player player = c.getPlayer();
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains("from")) {
            data.putLong("from", pos.asLong());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
            say(player, "message.traincraft.feeder.start");
            return InteractionResult.CONSUME;
        }
        BlockPos from = BlockPos.of(data.getLong("from"));
        data.remove("from");
        CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
        SubstationTile s = level.getBlockEntity(from) instanceof SubstationTile f ? f : level.getBlockEntity(pos) instanceof SubstationTile p ? p : null;
        CatenaryMastTile m = level.getBlockEntity(from) instanceof CatenaryMastTile f ? f : level.getBlockEntity(pos) instanceof CatenaryMastTile p ? p : null;
        if (s == null || m == null || from.equals(pos)) {
            say(player, "message.traincraft.feeder.ends");
            return InteractionResult.CONSUME;
        }
        if (s.getBlockPos().distSqr(m.getBlockPos()) > MAX_LENGTH * MAX_LENGTH) {
            say(player, "message.traincraft.feeder.toofar");
            return InteractionResult.CONSUME;
        }
        if (m.feeder != null) {
            say(player, "message.traincraft.feeder.taken");
            return InteractionResult.CONSUME;
        }
        s.addFeeder(m.getBlockPos());
        m.setFeeder(s.getBlockPos());
        if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        say(player, "message.traincraft.feeder.strung");
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("from");
    }

    private static void say(Player p, String key) {
        if (p != null) p.displayClientMessage(Component.translatable(key), true);
    }
}
