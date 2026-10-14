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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.HourglassModel;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.HoveringHourglassBlock;
import vazkii.botania.common.block.block_entity.HoveringHourglassBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class HoveringHourglassBlockEntityRenderer extends BotaniaBlockEntityRenderer<HoveringHourglassBlockEntity> {
	private static final double ANIM_HORIZONTAL_TICK_SCALE = 0.05;
	private static final double ANIM_VERTICAL_TICK_SCALE = 0.04;
	private static final double ANIM_TICK_CYCLE = 2 * Math.PI / ANIM_HORIZONTAL_TICK_SCALE / ANIM_VERTICAL_TICK_SCALE;

	final Identifier texture = Identifier.parse(ResourcesLib.MODEL_HOVERING_HOURGLASS);
	private final HourglassModel model;

	public HoveringHourglassBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new HourglassModel(ctx.bakeLayer(BotaniaModelLayers.HOURGLASS));
	}

	@Override
	public void render(@Nullable HoveringHourglassBlockEntity hourglass, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();

		Level level = hourglass != null ? hourglass.getLevel() : null;
		BlockState state = hourglass != null ? hourglass.getBlockState() : BotaniaBlocks.HOVERING_HOURGLASS.defaultBlockState();
		double time = level == null
				? 0
				: ClientTickHandler.getEntityTicksInGame() % ANIM_TICK_CYCLE
						+ partialTick
						+ new Random(state.getSeed(hourglass.getBlockPos())).nextDouble(ANIM_TICK_CYCLE);
		boolean flipped = state.getValue(HoveringHourglassBlock.FLIPPED);

		double xt = 0.5 + Math.cos(time * ANIM_HORIZONTAL_TICK_SCALE) * 0.025;
		double yt = 0.55 + Math.sin(time * ANIM_VERTICAL_TICK_SCALE) * 0.05;
		double zt = 0.5 + Math.sin(time * ANIM_HORIZONTAL_TICK_SCALE) * 0.025;
		ms.translate(xt, yt, zt);

		if (level != null) {
			float targetRotation = flipped ? 180 : 0;
			if (hourglass.lastRotation == -1) {
				// clientside rotation was not initialized yet
				hourglass.lastRotation = targetRotation;
				hourglass.rotation = targetRotation;
			} else if (hourglass.rotation != targetRotation) {
				float rotationDiff = targetRotation - hourglass.lastRotation;
				float rotationProgress = Math.clamp(
						((level.getGameTime() - hourglass.rotationStartTime) + partialTick)
								/ HoveringHourglassBlockEntity.FLIP_TICKS,
						0, 1);
				if (rotationProgress >= 1) {
					hourglass.lastRotation = targetRotation;
					hourglass.rotation = targetRotation;
				} else {
					hourglass.rotation = hourglass.lastRotation + rotationProgress * rotationDiff;
				}
			}
			ms.rotate(VecHelper.rotateZ(hourglass.rotation));
		}

		boolean active = state.getValue(HoveringHourglassBlock.ACTIVE);
		int totalTicks = hourglass != null ? hourglass.getTotalTime() : 0;
		float currentTicks = hourglass != null ? Math.max(0, hourglass.getTime() - (active ? partialTick : 0)) : 0;
		float bottomFraction = totalTicks > 0 ? currentTicks / totalTicks : 0;
		float topFraction = totalTicks > 0 ? 1 - bottomFraction : 0;

		int color = hourglass != null ? hourglass.getColor() : 0;

		ms.scale(1F, -1F, -1F);
		RenderHelper.submit(ms, buffers, model.renderType(texture),
				(ps, buffer) -> model.render(ps, buffer, light, overlay, color | 0xFF000000, topFraction, bottomFraction, flipped));
		ms.popPose();
	}

}
