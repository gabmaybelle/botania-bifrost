/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.neoforge.internal_caps;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.item.BotaniaItems;

/**
 * PORT-26.3: a bucket of water that is a bowl: holds exactly one bucket of water, cannot be filled, and turns into an
 * empty bowl once drained (NeoForge's transfer API; was FluidHandlerItemStackSimple.SwapEmpty).
 */
public class WaterBowlFluidHandler extends ItemAccessResourceHandler<FluidResource> {
	public WaterBowlFluidHandler(ItemAccess itemAccess) {
		super(itemAccess, 1);
	}

	@Override
	protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
		return accessResource.is(BotaniaItems.WATER_BOWL) ? FluidResource.of(Fluids.WATER) : FluidResource.EMPTY;
	}

	@Override
	protected int getAmountFrom(ItemResource accessResource, int index) {
		return accessResource.is(BotaniaItems.WATER_BOWL) ? FluidType.BUCKET_VOLUME : 0;
	}

	@Override
	@Nullable
	protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
		if (newAmount == 0) {
			return ItemResource.of(Items.BOWL);
		}
		return newResource.is(Fluids.WATER) && newAmount == FluidType.BUCKET_VOLUME ? accessResource : null;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		// nothing can be poured in
		return false;
	}

	@Override
	protected int getCapacity(int index, FluidResource resource) {
		return FluidType.BUCKET_VOLUME;
	}
}
