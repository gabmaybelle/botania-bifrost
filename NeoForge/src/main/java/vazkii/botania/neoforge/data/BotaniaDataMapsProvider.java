/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.neoforge.data;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Transformable;

import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * PORT-26.3: composting chances and furnace fuel are item components now (set in
 * {@code BotaniaBlocks#registerItemBlocks});
 * what is left for the data map is the axe stripping, hoe tilling and shovel flattening of Botania's blocks, which are
 * data-driven block transformers. Fabric appends to them in code ({@code FabricCommonInitializer}).
 */
public class BotaniaDataMapsProvider extends DataMapProvider {
	public BotaniaDataMapsProvider(PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		final var transformables = builder(NeoForgeDataMaps.TRANSFORMABLES);
		BotaniaBlocks.addAxeStripping((input, output) -> transformables.add(
				input.builtInRegistryHolder(), Transformable.stripping(input, output), false));
		for (Block grass : List.of(BotaniaBlocks.DRY_GRASS_BLOCK, BotaniaBlocks.GOLDEN_GRASS_BLOCK,
				BotaniaBlocks.VIVID_GRASS_BLOCK, BotaniaBlocks.SCORCHED_GRASS_BLOCK,
				BotaniaBlocks.INFUSED_GRASS_BLOCK, BotaniaBlocks.MUTATED_GRASS_BLOCK)) {
			// like vanilla's grass block: only with air above
			BlockPredicate airAbove = BlockPredicate.allOf(BlockPredicate.matchesBlocks(grass), BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR));
			transformables.add(grass.builtInRegistryHolder(), new Transformable(java.util.Map.of(
					BlockTransformers.HOE, BlockTransformer.BlockTransformData.builder(airAbove, Blocks.FARMLAND).build(),
					BlockTransformers.SHOVEL, BlockTransformer.BlockTransformData.builder(airAbove, Blocks.DIRT_PATH).build())), false);
		}
	}
}
