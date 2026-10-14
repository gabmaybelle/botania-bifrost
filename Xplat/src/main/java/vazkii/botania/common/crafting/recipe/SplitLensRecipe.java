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

import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;

public class SplitLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<SplitLensRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(SplitLensRecipe::new), StreamCodec.unit(new SplitLensRecipe()));

	public SplitLensRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		return !assemble(inv).isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack found = ItemStack.EMPTY;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack candidate = inv.getItem(i);
			if (candidate.isEmpty()) {
				continue;
			}
			if (!found.isEmpty() || (found = getComposite(candidate)).isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		if (!found.isEmpty()) {
			found = found.copyWithCount(1);
		}
		return found;
	}

	private ItemStack getComposite(ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof BasicLensItem basicLens)) {
			return ItemStack.EMPTY;
		}
		return basicLens.getCompositeLens(stack);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		for (int i = 0; i < inv.size(); i++) {
			ItemStack candidate = inv.getItem(i);
			if (candidate.getItem() instanceof BasicLensItem basicLensItem) {
				ItemStack newLens = candidate.copyWithCount(1);
				basicLensItem.setCompositeLens(newLens, ItemStack.EMPTY);
				remaining.set(i, newLens);
			}
		}
		return remaining;
	}

	@Override
	public RecipeSerializer<SplitLensRecipe> getSerializer() {
		return SERIALIZER;
	}
}
