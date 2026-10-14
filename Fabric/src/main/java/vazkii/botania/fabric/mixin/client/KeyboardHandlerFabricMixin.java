/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.core.handler.KonamiHandler;
import vazkii.botania.common.item.equipment.bauble.RingOfDexterousMotionItem;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerFabricMixin {
	@Inject(at = @At("HEAD"), method = "keyPress")
	// PORT-26.3: the key, scancode and modifiers travel as a KeyEvent
	private void keyEvent(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (handle == Minecraft.getInstance().getWindow().handle()) {
			RingOfDexterousMotionItem.ClientLogic.onKeyDown();
			KonamiHandler.handleInput(event.key(), action, event.modifiers());
		}
	}
}
