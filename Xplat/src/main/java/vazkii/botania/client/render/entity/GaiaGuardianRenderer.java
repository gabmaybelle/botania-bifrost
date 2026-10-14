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

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.model.GaiaGuardianModel;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.common.entity.GaiaGuardianEntity;

public class GaiaGuardianRenderer extends HumanoidMobRenderer<GaiaGuardianEntity, GaiaGuardianRenderState, GaiaGuardianModel> {
	public static final float DEFAULT_GRAIN_INTENSITY = 0.05F;
	public static final float DEFAULT_DISFIGURATION = 0.025F;

	private final GaiaGuardianModel normalModel;
	private final GaiaGuardianModel slimModel;

	public GaiaGuardianRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new GaiaGuardianModel(ctx.bakeLayer(ModelLayers.PLAYER), false), 0F);
		this.normalModel = this.getModel();
		this.slimModel = new GaiaGuardianModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true);
		// Call this here bc no other place with access to Context
		ArmorModels.init(ctx);
	}

	@Override
	public GaiaGuardianRenderState createRenderState() {
		return new GaiaGuardianRenderState();
	}

	@Override
	public void extractRenderState(GaiaGuardianEntity gaiaGuardian, GaiaGuardianRenderState state, float partialTicks) {
		super.extractRenderState(gaiaGuardian, state, partialTicks);
		state.invulTime = gaiaGuardian.getInvulTime();
		state.hurtTime = gaiaGuardian.hurtTime;
		// Botania: using spectated player or local player as reference skin
		AbstractClientPlayer player = getViewingPlayer();
		if (player != null) {
			state.slim = player.getSkin().model() == PlayerModelType.SLIM;
			state.skinTexture = player.getSkin().body().texturePath();
			state.showHat = player.isModelPartShown(PlayerModelPart.HAT);
			state.showJacket = player.isModelPartShown(PlayerModelPart.JACKET);
			state.showLeftPants = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			state.showRightPants = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			state.showLeftSleeve = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			state.showRightSleeve = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
		} else {
			state.slim = false;
			state.skinTexture = DefaultPlayerSkin.get(gaiaGuardian.getUUID()).body().texturePath();
			state.showHat = state.showJacket = state.showLeftPants = state.showRightPants = state.showLeftSleeve = state.showRightSleeve = false;
		}
	}

	@Override
	public void submit(GaiaGuardianRenderState state, PoseStack ms, SubmitNodeCollector buffers, CameraRenderState camera) {
		this.model = state.slim ? slimModel : normalModel;
		super.submit(state, ms, buffers, camera);
	}

	// PORT-26.3: the grain and disfiguration were shader uniforms; they ride on the model tint now (see CoreShaders.GAIA_NOISE)
	@Override
	protected int getModelTint(GaiaGuardianRenderState state) {
		int invulTime = state.invulTime;
		float grainIntensity, disfiguration;
		if (invulTime > 0) {
			grainIntensity = invulTime > 20 ? 1F : invulTime * 0.05F;
			disfiguration = grainIntensity * 0.3F;
		} else {
			disfiguration = (0.025F + state.hurtTime * ((1F - 0.15F) / 20F)) / 2F;
			grainIntensity = 0.05F + state.hurtTime * ((1F - 0.15F) / 10F);
		}
		return RenderHelper.gaiaNoiseTint(disfiguration, grainIntensity);
	}

	@Nullable
	public static AbstractClientPlayer getViewingPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.getCameraEntity() instanceof AbstractClientPlayer clientPlayer
				? clientPlayer
				: minecraft.player;
	}

	@Override
	public Identifier getTextureLocation(GaiaGuardianRenderState state) {
		return state.skinTexture;
	}

	@Override
	protected boolean isBodyVisible(GaiaGuardianRenderState state) {
		return true;
	}
}
