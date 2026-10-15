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
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;

/**
 * Base of Botania's recipe generators: vanilla's {@link RecipeProvider}, which writes into the recipe and
 * advancement registries now, keeping the {@code buildRecipes(RecipeOutput)} shape the providers use.
 */
public abstract class BotaniaRecipeProvider extends RecipeProvider {
	protected final HolderGetter<Item> items;

	public BotaniaRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
		super(recipeOutput, advancementOutput);
		this.items = recipeOutput.lookup(Registries.ITEM);
	}

	protected Criterion<InventoryChangeTrigger.TriggerInstance> conditionsFromItem(ItemLike item) {
		return has(item);
	}

	protected Criterion<InventoryChangeTrigger.TriggerInstance> conditionsFromTag(TagKey<Item> tag) {
		return has(tag);
	}

	// [VanillaCopy] RecipeProvider#nineBlockStorageRecipes, with the output as a parameter so the packed/unpacked pair
	// can carry load conditions (the eight-argument form is private there now)
	protected void nineBlockStorageRecipes(RecipeOutput recipeOutput, RecipeCategory unpackedFormCategory, ItemLike unpackedForm,
			RecipeCategory packedFormCategory, ItemLike packedForm) {
		nineBlockStorageRecipes(recipeOutput, unpackedFormCategory, unpackedForm, packedFormCategory, packedForm,
				getSimpleRecipeName(packedForm), null, getSimpleRecipeName(unpackedForm), null);
	}

	protected void nineBlockStorageRecipes(RecipeOutput recipeOutput, RecipeCategory unpackedFormCategory, ItemLike unpackedForm,
			RecipeCategory packedFormCategory, ItemLike packedForm, String packingRecipeId, @Nullable String packingRecipeGroup,
			String unpackingRecipeId, @Nullable String unpackingRecipeGroup) {
		shapeless(unpackedFormCategory, unpackedForm, 9)
				.requires(packedForm)
				.group(unpackingRecipeGroup)
				.unlockedBy(getHasName(packedForm), has(packedForm))
				// saved by key like vanilla: the String overload refuses an id equal to the default one
				.save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.parse(unpackingRecipeId)));
		shaped(packedFormCategory, packedForm)
				.define('#', unpackedForm)
				.pattern("###")
				.pattern("###")
				.pattern("###")
				.group(packingRecipeGroup)
				.unlockedBy(getHasName(unpackedForm), has(unpackedForm))
				.save(recipeOutput, ResourceKey.create(Registries.RECIPE, Identifier.parse(packingRecipeId)));
	}

	// PORT-26.3: vanilla's save(output, String) refuses an id equal to the builder's default one; Botania names its
	// recipes deliberately, so the explicit ids go through the key overload like vanilla's own helpers do
	protected static ResourceKey<Recipe<?>> recipeKey(String id) {
		return ResourceKey.create(Registries.RECIPE, Identifier.parse(id));
	}

	@Override
	public void buildRecipes() { // public: NeoForge widens it
		buildRecipes(this.output);
	}

	public abstract void buildRecipes(RecipeOutput recipeOutput);
}
