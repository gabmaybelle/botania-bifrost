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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterRemoveLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterRemoveLensRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(ManaBlasterRemoveLensRecipe::new), StreamCodec.unit(new ManaBlasterRemoveLensRecipe()));

	public ManaBlasterRemoveLensRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundGun = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem
						&& !ManaBlasterItem.getLens(stack).isEmpty() && !foundGun) {
					foundGun = true;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundGun;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack gun = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem) {
					gun = stack;
				}
			}
		}

		ItemStack gunCopy = gun.copyWithCount(1);
		ManaBlasterItem.setLens(gunCopy, ItemStack.EMPTY);

		return gunCopy;
	}

	@Override
	public RecipeSerializer<ManaBlasterRemoveLensRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		return RecipeUtils.getRemainingItemsSub(inv, s -> {
			if (s.is(BotaniaItems.MANA_BLASTER)) {
				ItemStack stack = ManaBlasterItem.getLens(s);
				stack.setCount(1);
				return stack;
			}
			return null;
		});
	}
}
