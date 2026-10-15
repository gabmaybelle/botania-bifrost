/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.storage.loot.LootTable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.configdata.LooniumStructureConfiguration;
import vazkii.botania.api.configdata.MobAttributeModifier;
import vazkii.botania.api.configdata.MobEffectToApply;
import vazkii.botania.api.configdata.MobSpawnData;
import vazkii.botania.common.config.ConfigDataManagerImpl;
import vazkii.botania.common.loot.BotaniaLootTables;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class LooniumStructureConfigurationProvider implements DataProvider {

	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

	public LooniumStructureConfigurationProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
		pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, ConfigDataManagerImpl.LOONIUM_CONFIG_PATH);
		this.registryLookupFuture = registryLookupFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return registryLookupFuture.thenCompose(registryLookup -> this.run(cache, registryLookup));
	}

	private CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider registries) {
		Map<Identifier, LooniumStructureConfiguration> configs = new HashMap<>();
		addConfigs(configs);

		var output = new ArrayList<CompletableFuture<?>>(configs.size());
		for (Map.Entry<Identifier, LooniumStructureConfiguration> e : configs.entrySet()) {
			Path path = pathProvider.json(e.getKey());
			LooniumStructureConfiguration config = e.getValue();
			JsonElement jsonTree = LooniumStructureConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, config)
					.getOrThrow();
			output.add(DataProvider.saveStable(cache, jsonTree, path));
		}
		return CompletableFuture.allOf(output.toArray(CompletableFuture<?>[]::new));
	}

	private void addConfigs(Map<Identifier, LooniumStructureConfiguration> configs) {
		Identifier defaultConfigId = LooniumStructureConfiguration.DEFAULT_CONFIG_ID;
		configs.put(defaultConfigId, getDefaultConfig());

		configs.put(BuiltinStructures.ANCIENT_CITY.identifier(), getConfigAncientCity(defaultConfigId));
		configs.put(BuiltinStructures.BASTION_REMNANT.identifier(), getConfigBastionRemnant(defaultConfigId));
		configs.put(BuiltinStructures.DESERT_PYRAMID.identifier(), getConfigDesertPyramid(defaultConfigId));
		configs.put(BuiltinStructures.END_CITY.identifier(), getConfigEndCity(defaultConfigId));
		configs.put(BuiltinStructures.FORTRESS.identifier(), getConfigFortress(defaultConfigId));
		configs.put(BuiltinStructures.JUNGLE_TEMPLE.identifier(), getConfigJungleTemple(defaultConfigId));
		configs.put(BuiltinStructures.OCEAN_MONUMENT.identifier(), getConfigOceanMonument(defaultConfigId));

		Identifier oceanRuinId = botaniaRL("ocean_ruins");
		configs.put(oceanRuinId,
				LooniumStructureConfiguration.forParent(defaultConfigId)
						.boundingBoxType(StructureSpawnOverride.BoundingBoxType.STRUCTURE).build()
		);
		configs.put(BuiltinStructures.OCEAN_RUIN_COLD.identifier(), getConfigOceanRuinCold(oceanRuinId));
		configs.put(BuiltinStructures.OCEAN_RUIN_WARM.identifier(), getConfigOceanRuinWarm(oceanRuinId));

		configs.put(BuiltinStructures.PILLAGER_OUTPOST.identifier(), getConfigPillagerOutpost(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_DESERT.identifier(), getConfigRuinedPortalDesert(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_JUNGLE.identifier(), getConfigRuinedPortalJungle(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_MOUNTAIN.identifier(), getConfigRuinedPortalMountain(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_NETHER.identifier(), getConfigRuinedPortalNether(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_OCEAN.identifier(), getConfigRuinedPortalOcean(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_STANDARD.identifier(), getConfigRuinedPortalStandard(defaultConfigId));
		configs.put(BuiltinStructures.RUINED_PORTAL_SWAMP.identifier(), getConfigRuinedPortalSwamp(defaultConfigId));

		configs.put(BuiltinStructures.SHIPWRECK.identifier(), getConfigShipwreck(defaultConfigId));
		configs.put(BuiltinStructures.SHIPWRECK_BEACHED.identifier(),
				LooniumStructureConfiguration.forParent(BuiltinStructures.SHIPWRECK.identifier()).build());

		configs.put(BuiltinStructures.STRONGHOLD.identifier(), getConfigStronghold(defaultConfigId));
		configs.put(BuiltinStructures.TRAIL_RUINS.identifier(), getConfigTrailRuins(defaultConfigId));
		configs.put(BuiltinStructures.TRIAL_CHAMBERS.identifier(), getConfigTrialChamber(defaultConfigId));

		Identifier villageId = botaniaRL("village");
		configs.put(villageId, LooniumStructureConfiguration.forParent(defaultConfigId)
				.boundingBoxType(StructureSpawnOverride.BoundingBoxType.STRUCTURE).build()
		);
		configs.put(BuiltinStructures.VILLAGE_DESERT.identifier(), getConfigVillageDesert(villageId));
		configs.put(BuiltinStructures.VILLAGE_PLAINS.identifier(), getConfigVillagePlains(villageId));
		configs.put(BuiltinStructures.VILLAGE_SAVANNA.identifier(), getConfigVillageSavanna(villageId));
		configs.put(BuiltinStructures.VILLAGE_SNOWY.identifier(), getConfigVillageSnowy(villageId));
		configs.put(BuiltinStructures.VILLAGE_TAIGA.identifier(), getConfigVillageTaiga(villageId));

		configs.put(BuiltinStructures.WOODLAND_MANSION.identifier(), getConfigWoodlandMansion(defaultConfigId));
	}

	public static LooniumStructureConfiguration getDefaultConfig() {
		return LooniumStructureConfiguration.builder()
				.manaCost(LooniumStructureConfiguration.DEFAULT_COST)
				.maxNearbyMobs(LooniumStructureConfiguration.DEFAULT_MAX_NEARBY_MOBS)
				.boundingBoxType(StructureSpawnOverride.BoundingBoxType.PIECE)
				.spawnedMobs(
						// weights roughly based on original Loonium mob selection logic
						MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
						getCreeperSpawnData(195, false, getCreeperEffects(false)),
						getCreeperSpawnData(1, true, getCreeperEffects(false)),
						MobSpawnData.entityWeight(EntityTypes.HUSK, 59)
								.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.DROWNED, 106)
								.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
								.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.STRAY, 59)
								.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.SKELETON, 529)
								.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
						MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
				)
				.attributeModifiers(
						new MobAttributeModifier(
								Attributes.MAX_HEALTH, 2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						new MobAttributeModifier(
								Attributes.ATTACK_DAMAGE, 1.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
				)
				.effectsToApply(getStandardEffects(false, true))
				.build();
	}

	public static LooniumStructureConfiguration getConfigAncientCity(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 30).build(),
				getCreeperSpawnData(99, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_ANCIENT_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 80)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_ANCIENT_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 410)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_ANCIENT_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_ANCIENT_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_ANCIENT_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 400)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_ANCIENT_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 100).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 200).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigBastionRemnant(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 30).build(),
				getCreeperSpawnData(99, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				getPiglinSpawnData(450, BotaniaLootTables.LOONIUM_PIGLIN_BASTION_REMNANT, false, false),
				MobSpawnData.entityWeight(EntityTypes.PIGLIN_BRUTE, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_PIGLIN_BRUTE_DEFAULT)
						.attributeModifiers(
								new MobAttributeModifier(
										Attributes.MAX_HEALTH, 1.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
								new MobAttributeModifier(
										Attributes.ATTACK_DAMAGE, 1.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
						)
						.build(),
				MobSpawnData.entityWeight(EntityTypes.HOGLIN, 300).spawnAsAdult().build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigDesertPyramid(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 50).build(),
				getCreeperSpawnData(149, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DESERT_PYRAMID).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DESERT_PYRAMID).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 500)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DESERT_PYRAMID).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 40).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 360).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigEndCity(Identifier parentId) {
		MobEffectToApply[] creeperEffects = {
				MobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).duration(100).build(),
				MobEffectToApply.effect(MobEffects.REGENERATION).duration(100).build(),
				MobEffectToApply.effect(MobEffects.SLOW_FALLING).duration(400).build()
		};
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.SHULKER, 100)
						.effectsToApply(getStandardEffects(true, true)).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 300).build(),
				getCreeperSpawnData(99, false, creeperEffects),
				getCreeperSpawnData(1, true, creeperEffects),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 300)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_END_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 300)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_END_CITY).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 300).build()
		).effectsToApply(
				MobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).build(),
				MobEffectToApply.effect(MobEffects.REGENERATION).build(),
				MobEffectToApply.effect(MobEffects.SLOW_FALLING).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigFortress(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 30).build(),
				getCreeperSpawnData(99, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.BLAZE, 300)
						.effectsToApply(getStandardEffects(false, false)).build(),
				MobSpawnData.entityWeight(EntityTypes.WITHER_SKELETON, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_FORTRESS)
						.effectsToApply(getStandardEffects(false, false)).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_FORTRESS).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 400)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_FORTRESS)
						.effectsToApply(getStandardEffects(false, false)).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigJungleTemple(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 30).build(),
				getCreeperSpawnData(149, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_JUNGLE_TEMPLE).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 360)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_JUNGLE_TEMPLE).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_JUNGLE_TEMPLE).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_JUNGLE_TEMPLE).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 300).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 300).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigOceanMonument(Identifier parentId) {
		MobEffectToApply[] standardEffectsInWater = getStandardEffects(true, true);
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.GUARDIAN, 200).build(),
				getCreeperSpawnData(199, false, getCreeperEffects(true)),
				getCreeperSpawnData(1, true, getCreeperEffects(true)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 540)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_MONUMENT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_MONUMENT).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_MONUMENT).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_MONUMENT).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 320)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_MONUMENT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 30)
						.effectsToApply(standardEffectsInWater).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 270)
						.effectsToApply(standardEffectsInWater).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigOceanRuinCold(Identifier parentId) {
		MobEffectToApply[] standardEffectsInWater = getStandardEffects(true, true);
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				getCreeperSpawnData(199, false, getCreeperEffects(true)),
				getCreeperSpawnData(1, true, getCreeperEffects(true)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 540)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 360)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 30)
						.effectsToApply(standardEffectsInWater).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 270)
						.effectsToApply(standardEffectsInWater).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigOceanRuinWarm(Identifier parentId) {
		MobEffectToApply[] standardEffectsInWater = getStandardEffects(true, true);
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				getCreeperSpawnData(199, false, getCreeperEffects(true)),
				getCreeperSpawnData(1, true, getCreeperEffects(true)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 540)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 360)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 30)
						.effectsToApply(standardEffectsInWater).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 270)
						.effectsToApply(standardEffectsInWater).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigPillagerOutpost(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId)
				.boundingBoxType(StructureSpawnOverride.BoundingBoxType.STRUCTURE).spawnedMobs(
						MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
						getCreeperSpawnData(199, false, getCreeperEffects(false)),
						getCreeperSpawnData(1, true, getCreeperEffects(false)),
						MobSpawnData.entityWeight(EntityTypes.PILLAGER, 900)
								.equipmentTable(BotaniaLootTables.LOONIUM_PILLAGER_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.VINDICATOR, 175)
								.equipmentTable(BotaniaLootTables.LOONIUM_VINDICATOR_DEFAULT).build(),
						MobSpawnData.entityWeight(EntityTypes.EVOKER, 25).build(),
						MobSpawnData.entityWeight(EntityTypes.SKELETON, 200)
								.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_OUTPOST).build(),
						MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 200)
								.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_OUTPOST).build(),
						MobSpawnData.entityWeight(EntityTypes.SPIDER, 200).build()
				).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalDesert(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 25)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(50, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, false, true),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 50).build(),
				getCreeperSpawnData(149, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 360).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalJungle(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 25)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(50, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, false, true),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 30).build(),
				getCreeperSpawnData(149, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 360)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 250).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 50).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalMountain(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 25)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(50, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, false, true),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 529)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 529)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SILVERFISH, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalNether(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 125)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(500, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, false, false),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 450)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 200)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 10).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 90).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalOcean(Identifier parentId) {
		MobEffectToApply[] standardEffectsInWater = getStandardEffects(true, true);
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 25)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(50, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, true, true),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				getCreeperSpawnData(199, false, getCreeperEffects(true)),
				getCreeperSpawnData(1, true, getCreeperEffects(true)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 540)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 360)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 30)
						.effectsToApply(standardEffectsInWater).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 270)
						.effectsToApply(standardEffectsInWater).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalStandard(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 25)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(50, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, false, true),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 529)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigRuinedPortalSwamp(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ZOGLIN, 25)
						.effectsToApply(getStandardEffects(false, false)).build(),
				getPiglinSpawnData(50, BotaniaLootTables.LOONIUM_PIGLIN_PORTAL, false, true),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIFIED_PIGLIN, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 30).build(),
				getCreeperSpawnData(149, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 360)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 400)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 100)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_PORTAL).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 50).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 250).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigShipwreck(Identifier parentId) {
		MobEffectToApply[] standardEffectsInWater = getStandardEffects(true, true);
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				getCreeperSpawnData(199, false, getCreeperEffects(true)),
				getCreeperSpawnData(1, true, getCreeperEffects(true)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 540)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_SHIPWRECK).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_SHIPWRECK).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_SHIPWRECK).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 40)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_SHIPWRECK).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 320)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_SHIPWRECK).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 30)
						.effectsToApply(standardEffectsInWater).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 270)
						.effectsToApply(standardEffectsInWater).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigStronghold(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 80).build(),
				getCreeperSpawnData(149, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_STRONGHOLD).build(),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_STRONGHOLD).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 400)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_STRONGHOLD).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_STRONGHOLD).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 50)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_STRONGHOLD).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 400)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_STRONGHOLD).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 100).build(),
				MobSpawnData.entityWeight(EntityTypes.SILVERFISH, 100).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 400).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigTrailRuins(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_TRAIL_RUINS).build(),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_TRAIL_RUINS).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_TRAIL_RUINS).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 49)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_TRAIL_RUINS).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 49)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_TRAIL_RUINS).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 509)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_TRAIL_RUINS).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigTrialChamber(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_TRIAL_CHAMBER).build(),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_TRIAL_CHAMBER).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_TRIAL_CHAMBER).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 49)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_TRIAL_CHAMBER).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 49)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_TRIAL_CHAMBER).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 509)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_TRIAL_CHAMBER).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build(),
				MobSpawnData.entityWeight(EntityTypes.BREEZE, 200).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigVillageDesert(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE_VILLAGER, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_VILLAGER).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 600)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigVillagePlains(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE_VILLAGER, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_VILLAGER).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 600)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigVillageSavanna(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 30)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.HUSK, 30)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE_VILLAGER, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_VILLAGER).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 60)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 540)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigVillageSnowy(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE_VILLAGER, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_VILLAGER).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 529)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigVillageTaiga(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(195, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.DROWNED, 59)
						.equipmentTable(BotaniaLootTables.LOONIUM_DROWNED_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE_VILLAGER, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_ZOMBIE_VILLAGER).build(),
				MobSpawnData.entityWeight(EntityTypes.STRAY, 106)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 423)
						.equipmentTable(BotaniaLootTables.LOONIUM_SKELETON_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 59).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 529).build()
		).build();
	}

	public static LooniumStructureConfiguration getConfigWoodlandMansion(Identifier parentId) {
		return LooniumStructureConfiguration.forParent(parentId).spawnedMobs(
				MobSpawnData.entityWeight(EntityTypes.ENDERMAN, 40).build(),
				getCreeperSpawnData(199, false, getCreeperEffects(false)),
				getCreeperSpawnData(1, true, getCreeperEffects(false)),
				MobSpawnData.entityWeight(EntityTypes.VINDICATOR, 600)
						.equipmentTable(BotaniaLootTables.LOONIUM_VINDICATOR_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.PILLAGER, 200)
						.equipmentTable(BotaniaLootTables.LOONIUM_PILLAGER_DEFAULT).build(),
				MobSpawnData.entityWeight(EntityTypes.EVOKER, 100).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 150)
						.equipmentTable(BotaniaLootTables.LOONIUM_ARMOR_MANSION).build(),
				MobSpawnData.entityWeight(EntityTypes.ZOMBIE, 50).spawnAsBaby()
						.equipmentTable(BotaniaLootTables.LOONIUM_ARMOR_MANSION).build(),
				MobSpawnData.entityWeight(EntityTypes.BOGGED, 20)
						.equipmentTable(BotaniaLootTables.LOONIUM_ARMOR_MANSION).build(),
				MobSpawnData.entityWeight(EntityTypes.SKELETON, 180)
						.equipmentTable(BotaniaLootTables.LOONIUM_ARMOR_MANSION).build(),
				MobSpawnData.entityWeight(EntityTypes.CAVE_SPIDER, 30).build(),
				MobSpawnData.entityWeight(EntityTypes.SPIDER, 270).build()
		).build();
	}

	public static MobEffectToApply[] getStandardEffects(boolean withWaterBreathing, boolean withFireResistance) {
		return withFireResistance
				? (withWaterBreathing
						? new MobEffectToApply[] {
								MobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).build(),
								MobEffectToApply.effect(MobEffects.REGENERATION).build(),
								MobEffectToApply.effect(MobEffects.WATER_BREATHING).build()
						}
						: new MobEffectToApply[] {
								MobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).build(),
								MobEffectToApply.effect(MobEffects.REGENERATION).build()
						})
				: (withWaterBreathing
						? new MobEffectToApply[] {
								MobEffectToApply.effect(MobEffects.REGENERATION).build(),
								MobEffectToApply.effect(MobEffects.WATER_BREATHING).build()
						}
						: new MobEffectToApply[] {
								MobEffectToApply.effect(MobEffects.REGENERATION).build()
						});
	}

	public static MobEffectToApply[] getCreeperEffects(boolean withWaterBreathing) {
		return withWaterBreathing
				? new MobEffectToApply[] {
						MobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).duration(100).build(),
						MobEffectToApply.effect(MobEffects.REGENERATION).duration(100).build(),
						MobEffectToApply.effect(MobEffects.WATER_BREATHING).build()
				}
				: new MobEffectToApply[] {
						MobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).duration(100).build(),
						MobEffectToApply.effect(MobEffects.REGENERATION).duration(100).build()
				};
	}

	public static Weighted<MobSpawnData> getCreeperSpawnData(int weight, boolean charged,
			MobEffectToApply... creeperEffects) {
		MobSpawnData.Builder builder = MobSpawnData.entityWeight(EntityTypes.CREEPER, weight);
		builder.effectsToApply(creeperEffects);

		if (charged) {
			CompoundTag chargedCreeperNbt = new CompoundTag();
			chargedCreeperNbt.putBoolean("powered", true);
			builder.nbt(chargedCreeperNbt);
		}

		return builder.build();
	}

	public static Weighted<MobSpawnData> getPiglinSpawnData(int weight, ResourceKey<LootTable> equipmentTable,
			boolean needWaterBreathing, boolean zombificationImmune) {
		CompoundTag piglinNbt = new CompoundTag();
		if (zombificationImmune) {
			piglinNbt.putBoolean("IsImmuneToZombification", true);
		}

		// PORT-26.3: brains are saved as a packed memory map now; a brain itself needs a living body. ATE_RECENTLY has
		// no codec (it never had one, so the old brain serializer silently dropped it) and the packed map refuses it.
		MemoryMap piglinMemories = new MemoryMap.Builder()
				.add(MemoryModuleType.UNIVERSAL_ANGER, ExpirableValue.of(true))
				.add(MemoryModuleType.ADMIRING_DISABLED, ExpirableValue.of(true))
				.build();

		DataResult<Tag> dataResult = Brain.Packed.CODEC.encodeStart(NbtOps.INSTANCE, new Brain.Packed(piglinMemories));
		dataResult.resultOrPartial(BotaniaAPI.LOGGER::error).ifPresent(tag -> piglinNbt.put("Brain", tag));

		return MobSpawnData.entityWeight(EntityTypes.PIGLIN, weight)
				.spawnAsAdult()
				.nbt(piglinNbt)
				.equipmentTable(equipmentTable)
				.effectsToApply(getStandardEffects(needWaterBreathing, true))
				.build();
	}

	@Override
	public String getName() {
		return "Loonium structure configuration";
	}
}
