/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;

import vazkii.botania.common.loot.BotaniaLootTables;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.function.Consumer;

/**
 * Fabric-specific loot table injection. Neoforge has global loot modifiers for this.
 */
public final class LootHandler {
	public static void injectLoot(Identifier id, HolderLookup.Provider registries, Consumer<LootPool.Builder> addPool) {
		ResourceKey<LootTable> injectedLootTable = BotaniaLootTables.getInjectedLootTable(id);
		if (BotaniaLootTables.all().contains(injectedLootTable)) {
			addPool.accept(LootPool.lootPool().add(NestedLootTable.lootTableReference(lootTable(registries, injectedLootTable))));
		}
	}

	public static void injectGogLoot(Identifier id, HolderLookup.Provider registries, Consumer<LootPool.Builder> addPool) {
		if (XplatAbstractions.INSTANCE.gogLoaded() && (isLootTable(Blocks.SHORT_GRASS, id) || isLootTable(Blocks.TALL_GRASS, id))) {
			addPool.accept(LootPool.lootPool().add(NestedLootTable.lootTableReference(lootTable(registries, BotaniaLootTables.GOG_EXTRA_SEEDS))));
		}
	}

	// PORT-26.3: a nested loot table takes a holder now (resolved through the registries the loot event hands over),
	// and a block's loot table is optional
	private static Holder<LootTable> lootTable(HolderLookup.Provider registries, ResourceKey<LootTable> key) {
		return registries.lookupOrThrow(Registries.LOOT_TABLE).getOrThrow(key);
	}

	private static boolean isLootTable(Block block, Identifier id) {
		return block.getLootTable().map(key -> key.identifier().equals(id)).orElse(false);
	}
}
