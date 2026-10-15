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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterClipRecipe extends CustomRecipe {
	// PORT-26.3: one shared instance for both codecs, as vanilla's stateless custom recipes do: StreamCodec.unit refuses to
	// encode any other instance, and the recipes are synced to the client now
	public static final ManaBlasterClipRecipe INSTANCE = new ManaBlasterClipRecipe();
	public static final RecipeSerializer<ManaBlasterClipRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	public ManaBlasterClipRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundGun = false;
		boolean foundClip = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem
						&& !ManaBlasterItem.hasClip(stack) && !foundGun) {
					foundGun = true;
				} else if (stack.is(BotaniaItems.LENS_CLIP) && !foundClip) {
					foundClip = true;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundGun && foundClip;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack gun = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ManaBlasterItem) {
				gun = stack;
			}
		}

		if (gun.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack gunCopy = gun.copy();
		ManaBlasterItem.addClip(gunCopy, true);
		return gunCopy;
	}

	@Override
	public RecipeSerializer<ManaBlasterClipRecipe> getSerializer() {
		return SERIALIZER;
	}
}
