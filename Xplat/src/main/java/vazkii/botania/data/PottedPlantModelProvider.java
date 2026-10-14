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
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.lib.LibBlockNames;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class PottedPlantModelProvider implements DataProvider {
	private final PackOutput packOutput;

	public PottedPlantModelProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		List<Pair<String, JsonElement>> jsons = new ArrayList<>();
		for (Block b : BuiltInRegistries.BLOCK) {
			Identifier blockId = BuiltInRegistries.BLOCK.getKey(b);
			if (BotaniaAPI.MODID.equals(blockId.getNamespace()) && b instanceof FlowerPotBlock) {
				String name = blockId.getPath();
				String nonPotted = name.replace(LibBlockNames.POTTED_PREFIX, "").replace("_motif", "");

				JsonObject obj = new JsonObject();
				obj.addProperty("parent", "minecraft:block/flower_pot_cross");
				JsonObject textures = new JsonObject();
				textures.addProperty("plant", ResourcesLib.PREFIX_MOD + "block/" + nonPotted);
				obj.add("textures", textures);
				jsons.add(new Pair<>(name, obj));
			}
		}
		List<CompletableFuture<?>> output = new ArrayList<>();
		PackOutput.PathProvider blocks = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
		for (Pair<String, JsonElement> pair : jsons) {
			output.add(DataProvider.saveStable(cache, pair.getSecond(), blocks.json(botaniaRL(pair.getFirst()))));
		}

		return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
	}

	static MultiVariantGenerator createSimpleBlock(Block block, Identifier resourceLocation) {
		return MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(resourceLocation))));
	}

	@Override
	public String getName() {
		return "Botania potted plant models";
	}
}
