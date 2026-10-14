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
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

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

	@Override
	public void buildRecipes() { // public: NeoForge widens it
		buildRecipes(this.output);
	}

	public abstract void buildRecipes(RecipeOutput recipeOutput);
}
