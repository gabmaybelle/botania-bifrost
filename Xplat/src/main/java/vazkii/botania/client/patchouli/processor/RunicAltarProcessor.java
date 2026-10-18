/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.patchouli.processor;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.client.patchouli.PatchouliUtils;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class RunicAltarProcessor extends ReagentRecipeProcessor<RunicAltarRecipe> {
	@Override
	public void setup(Level level, IVariableProvider variables) {
		Identifier id = Identifier.parse(variables.get("recipe", level.registryAccess()).asString());
		RunicAltarRecipe altarRecipe = PatchouliUtils.getRecipe(level, BotaniaRecipeTypes.RUNIC_ALTAR_TYPE, id);
		if (altarRecipe != null) {
			this.recipe = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), altarRecipe);
		} else {
			BotaniaAPI.LOGGER.warn("Missing Runic Altar recipe {}", id);
		}
	}

	@Override
	protected ItemStack getOutput(RunicAltarRecipe recipe, Level level) {
		return recipe.getResultItem(level.registryAccess());
	}

	@Override
	public IVariable process(Level level, String key) {
		if (recipe == null) {
			return super.process(level, key);
		}
		if (key.equals("mana")) {
			return IVariable.wrap(recipe.value().getMana(), level.registryAccess());
		}
		return super.process(level, key);
	}
}
