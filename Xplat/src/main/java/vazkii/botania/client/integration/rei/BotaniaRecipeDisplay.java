/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BotaniaRecipeDisplay<T extends Recipe<? extends RecipeInput>> implements Display {
	protected final RecipeHolder<? extends T> recipe;
	protected List<EntryIngredient> inputs;
	protected EntryIngredient outputs;

	// PORT-26.3: vanilla's Recipe no longer lists ingredients or a result, so each display passes Botania's own
	public BotaniaRecipeDisplay(RecipeHolder<? extends T> recipe, List<Ingredient> ingredients, ItemStack output) {
		this.recipe = recipe;
		this.inputs = EntryIngredients.ofIngredients(ingredients);
		this.outputs = EntryIngredients.of(output);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return this.inputs;
	}

	abstract public int getManaCost();

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(this.outputs);
	}

	@Override
	public Optional<Identifier> getDisplayLocation() {
		return Optional.ofNullable(this.recipe).map(holder -> holder.id().identifier());
	}

	/**
	 * PORT-26.3: REI 17+ wants displays built on the server and sent over, through a serializer. Botania builds its
	 * displays on the client from its own synced recipes instead, which REI allows with no serializer; the cost is
	 * that REI doesn't keep Botania pages in its favourites or history across restarts.
	 */
	@Override
	public DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}

}
