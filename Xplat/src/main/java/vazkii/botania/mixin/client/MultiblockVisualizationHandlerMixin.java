/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin.client;

import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import vazkii.patchouli.client.handler.MultiblockVisualizationHandler;

@Mixin(MultiblockVisualizationHandler.class)
public interface MultiblockVisualizationHandlerMixin {
	// There doesn't seem to be any other way to access the anchor position of the current Patchouli multiblock.
	// PORT-26.3: an instance field now (the handler is MultiblockVisualizationHandler.INSTANCE)
	@Accessor("pos")
	BlockPos botania_getPos();
}
