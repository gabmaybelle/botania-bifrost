/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;

import java.util.Optional;

public class TiaraWingsRecipe extends CustomRecipe {
	public static final RecipeSerializer<TiaraWingsRecipe> SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC);

	// PORT-26.3: ingredients can't be empty any more, so the material-less variant has no material at all
	private final Optional<Ingredient> material;
	private final int variant;

	public TiaraWingsRecipe(Optional<Ingredient> material, int variant) {
		// PORT-26.3: custom recipes have no category of their own any more (CustomRecipe answers "misc").
		if (material.isPresent() && material.get().test(BotaniaItems.FLUGEL_TIARA.getDefaultInstance())) {
			throw new IllegalArgumentException("Material cannot be a Flügel Tiara");
		}
		this.material = material;
		this.variant = variant;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.ingredientCount() != (material.isEmpty() ? 1 : 2)) {
			return false;
		}

		boolean foundTiara = false;
		boolean foundMaterial = material.isEmpty();

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}
			if (!foundTiara && matchesTiara(stack)) {
				foundTiara = true;
			} else if (!foundMaterial && material.get().test(stack)) {
				foundMaterial = true;
			} else {
				return false;
			}
		}

		return foundMaterial && foundTiara;
	}

	private boolean matchesTiara(ItemStack stack) {
		return stack.is(BotaniaItems.FLUGEL_TIARA) && FlugelTiaraItem.getVariant(stack) != variant;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack tiara = input.items().stream().filter(stack -> stack.is(BotaniaItems.FLUGEL_TIARA))
				.findFirst().orElseThrow().copy();
		FlugelTiaraItem.setVariant(tiara, variant);
		return tiara;
	}

	public NonNullList<Ingredient> getIngredients() {
		return material.isEmpty()
				? RecipeUtils.nonNullList(Ingredient.of(BotaniaItems.FLUGEL_TIARA))
				: RecipeUtils.nonNullList(Ingredient.of(BotaniaItems.FLUGEL_TIARA), material.get());
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		ItemStack tiara = new ItemStack(BotaniaItems.FLUGEL_TIARA);
		FlugelTiaraItem.setVariant(tiara, variant);
		return tiara;
	}

	@Override
	public String group() {
		return "botania:flugel_tiara_wings";
	}

	public Optional<Ingredient> material() {
		return material;
	}

	public int variant() {
		return variant;
	}

	@Override
	public RecipeSerializer<TiaraWingsRecipe> getSerializer() {
		return SERIALIZER;
	}

	private static class Serializer {
		static final MapCodec<TiaraWingsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Ingredient.CODEC.optionalFieldOf("material").forGetter(TiaraWingsRecipe::material),
				ExtraCodecs.intRange(0, FlugelTiaraItem.WING_TYPES).fieldOf("variant").forGetter(TiaraWingsRecipe::variant)
		).apply(instance, TiaraWingsRecipe::new));
		static final StreamCodec<RegistryFriendlyByteBuf, TiaraWingsRecipe> STREAM_CODEC = StreamCodec.composite(
				Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, TiaraWingsRecipe::material,
				ByteBufCodecs.VAR_INT, TiaraWingsRecipe::variant,
				TiaraWingsRecipe::new
		);

	}
}
