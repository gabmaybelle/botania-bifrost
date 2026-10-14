/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.FakeAirBlockEntity;

public class FakeAirBlock extends AirBlock implements EntityBlock, LiquidBlockContainer {

	public FakeAirBlock(Properties properties) {
		super(properties);
	}

	@Override
	public MutableComponent getName() {
		return Blocks.AIR.getName();
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation neighborPos, boolean movedByPiston) {
		if (shouldRemove(level, pos)) {
			level.scheduleTick(pos, this, 4);
		}
	}

	private boolean shouldRemove(Level world, BlockPos pos) {
		return !(world.getBlockEntity(pos) instanceof FakeAirBlockEntity fakeAir) || !fakeAir.canStay();
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (shouldRemove(level, pos)) {
			level.setBlockAndUpdate(pos, random.nextInt(10) == 0 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FakeAirBlockEntity(pos, state);
	}

	@Override
	public boolean canPlaceLiquid(@Nullable LivingEntity player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
		return false;
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
		return false;
	}
}
