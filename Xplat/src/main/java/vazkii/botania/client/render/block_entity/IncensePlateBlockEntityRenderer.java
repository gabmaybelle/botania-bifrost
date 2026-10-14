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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.IncensePlateBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class IncensePlateBlockEntityRenderer extends BotaniaBlockEntityRenderer<IncensePlateBlockEntity> {
	public IncensePlateBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable IncensePlateBlockEntity plate, float ticks, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ItemStack stack = plate.getItemHandler().getItem(0);
		if (stack.isEmpty()) {
			return;
		}

		Direction facing = plate.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

		ms.pushPose();
		ms.translate(0.5F, 1.5F, 0.5F);
		int degrees = switch (facing) {
			case WEST -> 90;
			case SOUTH -> 180;
			case EAST -> 270;
			default -> 0;
		};
		ms.rotate(VecHelper.rotateY(degrees));
		float s = 0.6F;
		ms.translate(-0.11F, -1.35F, 0F);
		ms.scale(s, s, s);
		RenderHelper.submitItem(stack, ItemDisplayContext.GROUND, ms, buffers, plate.getLevel(), light, overlay, 0);
		ms.popPose();
	}

}
