package train.core.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ebf.tim.TrainsInMotion;
import ebf.tim.entities.GenericRailTransport;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Traincraft's settings (1.12 ConfigHandler), in config/traincraft-common.toml and traincraft-client.toml.
 * Left out: what 1.12 had no use for any more (render flickering, villager id, energy track, remasters) or what
 * is not ported yet (the tracks builder; the zeppelins come with them).
 */
public final class ConfigHandler {
    private ConfigHandler() {}

    /** 1.12 DISABLE_FLICKERING, always on: the well car draws its cargo twice */
    public static boolean FLICKERING = true;

    public static final ModConfigSpec COMMON_SPEC, CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue SOUNDS, ORE_GEN, ENABLE_STEAM, ENABLE_DIESEL, ENABLE_ELECTRIC, ENABLE_TENDER;
    public static final ModConfigSpec.IntValue WINDMILL_CHECK_RADIUS;
    public static final ModConfigSpec.BooleanValue SPEED_IN_KMH;
    public static final ModConfigSpec.IntValue RAIL_SKIN;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        SOUNDS = b.comment("Engine sounds of the locomotives (idle and running). The horn and the bell always sound.")
                .define("ENABLE_SOUNDS", true);
        ORE_GEN = b.comment("Oil sand and petroleum ore in new chunks.")
                .define("ENABLE_FUEL_ORES_SPAWN", true);
        ENABLE_STEAM = b.comment("Steam locomotives: when off they are not in the creative tab, the assembly tables and the recipe book.")
                .define("ENABLE_STEAM_TRAINS", true);
        ENABLE_DIESEL = b.comment("Diesel locomotives, the same way.").define("ENABLE_DIESEL_TRAINS", true);
        ENABLE_ELECTRIC = b.comment("Electric locomotives, the same way.").define("ENABLE_ELECTRIC_TRAINS", true);
        ENABLE_TENDER = b.comment("Tenders, the same way.").define("ENABLE_TENDERS", true);
        WINDMILL_CHECK_RADIUS = b.comment("The area above a windmill that must see the sky for it to make energy: 0 = only above it, 1 = 3x3, 2 = 5x5 and so on; -1 turns the check off.")
                .defineInRange("WINDMILL_CHECK_RADIUS", 1, -1, 10);
        b.pop();
        COMMON_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.push("client");
        SPEED_IN_KMH = c.comment("Speed in km/h on the locomotive HUD; false for mph.").define("SPEED_IN_KMH", true);
        RAIL_SKIN = c.comment("How TiM rails are drawn, 0 to 3 (1.12 railSkin; 3 is the default look). Takes effect for rails drawn after the change.")
                .defineInRange("RAIL_SKIN", 3, 0, 3);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    public static void register(ModContainer container, IEventBus modBus) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        CONDITIONS.register(modBus);
        modBus.addListener((ModConfigEvent.Loading e) -> apply(e.getConfig()));
        modBus.addListener((ModConfigEvent.Reloading e) -> apply(e.getConfig()));
    }

    private static void apply(ModConfig config) {
        if (config.getSpec() == CLIENT_SPEC) {
            ebf.tim.utility.ClientProxy.railSkin = RAIL_SKIN.get();
        }
    }

    private static boolean get(ModConfigSpec.BooleanValue v) {
        //read before the config is loaded (nothing should): the default
        return !COMMON_SPEC.isLoaded() || v.get();
    }

    public static boolean sounds() {
        return get(SOUNDS);
    }

    /** the transport's kind is not switched off */
    public static boolean enabled(GenericRailTransport t) {
        if (t == null) return true;
        var types = t.getTypes();
        return !(types.contains(TrainsInMotion.transportTypes.STEAM) && !get(ENABLE_STEAM)
                || types.contains(TrainsInMotion.transportTypes.DIESEL) && !get(ENABLE_DIESEL)
                || types.contains(TrainsInMotion.transportTypes.ELECTRIC) && !get(ENABLE_ELECTRIC)
                || types.contains(TrainsInMotion.transportTypes.TENDER) && !get(ENABLE_TENDER));
    }

    public static int windmillCheckRadius() {
        return COMMON_SPEC.isLoaded() ? WINDMILL_CHECK_RADIUS.get() : 1;
    }

    public static boolean speedInKmh() {
        return !CLIENT_SPEC.isLoaded() || SPEED_IN_KMH.get();
    }

    /**
     * a data condition on a setting, for the worldgen files: {"type": "traincraft:config", "option": "fuel_ores"}
     */
    public record ConfigCondition(String option) implements ICondition {
        public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(i ->
                i.group(Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)).apply(i, ConfigCondition::new));

        @Override
        public boolean test(IContext context) {
            return switch (option) {
                case "fuel_ores" -> get(ORE_GEN);
                default -> true;
            };
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, TrainsInMotion.MODID);

    static {
        CONDITIONS.register("config", () -> ConfigCondition.CODEC);
    }
}
