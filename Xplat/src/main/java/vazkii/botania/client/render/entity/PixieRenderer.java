/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.PixieModel;
import vazkii.botania.common.entity.PixieEntity;

public class PixieRenderer extends MobRenderer<PixieEntity, PixieRenderState, PixieModel> {
	public PixieRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new PixieModel(ctx.bakeLayer(BotaniaModelLayers.PIXIE)), 0.0F);
	}

	@Override
	public PixieRenderState createRenderState() {
		return new PixieRenderState();
	}

	@Override
	public void extractRenderState(PixieEntity entity, PixieRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.evil = entity.isGaia();
	}

	@Override
	public Identifier getTextureLocation(PixieRenderState state) {
		return ClientProxy.dootDoot
				? Identifier.parse(ResourcesLib.MODEL_PIXIE_HALLOWEEN)
				: Identifier.parse(ResourcesLib.MODEL_PIXIE);
	}

	// PORT-26.3: the model used to pick the noise layer from a static flag; the renderer picks it from the state now
	@Nullable
	@Override
	protected RenderType getRenderType(PixieRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		if (state.evil && isBodyVisible && !forceTransparent) {
			return RenderHelper.getGaiaNoiseConstantLayer(getTextureLocation(state));
		}
		return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
	}

	@Override
	protected int getModelTint(PixieRenderState state) {
		return state.evil ? RenderHelper.gaiaNoiseConstantTint() : -1;
	}
}
