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

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.block.block_entity.flower.functional.VinculotusBlockEntity;

@Mixin(Enderman.class)
public class EnderManFabricMixin {
	@WrapOperation(method = "teleport(DDD)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Enderman;randomTeleport(DDDZLnet/minecraft/tags/TagKey;)Z"))
	// PORT-26.3: randomTeleport takes the avoidance tag as well
	private boolean checkForVincs(Enderman instance, double x, double y, double z, boolean broadcastTeleport, TagKey<Block> avoidanceTag, Operation<Boolean> original) {
		Vec3 vincPos = VinculotusBlockEntity.onEndermanTeleport(instance, x, y, z);
		if (vincPos != null) {
			x = vincPos.x();
			y = vincPos.y();
			z = vincPos.z();
		}
		return original.call(instance, x, y, z, broadcastTeleport, avoidanceTag);
	}
}
