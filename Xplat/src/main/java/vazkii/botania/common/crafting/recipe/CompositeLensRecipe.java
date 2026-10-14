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

import vazkii.botania.api.mana.CompositableLensItem;
import vazkii.botania.common.lib.BotaniaTags;

public class CompositeLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<CompositeLensRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(CompositeLensRecipe::new), StreamCodec.unit(new CompositeLensRecipe()));

	public CompositeLensRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundLens = false;
		boolean foundSecondLens = false;
		boolean foundGlue = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CompositableLensItem && !foundSecondLens) {
					if (foundLens) {
						foundSecondLens = true;
					} else {
						foundLens = true;
					}
				} else if (stack.is(BotaniaTags.Items.LENS_GLUE) && !foundGlue) {
					foundGlue = true;
				} else {
					return false; // Found an invalid or extra item, breaking the recipe
				}
			}
		}

		return foundSecondLens && foundGlue;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack lens = ItemStack.EMPTY;
		ItemStack secondLens = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CompositableLensItem) {
					if (lens.isEmpty()) {
						lens = stack;
					} else {
						secondLens = stack;
					}
				}
			}
		}

		if (lens.getItem() instanceof CompositableLensItem lensItem) {
			if (secondLens.isEmpty() || !lensItem.canCombineLenses(lens, secondLens) || !lensItem.getCompositeLens(lens).isEmpty() || !lensItem.getCompositeLens(secondLens).isEmpty()) {
				return ItemStack.EMPTY;
			}

			ItemStack lensCopy = lens.copyWithCount(1);
			ItemStack secondCopy = secondLens.copyWithCount(1);
			lensItem.setCompositeLens(lensCopy, secondCopy);

			return lensCopy;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<CompositeLensRecipe> getSerializer() {
		return SERIALIZER;
	}
}
