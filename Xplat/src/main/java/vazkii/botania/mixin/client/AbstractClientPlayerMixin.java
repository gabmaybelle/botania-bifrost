/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.item.BotaniaItems;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
	/**
	 * Applies the bow pull-back FOV effect to Botania's bows.
	 * 
	 * @param useStack The item currently being used by the player.
	 * @param item     The item type vanilla checks for (the bow).
	 * @param original The vanilla check.
	 * @return @{@code true} if the FOV zoom should be applied, otherwise {@code false}.
	 */
	// PORT-26.3: is(Item) is the erased TypedInstance#is(T) now, and the use stack is no longer a local variable,
	// so wrap the call and take the stack from it
	@WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
	private boolean isBowItem(ItemStack useStack, Object item, Operation<Boolean> original) {
		return original.call(useStack, item) || useStack.is(BotaniaItems.LIVINGWOOD_BOW) || useStack.is(BotaniaItems.CRYSTAL_BOW);
	}
}
