/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;

/** PORT-26.3: what the Gaia trap renderer reads off the entity each frame. */
public class GaiaTrapRenderState extends EntityRenderState {
	public AABB box;
	public int tickCount;
	public float partialTick;
}
