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
        if (level.getBlockState(pos).getBlock() instanceof CatenaryMastPartBlock) {
            BlockPos m = CatenaryMastPartBlock.master(level, pos);
            if (m != null) pos = m;
        }
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
        //the cantilevers made to measure for the track as it is now
        start.fitReach();
        mast.fitReach();
        start.link(pos);
        mast.link(from);
        if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        //on from here: the next click on a new mast strings the next span
        if (!stack.isEmpty()) {
            data.putLong("from", pos.asLong());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
        }
        say(player, overTrack(level, start.contactPoint(), mast.contactPoint()) ? "message.traincraft.catenary.strung" : "message.traincraft.catenary.offtrack");
        return InteractionResult.CONSUME;
    }

    /** how far across a pantograph still reaches the wire */
    public static final double PANTOGRAPH_REACH = 0.8;

    /**
     * whether the contact wire from a to b stays over the track all the way, within a pantograph's reach: on a turn
     * the straight wire between two masts strays inside the arc the more the longer the span
     */
    /** for tests: the points of the wire from a to b found off the track, as "x,z=off" */
    public static String offPoints(Level level, net.minecraft.world.phys.Vec3 a, net.minecraft.world.phys.Vec3 b) {
        StringBuilder sb = new StringBuilder();
        double len = Math.hypot(b.x - a.x, b.z - a.z), dx = (b.x - a.x) / len, dz = (b.z - a.z) / len;
        int n = Math.max(2, (int) Math.ceil(len * 2));
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n, x = a.x + (b.x - a.x) * t, z = a.z + (b.z - a.z) * t, y = a.y + (b.y - a.y) * t - 2;
            double off = CatenaryGeometry.offTrack(level, x, y, z, dx, dz);
            if (Double.isNaN(off) || off > PANTOGRAPH_REACH) sb.append(String.format(" %.2f,%.2f=%.2f", x, z, off));
        }
        return sb.toString();
    }

    public static boolean overTrack(Level level, net.minecraft.world.phys.Vec3 a, net.minecraft.world.phys.Vec3 b) {
        double len = Math.hypot(b.x - a.x, b.z - a.z);
        if (len < 1e-6) return true;
        double dx = (b.x - a.x) / len, dz = (b.z - a.z) / len;
        int n = Math.max(2, (int) Math.ceil(len * 2));
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double x = a.x + (b.x - a.x) * t, z = a.z + (b.z - a.z) * t, y = a.y + (b.y - a.y) * t - 2;
            double off = CatenaryGeometry.offTrack(level, x, y, z, dx, dz);
            if (Double.isNaN(off) || off > PANTOGRAPH_REACH) return false;
        }
        return true;
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
