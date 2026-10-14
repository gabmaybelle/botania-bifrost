/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.lib.BotaniaTags;

@Mixin(Bee.class)
public abstract class BeeMixin extends Animal {
	protected BeeMixin(EntityType<? extends Animal> type, Level worldIn) {
		super(type, worldIn);
	}

	/**
	 * Allows bees to treat special flowers as proper flowers for
	 * pollination despite being excluded from the flower tag.
	 * 
	 * @see PollinateGoalMixin
	 */
	// PORT-26.3: the flower check is the static Bee#attractsBees(BlockState), used by the pollinate goal
	@Inject(method = "attractsBees", cancellable = true, at = @At("HEAD"))
	private static void isSpecialFlower(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (state.is(BotaniaTags.Blocks.SPECIAL_FLOWERS)) {
			cir.setReturnValue(true);
		}
	}
}
