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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import vazkii.botania.common.block.mana.ManaSpreaderBlock;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.mixin.NormalCraftingRecipeAccessor;
import vazkii.botania.mixin.ShapelessRecipeAccessor;

public class ShapelessUncoverSpreaderRecipe extends ShapelessRecipe {
	public static final RecipeSerializer<ShapelessRecipe> SERIALIZER = WrappingRecipeSerializer.shapeless(ShapelessUncoverSpreaderRecipe::new);

	private ShapelessUncoverSpreaderRecipe(ShapelessRecipe recipe) {
		super(((NormalCraftingRecipeAccessor) recipe).botania_getCommonInfo(), ((NormalCraftingRecipeAccessor) recipe).botania_getBookInfo(),
				((ShapelessRecipeAccessor) recipe).botania_getResult(), ((ShapelessRecipeAccessor) recipe).botania_getIngredients());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); i++) {
			Item item = input.getItem(i).getItem();
			if (item instanceof BlockItem blockItem
					&& blockItem.getBlock() instanceof ManaSpreaderBlock manaSpreaderBlock
					&& manaSpreaderBlock.getOptionalColor().isPresent()) {
				nonnulllist.set(i, new ItemStack(ColorHelper.WOOL_MAP.apply(manaSpreaderBlock.getOptionalColor().get())));
			}
		}

		return nonnulllist;
	}

	@Override
	public RecipeSerializer<ShapelessRecipe> getSerializer() {
		return SERIALIZER;
	}
}
