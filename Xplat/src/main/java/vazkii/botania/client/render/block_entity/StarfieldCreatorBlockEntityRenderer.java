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
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.StarfieldCreatorBlockEntity;

public class StarfieldCreatorBlockEntityRenderer extends BotaniaBlockEntityRenderer<StarfieldCreatorBlockEntity> {
	public StarfieldCreatorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable StarfieldCreatorBlockEntity tile, float partialTicsk, PoseStack pose, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		// [VanillaCopy] Adapted from TheEndPortalRenderer, only renders UP face and sets the offset low in the blockspace
		float offset = 0.24F;
		RenderHelper.submit(pose, buffers, RenderHelper.STARFIELD,
				(ps, buffer) -> this.renderFace(buffer, ps.last().pose(), 0.0F, 1.0F, offset, offset, 1.0F, 1.0F, 0.0F, 0.0F));
	}

	private void renderFace(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, float i, float j, float k, float l, float m) {
		vertexConsumer.addVertex(matrix4f, f, h, j);
		vertexConsumer.addVertex(matrix4f, g, h, k);
		vertexConsumer.addVertex(matrix4f, g, i, l);
		vertexConsumer.addVertex(matrix4f, f, i, m);
	}
}
