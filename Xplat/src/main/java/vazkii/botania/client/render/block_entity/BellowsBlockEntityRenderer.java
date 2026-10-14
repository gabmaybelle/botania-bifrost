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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BellowsModel;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.common.block.block_entity.mana.BellowsBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class BellowsBlockEntityRenderer extends BotaniaBlockEntityRenderer<BellowsBlockEntity> {
	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_BELLOWS);
	private final BellowsModel model;

	public BellowsBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new BellowsModel(ctx.bakeLayer(BotaniaModelLayers.BELLOWS));
	}

	@Override
	public void render(@Nullable BellowsBlockEntity bellows, float f, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();
		ms.translate(0.5F, 1.5F, 0.5F);
		ms.scale(1F, -1F, -1F);
		float angle = 0;
		if (bellows != null) {
			switch (bellows.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
				case SOUTH:
					break;
				case NORTH:
					angle = 180F;
					break;
				case EAST:
					angle = 270F;
					break;
				case WEST:
					angle = 90F;
					break;
			}
		}
		ms.rotate(VecHelper.rotateY(angle));
		float fract = Math.max(0.1F, 1F - (bellows == null ? 0 : bellows.movePos + bellows.moving * f + 0.1F));
		RenderHelper.submit(ms, buffers, model.renderType(texture), (ps, buffer) -> model.render(ps, buffer, light, overlay, 0xFFFFFF, fract));
		ms.popPose();
	}

}
