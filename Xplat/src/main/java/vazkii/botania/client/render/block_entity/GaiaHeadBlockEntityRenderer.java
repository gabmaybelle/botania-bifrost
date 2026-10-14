/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.render.entity.GaiaGuardianRenderer;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.GaiaHeadBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class GaiaHeadBlockEntityRenderer extends BotaniaBlockEntityRenderer<GaiaHeadBlockEntity> {
	public static final Map<SkullBlock.Type, SkullModelBase> models = new HashMap<>();
	private static final WeakHashMap<GameProfile, ResolvableProfile> resolvedProfiles = new WeakHashMap<>();
	private final PlayerSkinRenderCache playerSkinRenderCache;

	public GaiaHeadBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		// PORT-26.3: vanilla no longer hands out a map of skull models; build one per vanilla skull type
		EntityModelSet modelSet = ctx.entityModelSet();
		for (SkullBlock.Types type : SkullBlock.Types.values()) {
			SkullModelBase model = SkullBlockRenderer.createModel(modelSet, type);
			if (model != null) {
				models.put(type, model);
			}
		}
		this.playerSkinRenderCache = ctx.playerSkinRenderCache();
	}

	public static SkullBlock.Type getViewType() {
		Entity view = Minecraft.getInstance().getCameraEntity();

		if (view instanceof WitherSkeleton || view instanceof WitherBoss) {
			return SkullBlock.Types.WITHER_SKELETON;
		}
		if (view instanceof AbstractSkeleton) {
			return SkullBlock.Types.SKELETON;
		}
		if (view instanceof Zombie) {
			return SkullBlock.Types.ZOMBIE;
		}
		if (view instanceof Creeper) {
			return SkullBlock.Types.CREEPER;
		}
		if (view instanceof AbstractPiglin) {
			return SkullBlock.Types.PIGLIN;
		}
		if (view instanceof EnderDragon) {
			return SkullBlock.Types.DRAGON;
		}
		return SkullBlock.Types.PLAYER;
	}

	// [VanillaCopy] SkullBlockRenderer::extractRenderState + submit
	@Override
	public void render(@Nullable GaiaHeadBlockEntity blockEntity, float partialTick, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight, int packedOverlay, @Nullable CameraRenderState camera) {
		// Botania: block entity can be null when rendering the item form
		float animationProgress = blockEntity != null ? blockEntity.getAnimation(partialTick) : 0;
		BlockState blockstate = blockEntity != null ? blockEntity.getBlockState() : BotaniaBlocks.GAIA_HEAD.defaultBlockState();
		boolean isWallSkull = blockstate.getBlock() instanceof WallSkullBlock;
		var transformation = isWallSkull
				? SkullBlockRenderer.TRANSFORMATIONS.wallTransformation(blockstate.getValue(WallSkullBlock.FACING))
				: SkullBlockRenderer.TRANSFORMATIONS.freeTransformations(blockEntity == null ? 8 : blockstate.getValue(SkullBlock.ROTATION));
		// Botania: the head takes the shape of whatever the camera is looking out of, not the block's own type
		SkullBlock.Type viewType = getViewType();
		SkullModelBase skullmodelbase = models.get(viewType);
		if (skullmodelbase == null) {
			return;
		}
		// Botania: using spectated player or local player as reference skin
		AbstractClientPlayer viewingPlayer = GaiaGuardianRenderer.getViewingPlayer();
		RenderType rendertype;
		if (viewType == SkullBlock.Types.PLAYER && viewingPlayer != null) {
			ResolvableProfile profile = resolvedProfiles.computeIfAbsent(viewingPlayer.getGameProfile(), ResolvableProfile::createResolved);
			rendertype = playerSkinRenderCache.getOrDefault(profile).renderType();
		} else {
			rendertype = SkullBlockRenderer.getSkullRenderType(viewType, null);
		}
		poseStack.pushPose();
		poseStack.mulPose(transformation);
		SkullBlockRenderer.submitSkull(animationProgress, poseStack, bufferSource, packedLight, skullmodelbase, rendertype, 0, null);
		poseStack.popPose();
	}
}
