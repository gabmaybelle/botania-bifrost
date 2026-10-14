/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.item.equipment.bauble.BenevolentGoddessCharmItem;

import java.util.ArrayList;
import java.util.List;

// PORT-26.3: Explosion is an interface now; the server-side class works out the blocks to blow in one method
@Mixin(ServerExplosion.class)
public abstract class ExplosionFabricMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@Shadow
	@Final
	private Vec3 center;

	@ModifyReturnValue(method = "calculateExplodedPositions", at = @At("RETURN"))
	private List<BlockPos> protectBlocks(List<BlockPos> original) {
		if (BenevolentGoddessCharmItem.shouldProtectExplosion(level, center)) {
			return new ArrayList<>();
		}
		return original;
	}
}
