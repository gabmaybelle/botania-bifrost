/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import vazkii.botania.common.block.block_entity.ManaEnchanterBlockEntity;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.test.BotaniaGameTest;
import vazkii.botania.test.TestingUtil;

/**
 * PORT-26.3: the structures gameplay checks with BotaniaMultiblock (they were Patchouli multiblocks). Each structure
 * is built by hand, the way the Lexica shows it, then checked whole, rotated a quarter turn, and with a block wrong.
 * The game test server runs without Patchouli, so these also show the classes load without it.
 */
public class MultiblockTest {
	private static final String ARENA = "botania:block/multiblock_arena";
	private static final BlockPos ANCHOR = new BlockPos(6, 1, 6);

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 13; x++) {
			for (int z = 0; z < 13; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
			}
		}
	}

	// the terrestrial agglomeration plate sits on a 3x3 of livingrock corners and centre with lapis edges
	private static void buildTerraPlate(GameTestHelper helper) {
		floor(helper);
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				boolean edge = (dx == 0) != (dz == 0);
				helper.setBlock(ANCHOR.offset(dx, 0, dz), edge ? Blocks.LAPIS_BLOCK : BotaniaBlocks.LIVINGROCK);
			}
		}
		helper.setBlock(ANCHOR.above(), BotaniaBlocks.TERRESTRIAL_AGGLOMERATION_PLATE);
	}

	@BotaniaGameTest(template = ARENA, timeoutTicks = 20)
	public void terraPlatePlatform(GameTestHelper helper) {
		buildTerraPlate(helper);
		var level = helper.getLevel();
		TestingUtil.assertThat(TerrestrialAgglomerationPlateBlockEntity.MULTIBLOCK.get().validate(level, helper.absolutePos(ANCHOR)) != null,
				() -> "A correct terra plate platform was not accepted");

		helper.setBlock(ANCHOR.offset(1, 0, 0), Blocks.STONE);
		TestingUtil.assertThat(TerrestrialAgglomerationPlateBlockEntity.MULTIBLOCK.get().validate(level, helper.absolutePos(ANCHOR)) == null,
				() -> "A platform with stone instead of lapis was accepted");
		helper.succeed();
	}

	// the alfheim portal: a 5 wide, 5 tall livingwood frame with glimmering logs in the middle of the top and sides,
	// standing on the gateway core, along the X axis (alongX) or the Z axis
	private static void buildPortal(GameTestHelper helper, boolean alongX) {
		floor(helper);
		Direction.Axis across = alongX ? Direction.Axis.X : Direction.Axis.Z;
		for (int i = -2; i <= 2; i++) {
			for (int y = 0; y <= 4; y++) {
				BlockPos pos = alongX ? ANCHOR.offset(i, y, 0) : ANCHOR.offset(0, y, i);
				boolean side = Math.abs(i) == 2;
				boolean topOrBottom = y == 0 || y == 4;
				BlockState state;
				if (i == 0 && y == 0) {
					state = BotaniaBlocks.ELVEN_GATEWAY_CORE.defaultBlockState();
				} else if (side && !topOrBottom) {
					state = log(y == 2 ? BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG : BotaniaBlocks.LIVINGWOOD_LOG, Direction.Axis.Y);
				} else if (!side && topOrBottom) {
					state = log(i == 0 ? BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG : BotaniaBlocks.LIVINGWOOD_LOG, across);
				} else {
					state = Blocks.AIR.defaultBlockState();
				}
				helper.setBlock(pos, state);
			}
		}
	}

	private static BlockState log(Block block, Direction.Axis axis) {
		return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}

	@BotaniaGameTest(template = ARENA, timeoutTicks = 20)
	public void alfheimPortalAlongX(GameTestHelper helper) {
		buildPortal(helper, true);
		Rotation rot = AlfheimPortalBlockEntity.MULTIBLOCK.get().validate(helper.getLevel(), helper.absolutePos(ANCHOR));
		TestingUtil.assertThat(rot == Rotation.NONE || rot == Rotation.CLOCKWISE_180,
				() -> "A portal along X was read as " + rot);

		helper.setBlock(ANCHOR.above(2), Blocks.STONE);
		TestingUtil.assertThat(AlfheimPortalBlockEntity.MULTIBLOCK.get().validate(helper.getLevel(), helper.absolutePos(ANCHOR)) == null,
				() -> "A portal with a block in its opening was accepted");
		helper.succeed();
	}

	@BotaniaGameTest(template = ARENA, timeoutTicks = 20)
	public void alfheimPortalAlongZ(GameTestHelper helper) {
		buildPortal(helper, false);
		Rotation rot = AlfheimPortalBlockEntity.MULTIBLOCK.get().validate(helper.getLevel(), helper.absolutePos(ANCHOR));
		TestingUtil.assertThat(rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90,
				() -> "A portal along Z was read as " + rot);

		helper.setBlock(ANCHOR.offset(0, 2, 2), BotaniaBlocks.LIVINGWOOD_LOG);
		TestingUtil.assertThat(AlfheimPortalBlockEntity.MULTIBLOCK.get().validate(helper.getLevel(), helper.absolutePos(ANCHOR)) == null,
				() -> "A portal without its glimmering side log was accepted");
		helper.succeed();
	}

	// the mana enchanter, read the way the Lexica draws it: each row of the pattern runs along Z, rows along X,
	// layers from the pylons down to the obsidian; swapXZ lays it out a quarter turn round
	private static void buildEnchanter(GameTestHelper helper, boolean swapXZ) {
		floor(helper);
		String[][] pattern = ManaEnchanterBlockEntity.PATTERN;
		for (int layer = 0; layer < pattern.length; layer++) {
			int y = pattern.length - 1 - layer;
			for (int row = 0; row < pattern[layer].length; row++) {
				for (int col = 0; col < pattern[layer][row].length(); col++) {
					int dx = row - pattern[layer].length / 2;
					int dz = col - pattern[layer][row].length() / 2;
					BlockPos pos = swapXZ ? ANCHOR.offset(dz, y, dx) : ANCHOR.offset(dx, y, dz);
					Block block = switch (pattern[layer][row].charAt(col)) {
						case 'P' -> BotaniaBlocks.MANA_PYLON;
						case 'F' -> BotaniaBlocks.WHITE_MYSTICAL_FLOWER;
						case 'L' -> Blocks.LAPIS_BLOCK;
						case 'B', '0' -> Blocks.OBSIDIAN;
						default -> null;
					};
					if (block != null) {
						helper.setBlock(pos, block);
					}
				}
			}
		}
	}

	@BotaniaGameTest(template = ARENA, timeoutTicks = 20)
	public void manaEnchanterAlongZ(GameTestHelper helper) {
		buildEnchanter(helper, false);
		var lapis = helper.absolutePos(ANCHOR.above());
		var axis = ManaEnchanterBlockEntity.canEnchanterExist(helper.getLevel(), lapis);
		TestingUtil.assertThat(axis == Direction.Axis.Z, () -> "An enchanter along Z was read as " + axis);

		helper.setBlock(ANCHOR.offset(-1, 1, -1), Blocks.AIR);
		TestingUtil.assertThat(ManaEnchanterBlockEntity.canEnchanterExist(helper.getLevel(), lapis) == null,
				() -> "An enchanter missing a flower was accepted");
		helper.succeed();
	}

	@BotaniaGameTest(template = ARENA, timeoutTicks = 20)
	public void manaEnchanterAlongX(GameTestHelper helper) {
		buildEnchanter(helper, true);
		var lapis = helper.absolutePos(ANCHOR.above());
		var axis = ManaEnchanterBlockEntity.canEnchanterExist(helper.getLevel(), lapis);
		TestingUtil.assertThat(axis == Direction.Axis.X, () -> "An enchanter along X was read as " + axis);

		helper.setBlock(ANCHOR.offset(0, 0, 1), Blocks.CRYING_OBSIDIAN);
		TestingUtil.assertThat(ManaEnchanterBlockEntity.canEnchanterExist(helper.getLevel(), lapis) == Direction.Axis.X,
				() -> "Crying obsidian in the base was not accepted");
		helper.succeed();
	}
}
