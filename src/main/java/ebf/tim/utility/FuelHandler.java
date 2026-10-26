package ebf.tim.utility;

import train.blocks.catenary.CatenaryNetwork;
import ebf.tim.TrainsInMotion;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMFluids;
import ebf.tim.registry.TiMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * <h1>Fuel management for trains</h1>
 * used to process burnHeat addition and consumption. The 1.12 logic: steam boilers heat from burning solid fuel and
 * turn water into steam, diesels burn diesel/fuel oil from their tank, electrics use stored "redstone" charge that
 * comes from redstone or from energy blocks (overhead wires, third rail) at the train.
 *
 * @author Eternal Blue Flame
 */
public class FuelHandler{

	/**the main burnHeat variable used by most trains*/
	public int burnHeat =0;
	public float burnTime =0;
	public float burnTimeMax =0;

	public enum DefaultTanks {
		STEAM(new String[][]{{"water"},{"steam"}}),
		HEAVY_STEAM(new String[][]{{"water"},{"heavysteam"}}),
		DIESEL(new String[][]{{"diesel", "fueloil"}}),
		ELECTRIC(new String[][]{{"redstone"}}),
		DIESEL_ELECTRIC(new String[][]{{"diesel", "fueloil"}, {"redstone"}});

		private final String[][] t;
		DefaultTanks(String[][] type){this.t = type;}
		public String[][] value(){return t;}
	}

	public float maxHeat(GenericRailTransport transport){
		return transport.getMaxFuel() * 750;
	}

	/**
	 * <h2>the fluid an item puts in the train</h2>
	 * redstone for electrics, diesel/fuel oil for diesels, water for steamers, anything for tankers.
	 */
	public static FluidStack getUseableFluid(ItemStack itemStack, GenericRailTransport transport){
		if(itemStack==null || itemStack.isEmpty()){
			return null;
		}
		//electrics take nothing from their slot: they run on the contact wire
		if(transport.getTypes().contains(TrainsInMotion.transportTypes.DIESEL)) {
			if (itemStack.getItem() == TiMItems.dieselCanister.get()) {
				return new FluidStack(TiMFluids.fluidDiesel, 1000);
			}
		}
		FluidStack contained = FluidStack.contained(itemStack);
		if(contained==null){
			return null;
		}
		String name = contained.getName();
		if(transport.getTypes().contains(TrainsInMotion.transportTypes.DIESEL)) {
			if (name.contains("diesel")) {
				return new FluidStack(TiMFluids.fluidDiesel, 1000);
			} else if (name.contains("fueloil")) {
				return new FluidStack(TiMFluids.fluidfueloil, 1000);
			} else if (name.contains("oil") || name.contains("fuel") || name.contains("ethanol") || name.contains("biomass")) {
				//other mods' fuels: kept as they are, the tank filter decides (1.12 did the same by name)
				return new FluidStack(contained.getFluid(), 1000);
			}
		}
		if(transport.getTypes().contains(TrainsInMotion.transportTypes.STEAM)){
			if(contained.getFluid()==Fluids.WATER){
				return new FluidStack(Fluids.WATER,1000);
			}
		}
		if(transport.getTypes().contains(TrainsInMotion.transportTypes.TANKER) || transport.getTypes().contains(TrainsInMotion.transportTypes.TENDER)){
			return contained;
		}
		return null;
	}

	public static FluidStack getUseableFluid(int slot, GenericRailTransport transport){
		return transport.getSlotIndexByID(slot)==null?null:getUseableFluid(transport.getSlotIndexByID(slot).getStack(),transport);
	}

	//adds burn times for items that aren't normally registered to minecraft's burnable system.
	public static int itemBurnTime(ItemStackSlot slotId) {
		if(slotId !=null && slotId.getHasStack()) {
			return slotId.getStackOrEmpty().getBurnTime(RecipeType.SMELTING);
		}
		return 0;
	}

	public static float getBoilerHeat(GenericRailTransport train){
		return (train.entityData.hasFloat("boilerHeat")?train.entityData.getFloat("boilerHeat"):0);
	}

	private static FluidStack tank(GenericRailTransport t, int i){
		return t.entityData.getFluidStack("tanks." + i);
	}

