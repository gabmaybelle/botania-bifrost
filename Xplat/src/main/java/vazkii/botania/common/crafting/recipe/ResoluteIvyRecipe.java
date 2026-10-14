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

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;

public class ResoluteIvyRecipe extends CustomRecipe {
	public static final RecipeSerializer<ResoluteIvyRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(ResoluteIvyRecipe::new), StreamCodec.unit(new ResoluteIvyRecipe()));

	public ResoluteIvyRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundIvy = false;
		boolean foundItem = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.is(BotaniaItems.RESOLUTE_IVY) && !foundIvy) {
					foundIvy = true;
				} else if (!foundItem
						&& !stack.has(BotaniaDataComponents.RESOLUTE_IVY)
						&& stack.getItem().getCraftingRemainder() == null) {
					foundItem = true;
				} else {
					return false;
				}
			}
		}

		return foundIvy && foundItem;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack item = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty() && !stack.is(BotaniaItems.RESOLUTE_IVY)) {
				item = stack;
			}
		}

		ItemStack copy = item.copyWithCount(1);
		copy.set(BotaniaDataComponents.RESOLUTE_IVY, Unit.INSTANCE);
		return copy;
	}

	@Override
	public RecipeSerializer<ResoluteIvyRecipe> getSerializer() {
		return SERIALIZER;
	}
}
