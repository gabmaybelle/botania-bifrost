/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.state.MinecartRenderState;

/** PORT-26.3: the pool cart's mana, read on the game thread for the pool renderer. */
public class ManaPoolMinecartRenderState extends MinecartRenderState {
	public int mana;
}
