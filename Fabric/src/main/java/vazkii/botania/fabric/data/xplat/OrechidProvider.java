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
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.flower.functional.MarimorphosisBlockEntity;
import vazkii.botania.common.block.block_entity.flower.functional.OrechidBlockEntity;
import vazkii.botania.common.block.block_entity.flower.functional.OrechidIgnemBlockEntity;
import vazkii.botania.common.crafting.*;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.util.BotaniaRecipeHelper;

public class OrechidProvider extends ConditionalRecipeProvider {

	public OrechidProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements, ConditionHook conditions) {
		super(recipes, advancements, conditions);
	}

	// TODO: We had an enormous amount of ores defined for mod compat.
	//       The old data needs to be completely revised.
	@Override
	public void buildRecipes(RecipeOutput consumer) {
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.COAL_ORE, 5129);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.IRON_ORE, 1568);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.REDSTONE_ORE, 71);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.COPPER_ORE, 2724,
				BotaniaTags.Biomes.ORECHID_STONE_COPPER_BONUS, 2500);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.GOLD_ORE, 106,
				BotaniaTags.Biomes.ORECHID_STONE_GOLD_BONUS, 1790);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.EMERALD_ORE, 1,
				BotaniaTags.Biomes.ORECHID_STONE_EMERALD_BONUS, 111);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.LAPIS_ORE, 281);
		stone(withConditions(consumer, GOG_NOT_LOADED), Blocks.DIAMOND_ORE, 10);

		deepslate(consumer, Blocks.DEEPSLATE_COAL_ORE, 57);
		deepslate(consumer, Blocks.DEEPSLATE_IRON_ORE, 2513);
		deepslate(consumer, Blocks.DEEPSLATE_REDSTONE_ORE, 2510);
		deepslate(consumer, Blocks.DEEPSLATE_COPPER_ORE, 471, BotaniaTags.Biomes.ORECHID_DEEPSLATE_COPPER_BONUS, 1350);
		deepslate(consumer, Blocks.DEEPSLATE_GOLD_ORE, 1605);
		deepslate(consumer, Blocks.DEEPSLATE_EMERALD_ORE, 1, BotaniaTags.Biomes.ORECHID_DEEPSLATE_EMERALD_BONUS, 10);
		deepslate(consumer, Blocks.DEEPSLATE_LAPIS_ORE, 1033);
		deepslate(consumer, Blocks.DEEPSLATE_DIAMOND_ORE, 1801);

		nether(consumer, Blocks.NETHERRACK, Blocks.NETHER_QUARTZ_ORE, 7387);
		nether(consumer, Blocks.NETHERRACK, Blocks.NETHER_GOLD_ORE, 2613);
		nether(consumer, Blocks.BLACKSTONE, Blocks.GILDED_BLACKSTONE, 100);

		biomeStone(consumer, BotaniaBlocks.FUCHSITE, BotaniaTags.Biomes.MARIMORPHOSIS_FUCHSITE_BONUS);
		biomeStone(consumer, BotaniaBlocks.TALC, BotaniaTags.Biomes.MARIMORPHOSIS_TALC_BONUS);
		biomeStone(consumer, BotaniaBlocks.GNEISS, BotaniaTags.Biomes.MARIMORPHOSIS_GNEISS_BONUS);
		biomeStone(consumer, BotaniaBlocks.MYCELITE, BotaniaTags.Biomes.MARIMORPHOSIS_MYCELITE_BONUS);
		biomeStone(consumer, BotaniaBlocks.CATACLASITE, BotaniaTags.Biomes.MARIMORPHOSIS_CATACLASITE_BONUS);
		biomeStone(consumer, BotaniaBlocks.SOLITE, BotaniaTags.Biomes.MARIMORPHOSIS_SOLITE_BONUS);
		biomeStone(consumer, BotaniaBlocks.LUNITE, BotaniaTags.Biomes.MARIMORPHOSIS_LUNITE_BONUS);
		biomeStone(consumer, BotaniaBlocks.ROSY_TALC, BotaniaTags.Biomes.MARIMORPHOSIS_ROSY_TALC_BONUS);
	}

	protected ResourceKey<Recipe<?>> orechidId(Block block) {
		return BotaniaRecipeHelper.deriveRecipeId(BotaniaRecipeTypes.ORECHID_TYPE, block);
	}

	protected ResourceKey<Recipe<?>> ignemId(Block block) {
		return BotaniaRecipeHelper.deriveRecipeId(BotaniaRecipeTypes.ORECHID_IGNEM_TYPE, block);
	}

	protected ResourceKey<Recipe<?>> marimorphosisId(Block block) {
		return BotaniaRecipeHelper.deriveRecipeId(BotaniaRecipeTypes.MARIMORPHOSIS_TYPE, block);
	}

	protected void stone(RecipeOutput consumer, Block output, int weight) {
		stone(consumer, output, weight, null, 0);
	}

	private void stone(RecipeOutput consumer, Block output, int weight, @Nullable TagKey<Biome> bonusBiomes, int bonusWeight) {
		consumer.accept(orechidId(output), new OrechidRecipe(forBlock(Blocks.STONE), forBlock(output),
				OrechidBlockEntity.DEFAULT_DELAY, OrechidBlockEntity.DEFAULT_COST, weight, bonusWeight, bonusBiomes), null);
	}

	protected void deepslate(RecipeOutput consumer, Block output, int weight) {
		deepslate(consumer, output, weight, null, 0);
	}

	protected void deepslate(RecipeOutput consumer, Block output, int weight, @Nullable TagKey<Biome> bonusBiome, int bonusWeight) {
		consumer.accept(orechidId(output), new OrechidRecipe(forBlock(Blocks.DEEPSLATE), forBlock(output),
				OrechidBlockEntity.DEFAULT_DELAY, OrechidBlockEntity.DEFAULT_COST, weight, bonusWeight, bonusBiome), null);
	}

	protected void nether(RecipeOutput consumer, Block input, Block output, int weight) {
		consumer.accept(ignemId(output), new OrechidIgnemRecipe(forBlock(input), forBlock(output),
				OrechidBlockEntity.DEFAULT_DELAY, OrechidIgnemBlockEntity.DEFAULT_COST, weight), null);
	}

	protected void biomeStone(RecipeOutput consumer, Block output, TagKey<Biome> biome) {
		consumer.accept(marimorphosisId(output), new MarimorphosisRecipe(
				forTag(BotaniaTags.Blocks.MARIMORPHOSIS_CONVERTABLE), forBlock(output),
				MarimorphosisBlockEntity.COST, 1, 11, biome), null);
	}

	protected static StateIngredient forBlock(Block block) {
		return StateIngredients.of(block);
	}

	protected static StateIngredient forTag(TagKey<Block> tag) {
		return StateIngredients.of(tag);
	}

}
