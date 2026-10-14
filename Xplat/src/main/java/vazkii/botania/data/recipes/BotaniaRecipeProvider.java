/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.data.recipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Base of Botania's recipe generators: vanilla's {@link RecipeProvider}, which writes into the recipe and
 * advancement registries now, keeping the {@code buildRecipes(RecipeOutput)} shape the providers use.
 */
public abstract class BotaniaRecipeProvider extends RecipeProvider {
	public BotaniaRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
		super(recipeOutput, advancementOutput);
	}

	@Override
	public void buildRecipes() { // public: NeoForge widens it
		buildRecipes(this.output);
	}

	public abstract void buildRecipes(RecipeOutput recipeOutput);
}
