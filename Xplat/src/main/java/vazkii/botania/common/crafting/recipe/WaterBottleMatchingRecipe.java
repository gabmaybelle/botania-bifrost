/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import vazkii.botania.mixin.NormalCraftingRecipeAccessor;
import vazkii.botania.mixin.ShapedRecipeAccessor;
import vazkii.botania.mixin.ShapedRecipePatternAccessor;

import java.util.List;
import java.util.Optional;

public class WaterBottleMatchingRecipe extends ShapedRecipe {
	public static final RecipeSerializer<ShapedRecipe> SERIALIZER = WrappingRecipeSerializer.shaped(WaterBottleMatchingRecipe::new);

	private static ShapedRecipePattern transformPattern(ShapedRecipePattern pattern) {
		// PORT-26.3: recipes load before item components are bound, so no test stack is built: the ingredient is asked by holder
		final var potion = Items.POTION.builtInRegistryHolder();
		// PORT-26.3: a pattern's ingredients are optional (empty slots) now
		final List<Optional<Ingredient>> ingredients = pattern.ingredients().stream()
				.map(slot -> slot.map(i -> i.acceptsItem(potion)
						? Ingredient.of(Items.POTION) // PORT-26.3: ingredients are items only; matches() checks for water
						: i))
				.toList();
		return new ShapedRecipePattern(pattern.width(), pattern.height(), ingredients,
				// TODO: verify
				((ShapedRecipePatternAccessor) (Object) pattern).botania_getData());
	}

	public WaterBottleMatchingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
		super(commonInfo, bookInfo, transformPattern(pattern), result);
	}

	private WaterBottleMatchingRecipe(ShapedRecipe recipe) {
		super(((NormalCraftingRecipeAccessor) recipe).botania_getCommonInfo(), ((NormalCraftingRecipeAccessor) recipe).botania_getBookInfo(),
				transformPattern(((ShapedRecipeAccessor) recipe).botania_getPattern()), ((ShapedRecipeAccessor) recipe).botania_getResult());
	}

	@Override
	public boolean matches(CraftingInput craftingContainer, Level level) {
		if (!super.matches(craftingContainer, level)) {
			return false;
		}
		for (int i = 0; i < craftingContainer.size(); i++) {
			var item = craftingContainer.getItem(i);
			if (item.is(Items.POTION) && !item.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return SERIALIZER;
	}
}
