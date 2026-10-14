/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.handler.ContributorList;

import java.util.Locale;

public final class ContributorFancinessHandler extends RenderLayer<AvatarRenderState, PlayerModel> {
	public ContributorFancinessHandler(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack ms, SubmitNodeCollector buffers, int light, AvatarRenderState state, float yRot, float xRot) {
		ContributorList.firstStart();

		if (state.isInvisible) {
			return;
		}
		// PORT-26.3: the layer only gets the render state; the name and flower come from the player it belongs to
		if (!(Minecraft.getInstance().level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
			return;
		}

		String name = player.getGameProfile().name();

		if (name.equals("haighyorkie")) {
			renderGoldfish(ms, buffers);
		}

		if (state.showCape) {
			ItemStack flower = ContributorList.getFlower(name.toLowerCase(Locale.ROOT));
			if (!flower.isEmpty()) {
				renderFlower(ms, buffers, player, flower);
			}
		}
	}

	private void renderGoldfish(PoseStack ms, SubmitNodeCollector buffers) {
		ms.pushPose();
		getParentModel().head.translateAndRotate(ms);
		ms.translate(-0.15F, -0.60F, 0F);
		ms.scale(0.4F, -0.4F, -0.4F);
		RenderHelper.submitBlockModel(MiscellaneousModels.INSTANCE.goldfishModel, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
		ms.popPose();
	}

	private void renderFlower(PoseStack ms, SubmitNodeCollector buffers, Player player, ItemStack flower) {
		ms.pushPose();
		getParentModel().head.translateAndRotate(ms);
		ms.translate(0, -0.75, 0);
		ms.scale(0.5F, -0.5F, -0.5F);
		RenderHelper.submitItem(flower, ItemDisplayContext.NONE, ms, buffers, player.level(), 0xF000F0, OverlayTexture.NO_OVERLAY, player.getId());
		ms.popPose();
	}
}
