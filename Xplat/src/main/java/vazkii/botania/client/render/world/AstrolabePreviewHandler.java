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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.item.AstrolabeItem;

import java.util.List;

public final class AstrolabePreviewHandler {
	// PORT-26.3: the preview layer no longer carries its 40% alpha; it goes in as the colour of every quad instead.
	// The block's own tints (grass, leaves) are lost for the preview.
	private static final int PREVIEW_COLOR = ARGB.colorFromFloat(0.4F, 1F, 1F, 1F);

	public static void onWorldRenderLast(Vec3 cameraPos, PoseStack ms, SubmitNodeCollector collector, Level level) {
		for (Player player : level.players()) {
			ItemStack currentStack = player.getMainHandItem();
			InteractionHand hand = InteractionHand.MAIN_HAND;
			if (currentStack.isEmpty() || !(currentStack.getItem() instanceof AstrolabeItem)) {
				currentStack = player.getOffhandItem();
				hand = InteractionHand.OFF_HAND;
			}

			if (!currentStack.isEmpty() && currentStack.getItem() instanceof AstrolabeItem) {
				Block block = AstrolabeItem.getBlock(currentStack, level.holderLookup(Registries.BLOCK));
				if (block != Blocks.AIR) {
					renderPlayerLook(cameraPos, ms, collector, player, currentStack, hand);
				}
			}
		}
	}

	private static void renderPlayerLook(Vec3 cameraPos, PoseStack ms, SubmitNodeCollector collector, Player player, ItemStack stack, InteractionHand hand) {
		Block blockToPlace = AstrolabeItem.getBlock(stack, player.level().holderLookup(Registries.BLOCK));
		int size = AstrolabeItem.getSize(stack);
		BlockPlaceContext ctx = AstrolabeItem.getBlockPlaceContext(player, hand, blockToPlace);
		List<BlockPos> placePositions = AstrolabeItem.getPlacePositions(ctx, size);

		if (ctx != null && AstrolabeItem.hasBlocks(stack, player, placePositions.size(), blockToPlace)) {
			for (BlockPos pos : placePositions) {
				BlockPlaceContext placeContext = getPlaceContext(player, ctx, pos);
				BlockState state = blockToPlace.getStateForPlacement(placeContext);
				if (state != null && placeContext.canPlace() && state.canSurvive(player.level(), pos)) {
					renderBlockAt(cameraPos, ms, collector, state, pos);
				}
			}
		}
	}

	private static BlockPlaceContext getPlaceContext(Player player, BlockPlaceContext ctx, BlockPos pos) {
		Vec3 newHitVec = new Vec3(pos.getX() + Mth.frac(ctx.getClickLocation().x()),
				pos.getY() + Mth.frac(ctx.getClickLocation().y()),
				pos.getZ() + Mth.frac(ctx.getClickLocation().z()));
		BlockHitResult newHit = new BlockHitResult(newHitVec, ctx.getClickedFace(), pos, false);
		return new BlockPlaceContext(player, ctx.getHand(), ctx.getItemInHand(), newHit);
	}

	private static void renderBlockAt(Vec3 cameraPos, PoseStack ms, SubmitNodeCollector collector, BlockState state, BlockPos pos) {
		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
		ms.translate(pos.getX(), pos.getY(), pos.getZ());
		// always use the translucent layer so blending is turned on
		RenderHelper.submitBlockModel(RenderHelper.getBlockStateModel(state), ms, collector, RenderHelper.ASTROLABE_PREVIEW, PREVIEW_COLOR, 0xF000F0, OverlayTexture.NO_OVERLAY);
		ms.popPose();
	}
}
