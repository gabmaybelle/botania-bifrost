/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.flower.functional.BubbellBlockEntity;

public class FakeAirBlockEntity extends BlockEntity {
	private static final String TAG_FLOWER_X = "flowerX";
	private static final String TAG_FLOWER_Y = "flowerY";
	private static final String TAG_FLOWER_Z = "flowerZ";

	@Nullable
	private BlockPos flowerPos;

	public FakeAirBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.FAKE_AIR, pos, state);
	}

	public void setFlower(BlockEntity tile) {
		flowerPos = tile.getBlockPos();
		setChanged();
	}

	public boolean canStay() {
		return flowerPos != null && BubbellBlockEntity.isValidBubbell(level, flowerPos);
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		if (flowerPos != null) {
			tag.putInt(TAG_FLOWER_X, flowerPos.getX());
			tag.putInt(TAG_FLOWER_Y, flowerPos.getY());
			tag.putInt(TAG_FLOWER_Z, flowerPos.getZ());
		}
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		flowerPos = tag.getInt(TAG_FLOWER_X).isPresent()
				? new BlockPos(
						tag.getIntOr(TAG_FLOWER_X, 0),
						tag.getIntOr(TAG_FLOWER_Y, 0),
						tag.getIntOr(TAG_FLOWER_Z, 0)
				)
				: null;
	}

}
