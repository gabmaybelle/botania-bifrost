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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.item.ItemEntity;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.ManaEnchanterBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.mixin.ItemEntityAccessor;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ManaEnchanterBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaEnchanterBlockEntity> {

	private final TextureAtlasSprite overlaySprite;
	@Nullable
	private ItemEntity item;

	public ManaEnchanterBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.overlaySprite = Objects.requireNonNull(
				RenderHelper.getBlockSprite(ctx.sprites(), botaniaRL("block/mana_enchanter_overlay"))
		);
	}

	@Override
	public void render(@Nullable ManaEnchanterBlockEntity enchanter, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		float alphaMod = 0F;

		if (enchanter.stage == ManaEnchanterBlockEntity.State.GATHER_MANA) {
			alphaMod = Math.min(20, enchanter.stageTicks) / 20F;
		} else if (enchanter.stage == ManaEnchanterBlockEntity.State.RESET) {
			alphaMod = (20 - enchanter.stageTicks) / 20F;
		} else if (enchanter.stage == ManaEnchanterBlockEntity.State.DO_ENCHANT) {
			alphaMod = 1F;
		}

		ms.pushPose();
		int ticksInGame = ClientTickHandler.getEntityTicksInGame();
		if (!enchanter.itemToEnchant.isEmpty()) {
			if (item == null) {
				item = new ItemEntity(enchanter.getLevel(), enchanter.getBlockPos().getX(), enchanter.getBlockPos().getY() + 1, enchanter.getBlockPos().getZ(), enchanter.itemToEnchant);
			}

			((ItemEntityAccessor) item).botania_setAge(ticksInGame);
			item.setItem(enchanter.itemToEnchant);

			ms.translate(0.5F, 1.25F, 0.5F);
			RenderHelper.submitEntity(item, partialTick, ms, buffers, camera);
			ms.translate(-0.5F, -1.25F, -0.5F);
		}

		ms.rotate(VecHelper.rotateX(90F));
		ms.translate(-2F, -2F, -0.001F);

		float alpha = (float) ((Math.sin((ticksInGame + partialTick) / 8D) + 1D) / 5D + 0.4D) * alphaMod;

		if (alpha > 0) {
			if (enchanter.stage == ManaEnchanterBlockEntity.State.DO_ENCHANT || enchanter.stage == ManaEnchanterBlockEntity.State.RESET) {
				float ticks = enchanter.stageTicks + enchanter.stage3EndTicks + partialTick;
				float angle = ticks * 2;
				float yTranslation = Math.min(20, ticks) / 20F * 1.15F;
				float scale = ticks < 10 ? 1F : 1F - Math.min(20, ticks - 10) / 20F * 0.75F;

				ms.translate(2.5F, 2.5F, -yTranslation);
				ms.scale(scale, scale, 1F);
				ms.rotate(VecHelper.rotateZ(angle));
				ms.translate(-2.5F, -2.5F, 0F);
			}

			RenderHelper.submit(ms, buffers, RenderHelper.ENCHANTER, (ps, buffer) -> RenderHelper.renderIconFullBright(
					ps, buffer,
					0, 0, 5, 5,
					0, 0, 1, 1,
					this.overlaySprite, 0xFFFFFF, alpha, light
			));
		}

		ms.popPose();
	}

}
