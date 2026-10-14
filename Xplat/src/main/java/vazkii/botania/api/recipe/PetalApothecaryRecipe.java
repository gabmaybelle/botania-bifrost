/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface PetalApothecaryRecipe extends RecipeWithReagent<ProcessingRecipeInput> {
	Identifier TYPE_ID = botaniaRL("petal_apothecary");

	/** The inputs, other than the reagent. (Vanilla recipes no longer list ingredients.) */
	NonNullList<Ingredient> getIngredients();

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends PetalApothecaryRecipe> getType() {
		return (RecipeType<? extends PetalApothecaryRecipe>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}
}
