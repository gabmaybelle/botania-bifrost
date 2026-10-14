/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.common.crafting.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.mixin.NormalCraftingRecipeAccessor;
import vazkii.botania.mixin.ShapelessRecipeAccessor;

public class LexiconReturningShapelessRecipe extends ShapelessRecipe {
	public static final RecipeSerializer<ShapelessRecipe> SERIALIZER = WrappingRecipeSerializer.shapeless(LexiconReturningShapelessRecipe::new);

	private LexiconReturningShapelessRecipe(ShapelessRecipe recipe) {
		super(((NormalCraftingRecipeAccessor) recipe).botania_getCommonInfo(), ((NormalCraftingRecipeAccessor) recipe).botania_getBookInfo(),
				((ShapelessRecipeAccessor) recipe).botania_getResult(), ((ShapelessRecipeAccessor) recipe).botania_getIngredients());
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		return ManaUpgradeRecipe.output(super.assemble(inv), inv);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); i++) {
			ItemStack itemStack = input.getItem(i);
			Item item = itemStack.getItem();
			var remainder = item.getCraftingRemainder();
			if (remainder != null) {
				nonnulllist.set(i, remainder.create());
			} else if (itemStack.is(BotaniaItems.LEXICA_BOTANIA)) {
				nonnulllist.set(i, itemStack.copy());
			}
		}

		return nonnulllist;
	}

	@Override
	public RecipeSerializer<ShapelessRecipe> getSerializer() {
		return SERIALIZER;
	}
}
