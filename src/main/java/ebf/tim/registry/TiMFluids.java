package ebf.tim.registry;

import ebf.tim.TrainsInMotion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * the TiM fluids: oil, diesel, fuel oil, steam, heavy steam and "redstone" (electric charge). Each has a fluid type,
 * a source and a flowing fluid, a block and a bucket. The names (diesel, steam...) are what tank filters match.
 */
public class TiMFluids {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TrainsInMotion.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TrainsInMotion.MODID);
    public static final List<TiMFluid> ALL = new ArrayList<>();

    public static final TiMFluid fluidOil = new TiMFluid("oil", 700, 3000, MapColor.COLOR_BLACK, false);
    public static final TiMFluid fluidDiesel = new TiMFluid("diesel", 500, 1500, MapColor.SAND, false);
    public static final TiMFluid fluidfueloil = new TiMFluid("fueloil", 600, 2000, MapColor.COLOR_BROWN, false);
    public static final TiMFluid fluidSteam = new TiMFluid("steam", 200, 200, MapColor.SNOW, true);
    public static final TiMFluid fluidHeavySteam = new TiMFluid("heavysteam", 600, 400, MapColor.SNOW, true);
    public static final TiMFluid fluidRedstone = new TiMFluid("redstone", 100, 1000, MapColor.COLOR_RED, false);

    /** one TiM fluid and everything that goes with it */
    public static class TiMFluid {
        public final String name;
        public final DeferredHolder<FluidType, FluidType> type;
        public final DeferredHolder<Fluid, FlowingFluid> source, flowing;
        public final DeferredBlock<LiquidBlock> block;
        public final DeferredItem<BucketItem> bucket;

        TiMFluid(String name, int density, int viscosity, MapColor color, boolean gas) {
            this.name = name;
            type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid." + name).density(gas ? -density : density).viscosity(viscosity)
                    .temperature(gas ? 373 : 300).canSwim(!gas).canDrown(!gas)));
            source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties()));
            flowing = FLUIDS.register(name + "_flowing", () -> new BaseFlowingFluid.Flowing(properties()));
            block = TiMBlocks.BLOCKS.register(name, () -> new LiquidBlock(source.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(color)));
            bucket = TiMGenericRegistry.ITEMS.register("bucket_" + name, () -> new BucketItem(source.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
            ALL.add(this);
        }

        private BaseFlowingFluid.Properties properties() {
            return new BaseFlowingFluid.Properties(type, source, flowing).block(block).bucket(bucket);
        }

        public Fluid get() {
            return source.get();
        }

        public String getName() {
            return name;
        }
    }

    /** the still and flowing textures (assets/trainsinmotion/textures/blocks/fluids) */
    public static void registerClient(RegisterClientExtensionsEvent event) {
        for (TiMFluid f : ALL) {
            ResourceLocation still = ResourceLocation.fromNamespaceAndPath("trainsinmotion", "blocks/fluids/" + f.name);
            ResourceLocation flow = ResourceLocation.fromNamespaceAndPath("trainsinmotion", "blocks/fluids/" + f.name + "_flow");
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return still;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flow;
                }
            }, f.type.get());
        }
    }

    public static void init() {}
}
