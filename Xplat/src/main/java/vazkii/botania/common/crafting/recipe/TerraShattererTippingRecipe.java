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
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;

public class TerraShattererTippingRecipe extends CustomRecipe {
	// PORT-26.3: one shared instance for both codecs, as vanilla's stateless custom recipes do: StreamCodec.unit refuses to
	// encode any other instance, and the recipes are synced to the client now
	public static final TerraShattererTippingRecipe INSTANCE = new TerraShattererTippingRecipe();
	public static final RecipeSerializer<TerraShattererTippingRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	public TerraShattererTippingRecipe() {}

	@Override
	public RecipeSerializer<TerraShattererTippingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundTerraPick = false;
		boolean foundElementiumPick = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof TerraShattererItem
						&& !TerraShattererItem.isTipped(stack) && !foundTerraPick) {
					foundTerraPick = true;
				} else if (stack.is(BotaniaItems.ELEMENTIUM_PICKAXE) && !foundElementiumPick) {
					foundElementiumPick = true;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundTerraPick && foundElementiumPick;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack terraPick = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof TerraShattererItem) {
				terraPick = stack;
			}
		}

		if (terraPick.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack terraPickCopy = terraPick.copy();
		TerraShattererItem.setTipped(terraPickCopy);
		return terraPickCopy;
	}

}
