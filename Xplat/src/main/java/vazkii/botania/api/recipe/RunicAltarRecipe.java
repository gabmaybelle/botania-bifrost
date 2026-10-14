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

public interface RunicAltarRecipe extends RecipeWithReagent<ProcessingRecipeInput>, RecipeWithCatalysts<ProcessingRecipeInput> {
	Identifier TYPE_ID = botaniaRL("runic_altar");
	Identifier HEAD_TYPE_ID = botaniaRL("runic_altar_head");

	/** The inputs, other than the reagent. (Vanilla recipes no longer list ingredients.) */
	NonNullList<Ingredient> getIngredients();

	int getMana();

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends RunicAltarRecipe> getType() {
		return (RecipeType<? extends RunicAltarRecipe>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}
}
