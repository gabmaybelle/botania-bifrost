/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.fabric.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityFabricAccessor {
	// PORT-26.3: canBurn takes the assembled result instead of the recipe
	@Invoker("canBurn")
	static boolean botania_canBurn(NonNullList<ItemStack> items, int maxStackSize, ItemStack burnResult) {
		throw new IllegalStateException();
	}
}
