package ebf.tim.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

/**
 * TiM materials: steel, copper and aluminium ores, blocks and ingots, plastic and coal dust.
 * Smelting, storage block recipes, item tags and ore generation are data files (data/traincraft, data/c).
 * Copper: vanilla 1.21 has its own; the TiM one stays (old recipes name it) and the tags take both.
 */
public class TiMOres {
    public static final List<DeferredItem<?>> MATERIALS = new ArrayList<>();

    public static DeferredItem<Item> ingotSteel = material("ingot.steel"), ingotCopper = material("ingot.copper"),
            ingotAluminum = material("ingot.aluminum"), dustPlastic = material("dust.plastic"), dustCoal = material("dust.coal");

    public static DeferredBlock<Block> blockCopper = block("blockcopper", 4.0F, MapColor.COLOR_ORANGE, SoundType.METAL),
            blockAluminum = block("blockaluminum", 4.0F, MapColor.METAL, SoundType.METAL),
            blockSteel = block("blocksteel", 5.0F, MapColor.METAL, SoundType.METAL),
            oreCopper = block("orecopper", 3.0F, MapColor.STONE, SoundType.STONE),
            oreAluminum = block("orealuminum", 3.0F, MapColor.STONE, SoundType.STONE),
            oreSteel = block("oresteel", 3.0F, MapColor.NETHER, SoundType.NETHER_ORE);

    private static DeferredItem<Item> material(String name) {
        DeferredItem<Item> i = TiMGenericRegistry.ITEMS.registerSimpleItem(name);
        MATERIALS.add(i);
        return i;
    }

    private static DeferredBlock<Block> block(String name, float strength, MapColor color, SoundType sound) {
        DeferredBlock<Block> b = TiMBlocks.BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of().mapColor(color)
                .strength(strength, strength).sound(sound).requiresCorrectToolForDrops());
        MATERIALS.add(TiMGenericRegistry.ITEMS.registerSimpleBlockItem(b));
        return b;
    }

    public static void init() {}
}
