/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.SparkBaseEntity;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Objects;
import java.util.Random;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public abstract class BaseSparkRenderer<T extends SparkBaseEntity> extends EntityRenderer<T, SparkRenderState> {
	protected final SpriteGetter sprites;
	private final TextureAtlasSprite starSprite;
	private final TextureAtlasSprite worldSprite;

	protected BaseSparkRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		this.sprites = ctx.getSprites();
		this.starSprite = Objects.requireNonNull(
				RenderHelper.getBlockSprite(this.sprites, botaniaRL("item/spark_star"))
		);
		this.worldSprite = Objects.requireNonNull(
				RenderHelper.getBlockSprite(this.sprites, botaniaRL("item/mana_spark"))
		);
	}

	@Override
	public SparkRenderState createRenderState() {
		return new SparkRenderState();
	}

	@Override
	public void extractRenderState(T tEntity, SparkRenderState state, float partialTicks) {
		super.extractRenderState(tEntity, state, partialTicks);
		state.baseIcon = getBaseIcon(tEntity);
		ClientXplatAbstractions.instance().markSpriteActive(state.baseIcon);
		state.spinningIcon = getSpinningIcon(tEntity);
		state.time = (tEntity.level().getGameTime() % 24000) + partialTicks + new Random(tEntity.getId()).nextInt(200);
		state.alpha = 0.1F + (tEntity.isInvisible() ? 0 : 1) * 0.8F;
		state.networkColor = tEntity.getNetwork().getTextureDiffuseColor();
	}

	@Override
	public void submit(SparkRenderState state, PoseStack ms, SubmitNodeCollector buffers, CameraRenderState camera) {
		TextureAtlasSprite iicon = state.baseIcon;
		ms.pushPose();

		float time = state.time;
		float a = state.alpha;
		float iconAlpha = (0.7f + 0.6f * (Mth.sin(time / 5) + 0.5f)) * a;
		int iconColor = ARGB.color(ARGB.as8BitChannel(iconAlpha), 0xFFFFFF);

		float scale = 0.75F + 0.1F * Mth.sin(time / 10);
		ms.scale(scale, scale, scale);

		ms.pushPose();
		ms.rotate(camera.orientation);
		renderIcon(ms, buffers, iicon, iconColor);

		ms.pushPose();
		ms.translate(-0.02 + Math.sin(time / 20) * 0.2, 0.24 + Math.cos(time / 20) * 0.2, 0.005);
		ms.scale(0.2F, 0.2F, 0.2F);
		int starColor = ARGB.color(ARGB.as8BitChannel(a), state.networkColor);
		renderIcon(ms, buffers, this.starSprite, starColor);
		ms.popPose();

		TextureAtlasSprite spinningIcon = state.spinningIcon;
		if (spinningIcon != null) {
			ms.translate(-0.02 + Math.sin(time / 20) * -0.2, 0.24 + Math.cos(time / 20) * -0.2, 0.005);
			ms.scale(0.2F, 0.2F, 0.2F);
			renderIcon(ms, buffers, spinningIcon, iconColor);
		}
		ms.popPose();

		ms.popPose();
		super.submit(state, ms, buffers, camera);
	}

	protected TextureAtlasSprite getBaseIcon(T entity) {
		return this.worldSprite;
	}

	@Nullable
	protected TextureAtlasSprite getSpinningIcon(T entity) {
		return null;
	}

	private void renderIcon(PoseStack ms, SubmitNodeCollector buffers, TextureAtlasSprite icon, int color) {
		RenderHelper.submit(ms, buffers, RenderHelper.SPARK, (ps, buffer) -> renderIcon(ps, buffer, icon, color));
	}

	private void renderIcon(PoseStack ms, VertexConsumer buffer, TextureAtlasSprite icon, int color) {
		float f = icon.getU0();
		float f1 = icon.getU1();
		float f2 = icon.getV0();
		float f3 = icon.getV1();
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		int fullbright = 0xF000F0;
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		Matrix4f mat = ms.last().pose();
		buffer.addVertex(mat, 0.0F - f5, 0.0F - f6, 0.0F).setColor(r, g, b, a).setUv(f, f3).setLight(fullbright);
		buffer.addVertex(mat, f4 - f5, 0.0F - f6, 0.0F).setColor(r, g, b, a).setUv(f1, f3).setLight(fullbright);
		buffer.addVertex(mat, f4 - f5, f4 - f6, 0.0F).setColor(r, g, b, a).setUv(f1, f2).setLight(fullbright);
		buffer.addVertex(mat, 0.0F - f5, f4 - f6, 0.0F).setColor(r, g, b, a).setUv(f, f2).setLight(fullbright);
	}
}
