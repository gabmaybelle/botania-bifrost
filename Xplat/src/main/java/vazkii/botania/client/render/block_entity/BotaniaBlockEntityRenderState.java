/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

/**
 * The render state Botania's block entity renderers extract.
 * <p>
 * PORT-26.3: vanilla renderers copy the values they draw from into a render state first and draw from that. Botania's
 * renderers read the block entity directly while drawing, so for now the state carries the block entity itself and
 * the partial tick; the block entity is null when the item form is drawn. Extracting proper values is a later tidy-up.
 */
public class BotaniaBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {
	@Nullable
	public T blockEntity;
	public float partialTick;
}
