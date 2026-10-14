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

import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.AlfheimPortalState;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class AlfheimPortalBlockEntityRenderer extends BotaniaBlockEntityRenderer<AlfheimPortalBlockEntity> {
	private final TextureAtlasSprite portalSprite;

	public AlfheimPortalBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.portalSprite = Objects.requireNonNull(
				RenderHelper.getBlockSprite(ctx.sprites(), botaniaRL("block/alfheim_portal_swirl"))
		);
	}

	@Override
	public void render(@Nullable AlfheimPortalBlockEntity portal, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		AlfheimPortalState state = portal.getBlockState().getValue(BotaniaStateProperties.ALFPORTAL_STATE);
		if (state == AlfheimPortalState.OFF) {
			return;
		}

		float alpha = Math.min(1, ((float) Math.sin((ClientTickHandler.getEntityTicksInGame() + partialTick) / 8) + 1) / 7 + 0.6f)
				* ((float) Math.min(AlfheimPortalBlockEntity.TICKS_UNTIL_FULLY_OPENED, portal.ticksOpen)
						/ AlfheimPortalBlockEntity.TICKS_UNTIL_FULLY_OPENED)
				* 0.5F;

		ms.pushPose();
		if (state == AlfheimPortalState.ON_X) {
			ms.translate(0.3125, 1, 2);
			ms.rotate(VecHelper.rotateY(90));
		} else {
			ms.translate(-1, 1, 0.3125);
		}
		renderIcon(ms, buffers, this.portalSprite, 0, 0, 3, 3, alpha, overlay);
		ms.popPose();

		ms.pushPose();
		if (state == AlfheimPortalState.ON_X) {
			ms.translate(0.6875, 1, -1);
			ms.rotate(VecHelper.rotateY(90));
		} else {
			ms.translate(2, 1, 0.6875);
		}
		ms.rotate(VecHelper.rotateY(180));
		renderIcon(ms, buffers, this.portalSprite, 0, 0, 3, 3, alpha, overlay);
		ms.popPose();
	}

	public void renderIcon(PoseStack ms, SubmitNodeCollector buffers, TextureAtlasSprite icon, int x, int y, int width, int height, float alpha, int overlay) {
		RenderHelper.submit(ms, buffers, RenderHelper.ICON_OVERLAY, (ps, buffer) -> {
			PoseStack.Pose pose = ps.last();
			buffer.addVertex(pose, x, y + height, 0).setColor(1, 1, 1, alpha).setUv(icon.getU0(), icon.getV1()).setOverlay(overlay).setLight(0xF000F0).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x + width, y + height, 0).setColor(1, 1, 1, alpha).setUv(icon.getU1(), icon.getV1()).setOverlay(overlay).setLight(0xF000F0).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x + width, y, 0).setColor(1, 1, 1, alpha).setUv(icon.getU1(), icon.getV0()).setOverlay(overlay).setLight(0xF000F0).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, y, 0).setColor(1, 1, 1, alpha).setUv(icon.getU0(), icon.getV0()).setOverlay(overlay).setLight(0xF000F0).setNormal(pose, 1, 0, 0);
		});
	}

}
