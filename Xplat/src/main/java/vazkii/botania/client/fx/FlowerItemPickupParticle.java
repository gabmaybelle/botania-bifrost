/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

// [VanillaCopy] ItemPickupParticle, but with BlockPos as target instead of an entity
// PORT-26.3: vanilla's pickup particle holds the item's extracted render state and the engine's item pickup group
// draws it, so this extends it and only swaps the target position for the block's centre
public class FlowerItemPickupParticle extends ItemPickupParticle {
	private final Vec3 targetPos;

	// Botania: target doesn't move, so no need for position updates (I can already hear Sable noises, though)
	public FlowerItemPickupParticle(ClientLevel level, EntityRenderState itemRenderState, Entity itemEntity, BlockPos target, boolean onFire) {
		super(level, itemRenderState, itemEntity, itemEntity.getDeltaMovement());
		if (onFire) {
			itemRenderState.displayFireAnimation = true;
		}
		this.targetPos = Vec3.atCenterOf(target).add(level.getBlockState(target).getOffset(target));
		setTarget();
		this.targetXOld = this.targetX;
		this.targetYOld = this.targetY;
		this.targetZOld = this.targetZ;
	}

	private void setTarget() {
		this.targetX = targetPos.x;
		this.targetY = targetPos.y;
		this.targetZ = targetPos.z;
	}

	@Override
	public void tick() {
		super.tick();
		setTarget();
	}
}
