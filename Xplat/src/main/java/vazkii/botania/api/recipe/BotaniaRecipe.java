/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: what every Botania recipe answers to the parts of vanilla's {@link Recipe} that arrived with
 * 1.21.2. Botania's recipes are made in blocks and flowers, not in the recipe book, so none of them is
 * placeable, they all sit in one Botania recipe-book category (registered by the mod), and they have no
 * group or toast.
 */
public interface BotaniaRecipe<T extends RecipeInput> extends Recipe<T> {
	/** The recipe-book category all Botania recipes report; the mod registers it. */
	Identifier BOOK_CATEGORY_ID = botaniaRL("botania");

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return Objects.requireNonNull(BuiltInRegistries.RECIPE_BOOK_CATEGORY.getValue(BOOK_CATEGORY_ID));
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default boolean showNotification() {
		return false;
	}
}
