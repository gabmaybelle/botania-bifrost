/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.fx.BoltRenderer;
import vazkii.botania.common.item.AssemblyHaloItem;

public final class WorldOverlays {
	public static void renderWorldLast(CameraRenderState camera, float entityPartialTick, float playerPartialTick, PoseStack matrix, SubmitNodeCollector collector, Level level) {
		BoltRenderer.onWorldRenderLast(camera.pos, entityPartialTick, matrix, collector);
		AssemblyHaloItem.Rendering.onRenderWorldLast(camera.pos, playerPartialTick, matrix, collector);
		BoundBlockRenderer.onWorldRenderLast(camera.pos, playerPartialTick, matrix, collector, level);
		AstrolabePreviewHandler.onWorldRenderLast(camera.pos, matrix, collector, level);
		RenderHelper.onWorldRenderLast();
	}

	private WorldOverlays() {}
}
