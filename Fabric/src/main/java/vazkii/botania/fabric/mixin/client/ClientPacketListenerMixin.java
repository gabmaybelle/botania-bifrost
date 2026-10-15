/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.fabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.handler.BotaniaRecipeIngredientsCache;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(at = @At("HEAD"), method = "sendChat", cancellable = true)
	private void onChat(String message, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;
		if (CorporeaIndexBlockEntity.ClientHandler.onChat(player, message)) {
			ci.cancel();
		}
	}

	// PORT-26.3: handleUpdateRecipes only stores the new recipe container now (the recipe book refresh is a separate
	// packet), so the cache is cleared at its end without the slice
	@Inject(method = "handleUpdateRecipes", at = @At(value = "RETURN"))
	private void onRecipesUpdated(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
		BotaniaRecipeIngredientsCache.clearClientCache();
	}
}
