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

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.block.PylonBlock;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuFabricMixin {
	// PORT-26.3: Minecraft is unobfuscated, so this is javac's name for the slotsChanged lambda (checked with javap on the 26.3 jar).
	// The pylon power is added where the bookshelf count goes into the cost; a STORE hook on the count ran but the
	// table never saw its result on 26.3 (PylonEnchantingTest).
	@WrapOperation(
		method = "lambda$slotsChanged$0(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"
		)
	)
	private int botaniaPylonEnchanting(RandomSource random, int slot, int bookcases, ItemStack item,
			Operation<Integer> original, ItemStack stack, Level level, BlockPos pos) {
		return original.call(random, slot, bookcases + pylonPower(level, pos), item);
	}

	@Unique
	private int pylonPower(Level level, BlockPos pos) {
		float power = 0;
		BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
		for (int x = -1; x <= 1; ++x) {
			for (int z = -1; z <= 1; ++z) {
				if ((x != 0 || z != 0) && level.isEmptyBlock(testPos.setWithOffset(pos, x, 0, z))
						&& level.isEmptyBlock(testPos.setWithOffset(pos, x, 1, z))) {
					power += getPylonValue(level.getBlockState(testPos.setWithOffset(pos, x * 2, 0, z * 2)), level, pos);
					power += getPylonValue(level.getBlockState(testPos.setWithOffset(pos, x * 2, 1, z * 2)), level, pos);
					if (x != 0 && z != 0) {
						power += getPylonValue(level.getBlockState(testPos.setWithOffset(pos, x * 2, 0, z)), level, pos);
						power += getPylonValue(level.getBlockState(testPos.setWithOffset(pos, x * 2, 1, z)), level, pos);
						power += getPylonValue(level.getBlockState(testPos.setWithOffset(pos, x, 0, z * 2)), level, pos);
						power += getPylonValue(level.getBlockState(testPos.setWithOffset(pos, x, 1, z * 2)), level, pos);
					}
				}
			}
		}
		return (int) power;
	}

	@Unique
	private float getPylonValue(BlockState state, LevelReader world, BlockPos pos) {
		if (state.getBlock() instanceof PylonBlock pylon) {
			return pylon.getEnchantPowerBonus(state, world, pos);
		}
		return 0;
	}
}
