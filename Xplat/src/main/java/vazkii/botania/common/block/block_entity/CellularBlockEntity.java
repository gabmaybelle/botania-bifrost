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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.flower.generating.DandelifeonBlockEntity;
import vazkii.botania.common.block.block_entity.flower.generating.DandelifeonBlockEntity.Cell;

public class CellularBlockEntity extends BlockEntity {
	private static final String TAG_GENERATION = "generation";
	private static final String TAG_TICKED = "ticked";
	private static final String TAG_FLOWER_X = "flowerX";
	private static final String TAG_FLOWER_Y = "flowerY";
	private static final String TAG_FLOWER_Z = "flowerZ";
	private static final String TAG_VALID_X = "validX";
	private static final String TAG_VALID_Y = "validY";
	private static final String TAG_VALID_Z = "validZ";

	private int generation;
	private int nextGeneration;
	private boolean ticked;
	@Nullable
	private BlockPos flowerCoords;
	@Nullable
	private BlockPos validCoords;

	public CellularBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CELLULAR_BLOCK, pos, state);
	}

	public void setGeneration(int gen) {
		generation = gen;
	}

	public void setNextGeneration(DandelifeonBlockEntity flower, int gen) {
		nextGeneration = gen;
		getLevel().scheduleTick(getBlockPos(), BotaniaBlocks.CELLULAR_BLOCK, 1);
		if (!ticked) {
			claim(flower);
			ticked = true;
		} else if (!getBlockPos().equals(validCoords) || !flower.getEffectivePos().equals(flowerCoords)) {
			level.removeBlock(worldPosition, false);
		}
	}

	public void claim(DandelifeonBlockEntity flower) {
		if (!ticked) {
			flowerCoords = flower.getEffectivePos();
			validCoords = getBlockPos();
		}
	}

	public void update(Level level) {
		if (nextGeneration == Cell.DEAD) {
			level.removeBlock(getBlockPos(), false);
		}
		generation = nextGeneration;
	}

	public boolean hasActiveParent(DandelifeonBlockEntity dandie) {
		return flowerCoords != null
				&& dandie.getLevel().getBlockEntity(flowerCoords) instanceof DandelifeonBlockEntity parent
				&& parent.shouldUpdateThisTick() && parent.isPowered();
	}

	public int getGeneration() {
		return generation;
	}

	@Override
	protected void saveAdditional(ValueOutput cmp) {
		cmp.putInt(TAG_GENERATION, generation);
		cmp.putBoolean(TAG_TICKED, ticked);
		if (ticked) {
			if (flowerCoords != null) {
				cmp.putInt(TAG_FLOWER_X, flowerCoords.getX());
				cmp.putInt(TAG_FLOWER_Y, flowerCoords.getY());
				cmp.putInt(TAG_FLOWER_Z, flowerCoords.getZ());
			}
			if (validCoords != null) {
				cmp.putInt(TAG_VALID_X, validCoords.getX());
				cmp.putInt(TAG_VALID_Y, validCoords.getY());
				cmp.putInt(TAG_VALID_Z, validCoords.getZ());
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput cmp) {
		generation = cmp.getIntOr(TAG_GENERATION, 0);
		ticked = cmp.getBooleanOr(TAG_TICKED, false);
		if (ticked) {
			if (cmp.getInt(TAG_FLOWER_X).isPresent()) {
				flowerCoords = new BlockPos(
						cmp.getIntOr(TAG_FLOWER_X, 0),
						cmp.getIntOr(TAG_FLOWER_Y, 0),
						cmp.getIntOr(TAG_FLOWER_Z, 0)
				);
			}
			if (cmp.getInt(TAG_VALID_X).isPresent()) {
				validCoords = new BlockPos(
						cmp.getIntOr(TAG_VALID_X, 0),
						cmp.getIntOr(TAG_VALID_Y, 0),
						cmp.getIntOr(TAG_VALID_Z, 0)
				);
			}
		}
	}

	// PORT-26.3: onlyOpCanSetNbt moved to BlockEntityType and vanilla keeps a fixed list there, so the Create
	// workaround ("sorry about any instances of https://xkcd.com/1172/") has no hook any more.
}
