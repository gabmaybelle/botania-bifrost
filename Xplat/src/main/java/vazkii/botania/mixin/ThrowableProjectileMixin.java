/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import vazkii.botania.common.entity.ManaBurstEntity;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin extends Projectile {
	protected ThrowableProjectileMixin(EntityType<? extends Projectile> type, Level level) {
		super(type, level);
	}

	// PORT-26.3: the drag is applied in applyInertia now, not inline in tick
	@ModifyArg(method = "applyInertia", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"))
	private double noDrag(double origScale) {
		// Do not apply drag to bursts
		if ((Object) this instanceof ManaBurstEntity) {
			return 1;
		}
		return origScale;
	}

	// PORT-26.3: tick now moves a projectile only up to whatever its move hits, and handles the hit after moving.
	// A burst starts inside its own spreader, which it ignores, so it crept out at a thousandth of its speed.
	// Bursts keep the 1.21.1 order: the hit first, then the full move with the motion the hit left them.
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;setPos(Lnet/minecraft/world/phys/Vec3;)V"))
	private void burstHitThenMove(ThrowableProjectile self, Vec3 newPosition, Operation<Void> original, @Local HitResult result) {
		if (self instanceof ManaBurstEntity) {
			if (result.getType() != HitResult.Type.MISS) {
				hitTargetOrDeflectSelf(result);
			}
			original.call(self, self.position().add(self.getDeltaMovement()));
		} else {
			original.call(self, newPosition);
		}
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
	private ProjectileDeflection burstHitAlreadyHandled(ThrowableProjectile self, HitResult result, Operation<ProjectileDeflection> original) {
		if (self instanceof ManaBurstEntity) {
			return ProjectileDeflection.NONE;
		}
		return original.call(self, result);
	}
}
