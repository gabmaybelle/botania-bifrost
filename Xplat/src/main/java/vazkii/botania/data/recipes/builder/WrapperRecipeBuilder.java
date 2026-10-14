/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */
package vazkii.botania.data.recipes.builder;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.common.crafting.recipe.WrappingRecipeSerializer;

/**
 * Saves a shaped or shapeless recipe as the Botania subclass that one of the
 * {@link WrappingRecipeSerializer} serializers makes (same JSON, Botania's recipe type).
 */
public class WrapperRecipeBuilder<T extends Recipe<?>> {
	private final RecipeSerializer<T> serializer;
	private final RecipeBuilder recipeBuilder;

	public WrapperRecipeBuilder(RecipeSerializer<T> serializer, RecipeBuilder recipeBuilder) {
		this.recipeBuilder = recipeBuilder;
		this.serializer = serializer;
	}

	public static <T extends Recipe<?>> WrapperRecipeBuilder<T> wrap(RecipeSerializer<T> serializer,
			RecipeBuilder recipeBuilder) {
		return new WrapperRecipeBuilder<>(serializer, recipeBuilder);
	}

	public void save(RecipeOutput recipeOutput) {
		CapturingRecipeOutput capturingRecipeOutput = new CapturingRecipeOutput(recipeOutput);
		var output = capturingRecipeOutput.captureSave(recipeBuilder::save);

		T wrappedRecipe = WrappingRecipeSerializer.wrap(serializer, output.getMiddle());
		recipeOutput.accept(output.getLeft(), wrappedRecipe, output.getRight().orElse(null));
	}

	public void save(RecipeOutput recipeOutput, Identifier id) {
		save(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
	}

	public void save(RecipeOutput recipeOutput, String id) {
		save(recipeOutput, Identifier.parse(id));
	}

	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
		CapturingRecipeOutput capturingRecipeOutput = new CapturingRecipeOutput(recipeOutput);
		var output = capturingRecipeOutput.captureSave(out -> recipeBuilder.save(out, id));

		T wrappedRecipe = WrappingRecipeSerializer.wrap(serializer, output.getMiddle());
		recipeOutput.accept(output.getLeft(), wrappedRecipe, output.getRight().orElse(null));
	}
}
