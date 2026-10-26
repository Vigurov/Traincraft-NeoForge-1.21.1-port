package ebf.tim.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * a fluid and an amount in mB, with the 1.12 Forge FluidStack calls TiM's tank code uses ({@code amount},
 * getFluid, getName for the tank filters)
 */
public class FluidStack {
    private final Fluid fluid;
    public int amount;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid == null ? Fluids.EMPTY : fluid;
        this.amount = amount;
    }

    public FluidStack(ebf.tim.registry.TiMFluids.TiMFluid fluid, int amount) {
        this(fluid.get(), amount);
    }

    public Fluid getFluid() {
        return fluid;
    }

    /**the fluid's registry name ("water", "diesel"): what the tank filters match*/
    public String getName() {
        return BuiltInRegistries.FLUID.getKey(fluid).getPath();
    }

    public String getLocalizedName() {
        return fluid.getFluidType().getDescription().getString();
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        t.putInt("amount", amount);
        return t;
    }

    public static FluidStack load(CompoundTag t) {
        return new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(t.getString("fluid"))), t.getInt("amount"));
    }

    /**the fluid a filled container (bucket, canister...) holds, 1.12 FluidUtil.getFluidContained*/
    public static FluidStack contained(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        var f = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(stack);
        return f.isPresent() && !f.get().isEmpty() ? new FluidStack(f.get().getFluid(), f.get().getAmount()) : null;
    }
}
