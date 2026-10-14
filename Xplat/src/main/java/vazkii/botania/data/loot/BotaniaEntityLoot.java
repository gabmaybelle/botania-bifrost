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

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.FillPlayerHead;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.loot.BindUuid;
import vazkii.botania.common.loot.BotaniaLootTables;
import vazkii.botania.common.loot.EnableRelics;
import vazkii.botania.common.loot.TrueGuardianKiller;

import java.util.function.BiConsumer;

// not extending EntityLootSubProvider, as that assumes 1:1 match with EntityTypes
public class BotaniaEntityLoot implements LootTableSubProvider {
	private final LootTableSubProvider.Context output;
	private final HolderGetter<Item> items;
	private final HolderGetter<Enchantment> enchantments;
	private final HolderGetter<LootTable> lootTables;

	public BotaniaEntityLoot(LootTableSubProvider.Context output) {
		this.output = output;
		this.items = output.lookup(Registries.ITEM);
		this.enchantments = output.lookup(Registries.ENCHANTMENT);
		this.lootTables = output.lookup(Registries.LOOT_TABLE);
	}

	@Override
	public void run() {
		generate(output::accept);
	}

	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		defineElementiumAxeBeheadingLoot(output);

		output.accept(BotaniaLootTables.FEL_BLAZE, LootTable.lootTable().withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(Items.BLAZE_POWDER)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(6)))
						.when(InvertedLootItemCondition.invert(LootItemKilledByPlayerCondition.killedByPlayer()))
				)
				.add(LootItem.lootTableItem(Items.BLAZE_POWDER)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(10)))
						.apply(EnchantedCountIncreaseFunction.lootingMultiplier(enchantments, ContextFloatProviders.between(0, 6)))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		));

		output.accept(BotaniaLootTables.GHAST_ENDER_ESSENCE_CRYING, LootTable.lootTable().withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(Items.GHAST_TEAR))
				.add(EmptyLootItem.emptyItem().setWeight(7))
		));

		output.accept(BotaniaLootTables.GAIA_GUARDIAN_REWARD, LootTable.lootTable()
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(6)))
								.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(8))
										.when(TrueGuardianKiller.builder())
								)
						)
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceCondition.randomChance(0.2f))
						.add(LootItem.lootTableItem(BotaniaItems.SCATHED_MUSIC_DISC_1))
				)
		);

		output.accept(BotaniaLootTables.GAIA_GUARDIAN_REWARD_HARD, LootTable.lootTable()
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(LootItem.lootTableItem(BotaniaItems.GAIA_SPIRIT)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(10)))
								.apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(16))
										.when(TrueGuardianKiller.builder())
								)
						)
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceCondition.randomChance(0.5f))
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_LOTUSES)))
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(TagEntry.expandTag(items.getOrThrow(BotaniaTags.Items.ANCIENT_WILLS)))
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_MATERIALS)))
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.setRolls(ContextIntProviders.between(1, 6))
						.add(NestedLootTable.lootTableReference(lootTables.getOrThrow(BotaniaLootTables.GAIA_GUARDIAN_RUNES)))
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceCondition.randomChance(0.1f))
						.add(LootItem.lootTableItem(BotaniaItems.THE_PINKINATOR))
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceCondition.randomChance(0.44f))
						.add(LootItem.lootTableItem(BotaniaItems.SCATHED_MUSIC_DISC_2).setWeight(14))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_13).setWeight(15))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_WAIT).setWeight(15))
				)
				.withPool(LootPool.lootPool().when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(EnableRelics.builder())
						.add(LootItem.lootTableItem(BotaniaItems.DICE_OF_FATE)
								.apply(BindUuid.builder())
						)
				)
		);
	}

	// Note: keep in sync with entity list in BotaniaLootTables
	private void defineElementiumAxeBeheadingLoot(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		// TODO: this can technically cause a double drop if injected as additional pool
		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.WITHER_SKELETON),
				defineAxeBeheadingDropTable(Items.WITHER_SKELETON_SKULL, 0.1154f, 0.0385f));

		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.SKELETON),
				defineAxeBeheadingDropTable(Items.SKELETON_SKULL, 0.1154f, 0.0385f));

		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.ZOMBIE),
				defineAxeBeheadingDropTable(Items.ZOMBIE_HEAD, 0.0769f, 0.0769f));

		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.PIGLIN),
				defineAxeBeheadingDropTable(Items.PIGLIN_HEAD, 0.0769f, 0.0769f));

		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.CREEPER),
				defineAxeBeheadingDropTable(Items.CREEPER_HEAD, 0.0769f, 0.0769f));

		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.ENDER_DRAGON),
				defineAxeBeheadingDropTable(Items.DRAGON_HEAD, 0.25f, 0.25f));

		output.accept(BotaniaLootTables.getInjectedLootTable(EntityTypes.PLAYER),
				defineAxeBeheadingDropTable(Items.PLAYER_HEAD, 0.0909f, 0.0909f)
						.apply(FillPlayerHead.fillPlayerHead(LootContext.EntityTarget.THIS)));

		output.accept(BotaniaEntities.GAIA_GUARDIAN.getDefaultLootTable().orElseThrow(),
				defineAxeBeheadingDropTable(BotaniaBlocks.GAIA_HEAD, 0.0769f, 0.0769f));
	}

	private LootTable.Builder defineAxeBeheadingDropTable(ItemLike head, float chance, float lootingMultiplier) {
		return LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(head))
				.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.ATTACKING_PLAYER,
						EntityPredicate.Builder.entity().equipment(
								EntityEquipmentPredicate.Builder.equipment().mainhand(
										ItemPredicate.Builder.item().of(items, BotaniaItems.ELEMENTIUM_AXE)))))
				.when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(enchantments,
						chance, lootingMultiplier))
		);
	}
}
