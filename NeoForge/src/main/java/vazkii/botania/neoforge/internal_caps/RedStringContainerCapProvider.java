/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.internal_caps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.red_string.RedStringContainerBlockEntity;

public class RedStringContainerCapProvider implements ICapabilityProvider<RedStringContainerBlockEntity, @Nullable Direction, ResourceHandler<ItemResource>> {
	@Override
	public ResourceHandler<ItemResource> getCapability(RedStringContainerBlockEntity container, @Nullable Direction side) {
		BlockPos bindingPos = container.getBinding();
		if (bindingPos != null) {
			ResourceHandler<ItemResource> handler = container.getLevel().getCapability(Capabilities.Item.BLOCK, bindingPos, side);
			if (handler != null) {
				return handler;
			}
		}
		return EmptyResourceHandler.instance();
	}
}
