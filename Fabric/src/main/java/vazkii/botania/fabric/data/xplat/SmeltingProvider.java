/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */
package vazkii.botania.fabric.data.xplat;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.data.recipes.BotaniaRecipeProvider;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;
import static vazkii.botania.data.util.BotaniaRecipeHelper.deriveRecipeId;

public class SmeltingProvider extends BotaniaRecipeProvider {
	public SmeltingProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
		super(recipes, advancements);
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_FUCHSITE, BotaniaBlocks.FUCHSITE);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_TALC, BotaniaBlocks.TALC);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_GNEISS, BotaniaBlocks.GNEISS);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_MYCELITE, BotaniaBlocks.MYCELITE);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_CATACLASITE, BotaniaBlocks.CATACLASITE);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_SOLITE, BotaniaBlocks.SOLITE);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_LUNITE, BotaniaBlocks.LUNITE);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.COBBLED_ROSY_TALC, BotaniaBlocks.ROSY_TALC);
		makeDefaultSmeltingRecipe(recipeOutput, BotaniaBlocks.LIVINGROCK_BRICKS, BotaniaBlocks.CRACKED_LIVINGROCK_BRICKS);

		for (String variant : LibBlockNames.QUARTZ_VARIANTS) {
			Block base = BuiltInRegistries.BLOCK.getValue(botaniaRL(variant + LibBlockNames.BLOCK_SUFFIX));
			Block smooth = BuiltInRegistries.BLOCK.getValue(
					botaniaRL(LibBlockNames.SMOOTH_PREFIX + variant + LibBlockNames.BLOCK_SUFFIX));
			makeDefaultSmeltingRecipe(recipeOutput, base, smooth);
		}
	}

	private void makeDefaultSmeltingRecipe(RecipeOutput consumer, Block input, Block output) {
		SimpleCookingRecipeBuilder
				.smelting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, output, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(input))
				.save(consumer, deriveRecipeId(RecipeType.SMELTING, output));
	}

}
