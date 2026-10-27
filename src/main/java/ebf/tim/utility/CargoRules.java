package ebf.tim.utility;

import ebf.tim.entities.GenericRailTransport;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * The cars built for one load take only that, the way their models show it: the log cars logs and planks, the
 * rail car rails. The rest take anything.
 */
public final class CargoRules {
    private CargoRules() {}

    public static final String WOOD = "wood", RAILS = "rails";

    private static final Map<String, String> BY_CLASS = Map.of(
            "EntityFreightWood", WOOD, "EntityFreightWood2", WOOD, "EntityFlatCarLogs_DB", WOOD,
            "EntityFlatCartWoodUS", WOOD, "EntityFreightCenterbeam_Wood_1", WOOD, "EntityFreightCenterbeam_Wood_2", WOOD,
            "EntityFlatCarRails_DB", RAILS);

    /** what that car takes, or null for anything */
    public static String of(GenericRailTransport t) {
        return BY_CLASS.get(t.getClass().getSimpleName());
    }

    public static boolean takes(String load, ItemStack s) {
        return switch (load) {
            case WOOD -> s.is(ItemTags.LOGS) || s.is(ItemTags.PLANKS);
            case RAILS -> s.is(ItemTags.RAILS) || s.getItem() instanceof train.common.items.ItemTCRail
                    || s.getItem() instanceof ebf.tim.items.ItemRail;
            default -> true;
        };
    }
}
