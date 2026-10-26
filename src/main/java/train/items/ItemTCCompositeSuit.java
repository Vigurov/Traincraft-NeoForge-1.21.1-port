/*******************************************************************************
 * Copyright (c) 2012 Mrbrutal. All rights reserved.
 *
 * @name TrainCraft
 * @author Mrbrutal
 ******************************************************************************/

package train.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/**
 * The composite suit: each piece does its 1.12 trick and pays for it in durability.
 * Helmet: cures poison, wither, blindness and nausea, water breathing under water, night vision in the dark.
 * Chest: heals half a heart every 100 ticks. Pants: put the fire out. Boots: higher jumps, no fall damage.
 */
public class ItemTCCompositeSuit extends ItemTCArmor {
	public ItemTCCompositeSuit(Holder<ArmorMaterial> material, Type type, int color, String texture, Properties props) {
		super(material, type, color, texture, props);
	}

	/**
	 * 1.12: with more than {@code cost} durability left the effect happens and costs that much, otherwise the
	 * piece uses up what is left (breaks) and nothing happens
	 */
	private static boolean pay(ItemStack stack, Player player, EquipmentSlot slot, int cost) {
		int left = stack.getMaxDamage() - stack.getDamageValue();
		if (left > cost) {
			stack.hurtAndBreak(cost, player, slot);
			return true;
		}
		stack.hurtAndBreak(left, player, slot);
		return false;
	}

	private static void cure(Player player, ItemStack helmet, Holder<MobEffect> effect) {
		if (player.hasEffect(effect) && pay(helmet, player, EquipmentSlot.HEAD, 5)) {
			player.removeEffect(effect);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (level.isClientSide || !(entity instanceof Player player) || player.getItemBySlot(getEquipmentSlot()) != stack) {
			return;
		}
		switch (getType()) {
			case HELMET -> {
				cure(player, stack, MobEffects.POISON);
				cure(player, stack, MobEffects.WITHER);
				cure(player, stack, MobEffects.BLINDNESS);
				cure(player, stack, MobEffects.CONFUSION);
				if (player.isInWater() && !player.hasEffect(MobEffects.WATER_BREATHING) && pay(stack, player, EquipmentSlot.HEAD, 1)) {
					player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 10 * 20, 0));
				}
				BlockPos eyes = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
				if (level.getMaxLocalRawBrightness(eyes) <= 4 && (level.isEmptyBlock(eyes) || level.containsAnyLiquid(player.getBoundingBox()))) {
					MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
					if ((nv == null || nv.getDuration() < 220) && pay(stack, player, EquipmentSlot.HEAD, 1)) {
						player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 820, 0, true, false));
					}
				}
			}
			case CHESTPLATE -> {
				if (player.getHealth() < player.getMaxHealth() && player.tickCount % 100 == 0 && pay(stack, player, EquipmentSlot.CHEST, 1)) {
					player.heal(1);
				}
			}
			case LEGGINGS -> {
				if (player.isOnFire() && pay(stack, player, EquipmentSlot.LEGS, 1)) {
					player.clearFire();
				}
			}
			default -> {
			}
		}
	}

	private static ItemStack boots(Entity e) {
		if (e instanceof Player player && !player.isInWater()) {
			ItemStack b = player.getItemBySlot(EquipmentSlot.FEET);
			if (b.getItem() instanceof ItemTCCompositeSuit) return b;
		}
		return ItemStack.EMPTY;
	}

	/** boots: jump a bit higher */
	public static void onJump(LivingEvent.LivingJumpEvent event) {
		ItemStack b = boots(event.getEntity());
		if (!b.isEmpty() && pay(b, (Player) event.getEntity(), EquipmentSlot.FEET, 5)) {
			event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().add(0, 0.05, 0));
		}
	}

	/** boots: no fall damage */
	public static void onFall(LivingFallEvent event) {
		ItemStack b = boots(event.getEntity());
		if (!b.isEmpty() && event.getDistance() - 3 > 0 && pay(b, (Player) event.getEntity(), EquipmentSlot.FEET, 5)) {
			event.setCanceled(true);
		}
	}
}
