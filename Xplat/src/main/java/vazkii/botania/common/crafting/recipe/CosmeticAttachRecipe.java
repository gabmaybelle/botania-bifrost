/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.CosmeticAttachable;
import vazkii.botania.api.item.CosmeticBauble;

public class CosmeticAttachRecipe extends CustomRecipe {
	public static final RecipeSerializer<CosmeticAttachRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(CosmeticAttachRecipe::new), StreamCodec.unit(new CosmeticAttachRecipe()));

	public CosmeticAttachRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundCosmetic = false;
		boolean foundAttachable = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CosmeticBauble && !foundCosmetic) {
					foundCosmetic = true;
				} else if (!foundAttachable) {
					if (stack.getItem() instanceof CosmeticAttachable attachable && !(stack.getItem() instanceof CosmeticBauble) && attachable.getCosmeticItem(stack).isEmpty()) {
						foundAttachable = true;
					} else {
						return false;
					}
				}
			}
		}

		return foundCosmetic && foundAttachable;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack cosmeticItem = ItemStack.EMPTY;
		ItemStack attachableItem = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CosmeticBauble && cosmeticItem.isEmpty()) {
					cosmeticItem = stack;
				} else {
					attachableItem = stack;
				}
			}
		}

		if (!(attachableItem.getItem() instanceof CosmeticAttachable attachable)) {
			return ItemStack.EMPTY;
		}

		if (!attachable.getCosmeticItem(attachableItem).isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack copy = attachableItem.copy();
		attachable.setCosmeticItem(copy, cosmeticItem);
		return copy;
	}

	@Override
	public RecipeSerializer<CosmeticAttachRecipe> getSerializer() {
		return SERIALIZER;
	}
}
