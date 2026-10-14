/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

import vazkii.botania.data.recipes.BotaniaRecipeProvider;
import vazkii.botania.fabric.data.xplat.ConditionalRecipeProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Runs one of Botania's recipe generators as a Fabric data provider: Fabric builds the recipe and advancement
 * registries, hands them to the generator, and writes the JSON (with any load conditions) afterwards.
 */
public class BotaniaFabricRecipeProvider extends FabricRecipeProvider {
	@FunctionalInterface
	public interface Factory {
		BotaniaRecipeProvider create(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements,
				ConditionalRecipeProvider.ConditionHook conditions);
	}

	private final String name;
	private final Factory factory;

	public BotaniaFabricRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries,
			String name, Factory factory) {
		super(output, registries);
		this.name = name;
		this.factory = factory;
	}

	public static FabricDataGenerator.Pack.RegistryDependentFactory<BotaniaFabricRecipeProvider> of(String name, Factory factory) {
		return (output, registries) -> new BotaniaFabricRecipeProvider(output, registries, name, factory);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes,
			BootstrapContext<Advancement> advancements) {
		return factory.create(recipes, advancements, this::withConditions);
	}

	@Override
	public String getName() {
		return name;
	}
}
