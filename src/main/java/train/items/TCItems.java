package train.items;

import ebf.tim.registry.TiMGenericRegistry;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Traincraft's clothes (1.12 TCItems / ItemIDs armour entries), with the 1.12 names, defence values and colours.
 */
public class TCItems {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "traincraft");
    public static final List<DeferredItem<ItemTCArmor>> ARMOUR = new ArrayList<>();

    /** 1.12: durability factor 5, {1, 2, 2, 1}, enchantability 25 (its toughness of 20 was a porting slip, 1.7.10 had none) */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> armor = material("armor", 1, 2, 2, 1, 25, Ingredient.of(Items.LEATHER), false);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> armorCloth = material("tccloth", 1, 2, 2, 1, 25, Ingredient.of(Items.LEATHER), true);
    /** durability factor 70, {2, 6, 5, 2}, enchantability 50, repaired with diamonds */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> armorCompositeSuit = material("tcsuit", 2, 6, 5, 2, 50, Ingredient.of(Items.DIAMOND), true);

    public static final DeferredItem<ItemTCArmor> overalls = armour("overalls", armor, ArmorItem.Type.LEGGINGS, 5, 0, "blue_overalls", false);
    public static final DeferredItem<ItemTCArmor> jacket = armour("jacket", armor, ArmorItem.Type.CHESTPLATE, 5, 0, "orange_jacket", false);
    public static final DeferredItem<ItemTCArmor> hat = armour("hat", armor, ArmorItem.Type.HELMET, 5, 0, "blue_hat", false);

    public static final DeferredItem<ItemTCArmor> pants_ticketMan_paintable = armour("pants_ticketman_paintable", armorCloth, ArmorItem.Type.LEGGINGS, 5, 0xdedede, "ticket_man_pants_", false);
    public static final DeferredItem<ItemTCArmor> jacket_ticketMan_paintable = armour("jacket_ticketman_paintable", armorCloth, ArmorItem.Type.CHESTPLATE, 5, 0x002cdb, "ticket_man_", false);
    public static final DeferredItem<ItemTCArmor> hat_ticketMan_paintable = armour("hat_ticketman_paintable", armorCloth, ArmorItem.Type.HELMET, 5, 0x9fafb5, "ticket_man_", false);

    public static final DeferredItem<ItemTCArmor> pants_driver_paintable = armour("pants_driver_paintable", armorCloth, ArmorItem.Type.LEGGINGS, 5, 0x1535d4, "driver_pants_", false);
    public static final DeferredItem<ItemTCArmor> jacket_driver_paintable = armour("jacket_driver_paintable", armorCloth, ArmorItem.Type.CHESTPLATE, 5, 0x1469d9, "driver_", false);
    public static final DeferredItem<ItemTCArmor> hat_driver_paintable = armour("hat_driver_paintable", armorCloth, ArmorItem.Type.HELMET, 5, 0x1469d9, "driver_", false);

    public static final DeferredItem<ItemTCArmor> boots_suit_paintable = armour("boots_suit_paintable", armorCompositeSuit, ArmorItem.Type.BOOTS, 70, 0x1535d4, "composite_suit_", true);
    public static final DeferredItem<ItemTCArmor> pants_suit_paintable = armour("pants_suit_paintable", armorCompositeSuit, ArmorItem.Type.LEGGINGS, 70, 0x1535d4, "composite_suit_pants_", true);
    public static final DeferredItem<ItemTCArmor> jacket_suit_paintable = armour("jacket_suit_paintable", armorCompositeSuit, ArmorItem.Type.CHESTPLATE, 70, 0x1469d9, "composite_suit_", true);
    public static final DeferredItem<ItemTCArmor> helmet_suit_paintable = armour("helmet_suit_paintable", armorCompositeSuit, ArmorItem.Type.HELMET, 70, 0x1469d9, "composite_suit_", true);

    /** the TiM recipe book */
    public static final DeferredItem<ItemCraftGuide> recipeBook = TiMGenericRegistry.ITEMS.register("craftbook", () -> new ItemCraftGuide(new Item.Properties().stacksTo(1)));

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> material(String name, int boots, int legs, int chest, int helmet, int enchantability,
                                                                        Ingredient repair, boolean paintable) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("traincraft", name);
        //the paintable ones are leather-like: a tinted layer and an overlay (textures come from ItemTCArmor)
        List<ArmorMaterial.Layer> layers = paintable
                ? List.of(new ArmorMaterial.Layer(id, "", true), new ArmorMaterial.Layer(id, "_overlay", false))
                : List.of(new ArmorMaterial.Layer(id));
        return MATERIALS.register(name, () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.BOOTS, boots);
            m.put(ArmorItem.Type.LEGGINGS, legs);
            m.put(ArmorItem.Type.CHESTPLATE, chest);
            m.put(ArmorItem.Type.HELMET, helmet);
            m.put(ArmorItem.Type.BODY, chest);
        }), enchantability, SoundEvents.ARMOR_EQUIP_LEATHER, () -> repair, layers, 0.0F, 0.0F));
    }

    private static DeferredItem<ItemTCArmor> armour(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, int durability, int color,
                                                    String texture, boolean suit) {
        Item.Properties props = new Item.Properties().durability(type.getDurability(durability));
        if (suit) props.rarity(Rarity.EPIC);
        DeferredItem<ItemTCArmor> i = TiMGenericRegistry.ITEMS.register(name, () -> suit
                ? new ItemTCCompositeSuit(material, type, color, texture, props)
                : new ItemTCArmor(material, type, color, texture, props));
        ARMOUR.add(i);
        return i;
    }

    public static void init(IEventBus modBus) {
        MATERIALS.register(modBus);
        NeoForge.EVENT_BUS.addListener(ItemTCCompositeSuit::onJump);
        NeoForge.EVENT_BUS.addListener(ItemTCCompositeSuit::onFall);
    }
}
