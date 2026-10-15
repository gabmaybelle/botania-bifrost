/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test.block;

import com.mojang.serialization.DynamicOps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;

import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.test.BotaniaGameTest;
import vazkii.botania.test.TestingUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Checks that Botania's world data survives a save and reload: the blocks are saved, replaced with fresh ones and
 * loaded from their saved data, the way a chunk is written to disk and read back.
 */
public class SaveReloadTest {
	//Empty 33x33x33 structure
	private static final String TEMPLATE = "botania:block/flower_binding_arena";

	private static final BlockPos POOL = new BlockPos(16, 16, 16);
	private static final BlockPos SPREADER = new BlockPos(16, 16, 10);
	private static final BlockPos ENDOFLAME = new BlockPos(18, 16, 10);
	private static final BlockPos CLAYCONIA = new BlockPos(16, 16, 19);

	@BotaniaGameTest(template = TEMPLATE)
	public void blockDataSurvivesReload(GameTestHelper helper) {
		helper.setBlock(POOL, BotaniaBlocks.MANA_POOL);
		helper.setBlock(SPREADER, BotaniaBlocks.MANA_SPREADER);
		helper.setBlock(ENDOFLAME, BotaniaBlocks.FLOATING_ENDOFLAME);
		helper.setBlock(CLAYCONIA, BotaniaBlocks.FLOATING_CLAYCONIA);
		TestingUtil.bindWithWandOfTheForest(helper, SPREADER, POOL);

		BlockPos absolutePool = helper.absolutePos(POOL);
		BlockPos absoluteSpreader = helper.absolutePos(SPREADER);

		helper.runAfterDelay(1L, () -> {
			ManaPoolBlockEntity pool = TestingUtil.assertBlockEntity(helper, POOL, ManaPoolBlockEntity.class);
			ManaSpreaderBlockEntity spreader = TestingUtil.assertBlockEntity(helper, SPREADER, ManaSpreaderBlockEntity.class);
			GeneratingFlowerBlockEntity endoflame = TestingUtil.assertBlockEntity(helper, ENDOFLAME, GeneratingFlowerBlockEntity.class);
			FunctionalFlowerBlockEntity clayconia = TestingUtil.assertBlockEntity(helper, CLAYCONIA, FunctionalFlowerBlockEntity.class);

			pool.receiveMana(12345);
			endoflame.addMana(300);

			//Check the setup first, so a failure below can only come from the reload
			TestingUtil.assertEqualsAt(helper, SPREADER, spreader.getBinding(), absolutePool, () -> "Spreader should aim at the pool before saving");
			TestingUtil.assertEqualsAt(helper, ENDOFLAME, endoflame.getBinding(), absoluteSpreader, () -> "Endoflame should bind to the spreader before saving");
			TestingUtil.assertEqualsAt(helper, CLAYCONIA, clayconia.getBinding(), absolutePool, () -> "Clayconia should bind to the pool before saving");

			int poolMana = pool.getCurrentMana();
			int endoflameMana = endoflame.getMana();
			float rotationX = spreader.rotationX;
			float rotationY = spreader.rotationY;

			reload(helper, POOL, SPREADER, ENDOFLAME, CLAYCONIA);

			pool = TestingUtil.assertBlockEntity(helper, POOL, ManaPoolBlockEntity.class);
			spreader = TestingUtil.assertBlockEntity(helper, SPREADER, ManaSpreaderBlockEntity.class);
			endoflame = TestingUtil.assertBlockEntity(helper, ENDOFLAME, GeneratingFlowerBlockEntity.class);
			clayconia = TestingUtil.assertBlockEntity(helper, CLAYCONIA, FunctionalFlowerBlockEntity.class);

			TestingUtil.assertEqualsAt(helper, POOL, pool.getCurrentMana(), poolMana, () -> "Pool mana changed on reload");
			TestingUtil.assertEqualsAt(helper, ENDOFLAME, endoflame.getMana(), endoflameMana, () -> "Endoflame mana changed on reload");
			TestingUtil.assertEqualsAt(helper, SPREADER, spreader.rotationX, rotationX, () -> "Spreader aim changed on reload");
			TestingUtil.assertEqualsAt(helper, SPREADER, spreader.rotationY, rotationY, () -> "Spreader aim changed on reload");
			TestingUtil.assertEqualsAt(helper, SPREADER, spreader.getBinding(), absolutePool, () -> "Spreader lost its pool on reload");
			TestingUtil.assertEqualsAt(helper, ENDOFLAME, endoflame.getBinding(), absoluteSpreader, () -> "Endoflame lost its spreader on reload");
			TestingUtil.assertEqualsAt(helper, CLAYCONIA, clayconia.getBinding(), absolutePool, () -> "Clayconia lost its pool on reload");

			helper.succeed();
		});
	}

	@BotaniaGameTest(template = TEMPLATE)
	public void itemManaSurvivesReload(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		DynamicOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		tablet.set(BotaniaDataComponents.MANA, 54321);

		Tag saved = ItemStack.CODEC.encodeStart(ops, tablet).getOrThrow();
		ItemStack loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();

		TestingUtil.assertEquals(loaded.get(BotaniaDataComponents.MANA), 54321, () -> "Mana tablet mana changed on reload");
		helper.succeed();
	}

	private static void reload(GameTestHelper helper, BlockPos... positions) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();

		Map<BlockPos, CompoundTag> saved = new LinkedHashMap<>();
		Map<BlockPos, BlockState> states = new LinkedHashMap<>();
		for (BlockPos pos : positions) {
			saved.put(pos, helper.getBlockEntity(pos, BlockEntity.class).saveWithFullMetadata(registries));
			states.put(pos, helper.getBlockState(pos));
		}

		for (BlockPos pos : positions) {
			helper.setBlock(pos, Blocks.AIR);
		}
		for (BlockPos pos : positions) {
			helper.setBlock(pos, states.get(pos));
			BlockEntity fresh = helper.getBlockEntity(pos, BlockEntity.class);
			fresh.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, saved.get(pos)));
			fresh.setChanged();
		}
	}
}
