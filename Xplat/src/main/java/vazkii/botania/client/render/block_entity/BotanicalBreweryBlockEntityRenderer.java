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

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.BotanicalBreweryModel;
import vazkii.botania.common.block.block_entity.BotanicalBreweryBlockEntity;

public class BotanicalBreweryBlockEntityRenderer extends BotaniaBlockEntityRenderer<BotanicalBreweryBlockEntity> {
	final BotanicalBreweryModel model;

	public BotanicalBreweryBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new BotanicalBreweryModel(ctx.bakeLayer(BotaniaModelLayers.BREWERY));
	}

	@Override
	public void render(@Nullable BotanicalBreweryBlockEntity brewery, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();

		ms.scale(1F, -1F, -1F);
		ms.translate(0.5F, -1.5F, -0.5F);

		double time = ClientTickHandler.getEntityTicksInGame() + partialTick;

		model.render(brewery, time, ms, buffers, light, overlay);
		ms.popPose();
	}

}
