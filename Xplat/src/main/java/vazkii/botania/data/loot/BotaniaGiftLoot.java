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

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.loot.BotaniaLootTables;

import java.util.function.BiConsumer;

public class BotaniaGiftLoot implements LootTableSubProvider {
	private final LootTableSubProvider.Context output;
	private final HolderGetter<LootTable> lootTables;

	public BotaniaGiftLoot(LootTableSubProvider.Context output) {
		this.output = output;
		this.lootTables = output.lookup(Registries.LOOT_TABLE);
	}

	@Override
	public void run() {
		generate(output::accept);
	}

	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {

		output.accept(BotaniaLootTables.getDiceRollTable(1), LootTable.lootTable().withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 16)))
				)
		));

		output.accept(BotaniaLootTables.getDiceRollTable(2), LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 4)))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(BotaniaItems.THE_PINKINATOR)))
		);

		output.accept(BotaniaLootTables.getDiceRollTable(3), LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.between(4, 6)))))
				.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.between(2, 4))
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_RUNES)))
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))
				)
		);

		output.accept(BotaniaLootTables.getDiceRollTable(4), LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(4, 8)))))
				.withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_MATERIALS)))
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 10)))
				)
		);

		output.accept(BotaniaLootTables.getDiceRollTable(5), LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(6, 10)))))
				.withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_MATERIALS)))
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(4, 8)))
				)
				.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.between(0, 3))
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_RUNES)))
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))
				)
		);

		output.accept(BotaniaLootTables.getDiceRollTable(6), LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(8, 12)))))
				.withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_MATERIALS)))
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(5, 10)))
				)
				.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.between(1, 3))
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_RUNES)))
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))
				)
				.withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_LOTUSES)))
				)
		);
	}
}
