/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.fabric.data.xplat;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.data.recipes.BotaniaRecipeProvider;

/**
 * A recipe generator that can put Fabric load conditions on some of its recipes. The conditions are applied by the
 * Fabric data provider that writes the files, so it hands the hook in.
 */
public abstract class ConditionalRecipeProvider extends BotaniaRecipeProvider {
	public static final ResourceCondition GOG_NOT_LOADED = ResourceConditions.not(ResourceConditions.allModsLoaded(BotaniaAPI.GOG_MODID));

	@FunctionalInterface
	public interface ConditionHook {
		RecipeOutput withConditions(RecipeOutput output, ResourceCondition... conditions);
	}

	private final ConditionHook conditions;

	protected ConditionalRecipeProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements,
			ConditionHook conditions) {
		super(recipes, advancements);
		this.conditions = conditions;
	}

	protected RecipeOutput withConditions(RecipeOutput output, ResourceCondition... conditions) {
		return this.conditions.withConditions(output, conditions);
	}
}
