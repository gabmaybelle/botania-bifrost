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

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.LensItem;

public class LensDyeingRecipe extends CustomRecipe {
	// PORT-26.3: one shared instance for both codecs, as vanilla's stateless custom recipes do: StreamCodec.unit refuses to
	// encode any other instance, and the recipes are synced to the client now
	public static final LensDyeingRecipe INSTANCE = new LensDyeingRecipe();
	public static final RecipeSerializer<LensDyeingRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	public LensDyeingRecipe() {}

	@Override
	public RecipeSerializer<LensDyeingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundLens = false;
		boolean foundDye = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof BasicLensItem && !foundLens) {
					foundLens = true;
				} else if (!foundDye) {
					// TODO: rainbow tinting items should be a tag
					// PORT-26.3: dyes are one item with a DYE component since 26.1
					if (stack.has(DataComponents.DYE) || stack.is(BotaniaItems.MANA_PEARL)) {
						foundDye = true;
					} else {
						return false;
					}
				} else {
					return false;//This means we have an additional item in the recipe after the lens and dye
				}
			}
		}

		return foundLens && foundDye;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack lens = ItemStack.EMPTY;
		DyeColor color = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof BasicLensItem && lens.isEmpty()) {
					lens = stack;
				} else if (stack.has(DataComponents.DYE)) {
					// we can assume that otherwise it's rainbow color, as we matched the ingredients already
					color = stack.get(DataComponents.DYE);
				}
			}
		}

		if (lens.getItem() instanceof BasicLensItem) {
			ItemStack lensCopy = lens.copyWithCount(1);
			if (color != null) {
				LensItem.setLensColor(lensCopy, color);
			} else {
				LensItem.setLensRainbow(lensCopy);
			}

			return lensCopy;
		}

		return ItemStack.EMPTY;
	}

}
