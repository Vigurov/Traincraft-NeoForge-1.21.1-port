package ebf.tim.utility;

import train.blocks.catenary.CatenaryMastTile;
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
		if(transport.getTypes().contains(TrainsInMotion.transportTypes.ELECTRIC)) {
			if (itemStack.getItem() == Items.REDSTONE) {
				return new FluidStack(TiMFluids.fluidRedstone, 250);
			} else if (itemStack.getItem() == Blocks.REDSTONE_BLOCK.asItem()) {
				return new FluidStack(TiMFluids.fluidRedstone, 2250);
			}
			IEnergyStorage energy = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
			if (energy != null && energy.extractEnergy(250, true) > 0) {
				return new FluidStack(TiMFluids.fluidRedstone, energy.extractEnergy(250, true));
			}
		}
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

	public static void manageElectricFuel(EntityTrainCore train){
		//add redstone to the fuel tank
		FluidStack charge = getUseableFluid(train.fuelSlot().getSlotID(),train);
		if(charge!=null){
			if(train.fill(charge)){
				if (!train.getBoolean(GenericRailTransport.boolValues.CREATIVE)) {
					ItemStackSlot slot = train.getSlotIndexByID(train.fuelSlot().getSlotID());
					IEnergyStorage energy = slot.getStackOrEmpty().getCapability(Capabilities.EnergyStorage.ITEM);
					if (energy != null && slot.getItem() != Items.REDSTONE && slot.getItem() != Blocks.REDSTONE_BLOCK.asItem()) {
						energy.extractEnergy(250, false);
					} else {
						slot.decrStackSize(1);
					}
				}
			}
		}
		//the catenary over the train: up to 500 a second, enough to run on at full power
		int room = train.fill(new FluidStack(TiMFluids.fluidRedstone, 500), false);
		if (room > 0) {
			int got = CatenaryMastTile.feed(train.level(), train.getX(), train.getY(), train.getZ(), room);
			if (got > 0) train.fill(new FluidStack(TiMFluids.fluidRedstone, got), true);
		}
		//fill from overhead wires/3rd rail/under rail: any energy block from 2 below to 4 above the train
		if (train.fill(new FluidStack(TiMFluids.fluidRedstone, 100), false) == 100) {
			for (int i=-2; i<5;i++) {
				if(i==1){continue;}
				BlockPos pos = BlockPos.containing(train.getX(), train.getY() + i, train.getZ());
				IEnergyStorage te = train.level().getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
				//as much as the block gives in a tick, up to 100 (Traincraft's generators give at most 80 at a time)
				if (te != null && te.extractEnergy(100, true) > 0) {
					train.fill(new FluidStack(TiMFluids.fluidRedstone, te.extractEnergy(100, false)), true);
					break;
				}
			}
		}
		//use stored energy
		if (train.getBoolean(GenericRailTransport.boolValues.RUNNING)){
			//electric trains run at a generally set rate which is multiplied at the square of speed.
			int use = CommonUtil.floorDouble((1*train.getFuelEfficiency()) + (Math.copySign(train.accelerator, 1)*(5*train.getFuelEfficiency())));
			if (train.drain(0, use, false)<1){
				train.drain(0, use, true);
			} else {
				train.setBoolean(GenericRailTransport.boolValues.RUNNING, false);
				train.updateConsist();
			}
		}
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
