/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;

import net.minecraft.client.renderer.SkyRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkyRenderer.class)
public interface SkyRendererAccessor {
	@Accessor("starBuffer")
	GpuBuffer botania_getStarBuffer();

	@Accessor("starIndexCount")
	int botania_getStarIndexCount();

	@Accessor("quadIndices")
	RenderSystem.AutoStorageIndexBuffer botania_getQuadIndices();
}
