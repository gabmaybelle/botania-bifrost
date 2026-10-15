/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.BotaniaItems;

// PORT-26.3: ItemStack#is(Item) moved to the TypedInstance interface as a default method, so the
// shears hook (Botania shears count as vanilla shears) lives on the interface now
@Mixin(TypedInstance.class)
public interface TypedInstanceFabricMixin {
	@Inject(at = @At("HEAD"), method = "is(Ljava/lang/Object;)Z", cancellable = true)
	private void isBotaniaShears(Object rawType, CallbackInfoReturnable<Boolean> cir) {
		if (rawType == Items.SHEARS && (Object) this instanceof ItemStack stack
				&& (stack.is(BotaniaItems.MANASTEEL_SHEARS) || stack.is(BotaniaItems.ELEMENTIUM_SHEARS))) {
			cir.setReturnValue(true);
		}
	}
}
