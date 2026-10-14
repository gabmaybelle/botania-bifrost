/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import vazkii.botania.mixin.NormalCraftingRecipeAccessor;
import vazkii.botania.mixin.ShapedRecipeAccessor;

public class ArmorUpgradeRecipe extends ShapedRecipe {
	public static final RecipeSerializer<ShapedRecipe> SERIALIZER = WrappingRecipeSerializer.shaped(ArmorUpgradeRecipe::new);

	private ArmorUpgradeRecipe(ShapedRecipe recipe) {
		super(((NormalCraftingRecipeAccessor) recipe).botania_getCommonInfo(), ((NormalCraftingRecipeAccessor) recipe).botania_getBookInfo(),
				((ShapedRecipeAccessor) recipe).botania_getPattern(), ((ShapedRecipeAccessor) recipe).botania_getResult());
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack out = super.assemble(inv);
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.has(DataComponents.EQUIPPABLE)) { // PORT-26.3: ArmorItem is gone; armor is anything equippable
				// TODO: verify this works as intended
				out.applyComponents(stack.getComponentsPatch());
				break;
			}
		}
		return out;
	}

	@Override
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return SERIALIZER;
	}
}
