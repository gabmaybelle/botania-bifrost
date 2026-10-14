/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.common.block;

import net.minecraft.world.level.block.Blocks;

import vazkii.botania.mixin.FireBlockAccessor;

import static vazkii.botania.common.block.BotaniaBlocks.*;

public abstract class BotaniaBlockFlammability {
	public static void register() {
		// PORT-26.3: FireBlock#setFlammable is private now; FireBlockAccessor invokes it.
		FireBlockAccessor b = (FireBlockAccessor) Blocks.FIRE;
		// [VanillaCopy] FireBlock
		int logIgnite = 5;
		int logBurn = 5;
		// Planks, slabs, stairs, etc.
		int nonLogIgnite = 5;
		int nonLogBurn = 20;

		b.botania_setFlammable(LIVINGWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_LIVINGWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(GLIMMERING_LIVINGWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_GLIMMERING_LIVINGWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(LIVINGWOOD, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_LIVINGWOOD, logIgnite, logBurn);
		b.botania_setFlammable(GLIMMERING_LIVINGWOOD, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_GLIMMERING_LIVINGWOOD, logIgnite, logBurn);
		b.botania_setFlammable(LIVINGWOOD_PLANKS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_PLANKS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(MOSSY_LIVINGWOOD_PLANKS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(FRAMED_LIVINGWOOD, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(PATTERN_FRAMED_LIVINGWOOD, nonLogIgnite, nonLogBurn);

		b.botania_setFlammable(DREAMWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_DREAMWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(GLIMMERING_DREAMWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_GLIMMERING_DREAMWOOD_LOG, logIgnite, logBurn);
		b.botania_setFlammable(DREAMWOOD, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_DREAMWOOD, logIgnite, logBurn);
		b.botania_setFlammable(GLIMMERING_DREAMWOOD, logIgnite, logBurn);
		b.botania_setFlammable(STRIPPED_GLIMMERING_DREAMWOOD, logIgnite, logBurn);
		b.botania_setFlammable(DREAMWOOD_PLANKS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_PLANKS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(MOSSY_DREAMWOOD_PLANKS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(FRAMED_DREAMWOOD, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(PATTERN_FRAMED_DREAMWOOD, nonLogIgnite, nonLogBurn);

		b.botania_setFlammable(SOLID_VINE, 15, 100); // matches vines
		b.botania_setFlammable(CELLULAR_BLOCK, 30, 60); // matches leaves
		b.botania_setFlammable(SHIMMERWOOD_PLANKS, nonLogIgnite, nonLogBurn);

		b.botania_setFlammable(LIVINGWOOD_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_SLAB, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_WALL, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_FENCE, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_FENCE_GATE, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(STRIPPED_LIVINGWOOD_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(STRIPPED_LIVINGWOOD_SLAB, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(STRIPPED_LIVINGWOOD_WALL, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_PLANK_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(LIVINGWOOD_PLANK_SLAB, nonLogIgnite, nonLogBurn);

		b.botania_setFlammable(DREAMWOOD_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_SLAB, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_WALL, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_FENCE, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_FENCE_GATE, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(STRIPPED_DREAMWOOD_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(STRIPPED_DREAMWOOD_SLAB, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(STRIPPED_DREAMWOOD_WALL, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_PLANK_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(DREAMWOOD_PLANK_SLAB, nonLogIgnite, nonLogBurn);

		b.botania_setFlammable(SHIMMERWOOD_PLANK_SLAB, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(SHIMMERWOOD_PLANK_STAIRS, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(SHIMMERWOOD_FENCE, nonLogIgnite, nonLogBurn);
		b.botania_setFlammable(SHIMMERWOOD_FENCE_GATE, nonLogIgnite, nonLogBurn);
	}
}
