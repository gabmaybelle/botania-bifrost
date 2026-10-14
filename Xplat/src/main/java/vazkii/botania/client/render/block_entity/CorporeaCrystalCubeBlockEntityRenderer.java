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
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.corporea.CorporeaCrystalCubeBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.mixin.ItemEntityAccessor;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class CorporeaCrystalCubeBlockEntityRenderer extends BotaniaBlockEntityRenderer<CorporeaCrystalCubeBlockEntity> {
	public static final Identifier CUBE_MODEL_ID = botaniaRL("block/corporea_crystal_cube_glass");

	@Nullable
	private ItemEntity entity = null;

	public CorporeaCrystalCubeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable CorporeaCrystalCubeBlockEntity cube, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ItemStack stack;
		if (cube != null) {
			if (entity == null) {
				entity = new ItemEntity(cube.getLevel(), cube.getBlockPos().getX(), cube.getBlockPos().getY(), cube.getBlockPos().getZ(), new ItemStack(Blocks.STONE));
			}

			((ItemEntityAccessor) entity).botania_setAge(ClientTickHandler.getEntityTicksInGame());
			stack = cube.getRequestTarget();
			entity.setItem(stack);
		} else {
			stack = ItemStack.EMPTY;
		}

		Minecraft mc = Minecraft.getInstance();
		ms.pushPose();
		ms.translate(0.5F, 1.5F, 0.5F);
		ms.scale(1F, -1F, -1F);
		/*
			Using Mth.sin(((float)entity.getAge() + partialTick) / 10.0F + entity.bobOffs from ItemEntityRender#render to sync the bobbing.
			Divided by a negative number to make the item inside not be static (-1 almost make the cube and item the same speed).
			Making the divider smaller, slows down the cube bobbing (you can multiply instead to make it faster).
			This still keeps the item and the cube in sync, the cube just doesn't move as much as the item,
			but the item never goes outside the cube (based on the tests I made; some edge cases could exist).
		*/
		ms.translate(0F, (Mth.sin(((float) entity.getAge() + partialTick) / 10.0F + entity.bobOffs) * 0.1F + 0.1F) / -7F, 0F);

		if (!stack.isEmpty()) {
			ms.pushPose();
			ms.translate(0F, 0.96F, 0F);
			ms.scale(0.64F, 0.64F, 0.64F);
			ms.rotate(VecHelper.rotateZ(180F));
			RenderHelper.submitEntity(entity, partialTick, ms, buffers, camera);
			ms.popPose();
		}

		ms.pushPose();
		ms.translate(-0.5F, 0.25F, -0.5F);
		RenderHelper.submitBlockModel(getCubeModel(), ms, buffers, light, overlay);
		ms.popPose();

		if (!stack.isEmpty() && cube != null && !cube.isHiddenCount()) {
			int count = cube.getItemCount();
			String countStr;
			int color;
			if (count <= 9_999) {
				countStr = String.valueOf(count);
				color = DyeColor.WHITE.getTextColor();
			} else {
				if (count > 9_999_999) {
					countStr = count / 1_000_000 + "M";
					color = DyeColor.GREEN.getTextColor();
				} else {
					countStr = count / 1_000 + "K";
					color = DyeColor.YELLOW.getTextColor();
				}
			}
			int alpha = 160;
			int colorText = ARGB.color(alpha, color);
			int colorShade = ARGB.color(alpha, (color & 0xfcfcfc) >> 2);

			float s = 1F / 64F;
			ms.scale(s, s, s);
			int l = mc.font.width(countStr);
			FormattedCharSequence text = FormattedCharSequence.forward(countStr, Style.EMPTY);

			ms.translate(0F, 55F, 0F);
			float tr = -16.5F;
			for (int i = 0; i < 4; i++) {
				ms.rotate(VecHelper.rotateY(90F));
				ms.translate(0F, 0F, tr);
				// cannot use dropshadow parameter as that shadow somehow renders in front of the text, not behind it
				buffers.submitText(ms, -l / 2, 0, text, false, Font.DisplayMode.NORMAL, light, colorText, 0, 0);
				ms.translate(0F, 0F, 0.1F);
				buffers.submitText(ms, -l / 2 + 1, 1, text, false, Font.DisplayMode.NORMAL, light, colorShade, 0, 0);
				ms.translate(0F, 0F, -tr - 0.1F);
			}
		}

		ms.popPose();
	}

	private BlockStateModel getCubeModel() {
		return Objects.requireNonNull(ClientXplatAbstractions.instance().getResourceModel(CUBE_MODEL_ID));
	}
}
