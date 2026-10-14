/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.helper.DataComponentHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.LaputaShardItem;

public class LaputaShardUpgradeRecipe extends CustomRecipe {
	public static final RecipeSerializer<LaputaShardUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(LaputaShardUpgradeRecipe::new), StreamCodec.unit(new LaputaShardUpgradeRecipe()));

	public LaputaShardUpgradeRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level worldIn) {
		boolean foundShard = false;
		boolean foundSpirit = false;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(BotaniaItems.SHARD_OF_LAPUTA) && !foundShard
					&& LaputaShardItem.getShardLevel(stack) < LaputaShardItem.MAX_LEVEL) {
				foundShard = true;
			} else if (stack.is(BotaniaItems.GAIA_SPIRIT) && !foundSpirit) {
				foundSpirit = true;
			} else {
				return false;
			}
		}
		return foundShard && foundSpirit;
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return new ItemStack(BotaniaItems.SHARD_OF_LAPUTA);
	}

	public NonNullList<Ingredient> getIngredients() {
		return RecipeUtils.nonNullList(Ingredient.of(BotaniaItems.SHARD_OF_LAPUTA),
				Ingredient.of(BotaniaItems.GAIA_SPIRIT));
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.is(BotaniaItems.SHARD_OF_LAPUTA)) {
				ItemStack result = stack.copy();
				result.set(BotaniaDataComponents.SHARD_LEVEL, LaputaShardItem.getShardLevel(stack) + 1);
				if (LaputaShardItem.getShardLevel(result) == LaputaShardItem.MAX_LEVEL) {
					DataComponentHelper.bumpRarity(result);
				}
				return result;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<LaputaShardUpgradeRecipe> getSerializer() {
		return SERIALIZER;
	}
}
