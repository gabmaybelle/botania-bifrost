/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import vazkii.botania.common.block.BotaniaGrassBlock;

// Self-mixin to implement a method which has a forge-only param
@Mixin(BotaniaGrassBlock.class)
public abstract class BotaniaGrassBlockNeoForgeMixin implements IBlockExtension {
	@Override
	public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
		// PORT-26.3: HoeItem is gone; this is what its onlyIfAirAbove checked.
		if (itemAbility == ItemAbilities.HOE_TILL && context.getClickedFace() != Direction.DOWN
				&& context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
			return Blocks.FARMLAND.defaultBlockState();
		} else if (itemAbility == ItemAbilities.SHOVEL_FLATTEN) {
			return Blocks.DIRT_PATH.defaultBlockState();
		}
		return null;
	}
}