	/**
	 * <h2>steam management</h2>
	 *
	 * Credit to zodiacmal for the heat calculation.
	 */
	public void manageSteam(EntityTrainCore train){
		//manage solid burnHeat
		ItemStackSlot slotId=train.getSlotIndexByID(400);
		if (burnTime <1){
			burnTime=0;
			if (slotId != null && itemBurnTime(slotId)>0) {
				burnHeat = (int) (itemBurnTime(slotId) * train.getFuelEfficiency());
				burnTime = Mth.ceil(burnHeat *0.1);
				burnTimeMax = burnTime;
				if (!train.getBoolean(GenericRailTransport.boolValues.CREATIVE)) {
					ItemStack fuel = slotId.getStackOrEmpty();
					ItemStack remainder = fuel.getCraftingRemainingItem();
					slotId.decrStackSize(1);
					if (!remainder.isEmpty()) {
						train.addItem(remainder);
					}
				}
				train.entityData.putFloat("burnTime", burnTime);
				train.entityData.putFloat("maxBurn", burnTimeMax);
				train.entityData.putInt("burnHeat", burnHeat);
			} else {
				burnHeat = 0;
				burnTimeMax = 0;
				train.entityData.putFloat("maxBurn", 0f);
				train.entityData.putInt("burnHeat", 0);
			}
		} else {
			burnTime--;
			train.entityData.putFloat("burnTime", burnTime);
		}

		//if there's a fluid item in the slot and the train can consume the entire thing
		FluidStack water = getUseableFluid(train.waterSlot().getSlotID(),train);
		if (water !=null && train.fill(water,false)==water.amount) {
			train.fill(water, true);
			if (!train.getBoolean(GenericRailTransport.boolValues.CREATIVE)) {
				train.getSlotIndexByID(train.waterSlot().getSlotID()).decrStackSize(1);
				train.addItem(new ItemStack(Items.BUCKET));
				train.markDirty();
			}
		}
		//take water from a tender or tanker coupled to either end
		for (Integer id : new Integer[]{train.frontLinkedID, train.backLinkedID}) {
			if (id == null || !(train.level().getEntity(id) instanceof GenericRailTransport link)) {
				continue;
			}
			FluidStack sample = new FluidStack(Fluids.WATER, 100);
			if((link.getTypes().contains(TrainsInMotion.transportTypes.TENDER) ||
					link.getTypes().contains(TrainsInMotion.transportTypes.TANKER))
					&& train.fill(sample,false)==100
					&& link.drain(sample,false)!=null
					&& link.drain(sample,false).amount>=100){
				link.drain(sample, true);
				train.fill(new FluidStack(Fluids.WATER, 100),true);
			}
		}

		//manage boiler heat
		if (burnHeat > 1) {
			//calculate the heat increase
			float heat = getBoilerHeat(train);
			if(heat==0){heat=1;}
			train.entityData.putFloat("boilerHeat", heat +
					(float) ((1f- Math.sqrt(heat/maxHeat(train)))
							* Math.sqrt((heat+burnHeat)/burnHeat))*(train.getFuelEfficiency()*4)
			);
		} else {//if engine is not running
			float heat = getBoilerHeat(train);
			BlockPos pos = train.blockPosition();
			float biomeHeat =  (((train.level().getBiome(pos).value().getBaseTemperature() -0.15f)//biome temperature with offset to compensate for freezing point
					- (0.0014166695f * ((float)train.getY() - 64f)))//temperature changes by 0.00166667 for every meter above or below sea level (64)
					*0.368f//convert to celsius*0.01
			);
			//make steamers heat faster in creative
			if(train.getBoolean(GenericRailTransport.boolValues.CREATIVE)){
				biomeHeat=80;
			}
			if(heat>biomeHeat){
				heat -=biomeHeat*0.1f;
			} else {
				heat =Math.max(heat,biomeHeat);
			}
			train.entityData.putFloat("boilerHeat", heat);
		}

		if(tank(train, 0)!=null) {
			//if the boiler temp is above the boiling point, start generating steam.
			if (getBoilerHeat(train) > 100) {
				int steam = (int) Math.floor(
						((getBoilerHeat(train) - 100) * 0.005f) * //calculate heat from burnHeat
								(tank(train, 0).amount * 0.005f) //calculate surface area of water
				);
				//drain fluid
				if(steam>0 && steam/5>0) {
					if (train.drain(new FluidStack(Fluids.WATER, steam / 5), true) != null) {
						train.fill(new FluidStack(TiMFluids.fluidSteam, (int) (-(Math.abs(train.accelerator) * (train.getTankCapacity()[1] * 0.01f)) + steam * 0.9f)), true);
						//if no fluid left and not creative mode, explode.
					} else if (!train.getBoolean(GenericRailTransport.boolValues.CREATIVE)) {
						train.level().explode(train, train.getX(), train.getY(), train.getZ(), 5f, Level.ExplosionInteraction.NONE);
						train.spawnAtLocation(train.getCartItem(), 1);
						train.discard();
						return;
					}
					if (!train.getBoolean(GenericRailTransport.boolValues.RUNNING)) {
						train.setBoolean(GenericRailTransport.boolValues.RUNNING, true);
						train.updateConsist();
					}
				}
			} else {
				train.setBoolean(GenericRailTransport.boolValues.RUNNING, false);
				train.accelerator = 0;
				train.updateConsist();
			}
		}
		if (tank(train, 1)!=null && tank(train, 1).amount >0) {
			//steam is expelled through the pistons to push them back and forth, but even when the accelerator is off, a degree of steam is still escaping.
			train.entityData.putFluidStack("tanks.1", new FluidStack(tank(train, 1).getFluid(),
					Math.max(0, tank(train, 1).amount-(int)((5 * train.getFuelEfficiency()) * ((train.accelerator) * train.getFuelEfficiency()) * 0.55))));
			train.markDirty();
		}

		//update the datawatchers so client can display the info on the GUI.
		train.entityData().set(GenericRailTransport.FUEL_CONSUMPTION, burnTime>0?(int)((burnTime/ burnTimeMax)*18):0);
		train.entityData().set(GenericRailTransport.BOILER_HEAT, (int)(getBoilerHeat(train) * 100f));
	}

