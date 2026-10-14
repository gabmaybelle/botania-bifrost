/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** PORT-26.3: what the Gaia Guardian renderer reads off the entity and the viewing player each frame. */
public class GaiaGuardianRenderState extends HumanoidRenderState {
	public int invulTime;
	public int hurtTime;
	public boolean slim;
	public Identifier skinTexture;
	public boolean showHat = true;
	public boolean showJacket = true;
	public boolean showLeftPants = true;
	public boolean showRightPants = true;
	public boolean showLeftSleeve = true;
	public boolean showRightSleeve = true;
}
