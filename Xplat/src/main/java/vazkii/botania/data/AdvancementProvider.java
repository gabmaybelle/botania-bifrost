/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;

import net.minecraft.advancements.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.ConsumeItemTrigger;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.common.advancements.*;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.block.block_entity.flower.functional.LooniumBlockEntity;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.LaputaShardItem;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class AdvancementProvider {
	/** The Botania advancement generators, for the loader datagen entry points. */
	public static List<AdvancementSubProvider.Factory> subProviders() {
		return List.of(BotaniaStoryAdvancements::new, BotaniaChallengeAdvancements::new);
	}

	public static net.minecraft.data.advancements.AdvancementProvider create() {
		return new net.minecraft.data.advancements.AdvancementProvider(subProviders());
	}

	/** Shared helpers; item and entity predicates need the registry lookups from the bootstrap context now. */
	public abstract static class BotaniaAdvancements extends AdvancementSubProvider {
		protected final HolderGetter<Item> items;
		protected final HolderGetter<EntityType<?>> entityTypes;

		protected BotaniaAdvancements(BootstrapContext<Advancement> output) {
			super(output);
			this.items = output.lookup(Registries.ITEM);
			this.entityTypes = output.lookup(Registries.ENTITY_TYPE);
		}

		protected AdvancementHolder relicBindAdvancement(BootstrapContext<Advancement> output, AdvancementHolder parent, Item relicItem,
				String titleKey, String criterionName) {
			String id = challengeId(BuiltInRegistries.ITEM.getKey(relicItem).getPath());
			return Advancement.Builder.advancement()
					.display(simple(relicItem, titleKey, AdvancementType.CHALLENGE))
					.parent(parent)
					.rewards(AdvancementRewards.Builder.experience(50))
					.addCriterion(criterionName, RelicBindTrigger.Instance.bound(relicItem))
					.save(output, id);
		}

		protected Criterion<InventoryChangeTrigger.TriggerInstance> onPickup(TagKey<Item> tag) {
			return InventoryChangeTrigger.TriggerInstance.hasItems(
					ItemPredicate.Builder.item().of(items, tag).build());
		}

		protected Criterion<InventoryChangeTrigger.TriggerInstance> onPickup(ItemLike... itemLikes) {
			return InventoryChangeTrigger.TriggerInstance.hasItems(matchItems(itemLikes));
		}

		protected ItemPredicate matchItems(ItemLike... itemLikes) {
			return ItemPredicate.Builder.item().of(items, itemLikes).build();
		}
	}

	public static class BotaniaStoryAdvancements extends BotaniaAdvancements {
		public BotaniaStoryAdvancements(BootstrapContext<Advancement> output) {
			super(output);
		}

		@Override
		public void generate() {
			Criterion<InventoryChangeTrigger.TriggerInstance> elvenLexicon = InventoryChangeTrigger.TriggerInstance.hasItems(
					ItemPredicate.Builder.item().of(items, BotaniaItems.LEXICA_BOTANIA)
							.withComponents(DataComponentMatchers.Builder.components()
									.exact(DataComponentExactPredicate.expect(BotaniaDataComponents.ELVEN_UNLOCK, Unit.INSTANCE)).build())
							.build()
			);

			// Main progression line
			AdvancementHolder root = Advancement.Builder.advancement()
					.display(rootDisplay(BotaniaItems.LEXICA_BOTANIA, "itemGroup.botania",
							"botania.desc", botaniaRL("block/livingwood_log")))
					.addCriterion("flower", onPickup(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS))
					.save(output, mainId("root"));

			AdvancementHolder flowerPickup = Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.PINK_MYSTICAL_FLOWER, "flowerPickup", AdvancementType.TASK))
					.parent(root)
					.addCriterion("flower", onPickup(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS))
					.addCriterion("double_flower", onPickup(BotaniaTags.Items.TALL_MYSTICAL_FLOWERS))
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("flower_pickup"));

			AdvancementHolder manaPoolPickup = Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.MANA_POOL, "manaPoolPickup", AdvancementType.TASK))
					.parent(flowerPickup)
					.addCriterion("pickup", onPickup(BotaniaTags.Items.ALL_MANA_POOLS))
					.save(output, mainId("mana_pool_pickup"));

			AdvancementHolder runePickup = Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.RUNIC_ALTAR, "runePickup", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("rune", onPickup(BotaniaTags.Items.RUNES))
					.save(output, mainId("rune_pickup"));

			AdvancementHolder terrasteelPickup = Advancement.Builder.advancement()
					.display(simple(BotaniaItems.TERRASTEEL_INGOT, "terrasteelPickup", AdvancementType.TASK))
					.parent(runePickup)
					.addCriterion("terrasteel", onPickup(BotaniaItems.TERRASTEEL_INGOT))
					.save(output, mainId("terrasteel_pickup"));

			AdvancementHolder elfPortalOpen = Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.ELVEN_GATEWAY_CORE, "elfPortalOpen", AdvancementType.TASK))
					.parent(terrasteelPickup)
					.addCriterion("portal", AlfheimPortalTrigger.Instance.activatedPortal())
					.save(output, mainId("elf_portal_open"));

			AdvancementHolder gaiaGuardianKill = Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.GAIA_HEAD, "gaiaGuardianKill", AdvancementType.TASK))
					.parent(elfPortalOpen)
					.addCriterion("guardian", KilledTrigger.TriggerInstance
							.playerKilledEntity(EntityPredicate.Builder.entity().of(entityTypes, BotaniaEntities.GAIA_GUARDIAN)))
					.save(output, mainId("gaia_guardian_kill"));

			AdvancementHolder enderEssenceMake = Advancement.Builder.advancement()
					.display(simple(BotaniaItems.PURE_ENDER_ESSENCE, "enderEssenceMake", AdvancementType.TASK))
					.parent(elfPortalOpen)
					.addCriterion("essence", onPickup(BotaniaTags.Items.ENDER_ESSENCES))
					.save(output, mainId("ender_essence_make"));

			// Parent: root
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.LEXICA_BOTANIA, "lexiconUse", AdvancementType.TASK))
					.parent(root)
					.addCriterion("use_lexicon", UseItemSuccessTrigger.Instance.used(BotaniaItems.LEXICA_BOTANIA))
					.save(output, mainId("lexicon_use"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.CACOPHONIUM, "cacophoniumCraft", AdvancementType.TASK))
					.parent(root)
					.addCriterion("cacophonium", onPickup(BotaniaItems.CACOPHONIUM))
					.save(output, mainId("cacophonium_craft"));

			// Parent: mystical flowers
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.ENDOFLAME, "generatingFlower", AdvancementType.TASK))
					.parent(flowerPickup)
					.addCriterion("flower", onPickup(BotaniaTags.Items.GENERATING_SPECIAL_FLOWERS))
					.save(output, mainId("generating_flower"));

			// Parent: mana pool
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.MANA_ENCHANTER, "enchanterMake", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, mainId("enchanter_make"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.BELLETHORNE, "functionalFlower", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("flower", onPickup(BotaniaTags.Items.FUNCTIONAL_SPECIAL_FLOWERS))
					.save(output, mainId("functional_flower"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.MANA_POOL_MINECART, "manaCartCraft", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("poolcart", onPickup(BotaniaItems.MANA_POOL_MINECART))
					.save(output, mainId("mana_cart_craft"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.MANAWEAVE_CLOTH, "manaweaveArmorCraft", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("head", onPickup(
							BotaniaItems.MANAWEAVE_HELMET, BotaniaItems.MANAWEAVE_CHESTPLATE,
							BotaniaItems.MANAWEAVE_LEGGINGS, BotaniaItems.MANAWEAVE_BOOTS))
					.save(output, mainId("manaweave_armor_craft"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.MANA_SPARK, "sparkCraft", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("spark", onPickup(BotaniaItems.MANA_SPARK))
					.save(output, mainId("mana_spark_craft"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.BISCUIT_OF_TOTALITY, "manaCookieEat", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("cookie",
							ConsumeItemTrigger.TriggerInstance.usedItem(items, BotaniaItems.BISCUIT_OF_TOTALITY))
					.save(output, mainId("biscuit_of_totality_eat"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.ASSEMBLY_HALO, "craftingHaloCraft", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("pool", onPickup(BotaniaItems.ASSEMBLY_HALO))
					.save(output, mainId("assembly_halo_craft"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.BAND_OF_MANA, "baubleWear", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, mainId("bauble_wear"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.TINY_POTATO, "tinyPotatoPet", AdvancementType.TASK))
					.parent(manaPoolPickup)
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, mainId("tiny_potato_pet"));

			// Parent: runes
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.MANA_BLASTER, "manaBlasterShoot", AdvancementType.TASK))
					.parent(runePickup)
					.addCriterion("shoot", ManaBlasterTrigger.Instance.shoot())
					.save(output, mainId("mana_blaster_shoot"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.POLLIDISIAC, "pollidisiacPickup", AdvancementType.TASK))
					.parent(runePickup)
					.addCriterion("pollidisiac", onPickup(BotaniaBlocks.POLLIDISIAC))
					.save(output, mainId("pollidisiac_pickup"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.ROD_OF_THE_LANDS, "dirtRodCraft", AdvancementType.TASK))
					.parent(runePickup)
					.addCriterion("dirtrod", onPickup(BotaniaItems.ROD_OF_THE_LANDS))
					.save(output, mainId("rod_of_the_lands_craft"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.BOTANICAL_BREWERY, "brewPickup", AdvancementType.TASK))
					.parent(runePickup)
					.addCriterion("pickup", onPickup(BotaniaItems.BREW_FLASK, BotaniaItems.BREW_VIAL))
					.save(output, mainId("brew_pickup"));

			// Parent: terrasteel
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.TERRA_BLADE, "terrasteelWeaponCraft", AdvancementType.TASK))
					.parent(terrasteelPickup)
					.addCriterion("terrablade", onPickup(BotaniaItems.TERRA_BLADE, BotaniaItems.THORN_CHAKRAM))
					.save(output, mainId("terrasteel_weapon_craft"));

			// Parent: elven portal
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.HEISEI_DREAM, "heiseiDreamPickup", AdvancementType.TASK))
					.parent(elfPortalOpen)
					.addCriterion("heisei_dream", onPickup(BotaniaBlocks.HEISEI_DREAM))
					.save(output, mainId("heisei_dream_pickup"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.KEKIMURUS, "kekimurusPickup", AdvancementType.TASK))
					.parent(elfPortalOpen)
					.addCriterion("kekimurus", onPickup(BotaniaBlocks.KEKIMURUS))
					.save(output, mainId("kekimurus_pickup"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.BUBBELL, "bubbellPickup", AdvancementType.TASK))
					.parent(elfPortalOpen)
					.addCriterion("bubbell", onPickup(BotaniaBlocks.BUBBELL))
					.save(output, mainId("bubbell_pickup"));

			// Parent: gaia guardian
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.LIFE_AGGREGATOR, "spawnerMoverUse", AdvancementType.TASK))
					.parent(gaiaGuardianKill)
					.addCriterion("use_life_aggregator",
							UseItemSuccessTrigger.Instance.used(BotaniaItems.LIFE_AGGREGATOR))
					.save(output, mainId("life_aggregator_use"));
			DisplayInfo tiaraWings = simple(new ItemStackTemplate(BotaniaItems.FLUGEL_TIARA,
					DataComponentPatch.builder().set(BotaniaDataComponents.TIARA_VARIANT, 1).build()),
					"tiaraWings", AdvancementType.TASK);
			Criterion<?>[] variants = IntStream.range(1, FlugelTiaraItem.WING_TYPES)
					.mapToObj(i -> ItemPredicate.Builder.item().of(items, BotaniaItems.FLUGEL_TIARA)
							.withComponents(DataComponentMatchers.Builder.components()
									.exact(DataComponentExactPredicate.expect(BotaniaDataComponents.TIARA_VARIANT, i)).build())
							.build())
					.map(InventoryChangeTrigger.TriggerInstance::hasItems)
					.toArray(Criterion<?>[]::new);
			var builder = Advancement.Builder.advancement()
					.display(tiaraWings)
					.parent(gaiaGuardianKill)
					.requirements(AdvancementRequirements.Strategy.OR);
			for (int i = 0; i < variants.length; i++) {
				var variant = variants[i];
				builder.addCriterion("tiara_" + (i + 1), variant);
			}
			builder.save(output, mainId("tiara_wings"));

			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.DANDELIFEON, "dandelifeonPickup", AdvancementType.TASK))
					.parent(gaiaGuardianKill)
					.addCriterion("dandelifeon", onPickup(BotaniaBlocks.DANDELIFEON))
					.save(output, mainId("dandelifeon_pickup"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.MANASTORM_CHARGE, "manaBombIgnite", AdvancementType.TASK))
					.parent(gaiaGuardianKill)
					.addCriterion("bomb", onPickup(BotaniaBlocks.MANASTORM_CHARGE))
					.save(output, mainId("manastorm_charge_ignite"));

			// Parent: ender air
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.LUMINIZER, "luminizerRide", AdvancementType.TASK))
					.parent(enderEssenceMake)
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, mainId("luminizer_ride"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.CORPOREA_CRYSTAL_CUBE, "corporeaCraft", AdvancementType.TASK))
					.parent(enderEssenceMake)
					.addCriterion("pickup", onPickup(
							BotaniaBlocks.CORPOREA_CRYSTAL_CUBE, BotaniaBlocks.CORPOREA_FUNNEL,
							BotaniaBlocks.CORPOREA_INDEX, BotaniaBlocks.CORPOREA_INTERCEPTOR,
							BotaniaBlocks.CORPOREA_RETAINER
					))
					.save(output, mainId("corporea_craft"));

			// Lexicon locks
			Advancement.Builder.advancement()
					.parent(root)
					.addCriterion("flower", onPickup(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS))
					.addCriterion("double_flower", onPickup(BotaniaTags.Items.TALL_MYSTICAL_FLOWERS))
					.addCriterion("elven_lexicon", elvenLexicon)
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("flower_pickup_lexicon"));
			Advancement.Builder.advancement()
					.parent(flowerPickup)
					.addCriterion("apothecary", onPickup(BotaniaTags.Items.PETAL_APOTHECARIES))
					.addCriterion("elven_lexicon", elvenLexicon)
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("apothecary_pickup"));
			Advancement.Builder.advancement()
					.parent(flowerPickup)
					.addCriterion("daisy", onPickup(BotaniaBlocks.PURE_DAISY))
					.addCriterion("elven_lexicon", elvenLexicon)
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("pure_daisy_pickup"));
			Advancement.Builder.advancement()
					.parent(root)
					.addCriterion("pickup", onPickup(BotaniaTags.Items.ALL_MANA_POOLS))
					.addCriterion("elven_lexicon", elvenLexicon)
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("mana_pool_pickup_lexicon"));
			Advancement.Builder.advancement()
					.parent(flowerPickup)
					.addCriterion("altar", onPickup(BotaniaBlocks.RUNIC_ALTAR))
					.addCriterion("rune", onPickup(BotaniaTags.Items.RUNES))
					.addCriterion("elven_lexicon", elvenLexicon)
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("runic_altar_pickup"));
			Advancement.Builder.advancement()
					.parent(flowerPickup)
					.addCriterion("terrasteel", onPickup(BotaniaItems.TERRASTEEL_INGOT))
					.addCriterion("elven_lexicon", elvenLexicon)
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, mainId("terrasteel_pickup_lexicon"));
			Advancement.Builder.advancement()
					.parent(elfPortalOpen)
					.addCriterion("lexicon", elvenLexicon)
					.save(output, mainId("elf_lexicon_pickup"));
			Advancement.Builder.advancement()
					.parent(root)
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, mainId("dog"));
		}
	}

	public static class BotaniaChallengeAdvancements extends BotaniaAdvancements {
		public BotaniaChallengeAdvancements(BootstrapContext<Advancement> output) {
			super(output);
		}

		private static final EntityType<?>[] LOONIUM_MOBS_TO_KILL = {
				EntityTypes.BLAZE,
				EntityTypes.CAVE_SPIDER,
				EntityTypes.CREEPER,
				EntityTypes.DROWNED,
				EntityTypes.ENDERMAN,
				EntityTypes.EVOKER,
				EntityTypes.GUARDIAN,
				EntityTypes.HOGLIN,
				EntityTypes.HUSK,
				EntityTypes.PIGLIN,
				EntityTypes.PIGLIN_BRUTE,
				EntityTypes.PILLAGER,
				EntityTypes.SHULKER,
				EntityTypes.SILVERFISH,
				EntityTypes.SKELETON,
				EntityTypes.STRAY,
				EntityTypes.VINDICATOR,
				EntityTypes.WITHER_SKELETON,
				EntityTypes.ZOGLIN,
				EntityTypes.ZOMBIE_VILLAGER,
				EntityTypes.ZOMBIE,
				EntityTypes.ZOMBIFIED_PIGLIN
		};

		@Override
		public void generate() {
			AdvancementHolder root = Advancement.Builder.advancement()
					.display(rootDisplay(BotaniaItems.DICE_OF_FATE,
							"advancement.botania_challenge",
							"advancement.botania_challenge.desc",
							botaniaRL("block/livingrock_bricks")))
					.addCriterion("flower", onPickup(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS))
					.save(output, challengeId("root"));

			// hardmode Gaia Guardian related
			CompoundTag hardmodeNbt = new CompoundTag();
			hardmodeNbt.putBoolean("hardMode", true);
			AdvancementHolder hardMode = Advancement.Builder.advancement()
					.display(simple(BotaniaItems.GAIA_SPIRIT, "gaiaGuardianHardmode", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(100))
					.addCriterion("guardian", KilledTrigger.TriggerInstance.playerKilledEntity(
							EntityPredicate.Builder.entity()
									.of(entityTypes, BotaniaEntities.GAIA_GUARDIAN)
									.nbt(new NbtPredicate(hardmodeNbt))))
					.save(output, challengeId("gaia_guardian_hardmode"));

			relicBindAdvancement(output, hardMode, BotaniaItems.FRUIT_OF_GRISAIA, "infiniteFruit", "fruit");
			relicBindAdvancement(output, hardMode, BotaniaItems.KEY_OF_THE_KINGS_LAW, "kingKey", "key");
			relicBindAdvancement(output, hardMode, BotaniaItems.EYE_OF_THE_FLUGEL, "flugelEye", "eye");
			relicBindAdvancement(output, hardMode, BotaniaItems.RING_OF_THOR, "thorRing", "ring");
			relicBindAdvancement(output, hardMode, BotaniaItems.RING_OF_ODIN, "odinRing", "ring");
			AdvancementHolder lokiRing = relicBindAdvancement(output, hardMode, BotaniaItems.RING_OF_LOKI, "lokiRing", "ring");

			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.RING_OF_LOKI, "lokiRingMany", AdvancementType.CHALLENGE))
					.parent(lokiRing)
					.rewards(AdvancementRewards.Builder.experience(85))
					.addCriterion("place_blocks",
							LokiPlaceTrigger.Instance.blocksPlaced(MinMaxBounds.Ints.atLeast(255)))
					.save(output, challengeId("ring_of_loki_many"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.THE_PINKINATOR, "the_pinkinator", AdvancementType.CHALLENGE))
					.parent(hardMode)
					.rewards(AdvancementRewards.Builder.experience(40))
					.addCriterion("use_pinkinator",
							UseItemSuccessTrigger.Instance.used(BotaniaItems.THE_PINKINATOR))
					.save(output, challengeId("the_pinkinator"));

			// Misc challenges
			Advancement.Builder.advancement()
					.display(simple(Blocks.PLAYER_HEAD, "gaiaGuardianNoArmor", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(1000))
					.addCriterion("no_armor", GaiaGuardianNoArmorTrigger.Instance.unarmoredKill())
					.save(output, challengeId("gaia_guardian_no_armor"));
			Advancement.Builder.advancement()
					.display(hidden(BotaniaBlocks.DAYBLOOM_MOTIF, "old_flower_pickup", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(40))
					.addCriterion("flower", onPickup(BotaniaBlocks.DAYBLOOM_MOTIF, BotaniaBlocks.NIGHTSHADE_MOTIF))
					.requirements(AdvancementRequirements.Strategy.OR)
					.save(output, challengeId("old_flower_pickup"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.CORPOREA_INDEX, "superCorporeaRequest", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(85))
					.addCriterion("big_request", CorporeaRequestTrigger.Instance.numExtracted(
							MinMaxBounds.Ints.atLeast(CorporeaIndexBlockEntity.MAX_REQUEST)))
					.save(output, challengeId("super_corporea_request"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.TERRA_SHATTERER, "rankSSPick", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(500))
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, challengeId("rank_ss_pick"));
			CompoundTag level20Shard = new CompoundTag();
			level20Shard.putInt("level", 19);
			Advancement.Builder.advancement()
					.display(simple(BotaniaItems.SHARD_OF_LAPUTA, "l20ShardUse", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(65))
					.addCriterion("use_l20_shard", InventoryChangeTrigger.TriggerInstance.hasItems(
							ItemPredicate.Builder.item().of(items, BotaniaItems.SHARD_OF_LAPUTA).withComponents(
									DataComponentMatchers.Builder.components()
											.exact(DataComponentExactPredicate.expect(BotaniaDataComponents.SHARD_LEVEL, LaputaShardItem.MAX_LEVEL)).build())
									.build()))
					.save(output, challengeId("l20_shard_use"));
			Advancement.Builder.advancement()
					.display(hidden(Items.BREAD, "alfPortalBread", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(40))
					.addCriterion("bread", AlfheimPortalBreadTrigger.Instance.sentBread())
					.save(output, challengeId("alf_portal_bread"));
			Advancement.Builder.advancement()
					.display(simple(BotaniaBlocks.TINY_POTATO, "tinyPotatoBirthday", AdvancementType.CHALLENGE))
					.parent(root)
					.rewards(AdvancementRewards.Builder.experience(40))
					.addCriterion("code_triggered",
							CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
					.save(output, challengeId("tiny_potato_birthday"));
			addLooniumMobsToKill(Advancement.Builder.advancement())
					.display(simple(BotaniaBlocks.LOONIUM, "allLooniumMobs", AdvancementType.CHALLENGE))
					.parent(root)
					.requirements(AdvancementRequirements.Strategy.AND)
					.save(output, challengeId("all_loonium_mobs"));
		}

		private Advancement.Builder addLooniumMobsToKill(Advancement.Builder builder) {
			for (EntityType<?> entityType : LOONIUM_MOBS_TO_KILL) {
				builder.addCriterion(
						BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString(),
						KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
								.of(entityTypes, entityType).team(LooniumBlockEntity.LOONIUM_TEAM_NAME))
				);
			}

			return builder;
		}
	}

	protected static DisplayInfo simple(ItemLike icon, String name, AdvancementType advancementType) {
		return simple(new ItemStackTemplate(icon.asItem()), name, advancementType);
	}

	protected static DisplayInfo simple(ItemStackTemplate icon, String name, AdvancementType advancementType) {
		String expandedName = "advancement.botania:" + name;
		return new DisplayInfo(icon,
				Component.translatable(expandedName),
				Component.translatable(expandedName + ".desc"),
				Optional.empty(), advancementType, true, true, false);
	}

	protected static DisplayInfo hidden(ItemLike icon, String name, AdvancementType advancementType) {
		String expandedName = "advancement.botania:" + name;
		return new DisplayInfo(new ItemStackTemplate(icon.asItem()),
				Component.translatable(expandedName),
				Component.translatable(expandedName + ".desc"),
				Optional.empty(), advancementType, true, true, true);
	}

	protected static DisplayInfo rootDisplay(ItemLike icon, String titleKey, String descKey, Identifier background) {
		return new DisplayInfo(new ItemStackTemplate(icon.asItem()),
				Component.translatable(titleKey),
				Component.translatable(descKey),
				Optional.of(new ClientAsset.ResourceTexture(background)), AdvancementType.TASK, false, false, false);
	}

	private static String mainId(String name) {
		return botaniaRL("main/" + name).toString();
	}

	private static String challengeId(String name) {
		return botaniaRL("challenge/" + name).toString();
	}
}
