/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block.IslandType;
import vazkii.botania.common.block.block_entity.flower.BotaniaIslandTypes;
import vazkii.botania.common.block.block_entity.flower.FloatingFlowerBlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PORT-26.3: the floating flower model loader. The model file ("loader": "botania:floating_flower" with a "flower"
 * model) loads as an unbaked model whose geometry is the flower, shrunk and lifted, on a grass island; that is what
 * items and anything else reading the file get. In the world the island follows the flower's island type: every
 * floating flower block gets a standalone model that bakes the flower and one part per island type, and the baking
 * result swaps it in for the block's states.
 */
public class NeoForgeFloatingFlowerModel implements UnbakedModel {
	private static final Transformation MOVE_FLOWER = new Transformation(new Vector3f(0F, 0.2F, 0F), null, new Vector3f(0.5F, 0.5F, 0.5F), null);
	private static final Map<Block, StandaloneModelKey<BlockStateModel>> KEYS = new HashMap<>();

	public static final UnbakedModelLoader<NeoForgeFloatingFlowerModel> LOADER = (json, context) -> new NeoForgeFloatingFlowerModel(flowerModelId(json));

	private final Identifier flowerModelId;

	private NeoForgeFloatingFlowerModel(Identifier flowerModelId) {
		this.flowerModelId = flowerModelId;
	}

	private static Identifier flowerModelId(JsonObject json) {
		JsonElement flower = json.get("flower");
		if (flower != null && flower.isJsonPrimitive()) {
			return Identifier.parse(flower.getAsString());
		} else if (flower != null && flower.isJsonObject() && flower.getAsJsonObject().has("parent")) {
			// the generated files nest a model that only names its parent: that parent is the flower
			return Identifier.parse(GsonHelper.getAsString(flower.getAsJsonObject(), "parent"));
		}
		throw new JsonParseException("Floating flower model needs a \"flower\" model");
	}

	@Nullable
	@Override
	public Identifier parent() {
		// textures, item transforms and gui light come from the flower
		return flowerModelId;
	}

	@Override
	public UnbakedGeometry geometry() {
		return (textureSlots, baker, state, name) -> new QuadCollection.Builder()
				.addAll(bakeGeometry(baker, flowerModelId, shrinkAndLift(state)))
				.addAll(bakeGeometry(baker, BotaniaIslandTypes.GRASS.islandModel(), state))
				.build();
	}

	private static QuadCollection bakeGeometry(ModelBaker baker, Identifier id, ModelState state) {
		ResolvedModel model = baker.getModel(id);
		return model.bakeTopGeometry(model.getTopTextureSlots(), baker, state);
	}

	private static ModelState shrinkAndLift(ModelState state) {
		Transformation mul = MOVE_FLOWER.compose(state.transformation());
		return new ModelState() {
			@Override
			public Transformation transformation() {
				return mul;
			}
		};
	}

	/** The standalone model that carries a floating flower block's in-world model. */
	public static StandaloneModelKey<BlockStateModel> key(Block block) {
		return KEYS.computeIfAbsent(block, b -> new StandaloneModelKey<>(() -> "floating flower " + BuiltInRegistries.BLOCK.getKey(b)));
	}

	/** Bakes the block model's flower, plus one island part per island type. */
	public record Standalone(Identifier blockModelId) implements UnbakedStandaloneModel<BlockStateModel> {
		public static Standalone forBlock(Block block) {
			return new Standalone(BuiltInRegistries.BLOCK.getKey(block).withPrefix("block/"));
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(blockModelId);
			BotaniaAPI.instance().getIslandTypeRegistry().forEach(type -> resolver.markDependency(type.islandModel()));
		}

		@Override
		public BlockStateModel bake(ModelBaker baker, ModelDebugName name) {
			if (!(baker.getModel(blockModelId).wrapped() instanceof NeoForgeFloatingFlowerModel floating)) {
				// a resource pack replaced the model with something else: bake it like a plain variant
				return new SingleVariant(SimpleModelWrapper.bake(baker, blockModelId, BlockModelRotation.IDENTITY));
			}
			BlockStateModelPart flower = SimpleModelWrapper.bake(baker, floating.flowerModelId, shrinkAndLift(BlockModelRotation.IDENTITY));
			Map<IslandType, BlockStateModelPart> islands = new HashMap<>();
			BotaniaAPI.instance().getIslandTypeRegistry().forEach(
					type -> islands.put(type, SimpleModelWrapper.bake(baker, type.islandModel(), BlockModelRotation.IDENTITY)));
			return new Baked(flower, islands);
		}
	}

	public static class Baked implements DynamicBlockStateModel {
		private final BlockStateModelPart flower;
		private final Map<IslandType, BlockStateModelPart> islands;

		Baked(BlockStateModelPart flower, Map<IslandType, BlockStateModelPart> islands) {
			this.flower = flower;
			this.islands = islands;
		}

		@Override
		public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
			Object data = level.getBlockEntity(pos) instanceof FloatingFlowerBlockEntity flowerEntity ? flowerEntity.getRenderData() : null;
			parts.add(flower);
			parts.add(islands.get(data instanceof IslandType type ? type : BotaniaIslandTypes.GRASS));
		}

		@Override
		public Material.Baked particleMaterial() {
			return flower.particleMaterial();
		}

		@Override
		public int materialFlags() {
			int flags = flower.materialFlags();
			for (BlockStateModelPart island : islands.values()) {
				flags |= island.materialFlags();
			}
			return flags;
		}
	}
}
