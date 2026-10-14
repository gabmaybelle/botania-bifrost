/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.crafting.recipe.RecipeUtils;

public class ManaInfusionRecipe implements vazkii.botania.api.recipe.ManaInfusionRecipe {
	public static final RecipeSerializer<ManaInfusionRecipe> SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC);
	private final ItemStack output;
	private final Ingredient input;
	private final int mana;
	private final StateIngredient catalyst;
	private final String group;

	public ManaInfusionRecipe(ItemStack output, Ingredient input, int mana, @Nullable String group, @Nullable StateIngredient catalyst) {
		this.output = output;
		this.input = input;
		this.mana = mana;
		this.group = group == null ? "" : group;
		this.catalyst = catalyst == null ? StateIngredients.NONE : catalyst;
	}

	@Override
	public RecipeSerializer<ManaInfusionRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(ItemStack stack) {
		return input.test(stack);
	}

	@Override
	public StateIngredient getRecipeCatalyst() {
		return catalyst;
	}

	@Override
	public int getManaToConsume() {
		return mana;
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return output;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return RecipeUtils.nonNullList(input);
	}

	@Override
	public String group() {
		return group;
	}

	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.MANA_POOL);
	}

	protected Ingredient getInput() {
		return input;
	}

	protected ItemStack getOutput() {
		return output;
	}

	public static class Serializer {
		public static final MapCodec<ManaInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ItemStack.CODEC.fieldOf("output").forGetter(ManaInfusionRecipe::getOutput),
				Ingredient.CODEC.fieldOf("input").forGetter(ManaInfusionRecipe::getInput),
				// Leaving wiggle room for a certain modpack having creative-pool-only recipes
				ExtraCodecs.intRange(1, ManaPoolBlock.MAX_MANA + 1).fieldOf("mana")
						.forGetter(ManaInfusionRecipe::getManaToConsume),
				Codec.STRING.optionalFieldOf("group", "").forGetter(ManaInfusionRecipe::group),
				StateIngredients.TYPED_CODEC.optionalFieldOf("catalyst", StateIngredients.NONE)
						.forGetter(ManaInfusionRecipe::getRecipeCatalyst)
		).apply(instance, ManaInfusionRecipe::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, ManaInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
				ItemStack.STREAM_CODEC, ManaInfusionRecipe::getOutput,
				Ingredient.CONTENTS_STREAM_CODEC, ManaInfusionRecipe::getInput,
				ByteBufCodecs.VAR_INT, ManaInfusionRecipe::getManaToConsume,
				ByteBufCodecs.STRING_UTF8, ManaInfusionRecipe::group,
				StateIngredients.TYPED_STREAM_CODEC, ManaInfusionRecipe::getRecipeCatalyst,
				ManaInfusionRecipe::new
		);

	}
}