	public static void manageDieselFuel(EntityTrainCore train){
		//add fuel to the fuel tank
		FluidStack fuel = getUseableFluid(400,train);
		if(fuel !=null) {
			if (train.fill(fuel)) {
				if(!train.getBoolean(GenericRailTransport.boolValues.CREATIVE)) {
					ItemStackSlot slot = train.getSlotIndexByID(train.fuelSlot().getSlotID());
					if (slot.getItem() == TiMItems.dieselCanister.get()) {
						slot.decrStackSize(1);
						train.addItem(new ItemStack(TiMItems.emptyCanister.get()));
					} else {
						slot.decrStackSize(1);
						train.addItem(new ItemStack(Items.BUCKET));
					}
				}
			}
		}
		//use stored fuel
		if (train.getBoolean(GenericRailTransport.boolValues.RUNNING)){
			//diesel trains use fuel similar to electric, except idle will use fuel.
			int use = train.accelerator==0 ? (int)(1f*train.getFuelEfficiency())
					: CommonUtil.floorDouble((1 * train.getFuelEfficiency()) + (Math.copySign(train.accelerator, 1) * (5 * train.getFuelEfficiency())));
			if (train.drain(0, use, false)==0) {
				train.drain(0, use, true);
			} else {
				train.setBoolean(GenericRailTransport.boolValues.RUNNING, false);
				train.updateConsist();
			}
		}
	}

	/**
	 * an electric runs on the contact wire over it, as a real one: it takes what its motors use from the line (the
	 * substations feeding it), keeping a few seconds' worth (its capacitors) to ride over a short gap. Without
	 * power from the wire it does not pull, its engine still on (1.12 filled a tank with redstone and ran on that)
	 */
	public static void manageElectricFuel(EntityTrainCore train){
		int perSecond = electricUse(train);
		int keep = Math.min(train.getTankCapacity()[0], 3 * electricMaxUse(train));
		FluidStack in = tank(train, 0);
		int have = in == null ? 0 : in.amount;
		int want = keep - have + (train.getBoolean(GenericRailTransport.boolValues.RUNNING) ? perSecond : 0);
		if (want > 0) {
			int got = CatenaryNetwork.draw(train.level(), train.getX(), train.getY(), train.getZ(), want);
			if (got > 0) train.fill(new FluidStack(TiMFluids.fluidRedstone, got), true);
		}
		if (train.getBoolean(GenericRailTransport.boolValues.RUNNING) && perSecond > 0) {
			FluidStack now = tank(train, 0);
			int use = Math.min(perSecond, now == null ? 0 : now.amount);
			if (use > 0) train.drain(0, use, true);
		}
	}

	/** what an electric's motors use in a second at its throttle */
	public static int electricUse(EntityTrainCore train) {
		return CommonUtil.floorDouble(train.getFuelEfficiency() + Math.abs(train.accelerator) * 5 * train.getFuelEfficiency());
	}

	private static int electricMaxUse(EntityTrainCore train) {
		return CommonUtil.floorDouble(train.getFuelEfficiency() * 41);
	}

	/**tankers and tenders: fill from buckets in the input slot, fill empty buckets in the output slot*/
	public static void manageTanker(GenericRailTransport transport){
		FluidStack in = getUseableFluid(transport.tankerInputSlot().getSlotID(),transport);
		if (in !=null && transport.fill(in,false)==in.amount) {
			transport.fill(in, true);
			if (!transport.getBoolean(GenericRailTransport.boolValues.CREATIVE)) {
				transport.getSlotIndexByID(transport.tankerInputSlot().getSlotID()).decrStackSize(1);
				transport.addItem(new ItemStack(Items.BUCKET));
			}
		}
		//attempt to fill any buckets in the drain slot
		ItemStackSlot out = transport.getSlotIndexByID(transport.tankerOutputSlot().getSlotID());
		if (out!=null && out.getItem() == Items.BUCKET) {
			for (int i = 0; i < transport.getTankCapacity().length; i++) {
				FluidStack t = tank(transport, i);
				if (t != null && t.amount >= 1000 && t.getFluid().getBucket() != Items.AIR) {
					ItemStack filled = new ItemStack(t.getFluid().getBucket());
					transport.drain(i, 1000, true);
					out.decrStackSize(1);
					transport.addItem(filled);
					break;
				}
			}
		}
	}
}
