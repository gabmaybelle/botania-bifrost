/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.data;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.world.SkyblockChunkGenerator;
import vazkii.botania.data.util.DatagenItemComponents;
import vazkii.botania.neoforge.data.gog.GogBlockLootSubProvider;
import vazkii.botania.neoforge.data.gog.GogBlockTagsProvider;
import vazkii.botania.neoforge.data.gog.GogBlockUseLootProvider;
import vazkii.botania.neoforge.data.gog.GogGlobalLootModifierProvider;
import vazkii.botania.neoforge.data.gog.GogRecipeProvider;
import vazkii.botania.neoforge.data.gog.GogSoundDefinitionProvider;
import vazkii.botania.neoforge.data.gog.GogWorldPresetTagsProvider;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = BotaniaAPI.MODID)
public class NeoForgeDatagenInitializer {
	public static final ResourceKey<WorldPreset> SKYBLOCK_PRESET = ResourceKey.create(
			Registries.WORLD_PRESET, BotaniaAPI.gogRL("gardenofglass"));

	// PORT-26.3: NeoForge generates client and server data in one clientData run (GatherDataEvent.Client);
	// recipes and loot tables are registries now and go through GatherDataRegistryEntriesEvent
	@SubscribeEvent
	public static void configureDatagen(GatherDataEvent.Client evt) {
		bindItemComponents();
		if (System.getProperty("botania.gog_datagen") != null) {
			configureGogDatagen(evt);
		} else {
			configureNeoForgeDatagen(evt);
		}
	}

	// PORT-26.3: bind the item components (a server job normally): the recipe providers build item stacks, and the
	// Garden of Glass recipe bootstrap runs while NeoForge builds its registry set, before GatherDataEvent fires.
	// NeoForge's dev-time component self-check (gated by this flag) rejects the empty tag sets the binder hands
	// out for datapack registries (HolderSet.emptyNamed has no equals), so the check is off for the binding only.
	private static void bindItemComponents() {
		boolean inIde = SharedConstants.IS_RUNNING_IN_IDE;
		SharedConstants.IS_RUNNING_IN_IDE = false;
		try {
			DatagenItemComponents.bind();
		} finally {
			SharedConstants.IS_RUNNING_IN_IDE = inIde;
		}
	}

	@SubscribeEvent
	public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent evt) {
		bindItemComponents();
		if (System.getProperty("botania.gog_datagen") != null) {
			evt.gatherFor(BotaniaAPI.GOG_MODID)
					.lootTable(new LootTableProvider.SubProviderEntry(GogBlockUseLootProvider::new, LootContextParamSets.BLOCK_USE),
							new LootTableProvider.SubProviderEntry(GogBlockLootSubProvider::new, LootContextParamSets.BLOCK))
					.recipe(GogRecipeProvider::new);
		}
	}

	private static void configureNeoForgeDatagen(GatherDataEvent evt) {
		evt.createBlockAndItemTags(NeoForgeBlockTagProvider::new, NeoForgeItemTagProvider::new);
		evt.createProvider(NeoForgeEntityTagProvider::new);
		// PORT-26.3: the four biome modifiers are hand-written JSON in src/main/resources now (like the placed features
		// they point at): the 26.3 registry builder refuses references to entries that are not in its base set, and
		// the datagen base set is vanilla only
		evt.createProvider(BotaniaCuriosDataProvider::new);
		evt.createProvider(BotaniaGlobalLootModifierProvider::new);
		evt.createProvider(BotaniaDataMapsProvider::new);
	}

	private static void configureGogDatagen(GatherDataEvent evt) {
		evt.createProvider(GogBlockTagsProvider::new);
		evt.createProvider(GogGlobalLootModifierProvider::new);
		evt.getDefaultPackGenerator().createWorldRegistryObjects(addWorldPresets(), Set.of(BotaniaAPI.GOG_MODID));
		evt.createProvider(GogWorldPresetTagsProvider::new);
		evt.createProvider(GogSoundDefinitionProvider::new);
	}

	private static RegistrySetBuilder addWorldPresets() {
		return new RegistrySetBuilder().add(Registries.WORLD_PRESET, context -> {
			HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
			HolderGetter<MultiNoiseBiomeSourceParameterList> noiseParameterLists = context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
			HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);
			HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

			// same as preset minecraft:normal, except using SkyblockChunkGenerator for the overworld
			context.register(SKYBLOCK_PRESET, new WorldPreset(Map.of(
					LevelStem.OVERWORLD, new LevelStem(
							dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
							new SkyblockChunkGenerator(
									MultiNoiseBiomeSource.createFromPreset(noiseParameterLists
											.get(MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElseThrow()),
									noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
							)
					),
					LevelStem.NETHER, new LevelStem(
							dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
							new NoiseBasedChunkGenerator(
									MultiNoiseBiomeSource.createFromPreset(noiseParameterLists
											.get(MultiNoiseBiomeSourceParameterLists.NETHER).orElseThrow()),
									noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER)
							)
					),
					LevelStem.END, new LevelStem(
							dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
							new NoiseBasedChunkGenerator(TheEndBiomeSource.create(biomes),
									noiseSettings.getOrThrow(NoiseGeneratorSettings.END)
							)
					)
			)));
		});
	}
}
