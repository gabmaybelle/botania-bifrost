/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.block_entity.CorporeaCrystalCubeBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.ManaPumpBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.ManaSpreaderBlockEntityRenderer;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.common.item.equipment.bauble.ThirdEyeItem;
import vazkii.botania.common.item.relic.KeyOfTheKingsLawItem;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class MiscellaneousModels {
	private static final Identifier goldfishModelId = botaniaRL("icon/goldfish");
	private static final Identifier phiFlowerModelId = botaniaRL("icon/phiflower");
	private static final Identifier nerfBatModelId = botaniaRL("icon/nerfbat");
	private static final Identifier bloodPendantChainId = botaniaRL("icon/tainted_blood_pendant_chain");
	private static final Identifier bloodPendantGemId = botaniaRL("icon/tainted_blood_pendant_gem");
	private static final Identifier[] kingKeyWeaponModelIds = IntStream.range(0, KeyOfTheKingsLawItem.WEAPON_TYPES)
			.mapToObj(i -> botaniaRL("icon/gate_weapon_" + i)).toArray(Identifier[]::new);
	private static final Identifier terrasteelHelmWillModelId = botaniaRL("icon/will_flame");
	private static final Identifier[] thirdEyeLayerIds = IntStream.range(0, ThirdEyeItem.Renderer.NUM_LAYERS)
			.mapToObj(i -> botaniaRL("icon/third_eye_" + i)).toArray(Identifier[]::new);
	private static final Identifier pyroclastGemId = botaniaRL("icon/pyroclast_pendant_gem");
	private static final Identifier crimsonGemId = botaniaRL("icon/crimson_pendant_gem");
	private static final Identifier itemFinderGemId = botaniaRL("icon/the_spectator_gem");
	private static final Identifier cirrusGemId = botaniaRL("icon/cirrus_amulet_gem");
	private static final Identifier nimbusGemId = botaniaRL("icon/nimbus_amulet_gem");
	private static final Identifier snowflakePendantGemId = botaniaRL("icon/snowflake_pendant_gem");
	private static final Identifier[] tiaraWingIconIds = IntStream.range(0, FlugelTiaraItem.WING_TYPES)
			.mapToObj(i -> botaniaRL("icon/tiara_wing_" + (i + 1))).toArray(Identifier[]::new);

	public static final MiscellaneousModels INSTANCE = new MiscellaneousModels();

	private final Map<Identifier, Consumer<BlockStateModel>> modelConsumers;

	public boolean registeredModels = false;

	public final BlockStateModel[] tiaraWingIcons;
	public final BlockStateModel[] thirdEyeLayers;

	@UnknownNullability
	public BlockStateModel goldfishModel,
			phiFlowerModel,
			nerfBatModel,
			bloodPendantChain,
			bloodPendantGem,
			snowflakePendantGem,
			itemFinderGem,
			pyroclastGem,
			crimsonGem,
			cirrusGem,
			nimbusGem,
			terrasteelHelmWillModel;

	public final BlockStateModel[] kingKeyWeaponModels;

	public void onModelRegister(ResourceManager rm, Consumer<Identifier> consumer) {
		modelConsumers.keySet().forEach(consumer);

		registerTaters(rm, consumer);
		registerSpreaderComponents(consumer);
		consumer.accept(CorporeaCrystalCubeBlockEntityRenderer.CUBE_MODEL_ID);
		consumer.accept(ManaPumpBlockEntityRenderer.PUMP_HEAD_ID);
		// PORT-26.3: the mana blaster's gun models, drawn by its special renderer (the loader that composed them is gone)
		consumer.accept(botaniaRL("item/mana_blaster_clip"));
		consumer.accept(botaniaRL("item/mana_blaster_noclip"));

		if (!registeredModels) {
			registeredModels = true;
		}
	}

	private void registerSpreaderComponents(Consumer<Identifier> consumer) {
		Stream.of(BotaniaBlocks.MANA_SPREADER, BotaniaBlocks.PULSE_MANA_SPREADER,
				BotaniaBlocks.ELVEN_MANA_SPREADER, BotaniaBlocks.GAIA_MANA_SPREADER
		)
				.flatMap(spreader -> Stream
						.of(spreader.getSpreaderModelId(), spreader.getCoreModelId(), spreader.getScaffoldingModelId()))
				.distinct()
				.forEach(consumer);
		ManaSpreaderBlockEntityRenderer.WOOL_PADDING_IDS.values().forEach(consumer);
	}

	private static void registerTaters(ResourceManager rm, Consumer<Identifier> consumer) {
		for (Identifier model : rm.listResources(ResourcesLib.PREFIX_MODELS + ResourcesLib.PREFIX_TINY_POTATO, s -> s.getPath().endsWith(ResourcesLib.ENDING_JSON)).keySet()) {
			if (BotaniaAPI.MODID.equals(model.getNamespace())) {
				String path = model.getPath();
				path = path.substring(ResourcesLib.PREFIX_MODELS.length(), path.length() - ResourcesLib.ENDING_JSON.length());
				consumer.accept(botaniaRL(path));
			}
		}
	}

	// PORT-26.3: both loaders now resolve extra models by id once loading finishes; each one calls this with its lookup
	public void onModelsLoaded(Function<Identifier, @Nullable BlockStateModel> lookup) {
		if (!registeredModels) {
			BotaniaAPI.LOGGER.error("Additional models failed to register! Aborting baking models to avoid early crashing.");
			return;
		}
		modelConsumers.forEach((resourceLocation, bakedModelConsumer) -> bakedModelConsumer.accept(lookup.apply(resourceLocation)));
	}

	private MiscellaneousModels() {
		// PORT-26.3: the platform (camouflage) models and the named tiny potato item model were wrapped after baking;
		// 26.3 has no after-bake hook, so they return with the loader model plugins and the item model pass
		modelConsumers = new HashMap<>();

		modelConsumers.put(goldfishModelId, bakedModel -> this.goldfishModel = bakedModel);
		modelConsumers.put(phiFlowerModelId, bakedModel -> this.phiFlowerModel = bakedModel);
		modelConsumers.put(nerfBatModelId, bakedModel -> this.nerfBatModel = bakedModel);
		modelConsumers.put(bloodPendantChainId, bakedModel -> this.bloodPendantChain = bakedModel);
		modelConsumers.put(bloodPendantGemId, bakedModel -> this.bloodPendantGem = bakedModel);
		modelConsumers.put(terrasteelHelmWillModelId, bakedModel -> this.terrasteelHelmWillModel = bakedModel);
		modelConsumers.put(pyroclastGemId, bakedModel -> this.pyroclastGem = bakedModel);
		modelConsumers.put(crimsonGemId, bakedModel -> this.crimsonGem = bakedModel);
		modelConsumers.put(itemFinderGemId, bakedModel -> this.itemFinderGem = bakedModel);
		modelConsumers.put(cirrusGemId, bakedModel -> this.cirrusGem = bakedModel);
		modelConsumers.put(nimbusGemId, bakedModel -> this.nimbusGem = bakedModel);
		modelConsumers.put(snowflakePendantGemId, bakedModel -> this.snowflakePendantGem = bakedModel);

		kingKeyWeaponModels = getBakedModels(modelConsumers, kingKeyWeaponModelIds);
		thirdEyeLayers = getBakedModels(modelConsumers, thirdEyeLayerIds);
		tiaraWingIcons = getBakedModels(modelConsumers, tiaraWingIconIds);
	}

	private static BlockStateModel[] getBakedModels(Map<Identifier, Consumer<BlockStateModel>> consumers, Identifier[] ids) {
		final BlockStateModel[] bakedModels = new BlockStateModel[ids.length];
		for (int i = 0; i < ids.length; i++) {
			int index = i;
			consumers.put(ids[index], bakedModel -> bakedModels[index] = bakedModel);
		}
		return bakedModels;
	}
}
