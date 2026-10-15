/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.commands.RenderPass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.render.world.SkyblockSkyRenderer;

/**
 * The Garden of Glass skybox: bigger sun and moon, planets, rays, a rainbow and lots of extra stars.
 * <p>
 * PORT-26.3: the sky moved out of {@code LevelRenderer} into {@code SkyRenderer}, which draws it in its own render
 * pass; the old {@code LevelRendererMixin} hooks on {@code renderSky} are these.
 */
@Mixin(value = SkyRenderer.class, priority = 900)
public class SkyRendererMixin {
	private static final String CELESTIAL_TRANSFORM = "Lnet/minecraft/client/renderer/SkyRenderer;applyCelestialBodyTransform(Lcom/mojang/blaze3d/vertex/PoseStack;FF)Lorg/joml/Matrix4f;";

	/** Make the sun bigger by scaling it to double size */
	@ModifyArg(method = "renderSun", at = @At(value = "INVOKE", target = CELESTIAL_TRANSFORM), index = 2, require = 0)
	private float botania_makeSunBigger(float scale) {
		return SkyblockSkyRenderer.isGogSky() ? scale * 2F : scale;
	}

	/** Make the moon bigger by scaling it to 1.5x size */
	@ModifyArg(method = "renderMoon", at = @At(value = "INVOKE", target = CELESTIAL_TRANSFORM), index = 2, require = 0)
	private float botania_makeMoonBigger(float scale) {
		return SkyblockSkyRenderer.isGogSky() ? scale * 1.5F : scale;
	}

	/** Write the extras' vertex buffers before vanilla opens its sky render pass */
	@Inject(method = "render", at = @At("HEAD"))
	private void botania_prepareExtras(CallbackInfo ci) {
		if (SkyblockSkyRenderer.isGogSky()) {
			SkyblockSkyRenderer.prepare(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
		}
	}

	/** Render planets and other extras, then lots of extra stars, once vanilla's sun, moon and stars are in */
	@Inject(method = "renderSunMoonAndStars", at = @At("TAIL"), require = 0)
	private void botania_renderExtras(CallbackInfo ci, @Local(argsOnly = true) RenderPass renderPass,
			@Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true, ordinal = 0) float sunAngle,
			@Local(argsOnly = true, ordinal = 2) float starAngle) {
		if (!SkyblockSkyRenderer.isGogSky()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		// the sun angle is the day fraction times a full turn, in radians
		float celAng = (float) (sunAngle / (2 * Math.PI));
		// the frame vanilla draws its sun, moon and stars in
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, -90.0F);
		SkyblockSkyRenderer.renderExtra(renderPass, poseStack, mc.level, celAng, partialTick, 0);
		poseStack.pushPose();
		poseStack.rotate(Axis.XP, starAngle);
		SkyRendererAccessor self = (SkyRendererAccessor) this;
		SkyblockSkyRenderer.renderStars(renderPass, poseStack, self.botania_getStarBuffer(), self.botania_getQuadIndices(),
				self.botania_getStarIndexCount(), celAng, partialTick);
		poseStack.popPose();
		poseStack.popPose();
	}
}
