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
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.flower.FloatingFlowerBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.Random;

public class FloatingFlowerBlockEntityRenderer extends BotaniaBlockEntityRenderer<FloatingFlowerBlockEntity> {

	public FloatingFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable FloatingFlowerBlockEntity tile, float t, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		renderFloatingIsland(tile, t, ms, buffers, light, overlay);
	}

	public static void renderFloatingIsland(BlockEntity tile, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay) {
		if (BotaniaConfig.client().staticFloaters()) {
			return;
		}

		ms.pushPose();

		double worldTime = ClientTickHandler.getEntityTicksInGame() + partialTick;
		if (tile.getLevel() != null) {
			worldTime += new Random(tile.getBlockPos().hashCode()).nextInt(1000);
		}

		ms.translate(0.5F, 0, 0.5F);
		ms.rotate(VecHelper.rotateY(-((float) worldTime * 0.5F)));
		ms.translate(-0.5, (float) Math.sin(worldTime * 0.05F) * 0.1F, 0.5);

		ms.rotate(VecHelper.rotateX(4F * (float) Math.sin(worldTime * 0.04F)));
		ms.rotate(VecHelper.rotateY(90.0F));

		// PORT-26.3: drawn as a plain block model; the world-aware tessellation (ambient occlusion) is gone with the old renderer
		RenderHelper.submitBlockState(tile.getBlockState(), ms, buffers, light, overlay);

		ms.popPose();
	}

}
