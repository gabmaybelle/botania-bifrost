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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TeruTeruBozuBlockEntity extends BlockEntity {
	private boolean wasRaining = false;

	public TeruTeruBozuBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.TERU_TERU_BOZU, pos, state);
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state, TeruTeruBozuBlockEntity self) {
		boolean isRaining = level.isRaining();
		if (isRaining && level.getRandom().nextInt(9600) == 0) {
			setRaining(level, false);
			resetRainTime(level);
		}

		if (self.wasRaining != isRaining) {
			level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
		}
		self.wasRaining = isRaining;
	}

	// PORT-26.3: the weather is saved data on the server level now (Level#getLevelData has no weather)
	public static void setRaining(Level level, boolean raining) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.getWeatherData().setRaining(raining);
		}
	}

	public static void resetRainTime(Level w) {
		int time = w.getRandom().nextInt(w.isRaining() ? 12000 : 168000) + 12000;
		if (w instanceof ServerLevel serverLevel) {
			serverLevel.getWeatherData().setRainTime(time);
		}
	}
}
