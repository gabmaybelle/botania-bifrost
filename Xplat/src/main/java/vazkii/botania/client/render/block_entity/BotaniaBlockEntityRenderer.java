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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

/**
 * Base for Botania's block entity renderers.
 * <p>
 * PORT-26.3: vanilla's renderer is split into {@code extractRenderState} (copy values on the game thread) and
 * {@code submit} (hand drawing requests to the collector). This base keeps Botania's one {@link #render} method,
 * which also serves the item form (block entity null), and drives it from the two vanilla steps.
 */
public abstract class BotaniaBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, BotaniaBlockEntityRenderState<T>> {
	@Override
	public BotaniaBlockEntityRenderState<T> createRenderState() {
		return new BotaniaBlockEntityRenderState<>();
	}

	@Override
	public void extractRenderState(T blockEntity, BotaniaBlockEntityRenderState<T> state, float partialTick, Vec3 cameraPosition,
			@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
		state.blockEntity = blockEntity;
		state.partialTick = partialTick;
	}

	@Override
	public void submit(BotaniaBlockEntityRenderState<T> state, PoseStack ms, SubmitNodeCollector collector, CameraRenderState camera) {
		render(state.blockEntity, state.partialTick, ms, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, camera);
	}

	/**
	 * Draws the block entity, or its item form when {@code blockEntity} is null.
	 *
	 * @param camera the camera for this frame; null when drawing the item form
	 */
	public abstract void render(@Nullable T blockEntity, float partialTick, PoseStack ms, SubmitNodeCollector collector,
			int light, int overlay, @Nullable CameraRenderState camera);
}
