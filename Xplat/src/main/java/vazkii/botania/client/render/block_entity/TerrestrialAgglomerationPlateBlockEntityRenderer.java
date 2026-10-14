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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class TerrestrialAgglomerationPlateBlockEntityRenderer extends BotaniaBlockEntityRenderer<TerrestrialAgglomerationPlateBlockEntity> {
	private final TextureAtlasSprite overlaySprite;

	public TerrestrialAgglomerationPlateBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.overlaySprite = Objects.requireNonNull(
				RenderHelper.getBlockSprite(ctx.sprites(), botaniaRL("block/terrestrial_agglomeration_plate_overlay"))
		);
	}

	@Override
	public void render(@Nullable TerrestrialAgglomerationPlateBlockEntity plate, float partialTick, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight, int packedOverlay, @Nullable CameraRenderState camera) {
		float alphaMod = Math.min(1.0F, plate.getCompletion() / 0.1F);

		poseStack.pushPose();
		poseStack.translate(0F, 3F / 16F + 0.001F, 0F);
		poseStack.rotate(VecHelper.rotateX(90F));

		float alpha = (float) ((Math.sin((ClientTickHandler.getEntityTicksInGame() + partialTick) / 8D) + 1D) / 5D + 0.6D) * alphaMod;

		RenderHelper.submit(poseStack, bufferSource, RenderHelper.TERRA_PLATE,
				(ps, buffer) -> RenderHelper.renderIconFullBright(ps, buffer, this.overlaySprite, alpha));

		poseStack.popPose();
	}

}
