/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import vazkii.botania.mixin.NormalCraftingRecipeAccessor;
import vazkii.botania.mixin.ShapelessRecipeAccessor;

public class ShapelessManaUpgradeRecipe extends ShapelessRecipe {
	public static final RecipeSerializer<ShapelessRecipe> SERIALIZER = WrappingRecipeSerializer.shapeless(ShapelessManaUpgradeRecipe::new);

	private ShapelessManaUpgradeRecipe(ShapelessRecipe recipe) {
		super(((NormalCraftingRecipeAccessor) recipe).botania_getCommonInfo(), ((NormalCraftingRecipeAccessor) recipe).botania_getBookInfo(),
				((ShapelessRecipeAccessor) recipe).botania_getResult(), ((ShapelessRecipeAccessor) recipe).botania_getIngredients());
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		return ManaUpgradeRecipe.output(super.assemble(inv), inv);
	}

	@Override
	public RecipeSerializer<ShapelessRecipe> getSerializer() {
		return SERIALIZER;
	}
}
