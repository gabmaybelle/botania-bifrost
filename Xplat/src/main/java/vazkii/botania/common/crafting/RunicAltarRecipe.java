/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.common.base.Preconditions;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.ProcessingRecipeInput;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.recipe.RecipeUtils;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RunicAltarRecipe implements vazkii.botania.api.recipe.RunicAltarRecipe {
	public static final RecipeSerializer<RunicAltarRecipe> SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC);
	// PORT-26.3: a template, not a stack: recipes are a registry now and load before item components are bound
	private final ItemStackTemplate output;
	private final Ingredient reagent;
	private final NonNullList<Ingredient> ingredients;
	private final NonNullList<Ingredient> catalysts;
	private final int mana;

	public RunicAltarRecipe(ItemStackTemplate output, Ingredient reagent, int mana, Ingredient[] ingredients, Ingredient[] catalysts) {
		int numIngredients = ingredients.length;
		int numCatalysts = catalysts.length;
		Preconditions.checkArgument(numIngredients + numCatalysts > 0, "Must have at least one ingredient or catalyst");
		Preconditions.checkArgument(numIngredients + numCatalysts <= 16, "Cannot have more than 16 ingredients and/or catalysts");
		validateIngredients(ingredients, catalysts, reagent);
		this.output = output;
		this.reagent = reagent;
		this.ingredients = RecipeUtils.nonNullList(ingredients);
		this.catalysts = RecipeUtils.nonNullList(catalysts);
		this.mana = mana;
	}

	private void validateIngredients(Ingredient[] ingredients, Ingredient[] catalysts, Ingredient reagent) {
		// let's just entirely avoid this big headache
		var customTestIngredients =
				Stream.concat(Stream.concat(Stream.of(ingredients), Stream.of(catalysts)), Stream.of(reagent))
						.filter(XplatAbstractions.instance()::requiresCustomTesting).toList();
		if (!customTestIngredients.isEmpty()) {
			throw new IllegalArgumentException("Ingredients with custom test implementations are not supported: " + customTestIngredients);
		}

		// PORT-26.3: tag ingredients can't be expanded before the tags are bound (datagen, early world load); the
		// overlap check then waits for the next datapack reload, which is what the old cache reset hack achieved
		if (Stream.concat(Stream.concat(Stream.of(ingredients), Stream.of(catalysts)), Stream.of(reagent))
				.anyMatch(ingredient -> !RecipeUtils.canExpand(ingredient))) {
			return;
		}

		var ingredientItems = new ReferenceOpenHashSet<Item>(ingredients.length);
		var catalystItems = new ReferenceOpenHashSet<Item>(catalysts.length);
		var reagentItems = new ReferenceOpenHashSet<Item>(1);
		Stream.of(ingredients).flatMap(ingredient -> Stream.of(RecipeUtils.stacksOf(ingredient)))
				.map(ItemStack::getItem).forEach(ingredientItems::add);
		Stream.of(catalysts).flatMap(catalyst -> Stream.of(RecipeUtils.stacksOf(catalyst)))
				.map(ItemStack::getItem).forEach(catalystItems::add);
		Stream.of(RecipeUtils.stacksOf(reagent)).map(ItemStack::getItem).forEach(reagentItems::add);

		var ingredientsOverlap = ingredientItems.clone();
		ingredientsOverlap.retainAll(catalystItems);
		if (!ingredientsOverlap.isEmpty()) {
			throw new IllegalArgumentException("The following item types can match both as ingredient and as catalyst: " + ingredientsOverlap);
		}

		ingredientItems.retainAll(reagentItems);
		if (!ingredientItems.isEmpty()) {
			throw new IllegalArgumentException("The following item types can match both as ingredient and as reagent: " + ingredientItems);
		}

		catalystItems.retainAll(reagentItems);
		if (!catalystItems.isEmpty()) {
			throw new IllegalArgumentException("The following item types can match both as catalyst and as reagent: " + catalystItems);
		}

		// fallback check: test ingredient, catalyst and reagent definitions against each other directly
		List<Ingredient> ingredientList = List.of(ingredients);
		List<Ingredient> catalystsList = List.of(catalysts);
		if (ingredientList.contains(reagent) || catalystsList.contains(reagent)) {
			throw new IllegalArgumentException("Reagent definition must not occur in ingredients or catalysts: " + reagent);
		}
		if (Stream.of(catalysts).anyMatch(ingredientList::contains)) {
			throw new IllegalArgumentException("Ingredients and catalysts must not contain identical entries: %s/%s"
					.formatted(ingredientList, catalystsList));
		}

	}

	private static RunicAltarRecipe of(List<Ingredient> ingredients, List<Ingredient> catalysts, Ingredient reagent, int mana, ItemStackTemplate output) {
		return new RunicAltarRecipe(output, reagent, mana, ingredients.toArray(Ingredient[]::new), catalysts.toArray(Ingredient[]::new));
	}

	@Override
	public boolean matches(ProcessingRecipeInput input, Level world) {
		if (input.size() != ingredients.size() + catalysts.size()) {
			return false;
		}
		return input.getStackedContents().canCraft(RecipeUtils.wrapAllIngredients(this), null);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(ProcessingRecipeInput input) {
		// PORT-26.3: the matcher reports item holders now, not item ids, and matches a list of ingredients
		List<Holder<Item>> catalystItems = new ArrayList<>(catalysts.size());
		if (!catalysts.isEmpty()) {
			// we know ingredients and catalysts are disjoined sets of item types, so just try matching all catalysts
			input.getStackedContents().canCraft(catalysts, catalystItems::add);
		}
		NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack inputStack = input.getItem(slot);
			if (catalystItems.contains(inputStack.typeHolder())) {
				remainingItems.set(slot, inputStack);
			} else {
				ItemStack craftRemainder = XplatAbstractions.instance().getCraftingRemainingItem(inputStack);
				if (!craftRemainder.isEmpty()) {
					remainingItems.set(slot, craftRemainder);
				}
			}
		}

		return remainingItems;
	}

	public final ItemStack getResultItem(HolderLookup.Provider registries) {
		return output.create();
	}

	@Override
	public ItemStack assemble(ProcessingRecipeInput inv) {
		return getResultItem(null).copy();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public NonNullList<Ingredient> getCatalysts() {
		return catalysts;
	}

	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.RUNIC_ALTAR);
	}

	@Override
	public RecipeSerializer<? extends RunicAltarRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public int getMana() {
		return mana;
	}

	public ItemStack getOutput() {
		return output.create();
	}

	public ItemStackTemplate getOutputTemplate() {
		return output;
	}

	@Override
	public Ingredient getReagent() {
		return reagent;
	}

	public static class Serializer {
		private static final MapCodec<RunicAltarRecipe> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(RunicAltarRecipe::getIngredients),
				Ingredient.CODEC.listOf().fieldOf("catalysts").forGetter(RunicAltarRecipe::getCatalysts),
				Ingredient.CODEC.fieldOf("reagent").forGetter(RunicAltarRecipe::getReagent),
				ExtraCodecs.POSITIVE_INT.fieldOf("mana").forGetter(RunicAltarRecipe::getMana),
				ItemStackTemplate.CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
		).apply(instance, RunicAltarRecipe::of));
		public static final MapCodec<RunicAltarRecipe> CODEC = RAW_CODEC.validate(recipe -> {
			if (recipe.getIngredients().size() + recipe.getCatalysts().size() == 0) {
				return DataResult.error(() -> "Must have at least one ingredient or catalyst");
			}
			if (recipe.getIngredients().size() + recipe.getCatalysts().size() > 16) {
				return DataResult.error(() -> "Cannot have more than 16 ingredients and catalysts in total");
			}
			return DataResult.success(recipe);
		});
		public static final StreamCodec<RegistryFriendlyByteBuf, RunicAltarRecipe> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), RunicAltarRecipe::getIngredients,
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), RunicAltarRecipe::getCatalysts,
				Ingredient.CONTENTS_STREAM_CODEC, RunicAltarRecipe::getReagent,
				ByteBufCodecs.VAR_INT, RunicAltarRecipe::getMana,
				ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
				RunicAltarRecipe::of
		);

	}
}
