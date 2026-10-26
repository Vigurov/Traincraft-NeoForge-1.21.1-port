package ebf.tim.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.items.ItemRail;
import ebf.tim.registry.TiMBlocks;
import ebf.tim.registry.TiMGenericRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The recipes of the TiM tables, by table name: "traintable" (TiM, the parts and Traincraft's machines, from the
 * 1.12 JSON files listed in assets/&lt;ns&gt;/recipes/_manifest) and "traintabletier1".."3" (the Traincraft
 * assembly tables, from each transport's getRecipe). Ingredients also take items that share a material tag with
 * them (1.12 used the ore dictionary for that): any planks, any steel ingot...
 */
public class RecipeManager {
    private static final Logger LOG = LoggerFactory.getLogger("traincraft");
    private static final Map<String, List<Recipe>> recipeList = new HashMap<>();
    /** the raw recipes, expanded with tag equivalents once tags exist (and again when they reload) */
    private static final List<Object[]> pending = new ArrayList<>();
    private static boolean expanded = false;

    /** 1.12 ore dictionary names in the JSON recipes, as tags */
    private static final Map<String, String> ORE_TAGS = Map.ofEntries(
            Map.entry("ingotIron", "c:ingots/iron"), Map.entry("ingotGold", "c:ingots/gold"),
            Map.entry("ingotSteel", "c:ingots/steel"), Map.entry("ingotCopper", "c:ingots/copper"),
            Map.entry("ingotAluminum", "c:ingots/aluminum"), Map.entry("ingotAluminium", "c:ingots/aluminum"),
            Map.entry("ingotPlastic", "c:dusts/plastic"), Map.entry("itemPlastic", "c:dusts/plastic"),
            Map.entry("dustCoal", "c:dusts/coal"), Map.entry("plankWood", "minecraft:planks"),
            Map.entry("logWood", "minecraft:logs"), Map.entry("dustRedstone", "c:dusts/redstone"));

    public static void registerRecipe(Recipe recipe, String table){
        recipeList.computeIfAbsent(table, k -> new ArrayList<>()).add(recipe);
    }

    public static List<Recipe> getRecipes(String table) {
        ensureLoaded();
        return recipeList.getOrDefault(table, Collections.emptyList());
    }

    /** the items that count as the same material as the stack (same item first) */
    public static List<ItemStack> equivalents(ItemStack s) {
        List<ItemStack> list = new ArrayList<>();
        if (s == null || s.isEmpty()) {
            list.add(ItemStack.EMPTY);
            return list;
        }
        list.add(s);
        s.getItem().builtInRegistryHolder().tags().forEach(tag -> {
            String t = tag.location().toString();
            boolean material = t.startsWith("c:ingots/") || t.startsWith("c:nuggets/") || t.startsWith("c:dusts/")
                    || t.startsWith("c:storage_blocks/") || t.startsWith("c:gems/") || t.equals("c:dyes")
                    || t.equals("minecraft:planks") || t.equals("minecraft:logs") || t.equals("minecraft:wool") || t.equals("minecraft:coals");
            if (material) {
                for (var h : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    if (h.value() != s.getItem()) list.add(new ItemStack(h.value(), s.getCount()));
                }
            }
        });
        return list;
    }

    /** builds the recipes the first time they are needed (tags must be bound by then) */
    private static synchronized void ensureLoaded() {
        if (expanded) return;
        expanded = true;
        recipeList.clear();
        if (pending.isEmpty()) {
            collect();
        }
        for (Object[] p : pending) {
            List<ItemStack> result = (List<ItemStack>) p[0];
            List<Object> slots = (List<Object>) p[1];
            List<List<ItemStack>> input = new ArrayList<>();
            for (Object o : slots) {
                if (o == null) {
                    input.add(null);
                } else if (o instanceof ItemStack st) {
                    input.add(st.isEmpty() ? null : equivalents(st));
                } else if (o instanceof TagKey<?> tag) {
                    List<ItemStack> l = new ArrayList<>();
                    for (var h : BuiltInRegistries.ITEM.getTagOrEmpty((TagKey<Item>) tag)) l.add(new ItemStack(h.value()));
                    input.add(l);
                }
            }
            registerRecipe(new Recipe(result, input), (String) p[2]);
        }
    }

    /** tags reloaded: work the equivalents out again */
    public static synchronized void tagsChanged() {
        expanded = false;
    }

    private static void collect() {
        //transports
        for (GenericRailTransport t : TiMGenericRegistry.definitions()) {
            ItemStack[] r = t.getRecipe();
            if (r == null || t.getItem() == null) continue;
            pending.add(new Object[]{List.of(new ItemStack(t.getItem())), new ArrayList<Object>(Arrays.asList(r)), t.getCraftingTable()});
        }
        //JSON table recipes
        for (String ns : new String[]{"trainsinmotion", "traincraft"}) {
            loadManifest(ns);
        }
        LOG.info("TiM table recipes: " + pending.size());
    }

    private static void loadManifest(String ns) {
        InputStream m = RecipeManager.class.getClassLoader().getResourceAsStream("assets/" + ns + "/recipes/_manifest");
        if (m == null) return;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(m, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                InputStream f = RecipeManager.class.getClassLoader().getResourceAsStream("assets/" + ns + "/recipes/" + line.toLowerCase(Locale.ROOT));
                if (f == null) f = RecipeManager.class.getClassLoader().getResourceAsStream("assets/" + ns + "/recipes/" + line);
                if (f == null) {
                    LOG.warn("recipe in the manifest is missing: " + ns + "/" + line);
                    continue;
                }
                try (InputStreamReader fr = new InputStreamReader(f, StandardCharsets.UTF_8)) {
                    parse(JsonParser.parseReader(fr).getAsJsonObject(), line);
                } catch (Exception e) {
                    LOG.warn("could not read table recipe " + line + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.warn("could not read the recipe manifest of " + ns, e);
        }
    }

    /** a 1.12 item name in today's registry (TiM's items are in Traincraft's namespace, names lower case) */
    public static Item item(String id) {
        id = id.toLowerCase(Locale.ROOT);
        if (id.startsWith("trainsinmotion:")) id = "traincraft:" + id.substring("trainsinmotion:".length());
        id = switch (id) {
            case "traincraft:emptycanister" -> "traincraft:emptycanister";
            case "traincraft:block.dieselgenerator" -> "traincraft:block.dieselgenerator";
            default -> id;
        };
        ResourceLocation rl = ResourceLocation.tryParse(id);
        return rl == null ? Items.AIR : BuiltInRegistries.ITEM.get(rl);
    }

    private static Object ingredient(JsonObject o) {
        if (o.has("ore")) {
            String t = ORE_TAGS.get(o.get("ore").getAsString());
            if (t == null) throw new IllegalArgumentException("unknown ore name " + o.get("ore").getAsString());
            return TagKey.create(Registries.ITEM, ResourceLocation.parse(t));
        }
        Item i = item(o.get("item").getAsString());
        if (i == Items.AIR) throw new IllegalArgumentException("no item " + o.get("item").getAsString());
        return new ItemStack(i, o.has("count") ? o.get("count").getAsInt() : 1);
    }

    private static void parse(JsonObject json, String file) {
        JsonArray pattern = json.getAsJsonArray("pattern");
        JsonObject key = json.getAsJsonObject("key");
        JsonObject res = json.getAsJsonObject("result");
        Item out = item(res.get("item").getAsString());
        if (out == Items.AIR) {
            LOG.info("table recipe " + file + " skipped: no " + res.get("item").getAsString() + " yet");
            return;
        }
        //3x3, in reading order, the pattern at the top left (1.12 JsonRecipeHelper did the same)
        List<Object> slots = new ArrayList<>(Collections.nCopies(9, null));
        for (int row = 0; row < pattern.size() && row < 3; row++) {
            String p = pattern.get(row).getAsString();
            for (int col = 0; col < p.length() && col < 3; col++) {
                char c = p.charAt(col);
                if (c == ' ') continue;
                JsonElement e = key.get(String.valueOf(c));
                try {
                    slots.set(row * 3 + col, ingredient(e.getAsJsonObject()));
                } catch (IllegalArgumentException ex) {
                    LOG.info("table recipe " + file + " skipped: " + ex.getMessage());
                    return;
                }
            }
        }
        pending.add(new Object[]{List.of(new ItemStack(out, res.has("count") ? res.get("count").getAsInt() : 1)), slots, "traintable"});
    }

    /**
     * the results the grid can make on this table, or null
     */
    public static List<ItemStack> getResult(ItemStack[] recipe, String table){
        boolean empty = true;
        for (ItemStack is : recipe) {
            if (is != null && !is.isEmpty()) {
                empty = false;
                break;
            }
        }
        if (empty) return null;
        List<ItemStack> retStacks = new ArrayList<>();
        for(Recipe r : getRecipes(table)){
            if (r.inputMatches(Arrays.asList(recipe))) {
                for (ItemStack s : r.result) retStacks.add(s.copy());
            }
        }
        return retStacks.isEmpty() ? null : retStacks;
    }

    /** the recipe on the table that makes this item */
    public static Recipe getRecipe(ItemStack result, String table){
        for(Recipe r : getRecipes(table)){
            for(ItemStack stack : r.getresult()){
                if(result != null && stack.getItem()==result.getItem()){
                    return r;
                }
            }
        }
        return null;
    }

    /*
     * <h2>the rail table</h2>
     */
    public static List<ItemStack> getAcceptedRailItems(){
        List<ItemStack> ores=new ArrayList<>();
        ores.add(new ItemStack(Items.DIAMOND));
        ores.add(new ItemStack(Items.BLAZE_ROD));
        for (String name : new String[]{"traincraft:copperrail", "traincraft:steelrail"}) {
            Item rail = item(name);
            if (rail != Items.AIR) ores.add(new ItemStack(rail));
        }
        for (var tag : BuiltInRegistries.ITEM.getTagNames().toList()) {
            String t = tag.location().toString();
            if (t.startsWith("c:ingots/") || t.equals("minecraft:planks")) {
                for (var h : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) ores.add(new ItemStack(h.value()));
            }
        }
        return ores;
    }

    public static boolean ingotInDirectory(Item i){
        for(ItemStack stack : getAcceptedRailItems()){
            if (stack.getItem()==i){
                return true;
            }
        }
        return false;
    }

    /** the rail the rail table makes from rail material, ties and ballast */
    public static ItemStack railRecipe(ItemStack ingot, ItemStack ties, ItemStack ballast){
        if(ingot!=null && !ingot.isEmpty() && ingotInDirectory(ingot.getItem())) {
            return ItemRail.setStackData(new ItemStack(TiMBlocks.railItem()), ingot, ballast, ties, null);
        }
        return ItemStack.EMPTY;
    }
}
