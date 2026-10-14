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
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ManaPumpBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaPumpBlockEntity> {
	public static final Identifier PUMP_HEAD_ID = botaniaRL("block/mana_pump_head");

	public ManaPumpBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable ManaPumpBlockEntity pump, float partialTicks, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();
		ms.translate(0.5, 0, 0.5);
		float angle = 0;
		switch (pump.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
			case SOUTH:
				angle = 180;
				break;
			case EAST:
				angle = -90;
				break;
			case WEST:
				angle = 90;
				break;
			case NORTH:
			default:
				break;
		}
		ms.rotate(VecHelper.rotateY(angle));
		ms.translate(-0.5, 0, -0.5);
		double diff = Math.max(0F, Math.min(8F, pump.innerRingPos + pump.moving * partialTicks));
		ms.translate(0, 0, diff / 14);
		RenderHelper.submitBlockModel(getHeadModel(), ms, buffers, light, overlay);
		ms.popPose();
	}

	private BlockStateModel getHeadModel() {
		return Objects.requireNonNull(ClientXplatAbstractions.instance().getResourceModel(PUMP_HEAD_ID));
	}
}
