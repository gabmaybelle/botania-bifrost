/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.AnimatedTorchBlock;
import vazkii.botania.common.block.block_entity.AnimatedTorchBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class AnimatedTorchBlockEntityRenderer extends BotaniaBlockEntityRenderer<AnimatedTorchBlockEntity> {
	private static final double ANIM_HORIZONTAL_TICK_SCALE = 0.05;
	private static final double ANIM_VERTICAL_TICK_SCALE = 0.04;
	private static final double ANIM_TICK_CYCLE = 2 * Math.PI / ANIM_HORIZONTAL_TICK_SCALE / ANIM_VERTICAL_TICK_SCALE;

	public AnimatedTorchBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable AnimatedTorchBlockEntity torch, float partialTicks, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();

		Level level = torch.getLevel();
		double time = level == null
				? 0
				: ClientTickHandler.getEntityTicksInGame() % ANIM_TICK_CYCLE
						+ partialTicks
						+ new Random(torch.getBlockState().getSeed(torch.getBlockPos())).nextDouble(ANIM_TICK_CYCLE);

		ms.translate(0.5, 0.2, 0.5);
		ms.rotate(VecHelper.rotateX(90));

		if (level != null) {
			float targetRotation = torch.getBlockState().getValue(AnimatedTorchBlock.FACING).toYRot();
			if (torch.lastRotation == -1) {
				// clientside rotation was not initialized yet
				torch.lastRotation = targetRotation;
				torch.rotation = targetRotation;
			} else if (torch.rotation != targetRotation) {
				float rotationDiff = targetRotation - torch.lastRotation;
				if (rotationDiff > 180) {
					rotationDiff -= 360;
				} else if (rotationDiff < -180) {
					rotationDiff += 360;
				}
				float rotationProgress = Math.clamp(
						((level.getGameTime() - torch.rotationStartTime) + partialTicks)
								/ AnimatedTorchBlockEntity.ROTATION_TICKS,
						0, 1);
				if (rotationProgress >= 1) {
					torch.lastRotation = targetRotation;
					torch.rotation = targetRotation;
				} else {
					torch.rotation = torch.lastRotation + rotationProgress * rotationDiff;
				}
			}
			ms.rotate(VecHelper.rotateZ(torch.rotation));
		}

		double xt = -0.5 + Math.cos(time * ANIM_HORIZONTAL_TICK_SCALE) * 0.025;
		double yt = -0.3 + Math.sin(time * ANIM_VERTICAL_TICK_SCALE) * 0.05;
		double zt = -0.5 + Math.sin(time * ANIM_HORIZONTAL_TICK_SCALE) * 0.025;
		ms.translate(xt, yt, zt);

		BlockState torchBaseState = Blocks.REDSTONE_TORCH.defaultBlockState();
		BlockState torchState = torch.getBlockState().getValue(AnimatedTorchBlock.TRIGGERED)
				? torchBaseState.setValue(RedstoneTorchBlock.LIT, false)
				: torchBaseState;
		RenderHelper.submitBlockState(torchState, ms, buffers, light, overlay);
		ms.popPose();
	}

}
