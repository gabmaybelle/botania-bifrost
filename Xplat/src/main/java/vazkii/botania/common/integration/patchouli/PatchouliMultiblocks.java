/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.integration.patchouli;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import vazkii.botania.common.block.block_entity.ManaEnchanterBlockEntity;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.api.TriPredicate;

import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * PORT-26.3: the Patchouli multiblocks, for the Lexica's previews and the Gaia arena display. They lived on the block
 * entities and the Gaia Guardian, whose classes then couldn't load without Patchouli's API (the lambdas name its
 * types). Gameplay checks the same patterns with {@link vazkii.botania.common.block.BotaniaMultiblock}; the loaders
 * register these with Patchouli at startup.
 */
public final class PatchouliMultiblocks {
	private PatchouliMultiblocks() {}

	public static final Supplier<IMultiblock> TERRA_PLATE = Suppliers.memoize(() -> PatchouliAPI.get().makeMultiblock(
			TerrestrialAgglomerationPlateBlockEntity.PATTERN,
			'P', BotaniaBlocks.TERRESTRIAL_AGGLOMERATION_PLATE,
			'R', PatchouliAPI.get().tagMatcher(BotaniaTags.Blocks.TERRA_PLATE_BASE),
			'0', PatchouliAPI.get().tagMatcher(BotaniaTags.Blocks.TERRA_PLATE_BASE),
			'L', PatchouliAPI.get().tagMatcher(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks/lapis")))
	));

	public static final Supplier<IMultiblock> ALFHEIM_PORTAL = Suppliers.memoize(() -> {
		record Matcher(TagKey<Block> tag, Direction.Axis displayedRotation, Block defaultBlock) implements IStateMatcher {
			@Override
			public BlockState getDisplayedState(long ticks) {
				var blocks = StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(this.tag).spliterator(), false)
						.map(Holder::value)
						.toList();
				if (blocks.isEmpty()) {
					return Blocks.BEDROCK.defaultBlockState();
				}

				BlockState block = blocks.contains(defaultBlock)
						? defaultBlock.defaultBlockState()
						: blocks.get((int) ((ticks / 20) % blocks.size())).defaultBlockState();

				return block.hasProperty(BlockStateProperties.AXIS)
						? block.setValue(BlockStateProperties.AXIS, displayedRotation())
						: block;
			}

			@Override
			public TriPredicate<BlockGetter, BlockPos, BlockState> getStatePredicate() {
				return (blockGetter, pos, state) -> state.is(tag());
			}
		}
		var horizontal = new Matcher(BotaniaTags.Blocks.LIVINGWOOD_LOGS, Direction.Axis.X, BotaniaBlocks.LIVINGWOOD_LOG);
		var vertical = new Matcher(BotaniaTags.Blocks.LIVINGWOOD_LOGS, Direction.Axis.Y, BotaniaBlocks.LIVINGWOOD_LOG);
		var horizontalGlimmer = new Matcher(BotaniaTags.Blocks.LIVINGWOOD_LOGS_GLIMMERING, Direction.Axis.X, BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG);
		var verticalGlimmer = new Matcher(BotaniaTags.Blocks.LIVINGWOOD_LOGS_GLIMMERING, Direction.Axis.Y, BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG);

		return PatchouliAPI.get().makeMultiblock(
				AlfheimPortalBlockEntity.PATTERN,
				'W', vertical,
				'w', horizontal,
				'G', verticalGlimmer,
				'g', horizontalGlimmer,
				'0', BotaniaBlocks.ELVEN_GATEWAY_CORE
		);
	});

	private static final Supplier<IStateMatcher> OBSIDIAN_MATCHER = Suppliers.memoize(() -> PatchouliAPI.get().predicateMatcher(
			Blocks.OBSIDIAN,
			state -> state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)
	));

	public static final Supplier<IMultiblock> MANA_ENCHANTER = Suppliers.memoize(() -> PatchouliAPI.get().makeMultiblock(
			ManaEnchanterBlockEntity.PATTERN,
			'P', BotaniaBlocks.MANA_PYLON,
			'L', Blocks.LAPIS_BLOCK,
			'B', OBSIDIAN_MATCHER.get(),
			'0', OBSIDIAN_MATCHER.get(),
			'F', PatchouliAPI.get().tagMatcher(BotaniaTags.Blocks.ENCHANTER_FLOWERS)
	));

	public static final Supplier<IMultiblock> GAIA_ARENA = Suppliers.memoize(() -> {
		var beaconBase = PatchouliAPI.get().predicateMatcher(Blocks.IRON_BLOCK,
				state -> state.is(BlockTags.BEACON_BASE_BLOCKS));
		return PatchouliAPI.get().makeMultiblock(
				new String[][] {
						{
								"P_______P",
								"_________",
								"_________",
								"_________",
								"_________",
								"_________",
								"_________",
								"_________",
								"P_______P",
						},
						{
								"_________",
								"_________",
								"_________",
								"_________",
								"____B____",
								"_________",
								"_________",
								"_________",
								"_________",
						},
						{
								"_________",
								"_________",
								"_________",
								"___III___",
								"___I0I___",
								"___III___",
								"_________",
								"_________",
								"_________",
						}
				},
				'P', BotaniaBlocks.GAIA_PYLON,
				'B', Blocks.BEACON,
				'I', beaconBase,
				'0', beaconBase
		);
	});
}
