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
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.test.BotaniaGameTest;
import vazkii.botania.test.TestingUtil;

/**
 * Pylons count as bookshelves for an enchanting table: a Natura or Gaia Pylon gives the full 15 power, a Mana
 * Pylon 8. At 15 power the table's third option always costs 30 levels; with nothing around it costs at most 9.
 */
public class PylonEnchantingTest {
	//Empty 33x33x33 structure
	private static final String TEMPLATE = "botania:block/flower_binding_arena";
	private static final BlockPos TABLE = new BlockPos(16, 1, 16);

	private static int topCost(GameTestHelper helper) {
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		var menu = new EnchantmentMenu(0, player.getInventory(),
				ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TABLE)));
		menu.getSlot(0).set(new ItemStack(Items.BOOK));
		menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
		return menu.costs[2];
	}

	private static void placeTable(GameTestHelper helper, Block... pylons) {
		helper.setBlock(TABLE, Blocks.ENCHANTING_TABLE);
		BlockPos[] spots = { TABLE.east(2), TABLE.west(2), TABLE.north(2) };
		for (int i = 0; i < pylons.length; i++) {
			helper.setBlock(spots[i], pylons[i]);
		}
	}

	@BotaniaGameTest(template = TEMPLATE)
	public void bareTableHasLowCost(GameTestHelper helper) {
		placeTable(helper);
		int cost = topCost(helper);
		TestingUtil.assertThat(cost > 0 && cost < 30, () -> "A table with nothing around should cost under 30, got " + cost);
		helper.succeed();
	}

	@BotaniaGameTest(template = TEMPLATE)
	public void naturaPylonGivesFullPower(GameTestHelper helper) {
		placeTable(helper, BotaniaBlocks.NATURA_PYLON);
		int cost = topCost(helper);
		TestingUtil.assertEquals(cost, 30, () -> "A Natura Pylon should give the table full power, top cost was " + cost);
		helper.succeed();
	}

	@BotaniaGameTest(template = TEMPLATE)
	public void twoManaPylonsGiveFullPower(GameTestHelper helper) {
		placeTable(helper, BotaniaBlocks.MANA_PYLON, BotaniaBlocks.MANA_PYLON);
		int cost = topCost(helper);
		TestingUtil.assertEquals(cost, 30, () -> "Two Mana Pylons should give the table full power, top cost was " + cost);
		helper.succeed();
	}
}
