package train.core;

import com.mojang.brigadier.CommandDispatcher;
import ebf.tim.TrainsInMotion;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.ItemStackSlot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Operator commands. /traincraft refuel: fills the fuel (and water) slot of the transport you ride, or every
 * transport within 16 blocks: diesel canisters, coal and water, or redstone, by its kind.
 */
public final class TCCommands {
    private TCCommands() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> register(e.getDispatcher()));
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("traincraft").requires(s -> s.hasPermission(2))
                .then(Commands.literal("refuel").executes(c -> refuel(c.getSource()))));
    }

    private static int refuel(CommandSourceStack src) {
        Entity self = src.getEntity();
        java.util.List<GenericRailTransport> list;
        if (self != null && self.getVehicle() instanceof GenericRailTransport t) {
            list = java.util.List.of(t);
        } else {
            Vec3 at = src.getPosition();
            list = src.getLevel().getEntitiesOfClass(GenericRailTransport.class, new AABB(at, at).inflate(16));
        }
        int n = 0;
        for (GenericRailTransport t : list) {
            var types = t.getTypes();
            ItemStack fuel = types.contains(TrainsInMotion.transportTypes.STEAM) ? new ItemStack(Items.COAL, 64)
                    : types.contains(TrainsInMotion.transportTypes.DIESEL) ? new ItemStack(ebf.tim.registry.TiMItems.dieselCanister.get(), 16)
                    : types.contains(TrainsInMotion.transportTypes.ELECTRIC) ? new ItemStack(Items.REDSTONE_BLOCK, 64) : ItemStack.EMPTY;
            if (fuel.isEmpty()) continue;
            ItemStackSlot slot = t.getSlotIndexByID(400);
            if (slot == null) continue;
            slot.setStack(fuel);
            if (types.contains(TrainsInMotion.transportTypes.STEAM)) {
                ItemStackSlot water = t.getSlotIndexByID(401);
                if (water != null) water.setStack(new ItemStack(Items.WATER_BUCKET));
            }
            n++;
        }
        int count = n;
        src.sendSuccess(() -> Component.literal("Refuelled " + count + " transport(s)"), true);
        return n;
    }
}
