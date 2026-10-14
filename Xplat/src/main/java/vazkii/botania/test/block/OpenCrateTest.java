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
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import vazkii.botania.test.BotaniaGameTest;

public class OpenCrateTest {
	@BotaniaGameTest(template = "botania:block/opencrate_basic")
	public void testBasic(GameTestHelper helper) {
		helper.pullLever(new BlockPos(0, 4, 0));
		BlockPos belowCrate = new BlockPos(2, 2, 1);
		helper.succeedWhen(() -> helper.assertItemEntityCountIs(Items.STICK, belowCrate, 0, 8));
	}

	@BotaniaGameTest(template = "botania:block/opencrate_blocked")
	public void testBlocked(GameTestHelper helper) {
		helper.startSequence().thenExecute(() -> helper.pullLever(new BlockPos(0, 4, 0)))
				.thenExecuteFor(70, () -> helper.assertEntityNotPresent(EntityTypes.ITEM))
				.thenSucceed();
	}

	@BotaniaGameTest(template = "botania:block/opencrate_snug")
	public void testSnug(GameTestHelper helper) {
		helper.pullLever(new BlockPos(0, 4, 0));
		BlockPos belowCrate = new BlockPos(2, 2, 1);
		helper.succeedWhen(() -> {
			var be = helper.getBlockEntity(belowCrate, BlockEntity.class);
			if (!(be instanceof HopperBlockEntity hopper
					&& hopper.countItem(Items.STICK) == 8)) {
				throw new GameTestAssertException(Component.literal("Sticks not in lower hopper"), (int) helper.getTick());
			}
		});
	}
}
