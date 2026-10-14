/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.core.RecipeBookAccess;

/**
 * PORT-26.3: captures the ghost recipe stack whose tooltip is drawn (was a local in
 * RecipeBookComponent#renderGhostRecipeTooltip); RecipeBookComponentMixin hands it on.
 */
@Mixin(GhostSlots.class)
public class GhostSlotsMixin implements RecipeBookAccess {
	@Unique
	@Nullable
	private ItemStack botania_hoveredGhostRecipeStack;

	@Override
	@Nullable
	public ItemStack botania_getHoveredGhostRecipeStack() {
		return botania_hoveredGhostRecipeStack;
	}

	@Inject(method = "extractTooltip", at = @At("HEAD"))
	private void forgetHoveredGhostStack(CallbackInfo ci) {
		botania_hoveredGhostRecipeStack = null;
	}

	// Captures the stack that has its tooltip drawn.
	@ModifyVariable(method = "extractTooltip", at = @At("STORE"), ordinal = 0, require = 0)
	private ItemStack captureHoveredGhostStack(ItemStack stack) {
		botania_hoveredGhostRecipeStack = stack;
		return stack;
	}
}
