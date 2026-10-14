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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.common.lib.LibBlockNames;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface TerrestrialAgglomerationRecipe extends BotaniaRecipe<ProcessingRecipeInput> {
	Identifier TERRA_PLATE_ID = botaniaRL(LibBlockNames.TERRESTRIAL_AGGLOMERATION_PLATE);
	Identifier TYPE_ID = TERRA_PLATE_ID;

	/** The inputs. (Vanilla recipes no longer list ingredients.) */
	NonNullList<Ingredient> getIngredients();

	int getMana();

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends TerrestrialAgglomerationRecipe> getType() {
		return (RecipeType<? extends TerrestrialAgglomerationRecipe>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	default NonNullList<ItemStack> getRemainingItems(ProcessingRecipeInput input) {
		// terra plate always consumes all input items
		return NonNullList.create();
	}

	default ItemStack getToastSymbol() {
		return BuiltInRegistries.ITEM.getOptional(TERRA_PLATE_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
	}
}
