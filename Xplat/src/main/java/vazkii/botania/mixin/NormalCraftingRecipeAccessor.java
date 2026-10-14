/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * PORT-26.3: vanilla's shaped and shapeless recipes keep their group, category and notification flag in two
 * records on {@code NormalCraftingRecipe}; Botania's wrapping recipes copy them into their own constructors.
 */
@Mixin(NormalCraftingRecipe.class)
public interface NormalCraftingRecipeAccessor {
	@Accessor("commonInfo")
	Recipe.CommonInfo botania_getCommonInfo();

	@Accessor("bookInfo")
	CraftingRecipe.CraftingBookInfo botania_getBookInfo();
}
