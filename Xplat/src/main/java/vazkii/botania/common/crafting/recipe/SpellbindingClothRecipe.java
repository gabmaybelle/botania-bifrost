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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import vazkii.botania.common.item.BotaniaItems;

public class SpellbindingClothRecipe extends CustomRecipe {
	// PORT-26.3: one shared instance for both codecs, as vanilla's stateless custom recipes do: StreamCodec.unit refuses to
	// encode any other instance, and the recipes are synced to the client now
	public static final SpellbindingClothRecipe INSTANCE = new SpellbindingClothRecipe();
	public static final RecipeSerializer<SpellbindingClothRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

	public SpellbindingClothRecipe() {}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		boolean foundCloth = false;
		boolean foundEnchanted = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.isEnchanted() && !foundEnchanted) {
					foundEnchanted = true;
				} else if (stack.is(BotaniaItems.SPELLBINDING_CLOTH) && !foundCloth) {
					foundCloth = true;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundCloth && foundEnchanted;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack stackToDisenchant = ItemStack.EMPTY;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty() && stack.isEnchanted() && !stack.is(BotaniaItems.SPELLBINDING_CLOTH)) {
				stackToDisenchant = stack.copyWithCount(1);
				break;
			}
		}

		if (stackToDisenchant.isEmpty()) {
			return ItemStack.EMPTY;
		}

		EnchantmentHelper.updateEnchantments(stackToDisenchant, mutable -> mutable.removeIf(enchantment -> true));
		stackToDisenchant.remove(DataComponents.REPAIR_COST);

		return stackToDisenchant;
	}

	@Override
	public RecipeSerializer<SpellbindingClothRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		return RecipeUtils.getRemainingItemsSub(inv, s -> {
			if (s.is(BotaniaItems.SPELLBINDING_CLOTH)) {
				ItemStack copy = s.copyWithCount(1);
				copy.setDamageValue(copy.getDamageValue() + 1);
				return copy;
			}
			return null;
		});
	}
}
