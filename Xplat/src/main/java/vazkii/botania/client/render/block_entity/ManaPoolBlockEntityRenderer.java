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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import vazkii.botania.api.mana.PoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.helper.VecHelper;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ManaPoolBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaPoolBlockEntity> {

	// Overrides for when we call this renderer from a cart
	public static int cartMana = -1;
	public static int cartMaxMana = -1;
	@UnknownNullability
	public static ManaPoolBlock cartBlock;

	private final TextureAtlasSprite waterSprite;
	private final SpriteGetter sprites;

	public ManaPoolBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.sprites = ctx.sprites();
		this.waterSprite = Objects.requireNonNull(
				RenderHelper.getBlockSprite(ctx.sprites(), botaniaRL("block/mana_water"))
		);
	}

	@Override
	public void render(@Nullable ManaPoolBlockEntity pool, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();
		Minecraft minecraft = Minecraft.getInstance();

		boolean fabulous;
		VoxelShape innerShape;
		VoxelShape outerShape;
		int mana;
		int maxMana;
		BlockState state;
		Level level;

		if (pool != null) {
			level = pool.getLevel() != null ? pool.getLevel() : minecraft.level;
			state = pool.getBlockState();
			ManaPoolBlock block = (ManaPoolBlock) state.getBlock();
			fabulous = block.isFabulous();
			innerShape = block.getInnerShape(pool.getBlockState());
			outerShape = block.getInteractionShape(pool.getBlockState(), pool.getLevel(), pool.getBlockPos());
			mana = pool.getCurrentMana();
			maxMana = pool.getMaxMana();
		} else {
			level = minecraft.level;
			ManaPoolBlock block = cartBlock;
			state = block.defaultBlockState();
			fabulous = block.isFabulous();
			innerShape = block.getInnerShape(block.defaultBlockState());
			outerShape = block.getInteractionShape(block.defaultBlockState(), null, null);
			mana = cartMana;
			maxMana = cartMaxMana == -1 ? ManaPoolBlock.MAX_MANA : cartMaxMana;
		}

		float poolBottom = (float) innerShape.min(Direction.Axis.Y) + 0.001F;
		float poolTop = (float) outerShape.max(Direction.Axis.Y) - 1 / 16f;
		int uvStartX = (int) (16 * innerShape.min(Direction.Axis.X));
		int uvStartY = (int) (16 * innerShape.min(Direction.Axis.Z));
		int uvEndX = (int) (16 * innerShape.max(Direction.Axis.X));
		int uvEndY = (int) (16 * innerShape.max(Direction.Axis.Z));

		if (fabulous) {
			// PORT-26.3: the block model carries its own tint now
			RenderHelper.submitBlockState(state, ms, buffers, light, overlay);
		}

		if (pool != null) {
			Block below = pool.getLevel().getBlockState(pool.getBlockPos().below()).getBlock();
			if (below instanceof PoolOverlayProvider overlayProvider) {
				var overlaySpriteId = overlayProvider.getIcon(pool.getLevel(), pool.getBlockPos());
				var overlayIcon = RenderHelper.getBlockSprite(this.sprites, overlaySpriteId);
				ms.pushPose();

				float alpha = (float) ((Mth.sin((ClientTickHandler.getEntityTicksInGame() + partialTick) / 20.0f) + 1) * 0.3 + 0.2);

				ms.translate(0, poolBottom, 0);
				ms.rotate(VecHelper.rotateX(90F));

				RenderHelper.submit(ms, buffers, RenderHelper.ICON_OVERLAY, (ps, buffer) -> RenderHelper.renderIconCropped(
						ps, buffer,
						uvStartX, uvStartY, uvEndX, uvEndY,
						overlayIcon, 0xFFFFFF, alpha, light
				));

				ms.popPose();
			}
		}

		float manaLevel = (float) mana / (float) maxMana;
		if (manaLevel > 0) {
			ms.pushPose();
			ms.translate(0, Mth.clampedMap(manaLevel, 0, 1, poolBottom, poolTop), 0);
			ms.rotate(VecHelper.rotateX(90F));

			RenderHelper.submit(ms, buffers, RenderHelper.MANA_POOL_WATER, (ps, buffer) -> RenderHelper.renderIconCropped(
					ps, buffer,
					uvStartX, uvStartY, uvEndX, uvEndY,
					this.waterSprite, 0xFFFFFF, 1, light));

			ms.popPose();
		}
		ms.popPose();

		cartMana = -1;
		cartMaxMana = -1;
		cartBlock = null;
	}

}
