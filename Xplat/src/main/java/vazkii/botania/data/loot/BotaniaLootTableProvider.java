/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.data.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.List;
import java.util.Set;

public final class BotaniaLootTableProvider {
	private BotaniaLootTableProvider() {}

	/** Every Botania loot generator with its context parameter set, for the loader datagen entry points. */
	public static List<LootTableProvider.SubProviderEntry> subProviders() {
		return List.of(
				new LootTableProvider.SubProviderEntry(BotaniaBlockLoot::new, LootContextParamSets.BLOCK),
				new LootTableProvider.SubProviderEntry(BotaniaChestLoot::new, LootContextParamSets.CHEST),
				new LootTableProvider.SubProviderEntry(BotaniaEntityLoot::new, LootContextParamSets.ENTITY),
				new LootTableProvider.SubProviderEntry(BotaniaEquipmentLoot::new, LootContextParamSets.EQUIPMENT),
				new LootTableProvider.SubProviderEntry(BotaniaGenericLoot::new, LootContextParamSets.ALL_PARAMS),
				new LootTableProvider.SubProviderEntry(BotaniaGiftLoot::new, LootContextParamSets.GIFT)
		);
	}

	public static LootTableProvider create() {
		return new LootTableProvider(Set.of(), subProviders());
	}

	public static LootTable.Builder copyReferencedLootTable(LootTableSubProvider.Context output, ResourceKey<LootTable> reference) {
		return LootTable.lootTable().withPool(
				LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.add(NestedLootTable.lootTableReference(output.lookup(Registries.LOOT_TABLE).getOrThrow(reference)))
		);
	}
}
