/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

import org.joml.Vector3fc;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.GaiaHeadBlockEntityRenderer;
import vazkii.botania.client.render.entity.GaiaGuardianRenderer;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The gaia head item: the viewer's own head shape and skin, through the gaia noise shader.
 * {@code {"type": "botania:gaia_head"}}.
 * PORT-26.3: this was {@code GaiaHeadModel} drawn by vanilla's skull item path with {@code SkullBlockRendererMixin}
 * swapping in the noise shader.
 */
public class GaiaHeadSpecialRenderer implements NoDataSpecialModelRenderer {
	// [VanillaCopy] SkullBlockRenderer.SKIN_BY_TYPE
	private static final Map<SkullBlock.Types, Identifier> SKIN_BY_TYPE = new EnumMap<>(SkullBlock.Types.class);
	static {
		SKIN_BY_TYPE.put(SkullBlock.Types.SKELETON, Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png"));
		SKIN_BY_TYPE.put(SkullBlock.Types.WITHER_SKELETON, Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png"));
		SKIN_BY_TYPE.put(SkullBlock.Types.ZOMBIE, Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png"));
		SKIN_BY_TYPE.put(SkullBlock.Types.CREEPER, Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png"));
		SKIN_BY_TYPE.put(SkullBlock.Types.DRAGON, Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png"));
		SKIN_BY_TYPE.put(SkullBlock.Types.PIGLIN, Identifier.withDefaultNamespace("textures/entity/piglin/piglin.png"));
		SKIN_BY_TYPE.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultTexture());
	}

	private final Map<SkullBlock.Types, SkullModelBase> models = new EnumMap<>(SkullBlock.Types.class);
	private final PlayerSkinRenderCache skinCache;

	public GaiaHeadSpecialRenderer(BakingContext context) {
		for (SkullBlock.Types type : SkullBlock.Types.values()) {
			SkullModelBase model = SkullBlockRenderer.createModel(context.entityModelSet(), type);
			if (model != null) {
				models.put(type, model);
			}
		}
		this.skinCache = context.playerSkinRenderCache();
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		SkullBlock.Type type = GaiaHeadBlockEntityRenderer.getViewType();
		SkullModelBase model = type instanceof SkullBlock.Types vanillaType ? models.get(vanillaType) : null;
		if (model == null) {
			return;
		}
		Identifier texture;
		AbstractClientPlayer viewingPlayer = GaiaGuardianRenderer.getViewingPlayer();
		if (type == SkullBlock.Types.PLAYER && viewingPlayer != null) {
			ResolvableProfile profile = ResolvableProfile.createResolved(viewingPlayer.getGameProfile());
			texture = skinCache.getOrDefault(profile).playerSkin().body().texturePath();
		} else {
			texture = SKIN_BY_TYPE.get((SkullBlock.Types) type);
		}
		SkullModelBase.State state = new SkullModelBase.State();
		state.animationPos = 0;
		poseStack.pushPose();
		// vanilla's skull item sits on the item's centre with its base at the bottom
		poseStack.translate(0.0F, -0.5F, 0.0F);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		collector.submitModel(model, state, poseStack, RenderHelper.getGaiaNoiseConstantLayer(texture), light, overlay,
				RenderHelper.gaiaNoiseConstantTint(), null, outlineColor);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		SkullModelBase model = models.get(SkullBlock.Types.PLAYER);
		if (model != null) {
			PoseStack poseStack = new PoseStack();
			SkullModelBase.State state = new SkullModelBase.State();
			model.setupAnim(state);
			model.root().getExtentsForGui(poseStack, output);
		}
	}

	public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Override
		public SpecialModelRenderer<Void> bake(BakingContext context) {
			return new GaiaHeadSpecialRenderer(context);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
