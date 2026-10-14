/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** PORT-26.3: whether the pixie is one of the Gaia Guardian's, which picks the noise layer. */
public class PixieRenderState extends LivingEntityRenderState {
	public boolean evil;
}
