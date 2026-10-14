/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.function.Function;

/**
 * Serializers for Botania's recipes that are a vanilla shaped or shapeless recipe with extra behaviour: vanilla
 * reads and writes the recipe, and the wrap function makes Botania's subclass out of it.
 * <p>
 * PORT-26.3: {@code RecipeSerializer} is a final record of two codecs now, so this is no longer an interface
 * the serializers implement; it builds them.
 */
public final class WrappingRecipeSerializer {
	private WrappingRecipeSerializer() {}

	// The serializer's type is vanilla's, because ShapedRecipe#getSerializer fixes it; the codecs still make the subclass.
	public static RecipeSerializer<ShapedRecipe> shaped(Function<ShapedRecipe, ? extends ShapedRecipe> wrap) {
		return new RecipeSerializer<>(ShapedRecipe.MAP_CODEC.xmap(wrap::apply, recipe -> recipe),
				ShapedRecipe.STREAM_CODEC.map(wrap::apply, recipe -> recipe));
	}

	public static RecipeSerializer<ShapelessRecipe> shapeless(Function<ShapelessRecipe, ? extends ShapelessRecipe> wrap) {
		return new RecipeSerializer<>(ShapelessRecipe.MAP_CODEC.xmap(wrap::apply, recipe -> recipe),
				ShapelessRecipe.STREAM_CODEC.map(wrap::apply, recipe -> recipe));
	}
}
