/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.xplat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.ServiceUtil;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface ClientXplatAbstractions {
	Identifier FLOATING_FLOWER_MODEL_LOADER_ID = botaniaRL("floating_flower");
	Identifier MANA_GUN_MODEL_LOADER_ID = botaniaRL("mana_blaster");

	// Event firing
	void fireRenderTinyPotato(BlockEntity potato, Component name, float tickDelta, PoseStack ms, SubmitNodeCollector collector, int light, int overlay);

	// Networking
	void sendToServer(CustomPacketPayload packet);

	// Rendering stuff

	/**
	 * Try to load the resource/standalone model with the specified resource location.
	 * Returns null if no model was found.
	 * PORT-26.3: extra models are baked block state models now (a list of parts); see RenderHelper#submitBlockModel.
	 */
	@Nullable
	BlockStateModel getResourceModel(Identifier id);
	int getGuiRightHeight(Player player);
	/** Marks an animated sprite as "active" for Sodium, if present. */
	void markSpriteActive(TextureAtlasSprite sprite);

	ClientXplatAbstractions INSTANCE = ServiceUtil.findService(ClientXplatAbstractions.class, null);

	static ClientXplatAbstractions instance() {
		return INSTANCE;
	}
}
