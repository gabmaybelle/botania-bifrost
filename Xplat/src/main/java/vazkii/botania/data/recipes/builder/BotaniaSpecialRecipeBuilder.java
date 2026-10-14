/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes.builder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class BotaniaSpecialRecipeBuilder {
	private final Supplier<Recipe<?>> factory;

	public BotaniaSpecialRecipeBuilder(Supplier<Recipe<?>> factory) {
		this.factory = factory;
	}

	public static BotaniaSpecialRecipeBuilder special(Supplier<Recipe<?>> factory) {
		return new BotaniaSpecialRecipeBuilder(factory);
	}

	public void save(RecipeOutput recipeOutput) {
		Recipe<?> recipe = this.factory.get();
		RecipeSerializer<?> serializer = recipe.getSerializer();
		Identifier serializerKey = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
		Identifier recipeId = Identifier.fromNamespaceAndPath(serializerKey.getNamespace(),
				"dynamic/" + serializerKey.getPath().replaceFirst("crafting_(?:special|shaped|shapeless)_", ""));
		recipeOutput.accept(ResourceKey.create(Registries.RECIPE, recipeId), recipe, null);
	}
}
