package ebf.tim.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * the TiM crafting parts (cabins, frames, wheels, engines...) and tools. Names are the 1.12 ones.
 * The fields keep the 1.12 names so ported code reads the same; they hold the registry entries.
 */
public class TiMItems {
    /**every part, in 1.12 registration order: the "crafting" creative tab*/
    public static final List<DeferredItem<Item>> PARTS = new ArrayList<>();
    private static final Map<String, DeferredItem<Item>> BY_NAME = new LinkedHashMap<>();

    public static DeferredItem<Item> emptyCanister = item("emptycanister"), dieselCanister = item("dieselcanister");
    public static DeferredItem<Item> cabinWood = part("cabin.wood"), cabinAluminium = part("cabin.aluminium"), cabinCopper = part("cabin.copper"),
            cabinIron = part("cabin.iron"), cabinSteel = part("cabin.steel"), cabinFiberglass = part("cabin.fiberglass"), cabinPlastic = part("cabin.plastic");
    public static DeferredItem<Item> frameWood = part("frame.wood"), frameAluminium = part("frame.aluminium"), frameCopper = part("frame.copper"),
            frameIron = part("frame.iron"), frameSteel = part("frame.steel"), frameFiberglass = part("frame.fiberglass"), framePlastic = part("frame.plastic");
    public static DeferredItem<Item> wheelWood = part("wheel.wood"), wheelAluminium = part("wheel.aluminium"), wheelIron = part("wheel.iron"),
            wheelSteel = part("wheel.steel"), wheelPlastic = part("wheel.plastic");
    public static DeferredItem<Item> chimneyWood = part("chimney.wood"), chimneyAluminium = part("chimney.aluminium"), chimneyCopper = part("chimney.copper"),
            chimneyIron = part("chimney.iron"), chimneySteel = part("chimney.steel"), chimneyFiberglass = part("chimney.fiberglass"),
            chimneyPlastic = part("chimney.plastic"), chimneyAluminiumShort = part("chimney.aluminium.short"), chimneySteelShort = part("chimney.steel.short");
    public static DeferredItem<Item> boilerWood = part("boiler.wood"), boilerIron = part("boiler.iron"), boilerSteel = part("boiler.steel"), boilerCopper = part("boiler.copper");
    public static DeferredItem<Item> fireboxIron = part("firebox.iron"), fireboxSteel = part("firebox.steel");
    public static DeferredItem<Item> mediumDieselEngine = part("medium.diesel.engine");
    public static DeferredItem<Item> smallElectricEngine = part("small.electric.engine");
    public static DeferredItem<Item> seatsWooden = part("seats.wooden");
    public static DeferredItem<Item> hydraulicTransmission = part("hydraulic.transmission"), transformer = part("transformer");
    public static DeferredItem<Item> copperWire = part("copper.wire"), goldWire = part("gold.wire"), carbonWire = part("carbon.wire"),
            steelWire = part("steel.wire"), aluminiumWire = part("aluminium.wire");
    public static DeferredItem<Item> enginePiston = part("engine.piston"), cylinder = part("engine.cylinder"), camshaft = part("camshaft"),
            graphite = part("graphite"), steelPinCircuit = part("steel.pin.circuit");
    public static DeferredItem<Item> controlPanel = part("control.panel");
    public static DeferredItem<Item> generator = part("generator");

    private static DeferredItem<Item> item(String name) {
        DeferredItem<Item> i = TiMGenericRegistry.ITEMS.registerSimpleItem(name);
        BY_NAME.put(name, i);
        return i;
    }

    private static DeferredItem<Item> part(String name) {
        DeferredItem<Item> i = item(name);
        PARTS.add(i);
        return i;
    }

    public static Item byName(String name) {
        DeferredItem<Item> i = BY_NAME.get(name);
        return i == null ? null : i.get();
    }

    /**loads the class (the static fields register the items)*/
    public static void init() {}
}
