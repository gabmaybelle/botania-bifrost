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

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.GaiaTrapEntity;

public class GaiaTrapRenderer extends EntityRenderer<GaiaTrapEntity, GaiaTrapRenderState> {
	public GaiaTrapRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public GaiaTrapRenderState createRenderState() {
		return new GaiaTrapRenderState();
	}

	@Override
	public void extractRenderState(GaiaTrapEntity e, GaiaTrapRenderState state, float partialTicks) {
		super.extractRenderState(e, state, partialTicks);
		state.box = e.getBoundingBox().move(e.position().scale(-1));
		state.tickCount = e.tickCount;
		state.partialTick = partialTicks;
	}

	@Override
	public void submit(GaiaTrapRenderState state, PoseStack ms, SubmitNodeCollector buffers, CameraRenderState camera) {
		super.submit(state, ms, buffers, camera);
		float partialTicks = state.partialTick;
		ms.pushPose();
		AABB aabb = state.box;

		float gs = (float) (Math.sin((ClientTickHandler.getEntityTicksInGame() + partialTicks) / 20) + 1) * 0.2F + 0.6F;
		int r = (int) (105 * gs);
		int g = (int) (25 * gs);
		int b = (int) (145 * gs);
		int alpha = 32;
		if (state.tickCount < 8) {
			alpha = (int) (alpha * Math.min((state.tickCount + partialTicks) / 8F, 1F));
		} else if (state.tickCount > 47) {
			alpha = (int) (alpha * Math.min(1F - (state.tickCount - 47 + partialTicks) / 8F, 1F));
		}

		ms.translate(aabb.minX, aabb.minY + RenderHelper.getOffY(), aabb.minZ);
		float f = 1F / 16F;
		float x = (float) (aabb.getXsize() - f);
		float z = (float) (aabb.getZsize() - f);
		int finalAlpha = alpha;
		RenderHelper.submit(ms, buffers, RenderHelper.RECTANGLE,
				(ps, buffer) -> RenderHelper.flatRectangle(buffer, ps.last().pose(), f, x, 0, f, z, r, g, b, finalAlpha));
		RenderHelper.incrementOffY();
		ms.popPose();
	}
}
