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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.TeruTeruBozuModel;
import vazkii.botania.common.block.block_entity.TeruTeruBozuBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class TeruTeruBozuBlockEntityRenderer extends BotaniaBlockEntityRenderer<TeruTeruBozuBlockEntity> {

	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_TERU_TERU_BOZU);
	private static final Identifier textureHalloween = Identifier.parse(ResourcesLib.MODEL_TERU_TERU_BOZU_HALLOWEEN);
	private final TeruTeruBozuModel model;

	public TeruTeruBozuBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		model = new TeruTeruBozuModel(ctx.bakeLayer(BotaniaModelLayers.TERU_TERU_BOZU));
	}

	@Override
	public void render(@Nullable TeruTeruBozuBlockEntity teruTeruBozu, float partialTicks, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();
		ms.rotate(VecHelper.rotateX(180));
		double time = ClientTickHandler.getEntityTicksInGame() + partialTicks;
		boolean hasWorld = teruTeruBozu != null && teruTeruBozu.getLevel() != null;
		if (hasWorld) {
			time += new Random(teruTeruBozu.getBlockPos().hashCode()).nextInt(1000);
		}

		ms.translate(0.5F, -1.25F + (hasWorld ? (float) Math.sin(time * 0.01F) * 0.05F : 0F), -0.5F);
		if (hasWorld) {
			ms.rotate(VecHelper.rotateY((float) (time * 0.3)));
			ms.rotate(VecHelper.rotateZ(4F * (float) Math.sin(time * 0.05F)));
			float s = 0.75F;
			ms.scale(s, s, s);
		}

		model.setRaining(Minecraft.getInstance().level != null && Minecraft.getInstance().level.isRaining());
		RenderHelper.submit(ms, buffers, model.renderType(ClientProxy.dootDoot ? textureHalloween : texture),
				(ps, buffer) -> model.renderToBuffer(ps, buffer, light, overlay, ARGB.colorFromFloat(1, 1, 1, 1)));
		ms.popPose();
	}

}
