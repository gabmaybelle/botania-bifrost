/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;

/**
 * PORT-26.3: wraps the baked platform block model (swapped in from the baking result) and collects the camouflaged
 * block's parts instead, read from the block entity.
 */
public class NeoForgePlatformModel extends DelegateBlockStateModel {
	public NeoForgePlatformModel(BlockStateModel original) {
		super(original);
	}

	@Nullable
	private static BlockState camouflage(BlockAndTintGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PlatformBlockEntity platform) {
			BlockState heldState = new PlatformBlockEntity.PlatformData(platform).state();
			if (heldState != null && !heldState.is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
				return heldState;
			}
		}
		return null;
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		BlockState heldState = state.getBlock() instanceof PlatformBlock ? camouflage(level, pos) : null;
		if (heldState == null) {
			// No camo
			super.collectParts(level, pos, state, random, parts);
		} else {
			// Steal camo's model
			RenderHelper.getBlockStateModel(heldState).collectParts(level, pos, heldState, random, parts);
		}
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		BlockState heldState = camouflage(level, pos);
		return heldState == null
				? super.particleMaterial(level, pos, state)
				: RenderHelper.getBlockStateModel(heldState).particleMaterial(level, pos, heldState);
	}
}
