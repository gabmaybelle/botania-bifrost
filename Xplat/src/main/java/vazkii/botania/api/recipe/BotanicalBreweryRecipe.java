/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.api.brew.Brew;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface BotanicalBreweryRecipe extends BotaniaRecipe<ProcessingRecipeInput> {
	Identifier TYPE_ID = botaniaRL("brew");

	/** The inputs, other than the container. (Vanilla recipes no longer list ingredients.) */
	NonNullList<Ingredient> getIngredients();

	Brew getBrew();

	int getManaUsage();

	ItemStack getOutput(ItemStack container);

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends BotanicalBreweryRecipe> getType() {
		return (RecipeType<? extends BotanicalBreweryRecipe>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	default ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	default ItemStack assemble(ProcessingRecipeInput inv) {
		return inv.isEmpty() ? ItemStack.EMPTY : getOutput(inv.getItem(0));
	}

	default NonNullList<ItemStack> getRemainingItems(ProcessingRecipeInput input) {
		// PORT-26.3: vanilla's default remaining-items logic only exists for crafting recipes now; this is the same thing.
		ProcessingRecipeInput inputs = input.isEmpty() ? input : input.getSubset(1, input.size());
		NonNullList<ItemStack> remaining = NonNullList.withSize(inputs.size(), ItemStack.EMPTY);
		for (int i = 0; i < inputs.size(); i++) {
			var remainder = inputs.getItem(i).getItem().getCraftingRemainder();
			if (remainder != null) {
				remaining.set(i, remainder.create());
			}
		}
		return remaining;
	}

}
