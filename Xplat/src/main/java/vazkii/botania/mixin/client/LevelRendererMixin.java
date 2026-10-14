/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.render.world.WorldOverlays;

/**
 * This Mixin implements some in-world overlays (the Garden of Glass skybox lives in {@link SkyRendererMixin} since
 * 26.3).
 */
@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {
	/**
	 * PORT-26.3: the world is drawn from submitted requests now; the overlays are submitted with everything else, after
	 * the entities, block entities and particles, and the game sorts and draws them in the right passes.
	 */
	@Inject(method = "submitFeatures", at = @At("TAIL"))
	private void renderOverlays(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		PoseStack poseStack = new PoseStack();
		WorldOverlays.renderWorldLast(levelRenderState.cameraRenderState,
				mc.getDeltaTracker().getGameTimeDeltaPartialTick(false),
				mc.getDeltaTracker().getGameTimeDeltaPartialTick(true),
				poseStack, submitNodeCollector, mc.level);
	}
}
