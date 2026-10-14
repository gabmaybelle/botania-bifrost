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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.jetbrains.annotations.Nullable;

/** PORT-26.3: what a spark renderer reads off the spark each frame. */
public class SparkRenderState extends EntityRenderState {
	public TextureAtlasSprite baseIcon;
	@Nullable
	public TextureAtlasSprite spinningIcon;
	public float time;
	public float alpha;
	public int networkColor;
}
