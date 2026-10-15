/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.BotaniaRecipeIngredientsCache;
import vazkii.botania.common.handler.OrechidManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PORT-26.3: the client no longer has a recipe manager (vanilla only syncs the recipe book and the stonecutter
 * list), but Botania reads recipes on the client: the mana pool and apothecary HUDs, the runic altar, the assembly
 * halo, the rod of the molten core and the lexicon pages. Each loader sends the client the recipe types in
 * {@link BotaniaRecipeTypes#SYNCED_TYPES} on join and after a reload (NeoForge through its datapack sync event,
 * Fabric through a Botania packet) and stores them here; {@link BotaniaRecipeTypes} reads this copy for client
 * levels. Only the client thread touches it.
 */
public final class SyncedRecipes {
	private static final Map<RecipeType<?>, List<RecipeHolder<?>>> BY_TYPE = new HashMap<>();
	private static final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> BY_KEY = new HashMap<>();

	private SyncedRecipes() {}

	/**
	 * Replaces the copy of one recipe type, or extends it when {@code append} is set (a type can arrive in
	 * several packets).
	 */
	public static void update(RecipeType<?> type, Collection<? extends RecipeHolder<?>> recipes, boolean append) {
		List<RecipeHolder<?>> list = BY_TYPE.computeIfAbsent(type, t -> new ArrayList<>());
		if (!append) {
			list.forEach(holder -> BY_KEY.remove(holder.id()));
			list.clear();
		}
		for (RecipeHolder<?> holder : recipes) {
			list.add(holder);
			BY_KEY.put(holder.id(), holder);
		}
		BotaniaRecipeIngredientsCache.clearClientCache();
		OrechidManager.clearClientCache();
	}

	/** Replaces the copies of the given types with the matching recipes among {@code recipes}. */
	public static void replace(Collection<RecipeType<?>> types, Collection<? extends RecipeHolder<?>> recipes) {
		for (RecipeType<?> type : types) {
			update(type, List.of(), false);
		}
		for (RecipeHolder<?> holder : recipes) {
			if (types.contains(holder.value().getType())) {
				update(holder.value().getType(), List.of(holder), true);
			}
		}
	}

	public static List<RecipeHolder<?>> byType(RecipeType<?> type) {
		return BY_TYPE.getOrDefault(type, List.of());
	}

	@Nullable
	public static RecipeHolder<?> byKey(ResourceKey<Recipe<?>> key) {
		return BY_KEY.get(key);
	}
}
