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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.SparkTinkererBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class SparkTinkererBlockEntityRenderer extends BotaniaBlockEntityRenderer<SparkTinkererBlockEntity> {

	public SparkTinkererBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable SparkTinkererBlockEntity tileentity, float pticks, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();
		ms.rotate(VecHelper.rotateX(90));
		ms.translate(1.0F, -0.125F, -0.25F);
		ItemStack stack = tileentity.getItemHandler().getItem(0);
		if (!stack.isEmpty()) {
			ms.rotate(VecHelper.rotateY(180));
			ms.translate(0.5F, 0.5F, 0);
			RenderHelper.submitItem(stack, ItemDisplayContext.GROUND, ms, buffers, tileentity.getLevel(), light, overlay, 0);
		}
		ms.popPose();
	}

}
