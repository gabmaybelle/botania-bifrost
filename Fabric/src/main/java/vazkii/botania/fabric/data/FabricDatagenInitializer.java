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

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageType;

import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.render.ColorHandler;
import vazkii.botania.common.block.BotaniaBannerPatterns;
import vazkii.botania.data.BannerPatternProvider;
import vazkii.botania.data.BannerPatternTagsProvider;
import vazkii.botania.data.BlockTagProvider;
import vazkii.botania.data.BlockstateProvider;
import vazkii.botania.data.DamageTypeTagProvider;
import vazkii.botania.data.DataComponentTypeTagProvider;
import vazkii.botania.data.FloatingFlowerModelProvider;
import vazkii.botania.data.GaiaFightConfigurationProvider;
import vazkii.botania.data.ItemModelProvider;
import vazkii.botania.data.LooniumStructureConfigurationProvider;
import vazkii.botania.data.PottedPlantModelProvider;
import vazkii.botania.data.loot.BotaniaLootTableProvider;
import vazkii.botania.data.util.DatagenItemComponents;
import vazkii.botania.fabric.data.fabric.FabricBlockTagProvider;
import vazkii.botania.fabric.data.fabric.FabricItemTagProvider;
import vazkii.botania.fabric.data.xplat.BrewProvider;
import vazkii.botania.fabric.data.xplat.ConventionalBiomeTagProvider;
import vazkii.botania.fabric.data.xplat.ConventionalBlockTagProvider;
import vazkii.botania.fabric.data.xplat.ConventionalEntityTypeTagProvider;
import vazkii.botania.fabric.data.xplat.ConventionalFluidTagProvider;
import vazkii.botania.fabric.data.xplat.ConventionalItemTagProvider;
import vazkii.botania.fabric.data.xplat.CraftingRecipeProvider;
import vazkii.botania.fabric.data.xplat.ElvenTradeProvider;
import vazkii.botania.fabric.data.xplat.ManaInfusionProvider;
import vazkii.botania.fabric.data.xplat.OrechidProvider;
import vazkii.botania.fabric.data.xplat.PetalApothecaryProvider;
import vazkii.botania.fabric.data.xplat.PureDaisyProvider;
import vazkii.botania.fabric.data.xplat.RunicAltarProvider;
import vazkii.botania.fabric.data.xplat.SmeltingProvider;
import vazkii.botania.fabric.data.xplat.StonecuttingProvider;
import vazkii.botania.fabric.data.xplat.TerrestrialAgglomerationProvider;

import static vazkii.botania.common.BotaniaDamageTypes.*;

public class FabricDatagenInitializer implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		// PORT-26.3: the item model definitions are written through the item model codecs, which look Botania's item
		// properties and tint sources up by id, so they are registered here as well as in the client initializer
		BotaniaItemProperties.registerTests(ConditionalItemModelProperties.ID_MAPPER::put);
		BotaniaItemProperties.registerRanges(RangeSelectItemModelProperties.ID_MAPPER::put);
		ColorHandler.submitItemTintSources(ItemTintSources.ID_MAPPER::put);
		// PORT-26.3: bind the item components (a server job normally), the recipe providers build item stacks
		DatagenItemComponents.bind();
		if (System.getProperty("botania.xplat_datagen") != null) {
			configureXplatDatagen(generator.createPack());
		} else {
			configureFabricDatagen(generator.createPack());
		}
	}

	private static void configureFabricDatagen(FabricDataGenerator.Pack pack) {
		BlockTagProvider blockTagProvider = pack.addProvider(FabricBlockTagProvider::new);
		pack.addProvider((output,
				registriesFuture) -> new FabricItemTagProvider(output, registriesFuture, blockTagProvider.contentsGetter()));
	}

	private static void configureXplatDatagen(FabricDataGenerator.Pack pack) {
		for (LootTableProvider.SubProviderEntry entry : BotaniaLootTableProvider.subProviders()) {
			pack.addProvider(BotaniaSimpleLootWrapper.wrap(entry));
		}
		pack.addProvider(LooniumStructureConfigurationProvider::new);
		pack.addProvider(GaiaFightConfigurationProvider::new);
		// PORT-26.3: vanilla names tag providers by registry (final name), so the conventional tag providers extend
		// Botania's own and write both sets; one provider per registry
		BlockTagProvider blockTagProvider = pack.addProvider(ConventionalBlockTagProvider::new);
		pack.addProvider((output, registriesFuture) -> new ConventionalItemTagProvider(
				output, registriesFuture, blockTagProvider.contentsGetter()));
		pack.addProvider(ConventionalFluidTagProvider::new);
		pack.addProvider(ConventionalEntityTypeTagProvider::new);
		pack.addProvider(BannerPatternProvider::new);
		pack.addProvider(BannerPatternTagsProvider::new);
		pack.addProvider(ConventionalBiomeTagProvider::new);
		pack.addProvider(BotaniaDynamicRegistryProvider::new);
		pack.addProvider(DamageTypeTagProvider::new);
		pack.addProvider(DataComponentTypeTagProvider::new);
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania stonecutting recipes",
				(recipes, advancements, conditions) -> new StonecuttingProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania crafting recipes", CraftingRecipeProvider::new));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania smelting recipes",
				(recipes, advancements, conditions) -> new SmeltingProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania elven trade recipes",
				(recipes, advancements, conditions) -> new ElvenTradeProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania mana pool recipes",
				(recipes, advancements, conditions) -> new ManaInfusionProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania Pure Daisy recipes",
				(recipes, advancements, conditions) -> new PureDaisyProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania Brew recipes",
				(recipes, advancements, conditions) -> new BrewProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania petal apothecary recipes", PetalApothecaryProvider::new));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania runic altar recipes",
				(recipes, advancements, conditions) -> new RunicAltarProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania Terra Plate recipes",
				(recipes, advancements, conditions) -> new TerrestrialAgglomerationProvider(recipes, advancements)));
		pack.addProvider(BotaniaFabricRecipeProvider.of("Botania Orechid and Marimorphosis recipes", OrechidProvider::new));
		pack.addProvider((FabricPackOutput output) -> new BlockstateProvider(output));
		pack.addProvider((FabricPackOutput output) -> new FloatingFlowerModelProvider(output));
		pack.addProvider((FabricPackOutput output) -> new ItemModelProvider(output));
		pack.addProvider((FabricPackOutput output) -> new PottedPlantModelProvider(output));
		pack.addProvider(BotaniaFabricAdvancementProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder builder) {
		builder.add(Registries.DAMAGE_TYPE, FabricDatagenInitializer::damageTypeBC);
		builder.add(Registries.BANNER_PATTERN, bootstrapContext -> BotaniaBannerPatterns.provideData(bootstrapContext::register));
	}

	protected static void damageTypeBC(BootstrapContext<DamageType> context) {
		context.register(RELIC_DAMAGE, RELIC);
		context.register(PLAYER_ATTACK_ARMOR_PIERCING, PLAYER_AP);
		context.register(KEY_EXPLOSION, KEY);
		context.register(PORTAL_BREAD_EXPLOSION, PORTAL);
	}
}
