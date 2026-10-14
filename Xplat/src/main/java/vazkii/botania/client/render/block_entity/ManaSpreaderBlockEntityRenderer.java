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
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.block.mana.ManaSpreaderBlock;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ManaSpreaderBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaSpreaderBlockEntity> {
	public static final Map<DyeColor, Identifier> WOOL_PADDING_IDS = new EnumMap<>(
			ColorHelper.supportedColors().collect(Collectors.toMap(Function.identity(),
					color -> botaniaRL("block/" + color.getSerializedName() + "_spreader_padding"))));

	public ManaSpreaderBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@Nullable ManaSpreaderBlockEntity spreader, float partialTick, PoseStack ms, SubmitNodeCollector buffers, int light, int overlay, @Nullable CameraRenderState camera) {
		ms.pushPose();

		ms.translate(0.5F, 0.5, 0.5F);

		Quaternionf transform = VecHelper.rotateY(spreader.rotationX + 90F);
		transform.mul(VecHelper.rotateX(spreader.rotationY));
		ms.rotate(transform);

		ms.translate(-0.5F, -0.5F, -0.5F);

		float time = ClientTickHandler.getEntityTicksInGame() + partialTick + new Random(spreader.getBlockState().getSeed(spreader.getBlockPos())).nextFloat() * 360;

		float r = 1, g = 1, b = 1;
		ManaSpreaderBlock spreaderBlock = spreader.getSpreaderBlock();
		if (spreaderBlock.isRainbowRendered()) {
			int color = Mth.hsvToRgb((time % 180) / 180, 0.4F, 0.9F);
			r = ARGB.red(color) / 255F;
			g = ARGB.green(color) / 255F;
			b = ARGB.blue(color) / 255F;
		}

		int color = ARGB.colorFromFloat(1F, r, g, b);
		RenderHelper.submitBlockModel(RenderHelper.getBlockStateModel(spreader.getBlockState()), ms, buffers, color, light, overlay);

		ms.pushPose();
		ms.translate(0.5, 0.5, 0.5);
		ms.rotate(VecHelper.rotateY(time % 360));
		ms.translate(-0.5, -0.5, -0.5);
		ms.translate(0F, Mth.sin(time / 20.0f) * 0.05F, 0F);
		RenderHelper.submitBlockModel(getCoreModel(spreader), ms, buffers, light, overlay);
		ms.popPose();

		ItemStack stack = spreader.getItemHandler().getItem(0);
		if (!stack.isEmpty()) {
			ms.pushPose();
			ms.translate(0.5F, 0.5F, 0.094F);
			ms.rotate(VecHelper.rotateZ(180));
			ms.rotate(VecHelper.rotateX(180));
			// Prevents z-fighting. Otherwise not noticeable.
			ms.scale(0.997F, 0.997F, 1F);
			RenderHelper.submitItem(stack, ItemDisplayContext.NONE, ms, buffers, spreader.getLevel(), light, overlay, 0);
			ms.popPose();
		}

		if (spreaderBlock.isRainbowRendered() && spreaderBlock.getOptionalColor().isPresent()) {
			ms.pushPose();
			// The padding model is facing up so that the textures are rotated the correct way
			// It's simpler to do this than mess with rotation and UV in the json model
			ms.translate(0.5F, 0.5F, 0.5F);
			ms.rotate(VecHelper.rotateX(-90));
			ms.rotate(VecHelper.rotateY(180));
			ms.translate(-0.5F, -0.5F, -0.5F);
			RenderHelper.submitBlockModel(getPaddingModel(spreaderBlock.getOptionalColor().get()), ms, buffers, color, light, overlay);
			ms.popPose();
		}

		ms.popPose();

		if (spreader.getBlockState().getValue(BotaniaStateProperties.HAS_SCAFFOLDING)) {
			RenderHelper.submitBlockModel(getScaffoldingModel(spreader), ms, buffers, color, light, overlay);
		}

	}

	private BlockStateModel getCoreModel(ManaSpreaderBlockEntity tile) {
		Identifier coreModelId = tile.getSpreaderBlock().getCoreModelId();
		return Objects.requireNonNull(ClientXplatAbstractions.instance().getResourceModel(coreModelId));
	}

	private BlockStateModel getPaddingModel(DyeColor color) {
		Identifier paddingId = WOOL_PADDING_IDS.get(color);
		return Objects.requireNonNull(ClientXplatAbstractions.instance().getResourceModel(paddingId));
	}

	private BlockStateModel getScaffoldingModel(ManaSpreaderBlockEntity tile) {
		Identifier scaffoldingModelId = tile.getSpreaderBlock().getScaffoldingModelId();
		return Objects.requireNonNull(ClientXplatAbstractions.instance().getResourceModel(scaffoldingModelId));
	}
}
