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

/** PORT-26.3: what the King's Law weapon renderer reads off the entity each frame. */
public class BabylonWeaponRenderState extends EntityRenderState {
	public float rotation;
	public int liveTicks;
	public int delay;
	public int chargeTicks;
	public int variety;
	public long seed;
	public float partialTick;
}
