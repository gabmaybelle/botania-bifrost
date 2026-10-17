/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.ProcessingRecipeInput;
import vazkii.botania.common.crafting.recipe.RecipeUtils;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Arrays;
import java.util.List;

public class TerrestrialAgglomerationRecipe implements vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe {
	public static final RecipeSerializer<TerrestrialAgglomerationRecipe> SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC);
	private final int mana;
	private final NonNullList<Ingredient> ingredients;
	// PORT-26.3: a template, not a stack: recipes are a registry now and load before item components are bound
	private final ItemStackTemplate output;
	private final boolean requireSpecialMatching;

	public TerrestrialAgglomerationRecipe(int mana, ItemStackTemplate output, Ingredient... ingredients) {
		this.mana = mana;
		this.ingredients = RecipeUtils.nonNullList(ingredients);
		this.output = output;
		this.requireSpecialMatching = Arrays.stream(ingredients).anyMatch(XplatAbstractions.instance()::requiresCustomTesting);
	}

	private static TerrestrialAgglomerationRecipe of(List<Ingredient> ingredients, int mana, ItemStackTemplate output) {
		return new TerrestrialAgglomerationRecipe(mana, output, ingredients.toArray(Ingredient[]::new));
	}

	@Override
	public int getMana() {
		return mana;
	}

	public ItemStack getOutput() {
		return output.create();
	}

	@Override
	public boolean matches(ProcessingRecipeInput input, Level world) {
		return RecipeUtils.matches(getIngredients(), input, requireSpecialMatching);
	}

	@Override
	public ItemStack assemble(ProcessingRecipeInput inv) {
		return output.create();
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return output.create();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public RecipeSerializer<? extends TerrestrialAgglomerationRecipe> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer {
		public static final MapCodec<TerrestrialAgglomerationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ExtraCodecs.nonEmptyList(Ingredient.CODEC.listOf()).fieldOf("ingredients")
						.forGetter(TerrestrialAgglomerationRecipe::getIngredients),
				ExtraCodecs.POSITIVE_INT.fieldOf("mana").forGetter(TerrestrialAgglomerationRecipe::getMana),
				ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
		).apply(instance, TerrestrialAgglomerationRecipe::of));
		public static final StreamCodec<RegistryFriendlyByteBuf, TerrestrialAgglomerationRecipe> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), TerrestrialAgglomerationRecipe::getIngredients,
				ByteBufCodecs.VAR_INT, TerrestrialAgglomerationRecipe::getMana,
				ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
				TerrestrialAgglomerationRecipe::of
		);

	}
}
