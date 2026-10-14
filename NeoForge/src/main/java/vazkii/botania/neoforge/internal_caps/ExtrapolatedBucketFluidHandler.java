/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.internal_caps;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * PORT-26.3: the extrapolated bucket swallows fluid, one bucket's worth per insertion, and never holds anything
 * (NeoForge's transfer API; was a VoidFluidHandler).
 */
public class ExtrapolatedBucketFluidHandler implements ResourceHandler<FluidResource> {
	@Override
	public int size() {
		return 1;
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.EMPTY;
	}

	@Override
	public long getAmountAsLong(int index) {
		return 0;
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return FluidType.BUCKET_VOLUME;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return true;
	}

	/**
	 * Drain things one bucket worth of fluid at a time.
	 */
	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		return Math.min(FluidType.BUCKET_VOLUME, amount);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		return 0;
	}
}
