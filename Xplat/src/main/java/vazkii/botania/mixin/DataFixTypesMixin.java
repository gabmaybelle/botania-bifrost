/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;

import net.minecraft.util.datafix.DataFixTypes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.datafix.LegacyDataFixer;

/**
 * Every upgrade of a chunk, entity chunk, player or structure goes through here; Botania's own data is brought up to
 * date right after vanilla's.
 *
 * @see LegacyDataFixer
 */
@Mixin(DataFixTypes.class)
public class DataFixTypesMixin {
	@Inject(
		method = "update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;",
		at = @At("RETURN"), cancellable = true
	)
	private <T> void fixBotaniaData(DataFixer fixer, Dynamic<T> input, int fromVersion, int toVersion,
			CallbackInfoReturnable<Dynamic<T>> cir) {
		cir.setReturnValue(LegacyDataFixer.afterVanillaFix((DataFixTypes) (Object) this, fixer, cir.getReturnValue(), fromVersion, toVersion));
	}
}
