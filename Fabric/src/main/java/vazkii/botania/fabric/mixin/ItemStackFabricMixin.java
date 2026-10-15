/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.item.equipment.tool.ToolCommons;

@Mixin(ItemStack.class)
public abstract class ItemStackFabricMixin {
	// PORT-26.3: the durability change is computed by a private ItemStack method now (it also covers the creative check)
	@WrapOperation(
		method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;processDurabilityChange(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;)I"
		)
	)
	private int absorbDamageWithMana(ItemStack stack, int damage, ServerLevel level, @Nullable ServerPlayer player,
			Operation<Integer> original) {
		return ToolCommons.absorbDamageWithMana(stack, original.call(stack, damage, level, player), player);
	}
}
