/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;

import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;

import org.apache.commons.lang3.function.TriConsumer;
import org.slf4j.Logger;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.AlfheimPortalState;
import vazkii.botania.api.state.enums.CraftyCratePattern;
import vazkii.botania.common.block.*;
import vazkii.botania.common.block.flower.*;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.block.mana.ManaSpreaderBlock;
import vazkii.botania.common.block.red_string.RedStringBlock;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.data.util.BotaniaModelTemplates;
import vazkii.botania.mixin.BlockModelGeneratorsAccessor;
import vazkii.botania.mixin.TextureSlotAccessor;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation;
import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class BlockstateProvider implements DataProvider {
	protected final PackOutput packOutput;

	protected final List<BlockModelDefinitionGenerator> blockStateGenerators = new ArrayList<>();

	protected final Map<Identifier, ModelInstance> models = new HashMap<>();
	protected final BiConsumer<Identifier, ModelInstance> modelOutput = models::put;

	public BlockstateProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	protected Logger getLogger() {
		return BotaniaAPI.LOGGER;
	}

	@Override
	public String getName() {
		return "Botania Blockstates and Models";
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		try {
			registerStatesAndModels();
		} catch (Exception e) {
			getLogger().error("Error registering states and models", e);
		}

		PackOutput.PathProvider blockstatePathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
		PackOutput.PathProvider modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
		List<CompletableFuture<?>> outputList = new ArrayList<>();

		for (BlockModelDefinitionGenerator state : blockStateGenerators) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(state.block());
			Path path = blockstatePathProvider.json(id);
			outputList.add(DataProvider.saveStable(output, BlockStateModelDispatcher.CODEC, state.create(), path));
		}

		for (Map.Entry<Identifier, ModelInstance> e : models.entrySet()) {
			Identifier modelId = e.getKey();
			Path path = modelPathProvider.json(modelId);
			outputList.add(DataProvider.saveStable(output, e.getValue().get(), path));
		}
		return CompletableFuture.allOf(outputList.toArray(CompletableFuture[]::new));
	}

	protected void registerStatesAndModels() {
		Set<Block> remainingBlocks = BuiltInRegistries.BLOCK.stream()
				.filter(block -> BotaniaAPI.MODID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()))
				.collect(Collectors.toSet());

		// Manually written blockstate + models
		remainingBlocks.remove(BotaniaBlocks.SPECTRAL_RAIL);
		remainingBlocks.remove(BotaniaBlocks.SOLID_VINE);

		// Manually written model, generated blockstate
		manualModel(remainingBlocks, BotaniaBlocks.COCOON_OF_CAPRICE);
		manualModel(remainingBlocks, BotaniaBlocks.CORPOREA_CRYSTAL_CUBE);
		manualModel(remainingBlocks, BotaniaBlocks.MANA_SPLITTER);
		manualModel(remainingBlocks, BotaniaBlocks.MANA_PRISM);
		manualModel(remainingBlocks, BotaniaBlocks.RUNIC_ALTAR);
		manualModel(remainingBlocks, BotaniaBlocks.LIFE_IMBUER);

		// Single blocks
		var alfPortalModel = ModelTemplates.CUBE_ALL.create(
				getModelLocation(BotaniaBlocks.ELVEN_GATEWAY_CORE),
				TextureMapping.cube(BotaniaBlocks.ELVEN_GATEWAY_CORE),
				this.modelOutput);
		var alfPortalActivatedModel = ModelTemplates.CUBE_ALL.create(
				getModelLocation(BotaniaBlocks.ELVEN_GATEWAY_CORE, "_activated"),
				TextureMapping.cube(tex(getModelLocation(BotaniaBlocks.ELVEN_GATEWAY_CORE, "_activated"))),
				this.modelOutput);
		this.blockStateGenerators.add(
				MultiVariantGenerator.dispatch(BotaniaBlocks.ELVEN_GATEWAY_CORE).with(
						PropertyDispatch.initial(BotaniaStateProperties.ALFPORTAL_STATE)
								.select(AlfheimPortalState.OFF,
										mv(alfPortalModel))
								.select(AlfheimPortalState.ON_X,
										mv(plainModel(alfPortalActivatedModel)))
								.select(AlfheimPortalState.ON_Z,
										mv(plainModel(alfPortalActivatedModel)))
				));
		remainingBlocks.remove(BotaniaBlocks.ELVEN_GATEWAY_CORE);

		singleVariantBlockState(
				BotaniaBlocks.BIFROST_BRIDGE,
				ModelTemplates.CUBE_ALL.create(
						getModelLocation(BotaniaBlocks.BIFROST_BRIDGE),
						TextureMapping.cube(BotaniaBlocks.BIFROST), this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.BIFROST_BRIDGE);

		singleVariantBlockState(
				BotaniaBlocks.CACOPHONIUM_BLOCK,
				ModelTemplates.CUBE_TOP.create(
						BotaniaBlocks.CACOPHONIUM_BLOCK, (new TextureMapping())
								.put(TextureSlot.SIDE, tex(getBlockTexture(Blocks.NOTE_BLOCK)))
								.put(TextureSlot.TOP, tex(getBlockTexture(BotaniaBlocks.CACOPHONIUM_BLOCK, "_top"))),
						this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.CACOPHONIUM_BLOCK);

		var crateTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/crate")), Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.SIDE);
		var craftCrateBottomTex = getBlockTexture(BotaniaBlocks.CRAFTY_CRATE, "_bottom");
		var crateDispatch = PropertyDispatch.initial(BotaniaStateProperties.CRATE_PATTERN);
		for (var pattern : CraftyCratePattern.values()) {
			String suffix = pattern == CraftyCratePattern.NONE
					? ""
					: "_" + pattern.getSerializedName().substring("crafty_".length());
			var model = crateTemplate.create(getModelLocation(BotaniaBlocks.CRAFTY_CRATE, suffix),
					new TextureMapping().put(TextureSlot.BOTTOM, tex(craftCrateBottomTex))
							.put(TextureSlot.SIDE, tex(getBlockTexture(BotaniaBlocks.CRAFTY_CRATE, suffix))),
					this.modelOutput);
			crateDispatch = crateDispatch.select(pattern, mv(model));
		}
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(BotaniaBlocks.CRAFTY_CRATE).with(crateDispatch));
		remainingBlocks.remove(BotaniaBlocks.CRAFTY_CRATE);

		Identifier corpSlabSide = botaniaRL("block/corporea_slab_side");
		Identifier corpBlock = getBlockTexture(BotaniaBlocks.CORPOREA_BLOCK);
		var corpSlabBottomModel = ModelTemplates.SLAB_BOTTOM.create(
				BotaniaBlocks.CORPOREA_SLAB,
				new TextureMapping()
						.put(TextureSlot.BOTTOM, tex(corpBlock))
						.put(TextureSlot.TOP, tex(corpBlock))
						.put(TextureSlot.SIDE, tex(corpBlock)),
				this.modelOutput);
		var corpSlabTopModel = ModelTemplates.SLAB_TOP.create(
				getModelLocation(BotaniaBlocks.CORPOREA_SLAB, "_top"),
				new TextureMapping()
						.put(TextureSlot.BOTTOM, tex(corpBlock))
						.put(TextureSlot.TOP, tex(corpBlock))
						.put(TextureSlot.SIDE, tex(corpBlock)),
				this.modelOutput);
		var corpSlabDoubleModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
				botaniaRL("block/corporea_double_slab"),
				new TextureMapping()
						.put(TextureSlot.SIDE, tex(corpSlabSide))
						.put(TextureSlot.BOTTOM, tex(corpBlock))
						.put(TextureSlot.TOP, tex(corpBlock)),
				this.modelOutput);
		blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createSlab(BotaniaBlocks.CORPOREA_SLAB,
				mv(corpSlabBottomModel), mv(corpSlabTopModel), mv(corpSlabDoubleModel)));
		remainingBlocks.remove(BotaniaBlocks.CORPOREA_SLAB);

		stairsBlock(remainingBlocks, BotaniaBlocks.CORPOREA_STAIRS, corpBlock, corpBlock, corpBlock);

		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(
				BotaniaBlocks.ALFGLASS, mv(IntStream.rangeClosed(0, 3)
						.mapToObj(i -> {
							var model = ModelTemplates.CUBE_ALL.create(
									getModelLocation(BotaniaBlocks.ALFGLASS, "_" + i),
									TextureMapping.cube(tex(getBlockTexture(BotaniaBlocks.ALFGLASS, "_" + i))),
									this.modelOutput);
							return plainModel(model);
						})
						.toList())));
		remainingBlocks.remove(BotaniaBlocks.ALFGLASS);

		var pumpkinModel = ModelTemplates.CUBE_ORIENTABLE.create(
				BotaniaBlocks.FEL_PUMPKIN, new TextureMapping()
						.put(TextureSlot.SIDE, tex(getBlockTexture(Blocks.PUMPKIN, "_side")))
						.put(TextureSlot.FRONT, tex(getBlockTexture(BotaniaBlocks.FEL_PUMPKIN)))
						.put(TextureSlot.TOP, tex(getBlockTexture(Blocks.PUMPKIN, "_top"))),
				this.modelOutput
		);
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(
				BotaniaBlocks.FEL_PUMPKIN, mv(pumpkinModel))
				.with(BlockModelGeneratorsAccessor.botania_getRotationHorizontalFacing()));
		remainingBlocks.remove(BotaniaBlocks.FEL_PUMPKIN);

		ModelTemplate eightByEightTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/eightbyeight")),
				Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.WEST, TextureSlot.EAST);
		singleVariantBlockState(
				BotaniaBlocks.EYE_OF_THE_ANCIENTS, eightByEightTemplate.create(
						BotaniaBlocks.EYE_OF_THE_ANCIENTS,
						new TextureMapping()
								.put(TextureSlot.BOTTOM, tex(getBlockTexture(BotaniaBlocks.EYE_OF_THE_ANCIENTS, "_bottom")))
								.put(TextureSlot.TOP, tex(getBlockTexture(BotaniaBlocks.EYE_OF_THE_ANCIENTS, "_top")))
								.put(TextureSlot.NORTH, tex(getBlockTexture(BotaniaBlocks.EYE_OF_THE_ANCIENTS, "_north")))
								.put(TextureSlot.SOUTH, tex(getBlockTexture(BotaniaBlocks.EYE_OF_THE_ANCIENTS, "_south")))
								.put(TextureSlot.WEST, tex(getBlockTexture(BotaniaBlocks.EYE_OF_THE_ANCIENTS, "_west")))
								.put(TextureSlot.EAST, tex(getBlockTexture(BotaniaBlocks.EYE_OF_THE_ANCIENTS, "_east"))),
						this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.EYE_OF_THE_ANCIENTS);

		var plateFile = getModelLocation(BotaniaBlocks.INCENSE_PLATE);
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(
				BotaniaBlocks.INCENSE_PLATE, mv(plateFile))
				.with(BlockModelGeneratorsAccessor.botania_getRotationHorizontalFacing()));
		remainingBlocks.remove(BotaniaBlocks.INCENSE_PLATE);

		var fourHighBottomTopTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/four_high_bottom_top")),
				Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
		singleVariantBlockState(
				BotaniaBlocks.LUMINIZER_LAUNCHER, fourHighBottomTopTemplate.create(
						BotaniaBlocks.LUMINIZER_LAUNCHER,
						new TextureMapping()
								.put(TextureSlot.BOTTOM, tex(getBlockTexture(BotaniaBlocks.LUMINIZER_LAUNCHER, "_end")))
								.put(TextureSlot.TOP, tex(getBlockTexture(BotaniaBlocks.LUMINIZER_LAUNCHER, "_end")))
								.put(TextureSlot.SIDE, tex(getBlockTexture(BotaniaBlocks.LUMINIZER_LAUNCHER, "_side"))),
						this.modelOutput
				));
		remainingBlocks.remove(BotaniaBlocks.LUMINIZER_LAUNCHER);

		singleVariantBlockState(
				BotaniaBlocks.OPEN_CRATE,
				crateTemplate.create(
						BotaniaBlocks.OPEN_CRATE, new TextureMapping()
								.put(TextureSlot.SIDE, tex(getBlockTexture(BotaniaBlocks.OPEN_CRATE)))
								.put(TextureSlot.BOTTOM, tex(getBlockTexture(BotaniaBlocks.OPEN_CRATE, "_bottom"))),
						this.modelOutput
				));
		remainingBlocks.remove(BotaniaBlocks.OPEN_CRATE);

		var threeHighBottomTopTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/three_high_bottom_top")),
				Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
		singleVariantBlockState(
				BotaniaBlocks.SPARK_TINKERER, threeHighBottomTopTemplate.create(
						BotaniaBlocks.SPARK_TINKERER,
						TextureMapping.cubeBottomTop(BotaniaBlocks.SPARK_TINKERER),
						this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.SPARK_TINKERER);

		singleVariantBlockState(
				BotaniaBlocks.STARFIELD_CREATOR, fourHighBottomTopTemplate.create(
						BotaniaBlocks.STARFIELD_CREATOR,
						TextureMapping.cubeBottomTop(BotaniaBlocks.STARFIELD_CREATOR), this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.STARFIELD_CREATOR);

		singleVariantBlockState(
				BotaniaBlocks.TERRESTRIAL_AGGLOMERATION_PLATE, threeHighBottomTopTemplate.create(
						BotaniaBlocks.TERRESTRIAL_AGGLOMERATION_PLATE,
						TextureMapping.cubeBottomTop(BotaniaBlocks.TERRESTRIAL_AGGLOMERATION_PLATE), this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.TERRESTRIAL_AGGLOMERATION_PLATE);

		var tenByTenAllTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/tenbyten_all")),
				Optional.empty(),
				TextureSlot.ALL);
		singleVariantBlockState(
				BotaniaBlocks.TINY_PLANET, tenByTenAllTemplate.create(
						BotaniaBlocks.TINY_PLANET,
						TextureMapping.cube(BotaniaBlocks.TINY_PLANET), this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.TINY_PLANET);

		singleVariantBlockState(
				BotaniaBlocks.SPREADER_TURNTABLE, ModelTemplates.CUBE_BOTTOM_TOP.create(
						BotaniaBlocks.SPREADER_TURNTABLE,
						TextureMapping.cubeBottomTop(BotaniaBlocks.SPREADER_TURNTABLE),
						this.modelOutput
				));
		remainingBlocks.remove(BotaniaBlocks.SPREADER_TURNTABLE);

		Identifier[] topTexs = new Identifier[6];
		Identifier[] sideTexs = new Identifier[6];
		Identifier[] topStrippedTexs = new Identifier[6];
		Identifier[] sideStrippedTexs = new Identifier[6];
		Identifier[] sideGlimmeringTexs = new Identifier[6];
		Identifier[] sideGlimmeringStrippedTexs = new Identifier[6];
		Identifier[] logModels = new Identifier[6];
		Identifier[] strippedLogModels = new Identifier[6];

		for (int i = 0; i < 6; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			sideTexs[i] = getBlockTexture(BotaniaBlocks.DREAMWOOD_LOG, suffix);
			topTexs[i] = getBlockTexture(BotaniaBlocks.DREAMWOOD_LOG, "_top");
			sideStrippedTexs[i] = getBlockTexture(BotaniaBlocks.STRIPPED_DREAMWOOD_LOG, suffix);
			topStrippedTexs[i] = getBlockTexture(BotaniaBlocks.STRIPPED_DREAMWOOD_LOG, "_top");
			sideGlimmeringTexs[i] = getBlockTexture(BotaniaBlocks.GLIMMERING_DREAMWOOD_LOG, suffix);
			sideGlimmeringStrippedTexs[i] = getBlockTexture(BotaniaBlocks.STRIPPED_GLIMMERING_DREAMWOOD_LOG, suffix);
			logModels[i] = getModelLocation(BotaniaBlocks.DREAMWOOD, suffix);
			strippedLogModels[i] = getModelLocation(BotaniaBlocks.STRIPPED_DREAMWOOD, suffix);
		}

		pillarWithVariants(remainingBlocks, BotaniaBlocks.DREAMWOOD_LOG, topTexs, sideTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.DREAMWOOD, sideTexs, sideTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_DREAMWOOD_LOG, topStrippedTexs, sideStrippedTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_DREAMWOOD, sideStrippedTexs, sideStrippedTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.GLIMMERING_DREAMWOOD_LOG, topTexs, sideGlimmeringTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.GLIMMERING_DREAMWOOD, sideGlimmeringTexs, sideGlimmeringTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_GLIMMERING_DREAMWOOD_LOG,
				topStrippedTexs, sideGlimmeringStrippedTexs);
		pillarWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_GLIMMERING_DREAMWOOD,
				sideGlimmeringStrippedTexs, sideGlimmeringStrippedTexs);

		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.DREAMWOOD_STAIRS, sideTexs, sideTexs, sideTexs);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_DREAMWOOD_STAIRS,
				sideStrippedTexs, sideStrippedTexs, sideStrippedTexs);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.DREAMWOOD_SLAB, logModels, sideTexs, sideTexs, sideTexs);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_DREAMWOOD_SLAB,
				strippedLogModels, sideStrippedTexs, sideStrippedTexs, sideStrippedTexs);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.DREAMWOOD_WALL, sideTexs);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.STRIPPED_DREAMWOOD_WALL, sideStrippedTexs);

		pillar(remainingBlocks, BotaniaBlocks.LIVINGWOOD_LOG,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG, "_top"),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.LIVINGWOOD,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG,
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG, "_top"),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.STRIPPED_LIVINGWOOD,
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG, "_top"),
				getBlockTexture(BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.GLIMMERING_LIVINGWOOD,
				getBlockTexture(BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.STRIPPED_GLIMMERING_LIVINGWOOD_LOG,
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG, "_top"),
				getBlockTexture(BotaniaBlocks.STRIPPED_GLIMMERING_LIVINGWOOD_LOG));
		pillar(remainingBlocks, BotaniaBlocks.STRIPPED_GLIMMERING_LIVINGWOOD,
				getBlockTexture(BotaniaBlocks.STRIPPED_GLIMMERING_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.STRIPPED_GLIMMERING_LIVINGWOOD_LOG));

		pillarAlt(remainingBlocks, BotaniaBlocks.FRAMED_LIVINGWOOD,
				getBlockTexture(BotaniaBlocks.PATTERN_FRAMED_LIVINGWOOD),
				getBlockTexture(BotaniaBlocks.FRAMED_LIVINGWOOD));
		pillarAlt(remainingBlocks, BotaniaBlocks.FRAMED_DREAMWOOD,
				getBlockTexture(BotaniaBlocks.PATTERN_FRAMED_DREAMWOOD),
				getBlockTexture(BotaniaBlocks.FRAMED_DREAMWOOD));

		stairsBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_STAIRS,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		stairsBlock(remainingBlocks, BotaniaBlocks.STRIPPED_LIVINGWOOD_STAIRS,
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG));
		slabBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_SLAB,
				getModelLocation(BotaniaBlocks.LIVINGWOOD),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		slabBlock(remainingBlocks, BotaniaBlocks.STRIPPED_LIVINGWOOD_SLAB,
				getModelLocation(BotaniaBlocks.STRIPPED_LIVINGWOOD),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG),
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG));
		wallBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_WALL,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		wallBlock(remainingBlocks, BotaniaBlocks.STRIPPED_LIVINGWOOD_WALL,
				getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG));

		fenceBlock(remainingBlocks, BotaniaBlocks.DREAMWOOD_FENCE, getBlockTexture(BotaniaBlocks.DREAMWOOD_PLANKS));
		fenceGateBlock(remainingBlocks, BotaniaBlocks.DREAMWOOD_FENCE_GATE,
				getBlockTexture(BotaniaBlocks.DREAMWOOD_PLANKS));
		specialDoorBlock(remainingBlocks, BotaniaBlocks.DREAMWOOD_DOOR);
		specialTrapdoorBlock(remainingBlocks, BotaniaBlocks.DREAMWOOD_TRAPDOOR);
		buttonBlock(remainingBlocks, BotaniaBlocks.DREAMWOOD_BUTTON, getBlockTexture(BotaniaBlocks.DREAMWOOD_PLANKS));
		pressurePlateBlock(remainingBlocks, BotaniaBlocks.DREAMWOOD_PRESSURE_PLATE,
				getBlockTexture(BotaniaBlocks.DREAMWOOD_PLANKS));
		sign(remainingBlocks, BotaniaBlocks.DREAMWOOD_PLANKS,
				BotaniaBlocks.DREAMWOOD_SIGN,
				BotaniaBlocks.DREAMWOOD_WALL_SIGN);
		hangingSign(remainingBlocks, BotaniaBlocks.STRIPPED_DREAMWOOD_LOG,
				BotaniaBlocks.DREAMWOOD_HANGING_SIGN,
				BotaniaBlocks.DREAMWOOD_WALL_HANGING_SIGN);

		fenceBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_FENCE, getBlockTexture(BotaniaBlocks.LIVINGWOOD_PLANKS));
		fenceGateBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_FENCE_GATE,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_PLANKS));
		specialDoorBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_DOOR);
		specialTrapdoorBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_TRAPDOOR);
		buttonBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_BUTTON, getBlockTexture(BotaniaBlocks.LIVINGWOOD_PLANKS));
		pressurePlateBlock(remainingBlocks, BotaniaBlocks.LIVINGWOOD_PRESSURE_PLATE,
				getBlockTexture(BotaniaBlocks.LIVINGWOOD_PLANKS));
		sign(remainingBlocks, BotaniaBlocks.LIVINGWOOD_PLANKS,
				BotaniaBlocks.LIVINGWOOD_SIGN,
				BotaniaBlocks.LIVINGWOOD_WALL_SIGN);
		hangingSign(remainingBlocks, BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG,
				BotaniaBlocks.LIVINGWOOD_HANGING_SIGN,
				BotaniaBlocks.LIVINGWOOD_WALL_HANGING_SIGN);

		fenceBlock(remainingBlocks, BotaniaBlocks.SHIMMERWOOD_FENCE, getBlockTexture(BotaniaBlocks.SHIMMERWOOD_PLANKS));
		fenceGateBlock(remainingBlocks, BotaniaBlocks.SHIMMERWOOD_FENCE_GATE,
				getBlockTexture(BotaniaBlocks.SHIMMERWOOD_PLANKS));
		buttonBlock(remainingBlocks, BotaniaBlocks.SHIMMERWOOD_BUTTON,
				getBlockTexture(BotaniaBlocks.SHIMMERWOOD_PLANKS));
		pressurePlateBlock(remainingBlocks, BotaniaBlocks.SHIMMERWOOD_PRESSURE_PLATE,
				getBlockTexture(BotaniaBlocks.SHIMMERWOOD_PLANKS));

		wallBlock(remainingBlocks, BotaniaBlocks.CORPOREA_WALL, getBlockTexture(BotaniaBlocks.CORPOREA_BLOCK));
		buttonBlock(remainingBlocks, BotaniaBlocks.CORPOREA_BUTTON, getBlockTexture(BotaniaBlocks.CORPOREA_BLOCK));
		pressurePlateBlock(remainingBlocks, BotaniaBlocks.CORPOREA_PRESSURE_PLATE,
				getBlockTexture(BotaniaBlocks.CORPOREA_BLOCK));

		rotatedMirrored(remainingBlocks, BotaniaBlocks.LIVINGROCK, getBlockTexture(BotaniaBlocks.LIVINGROCK));

		Identifier polishedLivingrockTexture = getBlockTexture(BotaniaBlocks.POLISHED_LIVINGROCK);
		Identifier polishedLivingrockSlabSideTexture = getBlockTexture(BotaniaBlocks.POLISHED_LIVINGROCK_SLAB);
		Identifier polishedLivingrockSlabDoubleModel = ModelTemplates.CUBE_COLUMN.create(
				getModelLocation(BotaniaBlocks.POLISHED_LIVINGROCK_SLAB, "_double"),
				new TextureMapping()
						.put(TextureSlot.SIDE, tex(polishedLivingrockSlabSideTexture))
						.put(TextureSlot.END, tex(polishedLivingrockTexture)),
				this.modelOutput
		);
		slabBlock(remainingBlocks, BotaniaBlocks.POLISHED_LIVINGROCK_SLAB,
				polishedLivingrockSlabDoubleModel, polishedLivingrockSlabSideTexture,
				polishedLivingrockTexture, polishedLivingrockTexture);

		var conjurationTexture = getBlockTexture(BotaniaBlocks.CONJURATION_CATALYST);
		var conjurationMirrored = getBlockTexture(BotaniaBlocks.CONJURATION_CATALYST, "_mirrored");
		checkeredBlockWithBlockstate(remainingBlocks, BotaniaBlocks.CONJURATION_CATALYST,
				conjurationTexture, conjurationMirrored);

		// block entities with only particles
		particleOnly(remainingBlocks, BotaniaBlocks.ANIMATED_TORCH, getBlockTexture(Blocks.REDSTONE_TORCH));
		particleOnly(remainingBlocks, BotaniaBlocks.LIVINGWOOD_AVATAR, getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		particleOnly(remainingBlocks, BotaniaBlocks.MANATIDE_BELLOWS, getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG));
		particleOnly(remainingBlocks, BotaniaBlocks.BOTANICAL_BREWERY, getBlockTexture(BotaniaBlocks.LIVINGROCK));
		particleOnly(remainingBlocks, BotaniaBlocks.CORPOREA_INDEX, getBlockTexture(BotaniaBlocks.CORPOREA_BLOCK));
		particleOnly(remainingBlocks, BotaniaBlocks.DETECTOR_LUMINIZER, getBlockTexture(BotaniaBlocks.DETECTOR_LUMINIZER));
		// PORT-26.3: every model names a particle texture now; vanilla's air model uses missingno
		singleVariantBlockState(BotaniaBlocks.FAKE_AIR, ModelTemplates.PARTICLE_ONLY.create(BotaniaBlocks.FAKE_AIR,
				TextureMapping.particle(tex(Identifier.withDefaultNamespace("missingno"))), this.modelOutput));
		remainingBlocks.remove(BotaniaBlocks.FAKE_AIR);
		particleOnly(remainingBlocks, BotaniaBlocks.FORK_LUMINIZER, getBlockTexture(BotaniaBlocks.FORK_LUMINIZER));
		particleOnly(remainingBlocks, BotaniaBlocks.GAIA_HEAD, getBlockTexture(Blocks.SOUL_SAND));
		particleOnly(remainingBlocks, BotaniaBlocks.GAIA_WALL_HEAD_BLOCK, getBlockTexture(Blocks.SOUL_SAND));
		particleOnly(remainingBlocks, BotaniaBlocks.GAIA_PYLON, getBlockTexture(BotaniaBlocks.ELEMENTIUM_BLOCK));
		particleOnly(remainingBlocks, BotaniaBlocks.HOVERING_HOURGLASS, getBlockTexture(BotaniaBlocks.MANAGLASS));
		particleOnly(remainingBlocks, BotaniaBlocks.LUMINIZER, getBlockTexture(BotaniaBlocks.LUMINIZER));
		particleOnly(remainingBlocks, BotaniaBlocks.MANA_FLAME, Identifier.withDefaultNamespace("block/fire_0"));
		particleOnly(remainingBlocks, BotaniaBlocks.MANA_PYLON, getBlockTexture(BotaniaBlocks.MANASTEEL_BLOCK));
		particleOnly(remainingBlocks, BotaniaBlocks.NATURA_PYLON, getBlockTexture(BotaniaBlocks.TERRASTEEL_BLOCK));
		particleOnly(remainingBlocks, BotaniaBlocks.TERU_TERU_BOZU, getBlockTexture(Blocks.WOOL.pick(DyeColor.WHITE)));
		particleOnly(remainingBlocks, BotaniaBlocks.TOGGLE_LUMINIZER, getBlockTexture(BotaniaBlocks.TOGGLE_LUMINIZER));

		// Block groups
		Predicate<Block> flowers = block -> block instanceof SpecialFlowerBlock
				|| block instanceof ShimmeringMushroomBlock
				|| block instanceof BotaniaFlowerBlock;
		ModelTemplate crossTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/cross")), Optional.empty(), TextureSlot.CROSS);
		takeAll(remainingBlocks, flowers).forEach(block -> singleVariantBlockState(block,
				crossTemplate.create(block, TextureMapping.cross(block), this.modelOutput)));

		takeAll(remainingBlocks, block -> block instanceof FlowerMotifBlock).forEach(block -> {
			String name = BuiltInRegistries.BLOCK.getKey(block).getPath().replace("_motif", "");
			singleVariantBlockState(block, crossTemplate.create(block, new TextureMapping()
					.put(TextureSlot.CROSS, tex(botaniaRL("block/" + name))),
					this.modelOutput));
		});

		takeAll(remainingBlocks,
				BotaniaBlocks.CORPOREA_FUNNEL, BotaniaBlocks.CORPOREA_INTERCEPTOR, BotaniaBlocks.CORPOREA_RETAINER)
				.forEach(block -> singleVariantBlockState(block, ModelTemplates.CUBE_COLUMN.create(block,
						TextureMapping.column(tex(getBlockTexture(block, "_side")), tex(getBlockTexture(block, "_end"))),
						this.modelOutput)));

		var drumModelTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/drum")), Optional.empty(),
				TextureSlot.TOP, TextureSlot.SIDE);
		takeAll(remainingBlocks,
				BotaniaBlocks.DRUM_OF_THE_GATHERING, BotaniaBlocks.DRUM_OF_THE_CANOPY, BotaniaBlocks.DRUM_OF_THE_WILD)
				.forEach(block -> singleVariantBlockState(block, drumModelTemplate.create(block,
						new TextureMapping()
								.put(TextureSlot.TOP, tex(botaniaRL("block/drum_top")))
								.put(TextureSlot.SIDE, tex(getBlockTexture(block))),
						this.modelOutput)));

		var outsideSlot = TextureSlotAccessor.botania_create("outside");
		var coreSlot = TextureSlotAccessor.botania_create("core");
		var spreaderTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/spreader")), Optional.empty(),
				TextureSlot.SIDE, TextureSlot.BACK, TextureSlot.INSIDE, outsideSlot);
		var coveredSpreaderTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/spreader_covered")), Optional.empty(),
				TextureSlot.INSIDE, outsideSlot, TextureSlot.WOOL);
		var spreaderCoreTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/spreader_core")), Optional.of("_core"),
				coreSlot);
		var spreaderPaddingTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/spreader_padding")), Optional.empty(),
				TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.SIDE);
		var spreaderScaffoldingTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/spreader_scaffolding")), Optional.of("_scaffolding"),
				TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.BOTTOM);
		takeAll(remainingBlocks, ManaSpreaderBlock.class::isInstance).forEach(block -> {
			ManaSpreaderBlock spreaderBlock = (ManaSpreaderBlock) block;
			ManaSpreaderBlock baseBlock = ManaSpreaderBlock.getBaseBlock(spreaderBlock);
			Identifier outside;
			if (baseBlock == BotaniaBlocks.PULSE_MANA_SPREADER || baseBlock == BotaniaBlocks.MANA_SPREADER) {
				outside = getBlockTexture(BotaniaBlocks.LIVINGWOOD_LOG);
			} else if (baseBlock == BotaniaBlocks.ELVEN_MANA_SPREADER) {
				outside = getBlockTexture(BotaniaBlocks.DREAMWOOD_LOG, "_3");
			} else {
				outside = getBlockTexture(baseBlock, "_outside");
			}
			Identifier inside;
			if (baseBlock == BotaniaBlocks.PULSE_MANA_SPREADER || baseBlock == BotaniaBlocks.MANA_SPREADER) {
				inside = getBlockTexture(BotaniaBlocks.STRIPPED_LIVINGWOOD_LOG);
			} else if (baseBlock == BotaniaBlocks.ELVEN_MANA_SPREADER) {
				inside = getBlockTexture(BotaniaBlocks.STRIPPED_DREAMWOOD_LOG, "_3");
			} else {
				inside = getBlockTexture(baseBlock, "_inside");
			}
			DyeColor coverColor = spreaderBlock.getCoverColor();
			if (coverColor != null) {
				if (spreaderBlock.isRainbowRendered()) {
					// Gaia spreader needs to be rendered in two parts to not apply the color effect to the cover
					singleVariantBlockState(block, ModelLocationUtils.getModelLocation(baseBlock));
				} else {
					singleVariantBlockState(block, coveredSpreaderTemplate.create(block, new TextureMapping()
							.put(TextureSlot.INSIDE, tex(inside))
							.put(outsideSlot, tex(outside))
							.put(TextureSlot.WOOL,
									tex(TextureMapping.getBlockTexture(ColorHelper.WOOL_MAP.apply(coverColor)))),
							this.modelOutput));
				}
			} else {
				singleVariantBlockState(block, spreaderTemplate.create(block, new TextureMapping()
						.put(TextureSlot.SIDE, tex(getBlockTexture(block, "_side")))
						.put(TextureSlot.BACK, tex(getBlockTexture(block, "_back")))
						.put(TextureSlot.INSIDE, tex(inside))
						.put(outsideSlot, tex(outside)), this.modelOutput));
				spreaderCoreTemplate.create(block, new TextureMapping()
						.put(coreSlot, tex(getBlockTexture(block, "_core"))), this.modelOutput);
				Block sb = block == BotaniaBlocks.PULSE_MANA_SPREADER ? BotaniaBlocks.MANA_SPREADER : block;
				spreaderScaffoldingTemplate.create(block, new TextureMapping()
						.put(TextureSlot.TOP, tex(getBlockTexture(sb, "_scaffolding_top")))
						.put(TextureSlot.SIDE, tex(getBlockTexture(sb, "_scaffolding_side")))
						.put(TextureSlot.BOTTOM, tex(getBlockTexture(sb, "_scaffolding_bottom"))), this.modelOutput);
			}
		});
		ColorHelper.supportedColors().forEach(color -> {
			Block wool = ColorHelper.WOOL_MAP.apply(color);
			spreaderPaddingTemplate.create(botaniaRL("block/" + color.getName() + "_spreader_padding"),
					new TextureMapping()
							.put(TextureSlot.FRONT, tex(getBlockTexture(wool)))
							.put(TextureSlot.BACK, tex(getBlockTexture(wool)))
							.put(TextureSlot.SIDE, tex(getBlockTexture(wool))),
					this.modelOutput);
		});

		TextureSlot[] manaPoolSlots = new TextureSlot[] {
				TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.INSIDE
		};
		TextureSlot[] manaPoolFullSlots = new TextureSlot[] {
				TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.INSIDE, TextureSlot.CONTENT
		};
		takeAll(remainingBlocks, block -> block instanceof ManaPoolBlock)
				.forEach(block -> {
					ManaPoolBlock poolBlock = (ManaPoolBlock) block;
					ManaPoolBlock blockForTexture = poolBlock.fabulous
							? BotaniaBlocks.findOptionallyDyedBlock(BotaniaBlocks.MANA_POOL, poolBlock.color)
							: poolBlock;
					ManaPoolBlock baseBlock = ManaPoolBlock.getUndyedBlock(blockForTexture);
					String dyedPrefix = poolBlock.color != null ? "dyed_" : "";
					Identifier side = getBlockTexture(baseBlock, "_side", dyedPrefix);
					Identifier top = getBlockTexture(baseBlock, "_top", dyedPrefix);
					Identifier bottom = getBlockTexture(baseBlock, "_bottom", dyedPrefix);
					Identifier inside = getBlockTexture(baseBlock, "_inside", dyedPrefix);
					Identifier blockModelTemplateKey = BuiltInRegistries.BLOCK.getKey(baseBlock)
							.withPrefix("block/shapes/");
					ModelTemplate template = new ModelTemplate(Optional.of(blockModelTemplateKey), Optional.empty(),
							manaPoolSlots);
					TextureMapping mapping = new TextureMapping()
							.put(TextureSlot.SIDE, tex(side))
							.put(TextureSlot.TOP, tex(top))
							.put(TextureSlot.BOTTOM, tex(bottom))
							.put(TextureSlot.INSIDE, tex(inside));

					singleVariantBlockState(block, template.create(block, mapping, this.modelOutput));

					if (poolBlock.creative || poolBlock.color == null) {
						Identifier blockModelFullTemplateKey = blockModelTemplateKey.withSuffix("_full");
						ModelTemplate fullTemplate = new ModelTemplate(
								Optional.of(blockModelFullTemplateKey),
								Optional.of("_full"), manaPoolFullSlots
						);
						fullTemplate.create(
								block, mapping.put(TextureSlot.CONTENT, tex(botaniaRL("block/mana_water"))),
								this.modelOutput
						);
					}
				});

		takeAll(remainingBlocks, BotaniaBlocks.MANA_PUMP, BotaniaBlocks.TINY_POTATO)
				.forEach(block -> this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block,
						mv(getModelLocation(block)))
						.with(BlockModelGeneratorsAccessor.botania_getRotationHorizontalFacing()))
				);

		takeAll(remainingBlocks, BotaniaBlocks.ENDER_OVERSEER, BotaniaBlocks.MANA_DETECTOR).forEach(block -> {
			var offModel = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), this.modelOutput);
			var onModel = ModelTemplates.CUBE_ALL.create(getModelLocation(block, "_powered"),
					TextureMapping.cube(tex(getBlockTexture(block, "_powered"))), this.modelOutput);
			this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block).with(
					PropertyDispatch.initial(BlockStateProperties.POWERED)
							.select(false, mv(offModel))
							.select(true, mv(plainModel(onModel)))
			));
		});

		takeAll(remainingBlocks, block -> block instanceof BotaniaGrassBlock).forEach(block -> {
			var model = ModelTemplates.CUBE_BOTTOM_TOP.create(block, new TextureMapping()
					.put(TextureSlot.SIDE, tex(getBlockTexture(block, "_side")))
					.put(TextureSlot.BOTTOM, tex(getBlockTexture(Blocks.DIRT)))
					.put(TextureSlot.TOP, tex(getBlockTexture(block, "_top"))),
					this.modelOutput
			);
			this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block, BlockModelGeneratorsAccessor.botania_createRotatedVariants(plainModel(model))));
		});

		takeAll(remainingBlocks, block -> block instanceof RedStringBlock).forEach(this::redStringBlock);

		takeAll(remainingBlocks, block -> block instanceof TallMysticalFlowerBlock).forEach(block -> {
			var bottom = ModelTemplates.CROSS.create(block, TextureMapping.cross(block), this.modelOutput);
			var top = ModelTemplates.CROSS.create(getModelLocation(block, "_top"),
					TextureMapping.cross(tex(getBlockTexture(block, "_top"))), this.modelOutput);
			this.blockStateGenerators.add(
					MultiVariantGenerator.dispatch(block)
							.with(PropertyDispatch.initial(TallFlowerBlock.HALF)
									.select(DoubleBlockHalf.LOWER, mv(bottom))
									.select(DoubleBlockHalf.UPPER, mv(plainModel(top)))
							)
			);
		});

		var mountainTextures = new Identifier[] { getBlockTexture(BotaniaBlocks.GNEISS), getBlockTexture(
				BotaniaBlocks.GNEISS, "_1") };
		var mountainModels = new Identifier[] { getModelLocation(BotaniaBlocks.GNEISS), getModelLocation(
				BotaniaBlocks.GNEISS, "_1") };
		var mountainWeights = new Integer[] { 5, 1 };
		rotatedMirroredWithVariants(remainingBlocks, BotaniaBlocks.GNEISS, mountainTextures, mountainWeights);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_STAIRS, mountainTextures, mountainTextures, mountainTextures, mountainWeights);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_SLAB, mountainModels, mountainTextures, mountainTextures, mountainTextures, mountainWeights);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_WALL, mountainTextures, mountainWeights);

		var mountainBrickTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.GNEISS_BRICKS),
				getBlockTexture(BotaniaBlocks.GNEISS_BRICKS, "_1"),
				getBlockTexture(BotaniaBlocks.GNEISS_BRICKS, "_2"),
				getBlockTexture(BotaniaBlocks.GNEISS_BRICKS, "_3"),
				getBlockTexture(BotaniaBlocks.GNEISS_BRICKS, "_4"),
				getBlockTexture(BotaniaBlocks.GNEISS_BRICKS, "_5")
		};
		var mountainBrickModels = new Identifier[] {
				getModelLocation(BotaniaBlocks.GNEISS_BRICKS),
				getModelLocation(BotaniaBlocks.GNEISS_BRICKS, "_1"),
				getModelLocation(BotaniaBlocks.GNEISS_BRICKS, "_2"),
				getModelLocation(BotaniaBlocks.GNEISS_BRICKS, "_3"),
				getModelLocation(BotaniaBlocks.GNEISS_BRICKS, "_4"),
				getModelLocation(BotaniaBlocks.GNEISS_BRICKS, "_5"),
		};
		cubeAllWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_BRICKS, mountainBrickTextures);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_BRICK_STAIRS,
				mountainBrickTextures, mountainBrickTextures, mountainBrickTextures);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_BRICK_SLAB,
				mountainBrickModels, mountainBrickTextures, mountainBrickTextures, mountainBrickTextures);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.GNEISS_BRICK_WALL, mountainBrickTextures);

		var taigaTextures = new Identifier[] { getBlockTexture(BotaniaBlocks.LUNITE), getBlockTexture(
				BotaniaBlocks.LUNITE, "_1") };
		var taigaModels = new Identifier[] { getModelLocation(BotaniaBlocks.LUNITE), getModelLocation(
				BotaniaBlocks.LUNITE, "_1") };
		rotatedMirroredWithVariants(remainingBlocks, BotaniaBlocks.LUNITE, taigaTextures);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.LUNITE_STAIRS,
				taigaTextures, taigaTextures, taigaTextures);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.LUNITE_SLAB,
				taigaModels, taigaTextures, taigaTextures, taigaTextures);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.LUNITE_WALL, taigaTextures);

		var plainsBrickSide = getBlockTexture(BotaniaBlocks.TALC_BRICKS);
		var plainsBrickTop = getBlockTexture(BotaniaBlocks.TALC_BRICKS, "_top");
		pillarAlt(remainingBlocks, BotaniaBlocks.TALC_BRICKS, plainsBrickTop, plainsBrickSide);
		stairsBlock(remainingBlocks, BotaniaBlocks.TALC_BRICK_STAIRS, plainsBrickSide, plainsBrickTop, plainsBrickTop);
		slabBlock(remainingBlocks, BotaniaBlocks.TALC_BRICK_SLAB, getModelLocation(BotaniaBlocks.TALC_BRICKS),
				plainsBrickSide, plainsBrickTop, plainsBrickTop);
		wallBlock(remainingBlocks, BotaniaBlocks.TALC_BRICK_WALL, plainsBrickSide, plainsBrickTop, plainsBrickTop);

		var forestBrickTextures = new Identifier[] { getBlockTexture(BotaniaBlocks.FUCHSITE_BRICKS),
				getBlockTexture(BotaniaBlocks.FUCHSITE_BRICKS, "_1") };
		var forestBrickModels = new Identifier[] { getModelLocation(BotaniaBlocks.FUCHSITE_BRICKS),
				getModelLocation(BotaniaBlocks.FUCHSITE_BRICKS, "_1") };
		var forestBrickWeights = new Integer[] { 2, 1 };
		cubeAllWithVariants(remainingBlocks, BotaniaBlocks.FUCHSITE_BRICKS, forestBrickTextures, forestBrickWeights);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.FUCHSITE_BRICK_STAIRS,
				forestBrickTextures, forestBrickTextures, forestBrickTextures, forestBrickWeights);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.FUCHSITE_BRICK_SLAB, forestBrickModels,
				forestBrickTextures, forestBrickTextures, forestBrickTextures, forestBrickWeights);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.FUCHSITE_BRICK_WALL, forestBrickTextures, forestBrickWeights);

		var fungalBrickTextures = new Identifier[] { getBlockTexture(BotaniaBlocks.MYCELITE_BRICKS),
				getBlockTexture(BotaniaBlocks.MYCELITE_BRICKS, "_1") };
		var fungalBrickModels = new Identifier[] { getModelLocation(BotaniaBlocks.MYCELITE_BRICKS),
				getModelLocation(BotaniaBlocks.MYCELITE_BRICKS, "_1") };
		cubeAllWithVariants(remainingBlocks, BotaniaBlocks.MYCELITE_BRICKS, fungalBrickTextures);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.MYCELITE_BRICK_STAIRS,
				fungalBrickTextures, fungalBrickTextures, fungalBrickTextures);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.MYCELITE_BRICK_SLAB,
				fungalBrickModels, fungalBrickTextures, fungalBrickTextures, fungalBrickTextures);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.MYCELITE_BRICK_WALL, fungalBrickTextures);

		var swampBrickTopTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.CATACLASITE_BRICKS, "_top"),
				getBlockTexture(BotaniaBlocks.CATACLASITE_BRICKS, "_top_1")
		};
		var swampBrickBottomTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.CATACLASITE_BRICKS, "_bottom"),
				getBlockTexture(BotaniaBlocks.CATACLASITE_BRICKS, "_bottom")
		};
		var swampBrickSideTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.CATACLASITE_BRICKS),
				getBlockTexture(BotaniaBlocks.CATACLASITE_BRICKS)
		};
		var swampBrickModels = new Identifier[] {
				getModelLocation(BotaniaBlocks.CATACLASITE_BRICKS),
				getModelLocation(BotaniaBlocks.CATACLASITE_BRICKS, "_1")
		};
		directionalPillarWithVariants(remainingBlocks, BotaniaBlocks.CATACLASITE_BRICKS,
				swampBrickTopTextures, swampBrickBottomTextures, swampBrickSideTextures);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.CATACLASITE_BRICK_STAIRS,
				swampBrickSideTextures, swampBrickBottomTextures, swampBrickTopTextures);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.CATACLASITE_BRICK_SLAB,
				swampBrickModels, swampBrickSideTextures, swampBrickBottomTextures, swampBrickTopTextures);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.CATACLASITE_BRICK_WALL,
				swampBrickSideTextures, swampBrickBottomTextures, swampBrickTopTextures);

		var swampChiseledBrickTopTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.CHISELED_CATACLASITE_BRICKS, "_top"),
				getBlockTexture(BotaniaBlocks.CHISELED_CATACLASITE_BRICKS, "_top_1")
		};
		var swampChiseledBrickBottomTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.CHISELED_CATACLASITE_BRICKS, "_bottom"),
				getBlockTexture(BotaniaBlocks.CHISELED_CATACLASITE_BRICKS, "_bottom")
		};
		var swampChiseledBrickSideTextures = new Identifier[] {
				getBlockTexture(BotaniaBlocks.CHISELED_CATACLASITE_BRICKS),
				getBlockTexture(BotaniaBlocks.CHISELED_CATACLASITE_BRICKS)
		};
		directionalPillarWithVariants(remainingBlocks, BotaniaBlocks.CHISELED_CATACLASITE_BRICKS,
				swampChiseledBrickTopTextures, swampChiseledBrickBottomTextures, swampChiseledBrickSideTextures);

		var swampCobblestoneTextures = new Identifier[] { getBlockTexture(BotaniaBlocks.COBBLED_CATACLASITE),
				getBlockTexture(BotaniaBlocks.COBBLED_CATACLASITE, "_1") };
		var swampCobblestoneModels = new Identifier[] { getModelLocation(BotaniaBlocks.COBBLED_CATACLASITE),
				getModelLocation(BotaniaBlocks.COBBLED_CATACLASITE, "_1") };
		cubeAllWithVariants(remainingBlocks, BotaniaBlocks.COBBLED_CATACLASITE, swampCobblestoneTextures);
		stairsBlockWithVariants(remainingBlocks, BotaniaBlocks.COBBLED_CATACLASITE_STAIRS,
				swampCobblestoneTextures, swampCobblestoneTextures, swampCobblestoneTextures);
		slabBlockWithVariants(remainingBlocks, BotaniaBlocks.COBBLED_CATACLASITE_SLAB,
				swampCobblestoneModels, swampCobblestoneTextures, swampCobblestoneTextures, swampCobblestoneTextures);
		wallBlockWithVariants(remainingBlocks, BotaniaBlocks.COBBLED_CATACLASITE_WALL, swampCobblestoneTextures);

		var mesaBrick = getBlockTexture(BotaniaBlocks.ROSY_TALC_BRICKS);
		var mesaBrickMirrored = getBlockTexture(BotaniaBlocks.ROSY_TALC_BRICKS, "_mirrored");
		var mesaBrickModel = checkeredBlockWithBlockstate(remainingBlocks, BotaniaBlocks.ROSY_TALC_BRICKS,
				mesaBrick, mesaBrickMirrored);
		checkeredSlabBlock(remainingBlocks, BotaniaBlocks.ROSY_TALC_BRICK_SLAB, mesaBrickModel,
				mesaBrick, mesaBrickMirrored);
		checkeredStairsBlock(remainingBlocks, BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, mesaBrick, mesaBrickMirrored);
		checkeredWallBlock(remainingBlocks, BotaniaBlocks.ROSY_TALC_BRICK_WALL, mesaBrick, mesaBrickMirrored);

		var mesaChiseledBrickSide = getBlockTexture(BotaniaBlocks.CHISELED_ROSY_TALC_BRICKS);
		var mesaChiseledBrickTop = getBlockTexture(BotaniaBlocks.CHISELED_ROSY_TALC_BRICKS, "_top");
		pillarAlt(remainingBlocks, BotaniaBlocks.CHISELED_ROSY_TALC_BRICKS, mesaChiseledBrickTop, mesaChiseledBrickSide);

		// Remaining slabs, stairs, walls are handled automatically.
		for (Block stone : new Block[] {
				BotaniaBlocks.SOLITE, BotaniaBlocks.FUCHSITE, BotaniaBlocks.MYCELITE,
				BotaniaBlocks.ROSY_TALC, BotaniaBlocks.TALC, BotaniaBlocks.CATACLASITE
		}) {
			rotatedMirrored(remainingBlocks, stone, getBlockTexture(stone));
		}

		for (String variant : LibBlockNames.QUARTZ_VARIANTS) {
			Identifier quartzId = botaniaRL(variant);
			Block quartz = BuiltInRegistries.BLOCK.getValue(quartzId.withSuffix(LibBlockNames.BLOCK_SUFFIX));
			singleVariantBlockState(quartz, ModelTemplates.CUBE_TOP.create(quartz,
					TextureMapping.cubeTop(quartz),
					this.modelOutput));

			Identifier pillarId = quartzId.withSuffix(LibBlockNames.PILLAR_SUFFIX);
			Block pillar = BuiltInRegistries.BLOCK.getValue(pillarId);
			var pillarModel = ModelTemplates.CUBE_COLUMN.create(pillar,
					TextureMapping.column(tex(getBlockTexture(pillar)), tex(getBlockTexture(pillar, "_top"))),
					this.modelOutput);
			this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createAxisAlignedPillarBlock(pillar, mv(pillarModel)));

			Identifier bricksId = quartzId.withSuffix(LibBlockNames.BRICKS_SUFFIX);
			Block bricks = BuiltInRegistries.BLOCK.getValue(bricksId);
			singleVariantBlockState(bricks, ModelTemplates.CUBE_ALL.create(bricks, TextureMapping.cube(bricks), this.modelOutput));

			Identifier chiseledId = quartzId.withPrefix(LibBlockNames.CHISELED_PREFIX).withSuffix(LibBlockNames.BLOCK_SUFFIX);
			Block chiseled = BuiltInRegistries.BLOCK.getValue(chiseledId);
			singleVariantBlockState(chiseled,
					ModelTemplates.CUBE_COLUMN.create(chiseled, new TextureMapping()
							.put(TextureSlot.SIDE, tex(getBlockTexture(chiseled)))
							.put(TextureSlot.END, tex(getBlockTexture(chiseled, "_top"))), this.modelOutput));

			Identifier smoothId = quartzId.withPrefix(LibBlockNames.SMOOTH_PREFIX);
			Block smooth = BuiltInRegistries.BLOCK.getValue(smoothId.withSuffix(LibBlockNames.BLOCK_SUFFIX));
			singleVariantBlockState(smooth, ModelTemplates.CUBE_ALL.create(smooth,
					// it's a weird vanilla thing – regular block made of "top" and "side", smooth made of "bottom"
					TextureMapping.cube(tex(getBlockTexture(quartz, "_bottom"))),
					this.modelOutput));

			remainingBlocks.remove(quartz);
			remainingBlocks.remove(pillar);
			remainingBlocks.remove(bricks);
			remainingBlocks.remove(chiseled);
			remainingBlocks.remove(smooth);
		}

		takeAll(remainingBlocks, block -> block instanceof BuriedPetalBlock).forEach(block -> {
			DyeColor color = ((BuriedPetalBlock) block).color;
			Identifier wool = Identifier.withDefaultNamespace("block/" + color.getSerializedName() + "_wool");
			particleOnly(remainingBlocks, block, wool);
		});

		var apothecaryTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/petal_apothecary")),
				Optional.empty(), TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM);
		takeAll(remainingBlocks, block -> block instanceof PetalApothecaryBlock)
				.forEach(block -> singleVariantBlockState(block,
						apothecaryTemplate.create(block, new TextureMapping()
								.put(TextureSlot.SIDE, tex(getBlockTexture(block, "_side")))
								.put(TextureSlot.TOP, tex(getBlockTexture(block, "_top")))
								.put(TextureSlot.BOTTOM, tex(getBlockTexture(block, "_bottom"))), this.modelOutput))
				);

		takeAll(remainingBlocks,
				block -> block instanceof FloatingFlowerBaseBlock
						|| block instanceof FlowerPotBlock)
				// Models generated by FloatingFlowerModelProvider or PottedPlantModelProvider
				.forEach(block -> singleVariantBlockState(block, getModelLocation(block)));

		takeAll(remainingBlocks, block -> block instanceof IronBarsBlock).forEach(block -> {
			String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
			var mapping = new TextureMapping()
					.put(TextureSlot.EDGE, tex(getBlockTexture(block)))
					.put(TextureSlot.PANE, tex(botaniaRL("block/" + name.substring(0, name.length() - "_pane".length()))));
			Identifier postModel = ModelTemplates.STAINED_GLASS_PANE_POST.create(block, mapping, this.modelOutput);
			Identifier sideModel = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(block, mapping, this.modelOutput);
			Identifier sideAltModel = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(block, mapping, this.modelOutput);
			Identifier noSideModel = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(block, mapping, this.modelOutput);
			Identifier noSideAltModel = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(block, mapping, this.modelOutput);

			// [VanillaCopy] BlockModelGenerator glass panes
			this.blockStateGenerators.add(MultiPartGenerator.multiPart(block)
					.with(mv(postModel))
					.with(condition().term(BlockStateProperties.NORTH, true),
							mv(plainModel(sideModel)))
					.with(condition().term(BlockStateProperties.EAST, true),
							mv(plainModel(sideModel)
									.withYRot(Quadrant.R90)))
					.with(condition().term(BlockStateProperties.SOUTH, true),
							mv(plainModel(sideAltModel)))
					.with(condition().term(BlockStateProperties.WEST, true),
							mv(plainModel(sideAltModel)
									.withYRot(Quadrant.R90)))
					.with(condition().term(BlockStateProperties.NORTH, false),
							mv(plainModel(noSideModel)))
					.with(condition().term(BlockStateProperties.EAST, false),
							mv(plainModel(noSideAltModel)))
					.with(condition().term(BlockStateProperties.SOUTH, false),
							mv(plainModel(noSideAltModel)
									.withYRot(Quadrant.R90)))
					.with(condition().term(BlockStateProperties.WEST, false),
							mv(plainModel(noSideModel)
									.withYRot(Quadrant.R270))));
		});

		takeAll(remainingBlocks, block -> block instanceof StairBlock).forEach(block -> {
			String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
			boolean quartz = name.contains("quartz");
			String tentativeBaseName = name.substring(0, name.length() - LibBlockNames.STAIRS_SUFFIX.length());
			String baseName = tentativeBaseName.endsWith("brick") || tentativeBaseName.endsWith("plank")
					? tentativeBaseName + "s"
					: quartz ? tentativeBaseName + LibBlockNames.BLOCK_SUFFIX : tentativeBaseName;
			boolean smooth = name.contains("smooth");
			if (quartz) {
				if (smooth) {
					var tex = botaniaRL("block/" + baseName.substring("smooth_".length()) + "_bottom");
					stairsBlock(new HashSet<>(), block, tex, tex, tex);
				} else {
					Identifier side = botaniaRL("block/" + baseName + "_side");
					Identifier bottom = botaniaRL("block/" + baseName + "_top");
					Identifier top = botaniaRL("block/" + baseName + "_top");
					stairsBlock(new HashSet<>(), block, side, bottom, top);
				}
			} else {
				var tex = botaniaRL("block/" + baseName);
				stairsBlock(new HashSet<>(), block, tex, tex, tex);
			}
		});

		takeAll(remainingBlocks, block -> block instanceof SlabBlock).forEach(slabBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(slabBlock).getPath();
			boolean quartz = name.contains("quartz");
			String tentativeBaseName = name.substring(0, name.length() - LibBlockNames.SLAB_SUFFIX.length());
			String baseName = tentativeBaseName.endsWith("brick") || tentativeBaseName.endsWith("plank")
					? tentativeBaseName + "s"
					: quartz ? tentativeBaseName + LibBlockNames.BLOCK_SUFFIX : tentativeBaseName;
			Block base = BuiltInRegistries.BLOCK.getValue(botaniaRL(baseName));
			boolean smooth = name.contains("smooth");
			if (quartz) {
				if (smooth) {
					var baseTex = botaniaRL("block/" + baseName.substring("smooth_".length()) + "_bottom");
					var doubleModel = getModelLocation(base);
					slabBlock(new HashSet<>(), slabBlock, doubleModel, baseTex, baseTex, baseTex);
				} else {
					var side = getBlockTexture(base, "_side");
					var bottom = getBlockTexture(base, "_top");
					var top = getBlockTexture(base, "_top");
					var doubleModel = getModelLocation(base);
					slabBlock(new HashSet<>(), slabBlock, doubleModel, side, bottom, top);
				}
			} else {
				var baseTex = getBlockTexture(base);
				var doubleModel = getModelLocation(base);
				slabBlock(new HashSet<>(), slabBlock, doubleModel, baseTex, baseTex, baseTex);
			}
		});

		handleStandardBlockModel(remainingBlocks, WallBlock.class, LibBlockNames.WALL_SUFFIX, this::wallBlock);
		handleStandardBlockModel(remainingBlocks, ButtonBlock.class, LibBlockNames.BUTTON_SUFFIX, this::buttonBlock);
		handleStandardBlockModel(remainingBlocks, PressurePlateBlock.class, LibBlockNames.PRESSURE_PLATE_SUFFIX, this::pressurePlateBlock);

		remainingBlocks.forEach(this::cubeAllNoRemove);
	}

	// [VanillaCopy] TextureMapping::getBlockTexture(Block, String), except also with a prefix string
	public static Identifier getBlockTexture(Block block, String textureSuffix, String texturePrefix) {
		Identifier resourcelocation = BuiltInRegistries.BLOCK.getKey(block);
		return resourcelocation.withPath(path -> "block/" + texturePrefix + path + textureSuffix);
	}

	public static Identifier getBlockTexture(Block block, String textureSuffix) {
		return TextureMapping.getBlockTexture(block, textureSuffix).sprite();
	}

	public static Identifier getBlockTexture(Block block) {
		return TextureMapping.getBlockTexture(block).sprite();
	}

	private <T extends Block> void handleStandardBlockModel(Set<Block> remainingBlocks, Class<T> blockClass,
			String suffix, TriConsumer<Set<Block>, Block, Identifier> modelBuilder) {
		takeAll(remainingBlocks, blockClass::isInstance).forEach(block -> {
			String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
			String tentativeBaseName = name.substring(0, name.length() - suffix.length());
			String baseName = tentativeBaseName.endsWith("brick") || tentativeBaseName.endsWith("plank") ? tentativeBaseName + "s" : tentativeBaseName;
			Block base = BuiltInRegistries.BLOCK.getValue(botaniaRL(baseName));
			if (base == Blocks.AIR) {
				BotaniaAPI.LOGGER.error("Invalid base block name {} for {}", baseName, name);
			}
			var baseTexture = getBlockTexture(base);
			modelBuilder.accept(new HashSet<>(), block, baseTexture);
		});
	}

	protected void particleOnly(Set<Block> blocks, Block block, Identifier particle) {
		singleVariantBlockState(block, ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(tex(particle)), this.modelOutput));
		blocks.remove(block);
	}

	protected void manualModel(Set<Block> blocks, Block block) {
		singleVariantBlockState(block, getModelLocation(block));
		blocks.remove(block);
	}

	protected void stairsBlock(Set<Block> blocks, Block block, Identifier sideTex, Identifier bottomTex,
			Identifier topTex) {
		stairsBlockWithVariants(blocks, block,
				new Identifier[] { sideTex },
				new Identifier[] { bottomTex },
				new Identifier[] { topTex });
	}

	protected void checkeredStairsBlock(Set<Block> blocks, Block block, Identifier texture,
			Identifier mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate =
				(model, suffix) -> new ModelTemplate(
						Optional.of(botaniaRL("block/shapes/" + model)), suffix,
						TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, tex(texture)).put(TextureSlot.NORTH, tex(mirroredTexture));

		var checkeredStairsModel = checkeredTemplate.apply("stairs_checkered",
				Optional.empty()).create(BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, checkeredMapping, this.modelOutput);
		var checkeredStairsModelRot90 = checkeredTemplate.apply("stairs_checkered_90deg",
				Optional.of("_90deg")).create(BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, checkeredMapping, this.modelOutput);
		var checkeredStairsOuterModel = checkeredTemplate.apply("stairs_outer_checkered",
				Optional.of("_outer")).create(BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, checkeredMapping, this.modelOutput);
		var checkeredStairsOuterModelRot90 = checkeredTemplate.apply("stairs_outer_checkered_90deg",
				Optional.of("_outer_90deg")).create(BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, checkeredMapping, this.modelOutput);
		var checkeredStairsInnerModel = checkeredTemplate.apply("stairs_inner_checkered",
				Optional.of("_inner")).create(BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, checkeredMapping, this.modelOutput);
		var checkeredStairsInnerModelRot90 = checkeredTemplate.apply("stairs_inner_checkered_90deg",
				Optional.of("_inner_90deg")).create(BotaniaBlocks.ROSY_TALC_BRICK_STAIRS, checkeredMapping, this.modelOutput);

		stairsBlockWithModels(blocks, block, checkeredStairsInnerModel, checkeredStairsInnerModelRot90,
				checkeredStairsModel, checkeredStairsModelRot90, checkeredStairsOuterModel, checkeredStairsOuterModelRot90);
	}

	protected void stairsBlockWithVariants(Set<Block> blocks, Block block, Identifier[] sideTextures,
			Identifier[] bottomTextures, Identifier[] topTextures) {
		var weights = new Integer[sideTextures.length];
		Arrays.fill(weights, 1);
		stairsBlockWithVariants(blocks, block, sideTextures, bottomTextures, topTextures, weights);
	}

	protected void stairsBlockWithVariants(Set<Block> blocks, Block block, Identifier[] sideTextures,
			Identifier[] bottomTextures, Identifier[] topTextures, Integer[] weights) {
		int length = sideTextures.length;
		if (length != topTextures.length || length != bottomTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] innerModels = new Identifier[length];
		Identifier[] straightModels = new Identifier[length];
		Identifier[] outerModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			var mapping = new TextureMapping()
					.put(TextureSlot.SIDE, tex(sideTextures[i]))
					.put(TextureSlot.BOTTOM, tex(bottomTextures[i]))
					.put(TextureSlot.TOP, tex(topTextures[i]));
			Identifier modelIdInner = getModelLocation(block, "_inner" + suffix);
			Identifier modelIdStraight = getModelLocation(block, suffix);
			Identifier modelIdOuter = getModelLocation(block, "_outer" + suffix);
			innerModels[i] = ModelTemplates.STAIRS_INNER.create(modelIdInner, mapping, this.modelOutput);
			straightModels[i] = ModelTemplates.STAIRS_STRAIGHT.create(modelIdStraight, mapping, this.modelOutput);
			outerModels[i] = ModelTemplates.STAIRS_OUTER.create(modelIdOuter, mapping, this.modelOutput);
		}
		stairsBlockWithModels(blocks, block, innerModels, straightModels, outerModels, weights);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier[] innerModels,
			Identifier[] straightModels, Identifier[] outerModels, Integer[] weights) {
		stairsBlockWithModels(blocks, block, innerModels, straightModels, outerModels, weights, true);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier innerModel,
			Identifier innerModelRot90, Identifier straightModel, Identifier straightModelRot90,
			Identifier outerModel, Identifier outerModelRot90) {
		stairsBlockWithModels(blocks, block,
				new Identifier[] { innerModel },
				new Identifier[] { innerModelRot90 },
				new Identifier[] { straightModel },
				new Identifier[] { straightModelRot90 },
				new Identifier[] { outerModel },
				new Identifier[] { outerModelRot90 },
				new Integer[] { 1 },
				true);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier[] innerModels,
			Identifier[] straightModels, Identifier[] outerModels, Integer[] weights, Boolean uvlock) {
		stairsBlockWithModels(blocks, block,
				innerModels, innerModels,
				straightModels, straightModels,
				outerModels, outerModels,
				weights, uvlock);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier[] innerModels,
			Identifier[] innerModelsRot90, Identifier[] straightModels,
			Identifier[] straightModelsRot90, Identifier[] outerModels, Identifier[] outerModelsRot90,
			Integer[] weights, Boolean uvlock) {
		int length = innerModels.length;
		if (length != straightModels.length || length != outerModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var propertyDispatch = PropertyDispatch.initial(
				BlockStateProperties.HORIZONTAL_FACING,
				BlockStateProperties.HALF,
				BlockStateProperties.STAIRS_SHAPE
		);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			for (Half half : Half.values()) {
				for (StairsShape stairsShape : StairsShape.values()) {
					// Stair blockstates are super weird. If it's left and bottom, you need to rotate it 90deg ccw compared to
					// usual, and if it's right and top, you need to rotate it 90deg cw. This is the cleanest way I could think
					// of to do that.

					// Additionally, we're using an alternate model for everything that's rotated 90/270 degrees to be able to
					// have a consistent "checkered" texture for some stairs. Normal stairs just provide the same model twice
					// which makes no impact in the generated blockstate.
					boolean isLeft = stairsShape == StairsShape.INNER_LEFT || stairsShape == StairsShape.OUTER_LEFT;
					boolean isRight = stairsShape == StairsShape.INNER_RIGHT || stairsShape == StairsShape.OUTER_RIGHT;
					int rotationOffset = isLeft && half == Half.BOTTOM ? -1 : isRight && half == Half.TOP ? 1 : 0;

					Quadrant[] rotations = Quadrant.values();
					Quadrant yRot = switch (direction) {
						case EAST -> rotations[(4 + rotationOffset) % 4];
						case WEST -> rotations[(2 + rotationOffset) % 4];
						case SOUTH -> rotations[(1 + rotationOffset) % 4];
						case NORTH -> rotations[(3 + rotationOffset) % 4];
						default -> throw new IllegalStateException();
					};
					Quadrant xRot = switch (half) {
						case BOTTOM -> Quadrant.R0;
						case TOP -> Quadrant.R180;
					};
					boolean rotatedModel = yRot == Quadrant.R90 || yRot == Quadrant.R270;
					Identifier[] models = switch (stairsShape) {
						case STRAIGHT -> rotatedModel ? straightModelsRot90 : straightModels;
						case OUTER_RIGHT, OUTER_LEFT -> rotatedModel ? outerModelsRot90 : outerModels;
						case INNER_RIGHT, INNER_LEFT -> rotatedModel ? innerModelsRot90 : innerModels;
					};
					var indices = IntStream.range(0, length).boxed();
					propertyDispatch.select(direction, half, stairsShape,
							mv(indices.map(i -> maybeWeight(weights[i], maybeUVLock(uvlock, maybeYRot(yRot,
									maybeXRot(xRot, plainModel(models[i]))
							)))).toList()));
				}
			}
		}
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block).with(propertyDispatch));
		blocks.remove(block);
	}

	protected void slabBlock(Set<Block> blocks, Block block, Identifier doubleModel, Identifier side,
			Identifier bottom, Identifier top) {
		slabBlockWithVariants(blocks, block,
				new Identifier[] { doubleModel },
				new Identifier[] { side },
				new Identifier[] { bottom },
				new Identifier[] { top });
	}

	protected void checkeredSlabBlock(Set<Block> blocks, Block block, Identifier doubleModel,
			Identifier texture, Identifier mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate =
				(model, suffix) -> new ModelTemplate(
						Optional.of(botaniaRL("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, tex(texture)).put(TextureSlot.NORTH, tex(mirroredTexture));

		var slabModel = checkeredTemplate.apply("slab_checkered",
				Optional.empty()).create(BotaniaBlocks.ROSY_TALC_BRICK_SLAB, checkeredMapping, this.modelOutput);
		var slabTopModel = checkeredTemplate.apply("slab_top_checkered",
				Optional.of("_top")).create(BotaniaBlocks.ROSY_TALC_BRICK_SLAB, checkeredMapping, this.modelOutput);
		slabBlockWithModels(blocks, block, slabModel, slabTopModel, doubleModel);
	}

	protected void slabBlockWithVariants(Set<Block> blocks, Block block, Identifier[] doubleModels,
			Identifier[] sideTextures, Identifier[] bottomTextures, Identifier[] topTextures) {
		var weights = new Integer[sideTextures.length];
		Arrays.fill(weights, 1);
		slabBlockWithVariants(blocks, block, doubleModels, sideTextures, bottomTextures, topTextures, weights);
	}

	protected void slabBlockWithVariants(Set<Block> blocks, Block block, Identifier[] doubleModels,
			Identifier[] sideTextures, Identifier[] bottomTextures, Identifier[] topTextures,
			Integer[] weights) {
		int length = sideTextures.length;
		if (length != topTextures.length || length != bottomTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] bottomModels = new Identifier[length];
		Identifier[] topModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			var mapping = new TextureMapping()
					.put(TextureSlot.SIDE, tex(sideTextures[i]))
					.put(TextureSlot.BOTTOM, tex(bottomTextures[i]))
					.put(TextureSlot.TOP, tex(topTextures[i]));
			Identifier modelIdBottom = getModelLocation(block, suffix);
			Identifier modelIdTop = getModelLocation(block, "_top" + suffix);
			bottomModels[i] = ModelTemplates.SLAB_BOTTOM.create(modelIdBottom, mapping, this.modelOutput);
			topModels[i] = ModelTemplates.SLAB_TOP.create(modelIdTop, mapping, this.modelOutput);
		}
		slabBlockWithModels(blocks, block, bottomModels, topModels, doubleModels, weights);
	}

	protected void slabBlockWithModels(Set<Block> blocks, Block block, Identifier bottomModel,
			Identifier topModel, Identifier doubleModel) {
		slabBlockWithModels(blocks, block,
				new Identifier[] { bottomModel },
				new Identifier[] { topModel },
				new Identifier[] { doubleModel },
				new Integer[] { 1 });
	}

	protected void slabBlockWithModels(Set<Block> blocks, Block block, Identifier[] bottomModels,
			Identifier[] topModels, Identifier[] doubleModels, Integer[] weights) {
		int length = doubleModels.length;
		if (length != topModels.length || length != bottomModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indicesBottom = IntStream.range(0, length).boxed();
		var indicesTop = IntStream.range(0, length).boxed();
		var indicesDouble = IntStream.range(0, length).boxed();
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.SLAB_TYPE)
						.select(SlabType.BOTTOM, mv(indicesBottom.map(i -> maybeWeight(weights[i],
								plainModel(bottomModels[i]))).toList()))
						.select(SlabType.TOP, mv(indicesTop.map(i -> maybeWeight(weights[i],
								plainModel(topModels[i]))).toList()))
						.select(SlabType.DOUBLE, mv(indicesDouble.map(i -> maybeWeight(weights[i],
								plainModel(doubleModels[i]))).toList()))
		));
		blocks.remove(block);
	}

	protected void wallBlock(Set<Block> blocks, Block block, Identifier texture) {
		wallBlock(blocks, block, texture, texture, texture);
	}

	protected void wallBlock(Set<Block> blocks, Block block, Identifier sideTexture,
			Identifier bottomTexture, Identifier topTexture) {
		wallBlockWithVariants(blocks, block,
				new Identifier[] { sideTexture },
				new Identifier[] { bottomTexture },
				new Identifier[] { topTexture });
	}

	protected void checkeredWallBlock(Set<Block> blocks, Block block, Identifier texture,
			Identifier mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate =
				(model, suffix) -> new ModelTemplate(
						Optional.of(botaniaRL("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, tex(texture)).put(TextureSlot.NORTH, tex(mirroredTexture));

		var checkeredWallPostModel = checkeredTemplate.apply("wall_post_checkered",
				Optional.of("_post")).create(BotaniaBlocks.ROSY_TALC_BRICK_WALL, checkeredMapping, this.modelOutput);
		var checkeredWallSideModel = checkeredTemplate.apply("wall_side_checkered",
				Optional.of("_side")).create(BotaniaBlocks.ROSY_TALC_BRICK_WALL, checkeredMapping, this.modelOutput);
		var checkeredWallSideModelRot90 = checkeredTemplate.apply("wall_side_checkered_90deg",
				Optional.of("_side_90deg")).create(BotaniaBlocks.ROSY_TALC_BRICK_WALL, checkeredMapping, this.modelOutput);
		var checkeredWallSideTallModel = checkeredTemplate.apply("wall_side_tall_checkered",
				Optional.of("_side_tall")).create(BotaniaBlocks.ROSY_TALC_BRICK_WALL, checkeredMapping, this.modelOutput);
		var checkeredWallSideTallModelRot90 = checkeredTemplate.apply("wall_side_tall_checkered_90deg",
				Optional.of("_side_tall_90deg")).create(BotaniaBlocks.ROSY_TALC_BRICK_WALL, checkeredMapping, this.modelOutput);

		wallBlockWithModels(blocks, block, checkeredWallPostModel, checkeredWallSideModel, checkeredWallSideModelRot90,
				checkeredWallSideTallModel, checkeredWallSideTallModelRot90);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Identifier[] textures) {
		wallBlockWithVariants(blocks, block, textures, textures, textures);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Identifier[] textures, Integer[] weights) {
		wallBlockWithVariants(blocks, block, textures, textures, textures, weights);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Identifier[] sideTextures,
			Identifier[] bottomTextures, Identifier[] topTextures) {
		var weights = new Integer[sideTextures.length];
		Arrays.fill(weights, 1);
		wallBlockWithVariants(blocks, block, sideTextures, bottomTextures, topTextures, weights);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Identifier[] sideTextures,
			Identifier[] bottomTextures, Identifier[] topTextures, Integer[] weights) {
		int length = sideTextures.length;
		if (length != bottomTextures.length && length != topTextures.length && length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] postModels = new Identifier[length];
		Identifier[] lowModels = new Identifier[length];
		Identifier[] tallModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			var mapping = new TextureMapping()
					.put(TextureSlot.WALL, tex(sideTextures[i]))
					.put(TextureSlot.BOTTOM, tex(bottomTextures[i]))
					.put(TextureSlot.TOP, tex(topTextures[i]));
			Identifier modelIdPost = getModelLocation(block, "_post" + suffix);
			Identifier modelIdLow = getModelLocation(block, "_side" + suffix);
			Identifier modelIdTall = getModelLocation(block, "_side_tall" + suffix);
			var postTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/wall_post")),
					Optional.of("_post"), TextureSlot.WALL, TextureSlot.BOTTOM, TextureSlot.TOP);
			var sideTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/wall_side")),
					Optional.of("_side"), TextureSlot.WALL, TextureSlot.BOTTOM, TextureSlot.TOP);
			var sideTallTemplate = new ModelTemplate(Optional.of(botaniaRL("block/shapes/wall_side_tall")),
					Optional.of("_side_tall"), TextureSlot.WALL, TextureSlot.BOTTOM, TextureSlot.TOP);
			postModels[i] = postTemplate.create(modelIdPost, mapping, this.modelOutput);
			lowModels[i] = sideTemplate.create(modelIdLow, mapping, this.modelOutput);
			tallModels[i] = sideTallTemplate.create(modelIdTall, mapping, this.modelOutput);
		}
		wallBlockWithModels(blocks, block, postModels, lowModels, tallModels, weights);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier[] postModels,
			Identifier[] lowModels, Identifier[] tallModels, Integer[] weights) {
		wallBlockWithModels(blocks, block, postModels, lowModels, tallModels, weights, true);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier postModel,
			Identifier lowModel, Identifier lowModelRot90, Identifier tallModel,
			Identifier tallodelRot90) {
		wallBlockWithModels(blocks, block,
				new Identifier[] { postModel },
				new Identifier[] { lowModel },
				new Identifier[] { lowModelRot90 },
				new Identifier[] { tallModel },
				new Identifier[] { tallodelRot90 },
				new Integer[] { 1 },
				true);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier[] postModels,
			Identifier[] lowModels, Identifier[] tallModels, Integer[] weights, Boolean uvlock) {
		wallBlockWithModels(blocks, block, postModels, lowModels, lowModels, tallModels, tallModels, weights, uvlock);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier[] postModels,
			Identifier[] lowModels, Identifier[] lowModelsRot90, Identifier[] tallModels,
			Identifier[] tallModelsRot90, Integer[] weights, Boolean uvlock) {
		int length = postModels.length;
		if (length != lowModels.length || length != tallModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var multiPartGenerator = MultiPartGenerator.multiPart(block);
		var indicesPost = IntStream.range(0, length).boxed();
		multiPartGenerator.with(condition().term(BlockStateProperties.UP, true),
				mv(indicesPost.map(i -> maybeWeight(weights[i],
						plainModel(postModels[i]))).toList()));
		var wallSides = List.of(BlockStateProperties.EAST_WALL, BlockStateProperties.WEST_WALL,
				BlockStateProperties.SOUTH_WALL, BlockStateProperties.NORTH_WALL);

		// We're using an alternate model for everything that's rotated 90/270 degrees to be able to
		// have a consistent "checkered" texture for some walls. Normal walls just provide the same model twice
		// which makes no impact in the generated blockstate.
		for (EnumProperty<WallSide> wallSide : wallSides) {
			Quadrant yRot =
					wallSide == BlockStateProperties.EAST_WALL ? Quadrant.R90
							: wallSide == BlockStateProperties.WEST_WALL ? Quadrant.R270
							: wallSide == BlockStateProperties.SOUTH_WALL ? Quadrant.R180
							: Quadrant.R0;
			boolean rotatedModel = yRot == Quadrant.R90 || yRot == Quadrant.R270;
			var indicesLow = IntStream.range(0, length).boxed();
			var indicesTall = IntStream.range(0, length).boxed();
			multiPartGenerator
					.with(condition().term(wallSide, WallSide.LOW), mv(indicesLow.map(
							i -> maybeWeight(weights[i], maybeUVLock(uvlock, maybeYRot(yRot,
									plainModel(rotatedModel ? lowModelsRot90[i] : lowModels[i]))))
					).toList()))
					.with(condition().term(wallSide, WallSide.TALL), mv(indicesTall.map(
							i -> maybeWeight(weights[i], maybeUVLock(uvlock, maybeYRot(yRot,
									plainModel(rotatedModel ? tallModelsRot90[i] : tallModels[i]))))
					).toList()));
		}
		this.blockStateGenerators.add(multiPartGenerator);
		blocks.remove(block);
	}

	protected void fenceBlock(Set<Block> blocks, Block block, Identifier tex) {
		var mapping = TextureMapping.defaultTexture(tex(tex));
		var postModel = ModelTemplates.FENCE_POST.create(block, mapping, this.modelOutput);
		var sideModel = ModelTemplates.FENCE_SIDE.create(block, mapping, this.modelOutput);
		this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createFence(block, mv(postModel), mv(sideModel)));
		blocks.remove(block);
	}

	protected void fenceGateBlock(Set<Block> blocks, Block block, Identifier tex) {
		var mapping = TextureMapping.defaultTexture(tex(tex));
		var openModel = ModelTemplates.FENCE_GATE_OPEN.create(block, mapping, this.modelOutput);
		var closedModel = ModelTemplates.FENCE_GATE_CLOSED.create(block, mapping, this.modelOutput);
		var openWallModel = ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, mapping, this.modelOutput);
		var closedWallModel = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, mapping, this.modelOutput);
		this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createFenceGate(block, mv(openModel), mv(closedModel), mv(openWallModel), mv(closedWallModel), false));
		blocks.remove(block);
	}

	protected void specialDoorBlock(Set<Block> blocks, Block doorBlock) {
		TextureMapping texturemapping = BotaniaModelTemplates.doorMapping(doorBlock);
		Identifier bottomLeftModel = BotaniaModelTemplates.DOOR_BOTTOM_LEFT.create(doorBlock, texturemapping, this.modelOutput);
		Identifier bottomLeftOpenModel = BotaniaModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
		Identifier bottomRightModel = BotaniaModelTemplates.DOOR_BOTTOM_RIGHT.create(doorBlock, texturemapping, this.modelOutput);
		Identifier bottomRightOpenModel = BotaniaModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
		Identifier topLeftModel = BotaniaModelTemplates.DOOR_TOP_LEFT.create(doorBlock, texturemapping, this.modelOutput);
		Identifier topLeftOpenModel = BotaniaModelTemplates.DOOR_TOP_LEFT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
		Identifier topRightModel = BotaniaModelTemplates.DOOR_TOP_RIGHT.create(doorBlock, texturemapping, this.modelOutput);
		Identifier topRightOpenModel = BotaniaModelTemplates.DOOR_TOP_RIGHT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
		this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createDoor(doorBlock,
				mv(bottomLeftModel), mv(bottomLeftOpenModel),
				mv(bottomRightModel), mv(bottomRightOpenModel),
				mv(topLeftModel), mv(topLeftOpenModel),
				mv(topRightModel), mv(topRightOpenModel)));
		blocks.remove(doorBlock);
	}

	protected void specialTrapdoorBlock(Set<Block> blocks, Block trapdoorBlock) {
		TextureMapping texturemapping = BotaniaModelTemplates.trapdoorMapping(trapdoorBlock);
		Identifier topModel = BotaniaModelTemplates.TRAPDOOR_TOP.create(trapdoorBlock, texturemapping, this.modelOutput);
		Identifier bottomModel = BotaniaModelTemplates.TRAPDOOR_BOTTOM.create(trapdoorBlock, texturemapping, this.modelOutput);
		Identifier openModel = BotaniaModelTemplates.TRAPDOOR_OPEN.create(trapdoorBlock, texturemapping, this.modelOutput);
		this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createOrientableTrapdoor(trapdoorBlock, mv(topModel), mv(bottomModel), mv(openModel)));
		blocks.remove(trapdoorBlock);
	}

	protected void sign(Set<Block> blocks, Block particleBlock, Block signBlock, Block wallSignBlock) {
		TextureMapping texturemapping = TextureMapping.particle(particleBlock);
		Identifier resourcelocation = ModelTemplates.PARTICLE_ONLY.create(signBlock, texturemapping, this.modelOutput);
		singleVariantBlockState(signBlock, resourcelocation);
		singleVariantBlockState(wallSignBlock, resourcelocation);
		blocks.remove(signBlock);
		blocks.remove(wallSignBlock);
	}

	protected void hangingSign(Set<Block> blocks, Block particleBlock, Block hangingSignBlock, Block wallHangingSignBlock) {
		TextureMapping texturemapping = TextureMapping.particle(particleBlock);
		Identifier resourcelocation = ModelTemplates.PARTICLE_ONLY.create(hangingSignBlock, texturemapping, this.modelOutput);
		singleVariantBlockState(hangingSignBlock, resourcelocation);
		singleVariantBlockState(wallHangingSignBlock, resourcelocation);
		blocks.remove(hangingSignBlock);
		blocks.remove(wallHangingSignBlock);
	}

	protected void buttonBlock(Set<Block> blocks, Block buttonBlock, Identifier blockTexture) {
		TextureMapping texturemapping = TextureMapping.defaultTexture(tex(blockTexture));
		Identifier unpoweredModel = ModelTemplates.BUTTON.create(buttonBlock, texturemapping, this.modelOutput);
		Identifier poweredModel = ModelTemplates.BUTTON_PRESSED.create(buttonBlock, texturemapping, this.modelOutput);
		this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createButton(buttonBlock, mv(unpoweredModel), mv(poweredModel)));
		ModelTemplates.BUTTON_INVENTORY.create(buttonBlock, texturemapping, this.modelOutput);
		blocks.remove(buttonBlock);
	}

	protected void pressurePlateBlock(Set<Block> blocks, Block pressurePlateBlock, Identifier blockTexture) {
		TextureMapping texturemapping = TextureMapping.defaultTexture(tex(blockTexture));
		Identifier unpoweredModel = ModelTemplates.PRESSURE_PLATE_UP.create(pressurePlateBlock, texturemapping, this.modelOutput);
		Identifier poweredModel = ModelTemplates.PRESSURE_PLATE_DOWN.create(pressurePlateBlock, texturemapping, this.modelOutput);
		this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createPreasurePlate(pressurePlateBlock, mv(unpoweredModel), mv(poweredModel)));
		blocks.remove(pressurePlateBlock);
	}

	protected void cubeAllNoRemove(Block block) {
		cubeAll(new HashSet<>(), block);
	}

	protected void cubeAll(Set<Block> blocks, Block block) {
		Identifier texture = getBlockTexture(block);
		cubeAllWithVariants(blocks, block, new Identifier[] { texture });
	}

	protected Identifier checkeredBlockWithBlockstate(Set<Block> blocks, Block block, Identifier texture,
			Identifier mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate =
				(model, suffix) -> new ModelTemplate(
						Optional.of(botaniaRL("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, tex(texture)).put(TextureSlot.NORTH, tex(mirroredTexture));

		var blockModel = checkeredTemplate.apply("cube_checkered",
				Optional.empty()).create(block, checkeredMapping, this.modelOutput);
		cubeAllWithModels(blocks, block, new Identifier[] { blockModel }, new Integer[] { 1 });
		return blockModel;
	}

	protected void cubeAllWithVariants(Set<Block> blocks, Block block, Identifier[] textures) {
		var weights = new Integer[textures.length];
		Arrays.fill(weights, 1);
		cubeAllWithVariants(blocks, block, textures, weights);
	}

	protected void cubeAllWithVariants(Set<Block> blocks, Block block, Identifier[] textures, Integer[] weights) {
		int length = textures.length;
		if (length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] models = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelId = getModelLocation(block, suffix);
			models[i] = ModelTemplates.CUBE_ALL.create(modelId, TextureMapping.cube(tex(textures[i])), this.modelOutput);
		}
		cubeAllWithModels(blocks, block, models, weights);
	}

	protected void cubeAllWithModels(Set<Block> blocks, Block block, Identifier[] models, Integer[] weights) {
		int length = models.length;
		if (length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indices = IntStream.range(0, length).boxed();
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block, mv(indices.map(i -> maybeWeight(weights[i],
				plainModel(models[i]))).toList())));
		blocks.remove(block);
	}

	protected void singleVariantBlockState(Block block, Identifier model) {
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block, mv(model)));
	}

	protected void rotatedMirrored(Set<Block> blocks, Block block, Identifier texture) {
		rotatedMirroredWithVariants(blocks, block, new Identifier[] { texture });
	}

	protected void rotatedMirroredWithVariants(Set<Block> blocks, Block block, Identifier[] textures) {
		var weights = new Integer[textures.length];
		Arrays.fill(weights, 1);
		rotatedMirroredWithVariants(blocks, block, textures, weights);
	}

	protected void rotatedMirroredWithVariants(Set<Block> blocks, Block block, Identifier[] textures, Integer[] weights) {
		int length = textures.length;
		if (length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] models = new Identifier[length];
		Identifier[] mirroredModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelId = getModelLocation(block, suffix);
			Identifier mirriredModelId = getModelLocation(block, "_mirrored" + suffix);
			models[i] = ModelTemplates.CUBE_ALL.create(modelId, TextureMapping.cube(tex(textures[i])), this.modelOutput);
			mirroredModels[i] = ModelTemplates.CUBE_MIRRORED_ALL.create(mirriredModelId, TextureMapping.cube(tex(textures[i])), this.modelOutput);
		}
		rotatedMirroredWithModels(blocks, block, models, mirroredModels, weights);
	}

	protected void rotatedMirroredWithModels(Set<Block> blocks, Block block, Identifier[] models,
			Identifier[] mirroredModels, Integer[] weights) {
		int length = models.length;
		if (length != mirroredModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indices = IntStream.range(0, length).boxed();
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block, mv(indices.flatMap(i -> Stream.of(
				maybeWeight(weights[i], plainModel(models[i])),
				maybeWeight(weights[i], plainModel(mirroredModels[i])),
				maybeWeight(weights[i], plainModel(models[i])
						.withYRot(Quadrant.R180)),
				maybeWeight(weights[i], plainModel(mirroredModels[i])
						.withYRot(Quadrant.R180))
		)).toList())));
		blocks.remove(block);
	}

	protected void pillar(Set<Block> blocks, Block block, Identifier top, Identifier side) {
		pillarWithVariants(blocks, block, new Identifier[] { top }, new Identifier[] { side });
	}

	protected void pillarWithVariants(Set<Block> blocks, Block block, Identifier[] topTextures,
			Identifier[] sideTextures) {
		var weights = new Integer[topTextures.length];
		Arrays.fill(weights, 1);
		pillarWithVariants(blocks, block, topTextures, sideTextures, weights);
	}

	protected void pillarWithVariants(Set<Block> blocks, Block block, Identifier[] topTextures,
			Identifier[] sideTextures, Integer[] weights) {
		int length = topTextures.length;
		if (length != sideTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] topModels = new Identifier[length];
		Identifier[] horizontalModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelIdTop = getModelLocation(block, suffix);
			Identifier modelIdHorizontal = getModelLocation(block, "_horizontal" + suffix);
			topModels[i] = ModelTemplates.CUBE_COLUMN.create(modelIdTop, TextureMapping.column(tex(sideTextures[i]), tex(topTextures[i])), this.modelOutput);
			horizontalModels[i] = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(modelIdHorizontal, TextureMapping.column(tex(sideTextures[i]), tex(topTextures[i])), this.modelOutput);
		}
		pillarWithModels(blocks, block, topModels, horizontalModels, weights);
	}

	protected void pillarWithModels(Set<Block> blocks, Block block, Identifier[] topModels,
			Identifier[] horizontalModels, Integer[] weights) {
		int length = topModels.length;
		if (length != horizontalModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indicesX = IntStream.range(0, length).boxed();
		var indicesY = IntStream.range(0, length).boxed();
		var indicesZ = IntStream.range(0, length).boxed();
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.AXIS)
						.select(Direction.Axis.Y, mv(indicesX.map(i -> maybeWeight(weights[i], plainModel(topModels[i]))).toList()))
						.select(Direction.Axis.Z, mv(indicesY.map(i -> maybeWeight(weights[i], plainModel(horizontalModels[i])
								.withXRot(Quadrant.R90))).toList()))
						.select(Direction.Axis.X, mv(indicesZ.map(i -> maybeWeight(weights[i], plainModel(horizontalModels[i])
								.withXRot(Quadrant.R90)
								.withYRot(Quadrant.R90))).toList()))
		));
		blocks.remove(block);
	}

	// Alternative pillar model that rotates and mirrors some additional faces
	protected void pillarAlt(Set<Block> blocks, Block block, Identifier top, Identifier side) {
		pillarAltWithVariants(blocks, block, new Identifier[] { top }, new Identifier[] { side });
	}

	protected void pillarAltWithVariants(Set<Block> blocks, Block block, Identifier[] topTextures,
			Identifier[] sideTextures) {
		int length = topTextures.length;
		if (length != sideTextures.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] topModels = new Identifier[length];
		Identifier[] horizontalXModels = new Identifier[length];
		Identifier[] horizontalZModels = new Identifier[length];
		ModelTemplate horizontalXTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/cube_column_horizontal_x")),
				Optional.of("_horizontal_x"), TextureSlot.END, TextureSlot.SIDE);
		ModelTemplate horizontalZTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/cube_column_horizontal_z")),
				Optional.of("_horizontal_z"), TextureSlot.END, TextureSlot.SIDE);
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelIdTop = getModelLocation(block, suffix);
			Identifier modelIdHorizontalX = getModelLocation(block, "_horizontal_x" + suffix);
			Identifier modelIdHorizontalZ = getModelLocation(block, "_horizontal_z" + suffix);
			topModels[i] = ModelTemplates.CUBE_COLUMN.create(modelIdTop,
					TextureMapping.column(tex(sideTextures[i]), tex(topTextures[i])), this.modelOutput);
			horizontalXModels[i] = horizontalXTemplate.create(modelIdHorizontalX,
					TextureMapping.column(tex(sideTextures[i]), tex(topTextures[i])), this.modelOutput);
			horizontalZModels[i] = horizontalZTemplate.create(modelIdHorizontalZ,
					TextureMapping.column(tex(sideTextures[i]), tex(topTextures[i])), this.modelOutput);
		}
		pillarAltWithModels(blocks, block, topModels, horizontalXModels, horizontalZModels);
	}

	protected void pillarAltWithModels(Set<Block> blocks, Block block, Identifier[] yModels,
			Identifier[] xModels, Identifier[] zModels) {
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.AXIS)
						.select(Direction.Axis.Y, mv(Stream.of(yModels)
								.map(rl -> plainModel(rl)).toList()))
						.select(Direction.Axis.X, mv(Stream.of(xModels)
								.map(rl -> plainModel(rl)).toList()))
						.select(Direction.Axis.Z, mv(Stream.of(zModels)
								.map(rl -> plainModel(rl)).toList()))
		));
		blocks.remove(block);
	}

	protected void directionalPillar(Set<Block> blocks, Block block, Identifier top,
			Identifier bottom, Identifier side) {
		directionalPillarWithVariants(blocks, block,
				new Identifier[] { top },
				new Identifier[] { top },
				new Identifier[] { side });
	}

	protected void directionalPillarWithVariants(Set<Block> blocks, Block block, Identifier[] topTextures,
			Identifier[] bottomTextures, Identifier[] sideTextures) {
		var weights = new Integer[topTextures.length];
		Arrays.fill(weights, 1);
		directionalPillarWithVariants(blocks, block, topTextures, bottomTextures, sideTextures, weights);
	}

	protected void directionalPillarWithVariants(Set<Block> blocks, Block block, Identifier[] topTextures,
			Identifier[] bottomTextures, Identifier[] sideTextures, Integer[] weights) {
		int length = topTextures.length;
		if (length != bottomTextures.length || length != sideTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] topModels = new Identifier[length];
		Identifier[] horizontalModels = new Identifier[length];
		ModelTemplate topTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/cube_column_directional")), Optional.empty(),
				TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
		ModelTemplate horizontalTemplate = new ModelTemplate(
				Optional.of(botaniaRL("block/shapes/cube_column_directional_horizontal")), Optional.of("_horizontal"),
				TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
		for (int i = 0; i < length; i++) {
			TextureMapping mapping = new TextureMapping()
					.put(TextureSlot.SIDE, tex(sideTextures[i]))
					.put(TextureSlot.TOP, tex(topTextures[i]))
					.put(TextureSlot.BOTTOM, tex(bottomTextures[i]));
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelIdTop = getModelLocation(block, suffix);
			Identifier modelIdHorizontal = getModelLocation(block, "_horizontal" + suffix);
			topModels[i] = topTemplate.create(modelIdTop, mapping, this.modelOutput);
			horizontalModels[i] = horizontalTemplate.create(modelIdHorizontal, mapping, this.modelOutput);
		}
		directionalPillarWithModels(blocks, block, topModels, horizontalModels, weights);
	}

	protected void directionalPillarWithModels(Set<Block> blocks, Block block, Identifier[] topModels,
			Identifier[] horizontalModels, Integer[] weights) {
		int length = topModels.length;
		if (length != horizontalModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indicesUp = IntStream.range(0, length).boxed();
		var indicesDown = IntStream.range(0, length).boxed();
		var indicesNorth = IntStream.range(0, length).boxed();
		var indicesSouth = IntStream.range(0, length).boxed();
		var indicesEast = IntStream.range(0, length).boxed();
		var indicesWest = IntStream.range(0, length).boxed();
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.FACING)
						.select(Direction.UP, mv(indicesUp.map(i -> maybeWeight(weights[i], plainModel(topModels[i]))).toList()))
						.select(Direction.DOWN, mv(indicesDown.map(i -> maybeWeight(weights[i], plainModel(topModels[i])
								.withXRot(Quadrant.R180))).toList()))
						.select(Direction.NORTH, mv(indicesNorth.map(i -> maybeWeight(weights[i], plainModel(horizontalModels[i]))).toList()))
						.select(Direction.SOUTH, mv(indicesSouth.map(i -> maybeWeight(weights[i], plainModel(horizontalModels[i])
								.withYRot(Quadrant.R180))).toList()))
						.select(Direction.EAST, mv(indicesEast.map(i -> maybeWeight(weights[i], plainModel(horizontalModels[i])
								.withYRot(Quadrant.R90))).toList()))
						.select(Direction.WEST, mv(indicesWest.map(i -> maybeWeight(weights[i], plainModel(horizontalModels[i])
								.withYRot(Quadrant.R270))).toList()))
		));
		blocks.remove(block);
	}

	// PORT-26.3: variants are immutable records and a weight belongs to the multi-variant, not the variant
	protected Variant maybeUVLock(Boolean uvlock, Variant variant) {
		return uvlock ? variant.withUvLock(true) : variant;
	}

	protected Weighted<Variant> maybeWeight(int weight, Variant variant) {
		return new Weighted<>(variant, weight);
	}

	protected Variant maybeXRot(Quadrant rotation, Variant variant) {
		return rotation != Quadrant.R0 ? variant.withXRot(rotation) : variant;
	}

	protected Variant maybeYRot(Quadrant rotation, Variant variant) {
		return rotation != Quadrant.R0 ? variant.withYRot(rotation) : variant;
	}

	protected static Material tex(Identifier texture) {
		return new Material(texture);
	}

	protected static Material tex(Material texture) {
		return texture;
	}

	protected static Variant plainModel(Identifier model) {
		return new Variant(model);
	}

	protected static MultiVariant mv(Identifier model) {
		return new MultiVariant(WeightedList.of(new Variant(model)));
	}

	protected static MultiVariant mv(Variant variant) {
		return new MultiVariant(WeightedList.of(variant));
	}

	protected static MultiVariant mv(Variant... variants) {
		return new MultiVariant(WeightedList.of(variants));
	}

	/** A multi-variant from plain or weighted variants. */
	protected static MultiVariant mv(List<?> variants) {
		WeightedList.Builder<Variant> builder = WeightedList.builder();
		for (Object variant : variants) {
			if (variant instanceof Weighted<?> weighted) {
				builder.add((Variant) weighted.value(), weighted.weight());
			} else {
				builder.add((Variant) variant);
			}
		}
		return new MultiVariant(builder.build());
	}

	protected static ConditionBuilder condition() {
		return new ConditionBuilder();
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public final <T> Collection<T> takeAll(Set<T> src, T... items) {
		List<T> ret = Arrays.asList(items);
		for (T item : items) {
			if (!src.contains(item)) {
				getLogger().warn("Item {} not found in set", item);
			}
		}
		if (!src.removeAll(ret)) {
			getLogger().warn("takeAll array didn't yield anything ({})", Arrays.toString(items));
		}
		return ret;
	}

	public final <T> Collection<T> takeAll(Set<T> src, Predicate<T> pred) {
		List<T> ret = new ArrayList<>();

		Iterator<T> iter = src.iterator();
		while (iter.hasNext()) {
			T item = iter.next();
			if (pred.test(item)) {
				iter.remove();
				ret.add(item);
			}
		}

		if (ret.isEmpty()) {
			getLogger().warn("takeAll predicate yielded nothing", new Throwable());
		}
		return ret;
	}

	protected void redStringBlock(Block block) {
		Identifier selfName = getBlockTexture(block);
		Identifier front = botaniaRL("block/red_stringed_sender");
		var model = ModelTemplates.CUBE_ORIENTABLE.create(block, new TextureMapping()
				.put(TextureSlot.TOP, tex(selfName))
				.put(TextureSlot.FRONT, tex(front))
				.put(TextureSlot.SIDE, tex(selfName)), this.modelOutput);
		this.blockStateGenerators.add(MultiVariantGenerator.dispatch(block, mv(model))
				.with(BlockModelGeneratorsAccessor.botania_getRotationFacing()));
	}
}
