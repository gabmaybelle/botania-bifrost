/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serializers for Botania's recipes that are a vanilla shaped or shapeless recipe with extra behaviour: vanilla
 * reads and writes the recipe, and the wrap function makes Botania's subclass out of it.
 * <p>
 * PORT-26.3: {@code RecipeSerializer} is a final record of two codecs now, so this is no longer an interface
 * the serializers implement; it builds them.
 */
public final class WrappingRecipeSerializer {
	private static final Map<RecipeSerializer<?>, Function<Recipe<?>, Recipe<?>>> WRAPPERS = new IdentityHashMap<>();

	private WrappingRecipeSerializer() {}

	/** Turns a plain recipe into the subclass the given serializer makes; used by datagen to save wrapped recipes. */
	@SuppressWarnings("unchecked")
	public static <T extends Recipe<?>> T wrap(RecipeSerializer<T> serializer, Recipe<?> recipe) {
		Function<Recipe<?>, Recipe<?>> wrapper = WRAPPERS.get(serializer);
		if (wrapper == null) {
			throw new IllegalArgumentException("Not a wrapping serializer: " + serializer);
		}
		return (T) wrapper.apply(recipe);
	}

	// The serializer's type is vanilla's, because ShapedRecipe#getSerializer fixes it; the codecs still make the subclass.
	public static RecipeSerializer<ShapedRecipe> shaped(Function<ShapedRecipe, ? extends ShapedRecipe> wrap) {
		var serializer = new RecipeSerializer<>(ShapedRecipe.MAP_CODEC.xmap(wrap::apply, recipe -> recipe),
				ShapedRecipe.STREAM_CODEC.map(wrap::apply, recipe -> recipe));
		WRAPPERS.put(serializer, recipe -> wrap.apply((ShapedRecipe) recipe));
		return serializer;
	}

	public static RecipeSerializer<ShapelessRecipe> shapeless(Function<ShapelessRecipe, ? extends ShapelessRecipe> wrap) {
		var serializer = new RecipeSerializer<>(ShapelessRecipe.MAP_CODEC.xmap(wrap::apply, recipe -> recipe),
				ShapelessRecipe.STREAM_CODEC.map(wrap::apply, recipe -> recipe));
		WRAPPERS.put(serializer, recipe -> wrap.apply((ShapelessRecipe) recipe));
		return serializer;
	}
}
