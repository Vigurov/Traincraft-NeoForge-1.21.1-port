/*******************************************************************************
 * Copyright (c) 2012 Mrbrutal. All rights reserved.
 *
 * @name TrainCraft
 * @author Mrbrutal
 ******************************************************************************/

package train.items;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Traincraft clothes. {@code color} is the default colour of a paintable piece (0: not paintable); the dye itself is
 * the vanilla dyed_color component, so the vanilla armour dye recipe paints them (they are in #minecraft:dyeable).
 * On the body the paintable ones are two layers like leather: {@code _1} tinted, {@code _2} the overlay on top.
 */
public class ItemTCArmor extends ArmorItem {
	public final int color;
	/** the 1.12 armour texture, without the _1/_2 of the paintable ones */
	private final String texture;

	public ItemTCArmor(Holder<ArmorMaterial> material, Type type, int color, String texture, Properties props) {
		super(material, type, props);
		this.color = color;
		this.texture = texture;
	}

	@Override
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		if (color == 0) {
			return ResourceLocation.fromNamespaceAndPath("traincraft", "textures/armor/" + texture + ".png");
		}
		return ResourceLocation.fromNamespaceAndPath("traincraft", "textures/armor/" + texture + (layer.dyeable() ? "1" : "2") + ".png");
	}

	/** the colour the piece is painted, or its default one */
	public int getColor(ItemStack stack) {
		return DyedItemColor.getOrDefault(stack, color);
	}
}
