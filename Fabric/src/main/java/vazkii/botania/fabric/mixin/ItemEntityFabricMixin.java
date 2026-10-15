/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.item.ColoredContentsPouchItem;

@Mixin(ItemEntity.class)
public class ItemEntityFabricMixin {
	@Shadow
	private int pickupDelay;

	@Shadow
	private EntityReference<Entity> thrower; // PORT-26.3: an entity reference now

	@Inject(
		method = "playerTouch",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;", ordinal = 0),
		cancellable = true
	)
	private void onPickup(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (pickupDelay == 0 && (thrower == null || thrower.getUUID().equals(player.getUUID()))
				&& ColoredContentsPouchItem.onPickupItem(self, player)) {
			ci.cancel();
		}
	}

}
