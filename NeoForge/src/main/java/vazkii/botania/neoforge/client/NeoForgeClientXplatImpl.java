/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.neoforge.item.TinyPotatoRenderEvent;
import vazkii.botania.xplat.ClientXplatAbstractions;

public class NeoForgeClientXplatImpl implements ClientXplatAbstractions {
	@Override
	public void fireRenderTinyPotato(BlockEntity potato, Component name, float tickDelta, PoseStack ms, SubmitNodeCollector collector, int light, int overlay) {
		NeoForge.EVENT_BUS.post(new TinyPotatoRenderEvent(potato, name, tickDelta, ms, collector, light, overlay));
	}

	@Override
	public void sendToServer(CustomPacketPayload packet) {
		ClientPacketDistributor.sendToServer(packet);
	}

	@Nullable
	@Override
	public BlockStateModel getResourceModel(Identifier id) {
		return NeoForgeStandaloneModels.get(id);
	}

	@Override
	public int getGuiRightHeight(Player player) {
		// PORT-26.3: NeoForge no longer tracks the HUD's right-hand stack height; this mirrors what vanilla draws there
		int rightHeight = 49;

		if (player.isEyeInFluid(FluidTags.WATER) || player.getAirSupply() < player.getMaxAirSupply()) {
			// shift up single row if player is underwater or still recovering air
			rightHeight += 10;
		}

		Entity playerVehicle = player.getVehicle();
		if (playerVehicle instanceof LivingEntity vehicle && vehicle.showVehicleHealth()) {
			// shift up if vehicle health requires more than one row (vanilla HUD limits vehicle hearts to 3 rows)
			rightHeight += Math.min(30, (int) (vehicle.getMaxHealth() + 0.5) / 2) - 1;
		}

		return rightHeight;
	}

	@Override
	public void markSpriteActive(TextureAtlasSprite sprite) {
		// PORT-26.3: disabled until Sodium's and Embeddium's 26.3 APIs are checked
		// (src/disabled/integration/sodium and embeddium).
	}
}
