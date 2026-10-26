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
 * Power cable: right click a substation and then a mast (or the mast first): a feeder is strung from the substation
 * to a bracket at the mast's top, and the substation feeds the line there (a mast takes one). Or a generator (any
 * block that gives energy) and a substation: a power line from it to one of the substation's three bushings.
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
        if (level.getBlockState(pos).getBlock() instanceof SubstationPartBlock) {
            BlockPos m = SubstationPartBlock.master(level, pos);
            if (m != null) pos = m;
        }
        boolean sub = level.getBlockEntity(pos) instanceof SubstationTile, mast = level.getBlockEntity(pos) instanceof CatenaryMastTile;
        boolean source = !sub && !mast && level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, pos, null) != null;
        if (!sub && !mast && !source) return InteractionResult.PASS;
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
        BlockPos other = s == null ? null : s.getBlockPos().equals(from) ? pos : from;
        boolean generator = s != null && m == null && other != null && !(level.getBlockEntity(other) instanceof SubstationTile)
                && level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, other, null) != null;
        if (s == null || (m == null && !generator) || from.equals(pos)) {
            say(player, "message.traincraft.feeder.ends");
            return InteractionResult.CONSUME;
        }
        if (s.getBlockPos().distSqr(other) > MAX_LENGTH * MAX_LENGTH) {
            say(player, "message.traincraft.feeder.toofar");
            return InteractionResult.CONSUME;
        }
        if (generator) {
            //a power line in from a generator, to one of the substation's three bushings
            if (s.inputs.contains(other) || s.inputs.size() >= SubstationTile.MAX_INPUTS) {
                say(player, "message.traincraft.feeder.full");
                return InteractionResult.CONSUME;
            }
            s.addInput(other);
        } else {
            if (m.feeder != null) {
                say(player, "message.traincraft.feeder.taken");
                return InteractionResult.CONSUME;
            }
            s.addFeeder(m.getBlockPos());
            m.setFeeder(s.getBlockPos());
        }
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
