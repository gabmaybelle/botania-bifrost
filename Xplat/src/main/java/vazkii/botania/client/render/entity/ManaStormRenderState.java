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

/** PORT-26.3: what the manastorm renderer reads off the entity each frame. */
public class ManaStormRenderState extends EntityRenderState {
	public int burstsFired;
	public int deathTime;
	public long seed;
	public float partialTick;
}
