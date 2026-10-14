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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.CoordBoundItem;
import vazkii.botania.api.item.WireframeCoordinateListProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.List;

public final class BoundBlockRenderer {
	private BoundBlockRenderer() {}

	public static void onWorldRenderLast(Vec3 cameraPos, float partialTick, PoseStack ms, SubmitNodeCollector collector, Level level) {
		Player player = Minecraft.getInstance().player;
		if (!BotaniaConfig.client().boundBlockWireframe()
				|| player == null
				|| player.level() != level) {
			return;
		}

		ms.pushPose();
		int color = ARGB.opaque(Mth.hsvToRgb((ClientTickHandler.getEntityTicksInGame() + partialTick) % 200 / 200F, 0.6F, 1F));

		if (!player.getMainHandItem().isEmpty()) {
			var coordBoundItem = CoordBoundItem.LOOKUP.find(player.getMainHandItem(), player.level());
			if (coordBoundItem != null) {
				BlockPos coords = coordBoundItem.getBinding();
				if (coords != null) {
					renderBlockOutlineAt(cameraPos, ms, collector, player.level(), coords, color);
				}
			}
		}

		if (!player.getOffhandItem().isEmpty()) {
			var coordBoundItem = CoordBoundItem.LOOKUP.find(player.getOffhandItem(), player.level());
			if (coordBoundItem != null) {
				BlockPos coords = coordBoundItem.getBinding();
				if (coords != null) {
					renderBlockOutlineAt(cameraPos, ms, collector, player.level(), coords, color);
				}
			}
		}

		renderWireframeProviders(cameraPos, player.getInventory(), player, ms, collector, color);
		renderWireframeProviders(cameraPos, BotaniaAPI.instance().getAccessoriesInventory(player), player, ms, collector, color);

		ms.popPose();
	}

	private static void renderWireframeProviders(Vec3 cameraPos, Container inv,
			Player player, PoseStack ms, SubmitNodeCollector collector, int color) {
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stackInSlot = inv.getItem(i);

			if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof WireframeCoordinateListProvider provider) {
				List<BlockPos> coordsList = provider.getWireframesToDraw(player, stackInSlot);
				for (BlockPos coords : coordsList) {
					renderBlockOutlineAt(cameraPos, ms, collector, player.level(), coords, color);
				}

				BlockPos coords = provider.getSourceWireframe(player, stackInSlot);
				if (coords != null && coords.getY() != Integer.MIN_VALUE) {
					renderBlockOutlineAt(cameraPos, ms, collector, player.level(), coords, color, true);
				}
			}
		}
	}

	private static void renderBlockOutlineAt(Vec3 cameraPos, PoseStack ms, SubmitNodeCollector collector, Level level, BlockPos pos, int color) {
		renderBlockOutlineAt(cameraPos, ms, collector, level, pos, color, false);
	}

	private static void renderBlockOutlineAt(Vec3 cameraPos, PoseStack ms, SubmitNodeCollector collector, Level level, BlockPos pos, int color, boolean thick) {
		VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
		if (!shape.isEmpty()) {
			ms.pushPose();
			ms.translate(pos.getX() - cameraPos.x(), pos.getY() - cameraPos.y(), pos.getZ() - cameraPos.z());

			// PORT-26.3: vanilla submits shape outlines itself, with the line width per submit
			collector.submitShapeOutline(ms, shape, RenderHelper.LINES_NO_DEPTH, color, thick ? 5F : 1F, false);
			int alpha = 64;
			color = ARGB.color(alpha, color);
			collector.submitShapeOutline(ms, shape, RenderHelper.LINES_NO_DEPTH, color, thick ? 8F : 4F, false);

			ms.popPose();
		}
	}
}
