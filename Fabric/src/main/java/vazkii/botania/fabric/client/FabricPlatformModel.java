/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.client;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.function.Predicate;

/**
 * PORT-26.3: wraps the baked platform block model (model loading plugin, after bake) and draws the camouflaged
 * block's model instead, read from the block entity's render data.
 */
public class FabricPlatformModel extends WrapperBlockStateModel {
	public FabricPlatformModel(BlockStateModel original) {
		super(original);
	}

	@Nullable
	private static BlockState camouflage(BlockAndTintGetter blockView, BlockPos pos) {
		Object data = ((FabricBlockGetter) blockView).getBlockEntityRenderData(pos);
		if (data instanceof PlatformBlockEntity.PlatformData platformData) {
			BlockState heldState = platformData.state();
			if (heldState != null && !heldState.is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
				return heldState;
			}
		}
		return null;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
		if (!(state.getBlock() instanceof PlatformBlock)) {
			super.emitQuads(emitter, blockView, pos, state, random, cullTest);
			return;
		}

		Object data = ((FabricBlockGetter) blockView).getBlockEntityRenderData(pos);
		if (data instanceof PlatformBlockEntity.PlatformData platformData) {
			BlockState heldState = camouflage(blockView, pos);
			if (heldState == null) {
				// No camo
				super.emitQuads(emitter, blockView, pos, state, random, cullTest);
			} else {
				// Steal camo's model
				BlockStateModel model = RenderHelper.getBlockStateModel(heldState);
				((FabricBlockStateModel) model).emitQuads(emitter, blockView, platformData.pos(), heldState, random, cullTest);
			}
		}
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		BlockState heldState = camouflage(blockView, pos);
		return heldState == null
				? super.particleMaterial(blockView, pos, state)
				: RenderHelper.getBlockStateModel(heldState).particleMaterial();
	}
}
