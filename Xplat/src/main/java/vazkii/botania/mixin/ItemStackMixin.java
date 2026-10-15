/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.component.ManaRepair;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	/**
	 * Self-repair for mana-repaired items.
	 */
	// PORT-26.3: inventoryTick takes the equipment slot (null for plain inventory slots) instead of index + selected flag
	@Inject(method = "inventoryTick", at = @At("HEAD"))
	private void tickSelfRepair(Level level, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {

		ItemStack self = (ItemStack) (Object) this;

		if (level.isClientSide() || !(entity instanceof Player player)) {
			return;
		}

		ManaRepair manaRepair = self.get(BotaniaDataComponents.MANA_REPAIR);
		if (manaRepair == null || !self.isDamaged() || !ManaItemHandler.instance()
				.requestManaExactForTool(self, player, manaRepair.manaPerRepairedDurability(), true)) {
			return;
		}

		self.setDamageValue(self.getDamageValue() - 1);
	}
}
