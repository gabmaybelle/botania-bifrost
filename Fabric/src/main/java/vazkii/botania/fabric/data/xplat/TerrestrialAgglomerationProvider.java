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

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.ConventionalBotaniaTags;
import vazkii.botania.data.recipes.BotaniaRecipeProvider;
import vazkii.botania.data.util.BotaniaRecipeHelper;

public class TerrestrialAgglomerationProvider extends BotaniaRecipeProvider {
	public TerrestrialAgglomerationProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
		super(recipes, advancements);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {
		consumer.accept(BotaniaRecipeHelper.deriveRecipeId(BotaniaRecipeTypes.TERRA_PLATE_TYPE,
				BotaniaItems.TERRASTEEL_INGOT),
				new TerrestrialAgglomerationRecipe(
						ManaPoolBlock.MAX_MANA / 2,
						new ItemStackTemplate(BotaniaItems.TERRASTEEL_INGOT),
						tag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS),
						tag(ConventionalBotaniaTags.Items.MANA_PEARL_GEMS),
						tag(ConventionalBotaniaTags.Items.MANA_DIAMOND_GEMS)
				),
				null);
	}
}
