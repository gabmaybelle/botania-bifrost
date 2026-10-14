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

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.ManaStormEntity;

public class ManaStormRenderer extends EntityRenderer<ManaStormEntity, ManaStormRenderState> {
	public ManaStormRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public ManaStormRenderState createRenderState() {
		return new ManaStormRenderState();
	}

	@Override
	public void extractRenderState(ManaStormEntity storm, ManaStormRenderState state, float partialTick) {
		super.extractRenderState(storm, state, partialTick);
		state.burstsFired = storm.burstsFired;
		state.deathTime = storm.deathTime;
		state.seed = storm.getUUID().getMostSignificantBits();
		state.partialTick = partialTick;
	}

	@Override
	public void submit(ManaStormRenderState state, PoseStack ms, SubmitNodeCollector buffers, CameraRenderState camera) {
		ms.pushPose();
		float maxScale = 1.95F;
		float scale = 0.05F + ((float) state.burstsFired / ManaStormEntity.TOTAL_BURSTS - (state.deathTime == 0 ? 0 : state.deathTime + state.partialTick) / ManaStormEntity.DEATH_TIME) * maxScale;
		RenderHelper.renderStar(ms, buffers, 0x00FF00, scale, scale, scale, state.seed, state.partialTick);
		ms.popPose();
		super.submit(state, ms, buffers, camera);
	}
}
