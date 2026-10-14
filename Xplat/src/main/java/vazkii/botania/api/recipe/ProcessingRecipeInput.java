/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.api.recipe;

import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

/**
 * A recipe input type for "processing" type multi-item recipes, which are effectively fancy shapeless crafting recipes
 * that are used by something other than a crafting table.
 */
public interface ProcessingRecipeInput extends RecipeInput {
	/** PORT-26.3: vanilla split item counting into {@code StackedItemContents}. */
	StackedItemContents getStackedContents();
	List<ItemStack> getItems();
	ProcessingRecipeInput getSubset(int startSlot, int endSlot);
}
