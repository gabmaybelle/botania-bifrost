/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** PORT-26.3: lets {@code RenderHelper#renderItemCustomColor} walk an item's resolved layers. */
@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
	@Accessor("layers")
	ItemStackRenderState.LayerRenderState[] botania_getLayers();

	@Accessor("activeLayerCount")
	int botania_getActiveLayerCount();
}
