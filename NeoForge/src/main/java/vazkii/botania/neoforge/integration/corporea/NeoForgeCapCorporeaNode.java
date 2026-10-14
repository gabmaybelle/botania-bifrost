/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.integration.corporea;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import vazkii.botania.api.corporea.CorporeaRequest;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.impl.corporea.AbstractCorporeaNode;

import java.util.List;

public class NeoForgeCapCorporeaNode extends AbstractCorporeaNode {
	// PORT-26.3: NeoForge's item handlers are ResourceHandler<ItemResource> now, worked through transactions
	protected final ResourceHandler<ItemResource> inv;

	public NeoForgeCapCorporeaNode(Level world, BlockPos pos, ResourceHandler<ItemResource> inv, CorporeaSpark spark) {
		super(world, pos, spark);
		this.inv = inv;
	}

	@Override
	public List<ItemStack> countItems(CorporeaRequest request) {
		return iterateOverSlots(request, false);
	}

	@Override
	public List<ItemStack> extractItems(CorporeaRequest request) {
		return iterateOverSlots(request, true);
	}

	protected List<ItemStack> iterateOverSlots(CorporeaRequest request, boolean doit) {
		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
		for (int i = inv.size() - 1; i >= 0; i--) {
			ItemStack stackAt = ItemUtil.getStack(inv, i);
			if (request.getMatcher().test(stackAt)) {
				request.trackFound(stackAt.getCount());
				int rem = Math.min(stackAt.getCount(), request.getStillNeeded() == -1 ? stackAt.getCount() : request.getStillNeeded());
				if (rem > 0) {
					request.trackSatisfied(rem);
					if (doit) {
						ItemStack copy = stackAt.copy();
						builder.addAll(breakDownBigStack(extract(i, stackAt, rem, getSpark().isCreative())));
						getSpark().onItemExtracted(copy);
						request.trackExtracted(rem);
					} else {
						builder.add(extract(i, stackAt, rem, true));
					}
				}
			}
		}
		return builder.build();
	}

	/**
	 * Takes up to {@code amount} of the given stack out of slot {@code index}; a simulation leaves the inventory alone.
	 */
	private ItemStack extract(int index, ItemStack stackAt, int amount, boolean simulate) {
		ItemResource resource = ItemResource.of(stackAt);
		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = inv.extract(index, resource, amount, transaction);
			if (!simulate) {
				transaction.commit();
			}
			return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
		}
	}
}
