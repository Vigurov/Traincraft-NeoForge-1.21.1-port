package ebf.tim.registry;

import ebf.tim.TrainsInMotion;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.items.ItemTransport;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers the transports: each class gets an entity type and an item (1.12 TiM made the item in a static field
 * of the class and registered the entity by class).
 */
public class TiMGenericRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TrainsInMotion.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrainsInMotion.MODID);

    public static final DeferredItem<Item> STAKE = ITEMS.registerSimpleItem("stake", new Item.Properties().stacksTo(1));

    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<? extends GenericRailTransport>>> TRANSPORTS = new ArrayList<>();
    public static final List<DeferredItem<ItemTransport>> TRANSPORT_ITEMS = new ArrayList<>();
    private static final Map<Class<?>, DeferredItem<ItemTransport>> ITEM_BY_CLASS = new HashMap<>();
    private static final Map<Class<?>, EntityType.EntityFactory<?>> FACTORY_BY_CLASS = new java.util.LinkedHashMap<>();
    private static final Map<Class<?>, DeferredHolder<EntityType<?>, ? extends EntityType<? extends GenericRailTransport>>> TYPE_BY_CLASS = new HashMap<>();

    public static <T extends GenericRailTransport> DeferredHolder<EntityType<?>, EntityType<T>> registerTransport(
            Class<T> clazz, EntityType.EntityFactory<T> factory) {
        return registerTransport(clazz.getSimpleName().replaceFirst("^Entity", "").toLowerCase(), clazz, factory);
    }

    /**name: the 1.12 item name (transportName without spaces), lower case*/
    public static <T extends GenericRailTransport> DeferredHolder<EntityType<?>, EntityType<T>> registerTransport(
            String name, Class<T> clazz, EntityType.EntityFactory<T> factory) {
        DeferredHolder<EntityType<?>, EntityType<T>> type = ENTITIES.register(name, () ->
                EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(0.75f, 3f)
                        .clientTrackingRange(16)
                        .updateInterval(1)
                        .build(TrainsInMotion.MODID + ":" + name));
        DeferredItem<ItemTransport> item = ITEMS.register(name, () -> new ItemTransport(type, clazz, new Item.Properties().stacksTo(1)));
        FACTORY_BY_CLASS.put(clazz, factory);
        TYPE_BY_CLASS.put(clazz, type);
        TRANSPORTS.add(type);
        TRANSPORT_ITEMS.add(item);
        ITEM_BY_CLASS.put(clazz, item);
        return type;
    }

    public static Item itemFor(Class<?> clazz) {
        DeferredItem<ItemTransport> item = ITEM_BY_CLASS.get(clazz);
        return item == null ? null : item.get();
    }

    public static Item stakeItem() {
        return STAKE.get();
    }

    /**
     * a transport without a world, for the values it defines (1.12 kept one per item); EntityType.create needs a
     * world, the factory does not
     */
    @SuppressWarnings("unchecked")
    public static <T extends GenericRailTransport> T definition(Class<T> clazz) {
        EntityType.EntityFactory<T> factory = (EntityType.EntityFactory<T>) FACTORY_BY_CLASS.get(clazz);
        return factory == null ? null : factory.create((EntityType<T>) TYPE_BY_CLASS.get(clazz).get(), null);
    }

    /**a world-less instance of every transport*/
    @SuppressWarnings("unchecked")
    public static List<GenericRailTransport> definitions() {
        List<GenericRailTransport> l = new ArrayList<>();
        for (Class<?> c : FACTORY_BY_CLASS.keySet()) {
            GenericRailTransport t = definition((Class<? extends GenericRailTransport>) c);
            if (t != null) l.add(t);
        }
        return l;
    }

    /**1.12 registered the skins while registering each transport: do it once the types exist*/
    public static void registerSkins() {
        for (Class<?> c : FACTORY_BY_CLASS.keySet()) {
            GenericRailTransport t = definition((Class<? extends GenericRailTransport>) c);
            if (t != null) {
                t.registerSkins();
            }
        }
    }
}
