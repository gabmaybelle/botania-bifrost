/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block.IslandType;
import vazkii.botania.common.block.block_entity.flower.BotaniaIslandTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * PORT-26.3: the floating flower model loader. The model file ("fabric:type": "botania:floating_flower" with a
 * "flower" model) loads as an unbaked model whose geometry is the flower, shrunk and lifted, on a grass island;
 * that is what items and anything else reading the file get. In the world the island follows the flower's island
 * type, so every floating flower block gets a block state resolver whose model picks the island part from the
 * block entity's render data.
 */
public class FabricFloatingFlowerModel implements UnbakedModel {
	private static final Transformation MOVE_FLOWER = new Transformation(new Vector3f(0F, 0.2F, 0F), null, new Vector3f(0.5F, 0.5F, 0.5F), null);
	private final Identifier flowerModelId;

	private FabricFloatingFlowerModel(Identifier flowerModelId) {
		this.flowerModelId = flowerModelId;
	}

	public static UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
		JsonElement flower = json.get("flower");
		Identifier flowerModelId;
		if (flower != null && flower.isJsonPrimitive()) {
			flowerModelId = Identifier.parse(flower.getAsString());
		} else if (flower != null && flower.isJsonObject() && flower.getAsJsonObject().has("parent")) {
			// the generated files nest a model that only names its parent: that parent is the flower
			flowerModelId = Identifier.parse(GsonHelper.getAsString(flower.getAsJsonObject(), "parent"));
		} else {
			throw new JsonParseException("Floating flower model needs a \"flower\" model");
		}
		return new FabricFloatingFlowerModel(flowerModelId);
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

	/** Gives every state of a floating flower block the island-aware model. */
	public record Resolver(Block block) implements BlockStateResolver {
		@Override
		public void resolveBlockStates(BlockStateResolver.Context context) {
			Root root = new Root(BuiltInRegistries.BLOCK.getKey(block).withPrefix("block/"));
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				context.setModel(state, root);
			}
		}
	}

	/** Bakes the block model's flower, plus one island part per island type. */
	public static class Root implements BlockStateModel.UnbakedRoot {
		private final Identifier blockModelId;

		Root(Identifier blockModelId) {
			this.blockModelId = blockModelId;
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(blockModelId);
			BotaniaAPI.instance().getIslandTypeRegistry().forEach(type -> resolver.markDependency(type.islandModel()));
		}

		@Override
		public BlockStateModel bake(BlockState blockState, ModelBaker baker) {
			if (!(baker.getModel(blockModelId).wrapped() instanceof FabricFloatingFlowerModel floating)) {
				// a resource pack replaced the model with something else: bake it like a plain variant
				return new SingleVariant(SimpleModelWrapper.bake(baker, blockModelId, BlockModelRotation.IDENTITY));
			}
			BlockStateModelPart flower = SimpleModelWrapper.bake(baker, floating.flowerModelId, shrinkAndLift(BlockModelRotation.IDENTITY));
			Map<IslandType, BlockStateModelPart> islands = new HashMap<>();
			BotaniaAPI.instance().getIslandTypeRegistry().forEach(
					type -> islands.put(type, SimpleModelWrapper.bake(baker, type.islandModel(), BlockModelRotation.IDENTITY)));
			return new Baked(flower, islands);
		}

		@Override
		public Object visualEqualityGroup(BlockState blockState) {
			return this;
		}
	}

	public static class Baked implements BlockStateModel, FabricBlockStateModel {
		private final BlockStateModelPart flower;
		private final Map<IslandType, BlockStateModelPart> islands;

		Baked(BlockStateModelPart flower, Map<IslandType, BlockStateModelPart> islands) {
			this.flower = flower;
			this.islands = islands;
		}

		@Override
		public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
			output.add(flower);
			output.add(islands.get(BotaniaIslandTypes.GRASS));
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

		@Override
		public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
			Object data = ((FabricBlockGetter) blockView).getBlockEntityRenderData(pos);
			BlockStateModelPart island = islands.get(data instanceof IslandType type ? type : BotaniaIslandTypes.GRASS);
			((FabricBlockStateModelPart) flower).emitQuads(emitter, cullTest);
			((FabricBlockStateModelPart) island).emitQuads(emitter, cullTest);
		}
	}
}
