/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.item.AncientWillContainer;
import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.api.state.enums.CraftyCratePattern;
import vazkii.botania.client.gui.bag.ColoredContentsPouchMenu;
import vazkii.botania.client.gui.box.TrinketCaseMenu;
import vazkii.botania.client.gui.enderhand.HandOfEnderMenu;
import vazkii.botania.common.BotaniaStats;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.component.ManaRepair;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.item.brew.FlaskItem;
import vazkii.botania.common.item.brew.IncenseStickItem;
import vazkii.botania.common.item.brew.VialItem;
import vazkii.botania.common.item.equipment.armor.elementium.*;
import vazkii.botania.common.item.equipment.armor.elementium.ElementiumArmorItem;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelHelmItem;
import vazkii.botania.common.item.equipment.armor.manaweave.ManaweaveArmorItem;
import vazkii.botania.common.item.equipment.armor.manaweave.ManaweaveHelmItem;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelArmorItem;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;
import vazkii.botania.common.item.equipment.bauble.*;
import vazkii.botania.common.item.equipment.tool.SoulscribeItem;
import vazkii.botania.common.item.equipment.tool.StarcallerItem;
import vazkii.botania.common.item.equipment.tool.ThundercallerItem;
import vazkii.botania.common.item.equipment.tool.VitreousPickaxeItem;
import vazkii.botania.common.item.equipment.tool.bow.CrystalBowItem;
import vazkii.botania.common.item.equipment.tool.elementium.*;
import vazkii.botania.common.item.equipment.tool.manasteel.*;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraBladeItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;
import vazkii.botania.common.item.lens.*;
import vazkii.botania.common.item.material.*;
import vazkii.botania.common.item.record.BotaniaJukeboxSongs;
import vazkii.botania.common.item.relic.*;
import vazkii.botania.common.item.rod.*;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.mixin.CauldronInteractionDispatcherAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public final class BotaniaItems {
	private static final Map<String, Item> ALL = new LinkedHashMap<>(); // Preserve insertion order

	public static final LexicaBotaniaItem LEXICA_BOTANIA = make(LibItemNames.LEXICA_BOTANIA,
			LexicaBotaniaItem::new, unstackable());
	public static final Item WAND_OF_THE_FOREST = make(LibItemNames.WAND_OF_THE_FOREST,
			props -> new WandOfTheForestItem(ChatFormatting.DARK_GREEN, props), unstackable()
					.component(BotaniaDataComponents.WAND_BIND_MODE, Unit.INSTANCE));
	public static final Item WAND_OF_THE_ELVEN_FOREST = make(LibItemNames.WAND_OF_THE_ELVEN_FOREST,
			props -> new WandOfTheForestItem(ChatFormatting.LIGHT_PURPLE, props), unstackable()
					.component(BotaniaDataComponents.WAND_BIND_MODE, Unit.INSTANCE));
	public static final Item FLORAL_OBEDIENCE_STICK = make(LibItemNames.FLORAL_OBEDIENCE_STICK,
			FloralObedienceStickItem::new, unstackable());
	public static final Item FLORAL_FERTILIZER = make(LibItemNames.FLORAL_FERTILIZER,
			FloralFertilizerItem::new, defaultBuilder());

	// mystical petals
	public static final Item WHITE_MYSTICAL_PETAL = makePetal(DyeColor.WHITE);
	public static final Item LIGHT_GRAY_MYSTICAL_PETAL = makePetal(DyeColor.LIGHT_GRAY);
	public static final Item GRAY_MYSTICAL_PETAL = makePetal(DyeColor.GRAY);
	public static final Item BLACK_MYSTICAL_PETAL = makePetal(DyeColor.BLACK);
	public static final Item BROWN_MYSTICAL_PETAL = makePetal(DyeColor.BROWN);
	public static final Item RED_MYSTICAL_PETAL = makePetal(DyeColor.RED);
	public static final Item ORANGE_MYSTICAL_PETAL = makePetal(DyeColor.ORANGE);
	public static final Item YELLOW_MYSTICAL_PETAL = makePetal(DyeColor.YELLOW);
	public static final Item LIME_MYSTICAL_PETAL = makePetal(DyeColor.LIME);
	public static final Item GREEN_MYSTICAL_PETAL = makePetal(DyeColor.GREEN);
	public static final Item CYAN_MYSTICAL_PETAL = makePetal(DyeColor.CYAN);
	public static final Item LIGHT_BLUE_MYSTICAL_PETAL = makePetal(DyeColor.LIGHT_BLUE);
	public static final Item BLUE_MYSTICAL_PETAL = makePetal(DyeColor.BLUE);
	public static final Item PURPLE_MYSTICAL_PETAL = makePetal(DyeColor.PURPLE);
	public static final Item MAGENTA_MYSTICAL_PETAL = makePetal(DyeColor.MAGENTA);
	public static final Item PINK_MYSTICAL_PETAL = makePetal(DyeColor.PINK);

	public static final Item MANASTEEL_INGOT = make(LibItemNames.MANASTEEL_INGOT, Item::new, defaultBuilder());
	public static final Item MANA_PEARL = make(LibItemNames.MANA_PEARL, Item::new, defaultBuilder());
	public static final Item MANA_DIAMOND = make(LibItemNames.MANA_DIAMOND, Item::new, defaultBuilder());
	public static final Item LIVINGWOOD_TWIG = make(LibItemNames.LIVINGWOOD_TWIG, Item::new, defaultBuilder());
	public static final Item TERRASTEEL_INGOT = make(LibItemNames.TERRASTEEL_INGOT,
			props -> new GaiaRitualSacrificeItem(props, false), defaultBuilder()
					.rarity(Rarity.UNCOMMON));
	public static final Item GAIA_SPIRIT = make(LibItemNames.GAIA_SPIRIT,
			Item::new, defaultBuilder()
					.rarity(Rarity.RARE));
	public static final Item REDSTONE_ROOT = make(LibItemNames.REDSTONE_ROOT, Item::new, defaultBuilder());
	public static final Item ELEMENTIUM_INGOT = make(LibItemNames.ELEMENTIUM_INGOT, Item::new, defaultBuilder());
	public static final Item PIXIE_DUST = make(LibItemNames.PIXIE_DUST, Item::new, defaultBuilder());
	public static final Item DRAGONSTONE = make(LibItemNames.DRAGONSTONE, Item::new, defaultBuilder());
	public static final Item RED_STRING = make(LibItemNames.RED_STRING, Item::new, defaultBuilder());
	public static final Item DREAMWOOD_TWIG = make(LibItemNames.DREAMWOOD_TWIG, Item::new, defaultBuilder());
	public static final Item GAIA_INGOT = make(LibItemNames.GAIA_INGOT,
			props -> new GaiaRitualSacrificeItem(props, true), defaultBuilder().rarity(Rarity.RARE));
	public static final Item PURE_ENDER_ESSENCE = make(LibItemNames.PURE_ENDER_ESSENCE, ThrowableEnderEssenceItem::new, defaultBuilder());
	public static final Item DILUTED_ENDER_ESSENCE = make(LibItemNames.DILUTED_ENDER_ESSENCE, Item::new, defaultBuilder());
	public static final Item MANA_INFUSED_STRING = make(LibItemNames.MANA_STRING, Item::new, defaultBuilder());
	public static final Item MANASTEEL_NUGGET = make(LibItemNames.MANASTEEL_NUGGET, Item::new, defaultBuilder());
	public static final Item TERRASTEEL_NUGGET = make(LibItemNames.TERRASTEEL_NUGGET,
			Item::new, defaultBuilder().rarity(Rarity.UNCOMMON));
	public static final Item ELEMENTIUM_NUGGET = make(LibItemNames.ELEMENTIUM_NUGGET, Item::new, defaultBuilder());
	public static final Item LIVING_ROOT = make(LibItemNames.LIVING_ROOT, BoneMealItem::new, defaultBuilder());
	public static final Item PEBBLE = make(LibItemNames.PEBBLE, Item::new, defaultBuilder());
	public static final Item MANAWEAVE_CLOTH = make(LibItemNames.MANAWEAVE_CLOTH, Item::new, defaultBuilder());
	public static final Item MANA_POWDER = make(LibItemNames.MANA_POWDER, Item::new, defaultBuilder());

	// quartz variants
	public static final Item DARK_QUARTZ = make(LibItemNames.SMOKEY_QUARTZ, Item::new, defaultBuilder());
	public static final Item MANA_QUARTZ = make(LibItemNames.MANA_QUARTZ, Item::new, defaultBuilder());
	public static final Item BLAZE_QUARTZ = make(LibItemNames.BLAZE_QUARTZ, Item::new, defaultBuilder());
	public static final Item LAVENDER_QUARTZ = make(LibItemNames.LAVENDER_QUARTZ, Item::new, defaultBuilder());
	public static final Item RED_QUARTZ = make(LibItemNames.RED_QUARTZ, Item::new, defaultBuilder());
	public static final Item ELVEN_QUARTZ = make(LibItemNames.ELVEN_QUARTZ, Item::new, defaultBuilder());
	public static final Item SUNNY_QUARTZ = make(LibItemNames.SUNNY_QUARTZ, Item::new, defaultBuilder());

	// lenses
	public static final Item MANA_LENS = make(LibItemNames.MANA_LENS,
			props -> new LensItem(props, new Lens(), LensItem.PROP_NONE), stackTo16());
	public static final Item VELOCITY_LENS = make(LibItemNames.VELOCITY_LENS,
			props -> new LensItem(props, new VelocityLens(), LensItem.PROP_NONE), stackTo16());
	public static final Item POTENCY_LENS = make(LibItemNames.POTENCY_LENS,
			props -> new LensItem(props, new PotencyLens(), LensItem.PROP_POWER), stackTo16());
	public static final Item RESISTANCE_LENS = make(LibItemNames.RESISTANCE_LENS,
			props -> new LensItem(props, new ResistanceLens(), LensItem.PROP_NONE), stackTo16());
	public static final Item EFFICIENCY_LENS = make(LibItemNames.EFFICIENCY_LENS,
			props -> new LensItem(props, new EfficiencyLens(), LensItem.PROP_NONE), stackTo16());
	public static final Item BOUNCE_LENS = make(LibItemNames.BOUNCE_LENS,
			props -> new LensItem(props, new BounceLens(), LensItem.PROP_TOUCH), stackTo16());
	public static final Item GRAVITY_LENS = make(LibItemNames.GRAVITY_LENS,
			props -> new LensItem(props, new GravityLens(), LensItem.PROP_ORIENTATION), stackTo16());
	public static final Item BORE_LENS = make(LibItemNames.BORE_LENS,
			props -> new LensItem(props, new BoreLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION), stackTo16());
	public static final Item DAMAGING_LENS = make(LibItemNames.DAMAGING_LENS,
			props -> new LensItem(props, new DamagingLens(), LensItem.PROP_DAMAGE), stackTo16());
	public static final Item PHANTOM_LENS = make(LibItemNames.PHANTOM_LENS,
			props -> new LensItem(props, new PhantomLens(), LensItem.PROP_TOUCH), stackTo16());
	public static final Item MAGNETIZING_LENS = make(LibItemNames.MAGNETIZING_LENS,
			props -> new LensItem(props, new MagnetizingLens(), LensItem.PROP_ORIENTATION), stackTo16());
	public static final Item ENTROPIC_LENS = make(LibItemNames.ENTROPIC_LENS,
			props -> new LensItem(props, new EntropicLens(),
					LensItem.PROP_DAMAGE | LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION),
			stackTo16());
	public static final Item INFLUENCE_LENS = make(LibItemNames.INFLUENCE_LENS,
			props -> new LensItem(props, new InfluenceLens(), LensItem.PROP_NONE), stackTo16());
	public static final Item WEIGHT_LENS = make(LibItemNames.WEIGHT_LENS,
			props -> new LensItem(props, new WeightLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION), stackTo16());
	public static final Item PAINTSLINGER_LENS = make(LibItemNames.PAINTSLINGER_LENS,
			props -> new LensItem(props, new PaintslingerLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION), stackTo16());
	public static final Item KINDLE_LENS = make(LibItemNames.KINDLE_LENS,
			props -> new LensItem(props, new KindleLens(),
					LensItem.PROP_DAMAGE | LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION),
			stackTo16());
	public static final Item FORCE_LENS = make(LibItemNames.FORCE_LENS,
			props -> new LensItem(props, new ForceLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION), stackTo16());
	public static final Item FLASH_LENS = make(LibItemNames.FLASH_LENS,
			props -> new LensItem(props, new FlashLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION), stackTo16());
	public static final Item WARP_LENS = make(LibItemNames.WARP_LENS,
			props -> new LensItem(props, new WarpLens(), LensItem.PROP_NONE), stackTo16());
	public static final Item REDIRECTIVE_LENS = make(LibItemNames.REDIRECTIVE_LENS,
			props -> new LensItem(props, new RedirectiveLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION), stackTo16());
	public static final Item CELEBRATORY_LENS = make(LibItemNames.CELEBRATORY_LENS,
			props -> new LensItem(props, new CelebratoryLens(), LensItem.PROP_TOUCH), stackTo16());
	public static final Item FLARE_LENS = make(LibItemNames.FLARE_LENS,
			props -> new LensItem(props, new FlareLens(), LensItem.PROP_CONTROL), stackTo16());
	public static final Item MESSENGER_LENS = make(LibItemNames.MESSENGER_LENS,
			props -> new LensItem(props, new MessengerLens(), LensItem.PROP_POWER), stackTo16());
	public static final Item TRIPWIRE_LENS = make(LibItemNames.TRIPWIRE_LENS,
			props -> new LensItem(props, new TripwireLens(), LensItem.PROP_CONTROL), stackTo16());
	public static final Item STORM_LENS = make(LibItemNames.LENS_STORM,
			props -> new LensItem(props, new StormLens(), LensItem.PROP_NONE), stackTo16().rarity(Rarity.EPIC));

	// runes
	public static final Item RUNE_OF_WATER = make(LibItemNames.RUNE_OF_WATER, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_FIRE = make(LibItemNames.RUNE_OF_FIRE, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_EARTH = make(LibItemNames.RUNE_OF_EARTH, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_AIR = make(LibItemNames.RUNE_OF_AIR, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_SPRING = make(LibItemNames.RUNE_OF_SPRING, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_SUMMER = make(LibItemNames.RUNE_OF_SUMMER, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_AUTUMN = make(LibItemNames.RUNE_OF_AUTUMN, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_WINTER = make(LibItemNames.RUNE_OF_WINTER, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_MANA = make(LibItemNames.RUNE_OF_MANA, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_LUST = make(LibItemNames.RUNE_OF_LUST, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_GLUTTONY = make(LibItemNames.RUNE_OF_GLUTTONY, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_GREED = make(LibItemNames.RUNE_OF_GREED, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_SLOTH = make(LibItemNames.RUNE_OF_SLOTH, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_WRATH = make(LibItemNames.RUNE_OF_WRATH, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_ENVY = make(LibItemNames.RUNE_OF_ENVY, RuneItem::new, defaultBuilder());
	public static final Item RUNE_OF_PRIDE = make(LibItemNames.RUNE_OF_PRIDE, RuneItem::new, defaultBuilder());

	// seeds
	public static final Item PASTURE_SEEDS = make(LibItemNames.PASTURE_SEEDS,
			props -> new GrassSeedsItem(Blocks.GRASS_BLOCK, 0x006600, props), defaultBuilder());
	public static final Item BOREAL_SEEDS = make(LibItemNames.BOREAL_SEEDS,
			props -> new GrassSeedsItem(Blocks.PODZOL, 0x805E00, props), defaultBuilder());
	public static final Item INFESTATION_SPORES = make(LibItemNames.INFESTATION_SPORES,
			props -> new GrassSeedsItem(Blocks.MYCELIUM, 0x5E0054, props), defaultBuilder());
	public static final Item DRY_SEEDS = make(LibItemNames.DRY_PASTURE_SEEDS,
			props -> new GrassSeedsItem(BotaniaBlocks.DRY_GRASS_BLOCK, 0x66800D, props), defaultBuilder());
	public static final Item GOLDEN_SEEDS = make(LibItemNames.GOLDEN_PASTURE_SEEDS,
			props -> new GrassSeedsItem(BotaniaBlocks.GOLDEN_GRASS_BLOCK, 0xBFB300, props), defaultBuilder());
	public static final Item VIVID_SEEDS = make(LibItemNames.VIVID_PASTURE_SEEDS,
			props -> new GrassSeedsItem(BotaniaBlocks.VIVID_GRASS_BLOCK, 0x00801A, props), defaultBuilder());
	public static final Item SCORCHED_SEEDS = make(LibItemNames.SCORCHED_PASTURE_SEEDS,
			props -> new GrassSeedsItem(BotaniaBlocks.SCORCHED_GRASS_BLOCK, 0xBF0000, props), defaultBuilder());
	public static final Item INFUSED_SEEDS = make(LibItemNames.INFUSED_PASTURE_SEEDS,
			props -> new GrassSeedsItem(BotaniaBlocks.INFUSED_GRASS_BLOCK, 0x008C8C, props), defaultBuilder());
	public static final Item MUTATED_SEEDS = make(LibItemNames.MUTATED_PASTURE_SEEDS,
			props -> new GrassSeedsItem(BotaniaBlocks.MUTATED_GRASS_BLOCK, 0x661A66, props), defaultBuilder());

	// Rods
	public static final Item ROD_OF_THE_LANDS = make(LibItemNames.ROD_OF_THE_LANDS,
			LandsRodItem::new, unstackable());
	public static final Item ROD_OF_THE_HIGHLANDS = make(LibItemNames.ROD_OF_THE_HIGHLANDS,
			HighlandsRodItem::new, unstackable());
	public static final Item ROD_OF_THE_TERRA_FIRMA = make(LibItemNames.ROD_OF_THE_TERRA_FIRMA,
			TerraFirmaRodItem::new, unstackable()
					.rarity(Rarity.UNCOMMON));
	public static final Item ROD_OF_THE_DEPTHS = make(LibItemNames.ROD_OF_THE_DEPTHS,
			DepthsRodItem::new, unstackable());
	public static final Item ROD_OF_THE_SEAS = make(LibItemNames.ROD_OF_THE_SEAS,
			SeasRodItem::new, unstackable());
	public static final Item ROD_OF_THE_SKIES = make(LibItemNames.ROD_OF_THE_SKIES,
			SkiesRodItem::new, unstackable());
	public static final Item ROD_OF_THE_HELLS = make(LibItemNames.ROD_OF_THE_HELLS,
			HellsRodItem::new, unstackable());
	public static final Item ROD_OF_THE_PLENTIFUL_MANTLE = make(LibItemNames.ROD_OF_THE_PLENTIFUL_MANTLE,
			PlentifulMantleRodItem::new, unstackable());
	public static final Item ROD_OF_THE_MOLTEN_CORE = make(LibItemNames.ROD_OF_THE_MOLTEN_CORE,
			MoltenCoreRodItem::new, unstackable());
	public static final Item ROD_OF_THE_SHIFTING_CRUST = make(LibItemNames.ROD_OF_THE_SHIFTING_CRUST,
			ShiftingCrustRodItem::new, unstackable());
	public static final Item ROD_OF_THE_BIFROST = make(LibItemNames.ROD_OF_THE_BIFROST,
			BifrostRodItem::new, unstackable());
	public static final Item ROD_OF_THE_SHADED_MESA = make(LibItemNames.ROD_OF_THE_SHADED_MESA,
			ShadedMesaRodItem::new, unstackable());
	public static final Item ROD_OF_THE_UNSTABLE_RESERVOIR = make(LibItemNames.ROD_OF_THE_UNSTABLE_RESERVOIR,
			UnstableReservoirRodItem::new, unstackable()
					.rarity(Rarity.RARE));

	// Equipment
	public static final int MANASTEEL_DURABILITY_FACTOR = 18;
	public static final int MANA_PER_DAMAGE_METAL_ARMOR = 70;
	public static final int MANA_PER_DAMAGE_MANAWEAVE_ARMOR = 30;
	public static final int MANA_PER_DAMAGE_MANASTEEL_TOOL = 60;
	public static final int MANA_PER_DAMAGE_ELEMENTIUM_TOOL = 60;
	public static final int MANA_PER_DAMAGE_TERRASTEEL_TOOL = 100;
	public static final int MANA_PER_DAMAGE_UPGRADED_SWORD = 120;
	public static final int MANA_PER_DAMAGE_VITREOUS_TOOL = 160;
	public static final int MANA_PER_DAMAGE_SHEARS = 30;
	public static final int MANA_PER_DAMAGE_BOW = 40;
	public static final float ATTACK_SPEED_HOE = -1;
	public static final float ATTACK_DAMAGE_PICKAXE = 1;
	public static final float ATTACK_SPEED_PICKAXE = -2.8f;
	public static final float ATTACK_DAMAGE_SHOVEL = 1.5f;
	public static final float ATTACK_SPEED_SHOVEL = -3;
	public static final int ATTACK_DAMAGE_SWORD = 3;
	public static final float ATTACK_SPEED_SWORD = -2.4f;
	public static final Item MANASTEEL_HELMET = make(LibItemNames.MANASTEEL_HELMET,
			props -> new ManasteelHelmItem(ArmorType.HELMET, BotaniaArmorMaterials.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANASTEEL, ArmorType.HELMET));
	public static final Item MANASTEEL_CHESTPLATE = make(LibItemNames.MANASTEEL_CHESTPLATE,
			props -> new ManasteelArmorItem(ArmorType.CHESTPLATE, BotaniaArmorMaterials.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANASTEEL, ArmorType.CHESTPLATE));
	public static final Item MANASTEEL_LEGGINGS = make(LibItemNames.MANASTEEL_LEGGINGS,
			props -> new ManasteelArmorItem(ArmorType.LEGGINGS, BotaniaArmorMaterials.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANASTEEL, ArmorType.LEGGINGS));
	public static final Item MANASTEEL_BOOTS = make(LibItemNames.MANASTEEL_BOOTS,
			props -> new ManasteelArmorItem(ArmorType.BOOTS, BotaniaArmorMaterials.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANASTEEL, ArmorType.BOOTS));
	public static final Item MANASTEEL_PICKAXE = make(LibItemNames.MANASTEEL_PICKAXE,
			props -> new ManasteelPickaxeItem(BotaniaItemTiers.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANASTEEL_TOOL)
					.pickaxe(BotaniaItemTiers.MANASTEEL, ATTACK_DAMAGE_PICKAXE, ATTACK_SPEED_PICKAXE));
	public static final Item MANASTEEL_SHOVEL = make(LibItemNames.MANASTEEL_SHOVEL,
			props -> new ManasteelShovelItem(BotaniaItemTiers.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANASTEEL_TOOL)
					.shovel(BotaniaItemTiers.MANASTEEL, ATTACK_DAMAGE_SHOVEL, ATTACK_SPEED_SHOVEL));
	public static final Item MANASTEEL_AXE = make(LibItemNames.MANASTEEL_AXE,
			props -> new ManasteelAxeItem(BotaniaItemTiers.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANASTEEL_TOOL)
					.axe(BotaniaItemTiers.MANASTEEL, 6, -3.1f));
	public static final Item MANASTEEL_HOE = make(LibItemNames.MANASTEEL_HOE,
			props -> new ManasteelHoeItem(BotaniaItemTiers.MANASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANASTEEL_TOOL)
					.hoe(BotaniaItemTiers.MANASTEEL, -BotaniaItemTiers.MANASTEEL.attackDamageBonus(), ATTACK_SPEED_HOE));
	public static final Item MANASTEEL_SWORD = make(LibItemNames.MANASTEEL_SWORD,
			Item::new, manaRepairable(MANA_PER_DAMAGE_MANASTEEL_TOOL) // PORT-26.3: SwordItem is gone; a sword is an Item with sword properties
					.sword(BotaniaItemTiers.MANASTEEL, ATTACK_DAMAGE_SWORD, ATTACK_SPEED_SWORD));
	public static final int SHEARS_DURABILITY = 238;
	public static final Item MANASTEEL_SHEARS = make(LibItemNames.MANASTEEL_SHEARS,
			ManasteelShearsItem::new, manaRepairable(MANA_PER_DAMAGE_SHEARS)
					.durability(SHEARS_DURABILITY * BotaniaItemTiers.MANASTEEL.durability() / ToolMaterial.IRON.durability())
					.component(DataComponents.TOOL, ShearsItem.createToolProperties()));
	public static final int ELEMENTIUM_DURABILITY_FACTOR = 18;
	public static final Item ELEMENTIUM_HELMET = make(LibItemNames.ELEMENTIUM_HELMET,
			props -> new ElementiumHelmItem(ArmorType.HELMET, BotaniaArmorMaterials.ELEMENTIUM, 0.11,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.ELEMENTIUM, ArmorType.HELMET)
					.attributes(ElementiumArmorItem.createAttributes(ArmorType.HELMET, BotaniaArmorMaterials.ELEMENTIUM, 0.11)));
	public static final Item ELEMENTIUM_CHESTPLATE = make(LibItemNames.ELEMENTIUM_CHESTPLATE,
			props -> new ElementiumArmorItem(ArmorType.CHESTPLATE, BotaniaArmorMaterials.ELEMENTIUM, 0.17,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.ELEMENTIUM, ArmorType.CHESTPLATE)
					.attributes(ElementiumArmorItem.createAttributes(ArmorType.CHESTPLATE, BotaniaArmorMaterials.ELEMENTIUM, 0.17)));
	public static final Item ELEMENTIUM_LEGGINGS = make(LibItemNames.ELEMENTIUM_LEGGINGS,
			props -> new ElementiumArmorItem(ArmorType.LEGGINGS, BotaniaArmorMaterials.ELEMENTIUM, 0.15,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.ELEMENTIUM, ArmorType.LEGGINGS)
					.attributes(ElementiumArmorItem.createAttributes(ArmorType.LEGGINGS, BotaniaArmorMaterials.ELEMENTIUM, 0.15)));
	public static final Item ELEMENTIUM_BOOTS = make(LibItemNames.ELEMENTIUM_BOOTS,
			props -> new ElementiumArmorItem(ArmorType.BOOTS, BotaniaArmorMaterials.ELEMENTIUM, 0.09,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.ELEMENTIUM, ArmorType.BOOTS)
					.attributes(ElementiumArmorItem.createAttributes(ArmorType.BOOTS, BotaniaArmorMaterials.ELEMENTIUM, 0.09)));
	public static final Item ELEMENTIUM_PICKAXE = make(LibItemNames.ELEMENTIUM_PICKAXE,
			props -> new ElementiumPickaxeItem(BotaniaItemTiers.ELEMENTIUM,
					props),
			manaRepairable(MANA_PER_DAMAGE_ELEMENTIUM_TOOL)
					.pickaxe(BotaniaItemTiers.ELEMENTIUM, ATTACK_DAMAGE_PICKAXE, ATTACK_SPEED_PICKAXE));
	public static final Item ELEMENTIUM_SHOVEL = make(LibItemNames.ELEMENTIUM_SHOVEL,
			props -> new ElementiumShovelItem(BotaniaItemTiers.ELEMENTIUM,
					props),
			manaRepairable(MANA_PER_DAMAGE_ELEMENTIUM_TOOL)
					.shovel(BotaniaItemTiers.ELEMENTIUM, ATTACK_DAMAGE_SHOVEL, ATTACK_SPEED_SHOVEL));
	public static final Item ELEMENTIUM_AXE = make(LibItemNames.ELEMENTIUM_AXE,
			props -> new ElementiumAxeItem(BotaniaItemTiers.ELEMENTIUM,
					props),
			manaRepairable(MANA_PER_DAMAGE_ELEMENTIUM_TOOL)
					.axe(BotaniaItemTiers.ELEMENTIUM, 6, -3.1f));
	public static final Item ELEMENTIUM_HOE = make(LibItemNames.ELEMENTIUM_HOE,
			props -> new ElementiumHoeItem(BotaniaItemTiers.ELEMENTIUM,
					props),
			manaRepairable(MANA_PER_DAMAGE_ELEMENTIUM_TOOL)
					.hoe(BotaniaItemTiers.ELEMENTIUM, -BotaniaItemTiers.ELEMENTIUM.attackDamageBonus(), ATTACK_SPEED_HOE));
	public static final Item ELEMENTIUM_SWORD = make(LibItemNames.ELEMENTIUM_SWORD,
			ElementiumSwordItem::new, manaRepairable(MANA_PER_DAMAGE_ELEMENTIUM_TOOL)
					.sword(BotaniaItemTiers.ELEMENTIUM, ATTACK_DAMAGE_SWORD, ATTACK_SPEED_SWORD)
					.attributes(ElementiumSwordItem.createAttributes(
							BotaniaItemTiers.ELEMENTIUM,
							ATTACK_DAMAGE_SWORD, ATTACK_SPEED_SWORD)));
	public static final Item ELEMENTIUM_SHEARS = make(LibItemNames.ELEMENTIUM_SHEARS,
			ElementiumShearsItem::new, manaRepairable(MANA_PER_DAMAGE_SHEARS)
					.durability(SHEARS_DURABILITY * BotaniaItemTiers.ELEMENTIUM.durability() / ToolMaterial.IRON.durability())
					.component(DataComponents.TOOL, ShearsItem.createToolProperties()));
	public static final int TERRASTEEL_DURABILITY_FACTOR = 34;
	public static final Item TERRASTEEL_HELMET = make(LibItemNames.TERRASTEEL_HELMET,
			props -> new TerrasteelHelmItem(ArmorType.HELMET, BotaniaArmorMaterials.TERRASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.TERRASTEEL, ArmorType.HELMET)
					.attributes(TerrasteelArmorItem.createAttributes(ArmorType.HELMET, BotaniaArmorMaterials.TERRASTEEL))
					.fireResistant()
					.rarity(Rarity.UNCOMMON));
	public static final Item TERRASTEEL_CHESTPLATE = make(LibItemNames.TERRASTEEL_CHESTPLATE,
			props -> new TerrasteelArmorItem(ArmorType.CHESTPLATE, BotaniaArmorMaterials.TERRASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.TERRASTEEL, ArmorType.CHESTPLATE)
					.attributes(TerrasteelArmorItem.createAttributes(ArmorType.CHESTPLATE, BotaniaArmorMaterials.TERRASTEEL))
					.fireResistant()
					.rarity(Rarity.UNCOMMON));
	public static final Item TERRASTEEL_LEGGINGS = make(LibItemNames.TERRASTEEL_LEGGINGS,
			props -> new TerrasteelArmorItem(ArmorType.LEGGINGS, BotaniaArmorMaterials.TERRASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.TERRASTEEL, ArmorType.LEGGINGS)
					.attributes(TerrasteelArmorItem.createAttributes(ArmorType.LEGGINGS, BotaniaArmorMaterials.TERRASTEEL))
					.fireResistant()
					.rarity(Rarity.UNCOMMON));
	public static final Item TERRASTEEL_BOOTS = make(LibItemNames.TERRASTEEL_BOOTS,
			props -> new TerrasteelArmorItem(ArmorType.BOOTS, BotaniaArmorMaterials.TERRASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_METAL_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.TERRASTEEL, ArmorType.BOOTS)
					.attributes(TerrasteelArmorItem.createAttributes(ArmorType.BOOTS, BotaniaArmorMaterials.TERRASTEEL))
					.fireResistant()
					.rarity(Rarity.UNCOMMON));
	public static final Item TERRA_SHATTERER = make(LibItemNames.TERRA_SHATTERER,
			props -> new TerraShattererItem(BotaniaItemTiers.TERRASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_TERRASTEEL_TOOL)
					.fireResistant()
					.rarity(Rarity.UNCOMMON)
					.component(BotaniaDataComponents.MAX_MANA, TerraShattererItem.MAX_MANA)
					.component(BotaniaDataComponents.CAN_RECEIVE_MANA_FROM_POOL, Unit.INSTANCE)
					.pickaxe(BotaniaItemTiers.TERRASTEEL, ATTACK_DAMAGE_PICKAXE, ATTACK_SPEED_PICKAXE));
	public static final Item TERRA_TRUNCATOR = make(LibItemNames.TERRA_TRUNCATOR,
			props -> new TerraTruncatorItem(BotaniaItemTiers.TERRASTEEL,
					props),
			manaRepairable(MANA_PER_DAMAGE_TERRASTEEL_TOOL)
					.fireResistant()
					.rarity(Rarity.UNCOMMON)
					.axe(BotaniaItemTiers.TERRASTEEL, 5, -3));
	public static final Item TERRA_BLADE = make(LibItemNames.TERRA_BLADE,
			TerraBladeItem::new, manaRepairable(MANA_PER_DAMAGE_TERRASTEEL_TOOL)
					.fireResistant()
					.rarity(Rarity.UNCOMMON)
					.sword(BotaniaItemTiers.TERRASTEEL, ATTACK_DAMAGE_SWORD, ATTACK_SPEED_SWORD));
	public static final Item STARCALLER = make(LibItemNames.STARCALLER,
			StarcallerItem::new, manaRepairable(MANA_PER_DAMAGE_UPGRADED_SWORD)
					.rarity(Rarity.UNCOMMON)
					.sword(BotaniaItemTiers.TERRASTEEL, ATTACK_DAMAGE_SWORD, ATTACK_SPEED_SWORD));
	public static final Item THUNDERCALLER = make(LibItemNames.THUNDERCALLER,
			ThundercallerItem::new, manaRepairable(MANA_PER_DAMAGE_UPGRADED_SWORD)
					.rarity(Rarity.UNCOMMON)
					.sword(BotaniaItemTiers.TERRASTEEL, ATTACK_DAMAGE_SWORD, 1.5f));
	public static final int MANAWEAVE_DURABILITY_FACTOR = 5;
	public static final Item MANAWEAVE_HELMET = make(LibItemNames.MANAWEAVE_HELMET,
			props -> new ManaweaveHelmItem(ArmorType.HELMET, BotaniaArmorMaterials.MANAWEAVE,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANAWEAVE_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANAWEAVE, ArmorType.HELMET));
	public static final Item MANAWEAVE_CHESTPLATE = make(LibItemNames.MANAWEAVE_CHESTPLATE,
			props -> new ManaweaveArmorItem(ArmorType.CHESTPLATE, BotaniaArmorMaterials.MANAWEAVE,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANAWEAVE_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANAWEAVE, ArmorType.CHESTPLATE));
	public static final Item MANAWEAVE_LEGGINGS = make(LibItemNames.MANAWEAVE_LEGGINGS,
			props -> new ManaweaveArmorItem(ArmorType.LEGGINGS, BotaniaArmorMaterials.MANAWEAVE,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANAWEAVE_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANAWEAVE, ArmorType.LEGGINGS));
	public static final Item MANAWEAVE_BOOTS = make(LibItemNames.MANAWEAVE_BOOTS,
			props -> new ManaweaveArmorItem(ArmorType.BOOTS, BotaniaArmorMaterials.MANAWEAVE,
					props),
			manaRepairable(MANA_PER_DAMAGE_MANAWEAVE_ARMOR)
					.humanoidArmor(BotaniaArmorMaterials.MANAWEAVE, ArmorType.BOOTS));
	public static final Item SOULSCRIBE = make(LibItemNames.SOULSCRIBE,
			SoulscribeItem::new, defaultBuilder() // no mana repair
					.durability(69) // What you looking at?
					// PORT-26.3: as before, the material's durability (300) replaces the 69: SwordItem used to do the same
					.sword(BotaniaItemTiers.MANASTEEL, ATTACK_DAMAGE_SWORD, -1.25f));
	public static final Item VITREOUS_PICKAXE = make(LibItemNames.VITREOUS_PICKAXE,
			props -> new VitreousPickaxeItem(BotaniaItemTiers.VITREOUS,
					props),
			manaRepairable(MANA_PER_DAMAGE_VITREOUS_TOOL)
					.pickaxe(BotaniaItemTiers.VITREOUS, ATTACK_DAMAGE_PICKAXE, ATTACK_SPEED_PICKAXE)
					.component(DataComponents.TOOL, VitreousPickaxeItem.createTool()));
	public static final int BOW_DURABILITY = 384; // like vanilla
	public static final Item LIVINGWOOD_BOW = make(LibItemNames.LIVINGWOOD_BOW,
			BowItem::new, manaRepairable(MANA_PER_DAMAGE_BOW)
					.durability(BOW_DURABILITY));
	public static final Item CRYSTAL_BOW = make(LibItemNames.CRYSTAL_BOW,
			CrystalBowItem::new, manaRepairable(MANA_PER_DAMAGE_BOW)
					.durability(BOW_DURABILITY));
	public static final int CHAKRAM_STACK_SIZE = 6;
	public static final Item THORN_CHAKRAM = make(LibItemNames.THORN_CHAKRAM,
			ThornChakramItem::new, defaultBuilder()
					.stacksTo(CHAKRAM_STACK_SIZE)
					.rarity(Rarity.UNCOMMON));
	public static final Item FLARE_CHAKRAM = make(LibItemNames.FLARE_CHAKRAM,
			ThornChakramItem::new, defaultBuilder()
					.stacksTo(CHAKRAM_STACK_SIZE)
					.rarity(Rarity.UNCOMMON));

	// Misc tools
	public static final Item MANA_TABLET = make(LibItemNames.MANA_TABLET,
			ManaTabletItem::new, unstackable()
					.component(BotaniaDataComponents.MAX_MANA, ManaTabletItem.DEFAULT_MAX_MANA)
					.component(BotaniaDataComponents.CAN_PROVIDE_MANA_TO_ITEMS, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_DRAIN_MANA_TO_POOL, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_ACCEPT_MANA_FROM_ITEMS, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_RECEIVE_MANA_FROM_POOL, Unit.INSTANCE));
	public static final Item MANA_MIRROR = make(LibItemNames.MANA_MIRROR,
			ManaMirrorItem::new, unstackable()
					.rarity(Rarity.UNCOMMON)
					.component(BotaniaDataComponents.MAX_MANA, 0)
					.component(BotaniaDataComponents.MANA_BACKLOG, 0)
					.component(BotaniaDataComponents.CAN_PROVIDE_MANA_TO_ITEMS, Unit.INSTANCE));
	public static final Item MANA_BLASTER = make(LibItemNames.MANA_BLASTER,
			ManaBlasterItem::new, unstackable());
	public static final Item LENS_CLIP = make(LibItemNames.LENS_CLIP,
			Item::new, unstackable());
	public static final HornItem HORN_OF_THE_WILD = make(LibItemNames.HORN_OF_THE_WILD,
			HornOfTheWildItem::new, unstackable());
	public static final HornItem HORN_OF_THE_CANOPY = make(LibItemNames.HORN_OF_THE_CANOPY,
			HornOfTheCanopyItem::new, unstackable());
	public static final HornItem HORN_OF_THE_COVERING = make(LibItemNames.HORN_OF_THE_COVERING,
			HornOfTheCoveringItem::new, unstackable());
	public static final Item VINE_BALL = make(LibItemNames.VINE_BALL,
			VineBallItem::new, defaultBuilder());
	public static final Item LIVINGWOOD_SLINGSHOT = make(LibItemNames.LIVINGWOOD_SLINGSHOT,
			LivingwoodSlingshotItem::new, unstackable());
	public static final Item EXTRAPOLATED_BUCKET = make(LibItemNames.EXTRAPOLATED_BUCKET,
			ExtrapolatedBucketItem::new, unstackable());
	public static final Item LIFE_AGGREGATOR = make(LibItemNames.LIFE_AGGREGATOR,
			LifeAggregatorItem::new, unstackable().rarity(Rarity.RARE));
	public static final Item HAND_OF_ENDER = make(LibItemNames.HAND_OF_ENDER,
			EnderHandItem::new, unstackable());
	public static final Item ASSEMBLY_HALO = make(LibItemNames.ASSEMBLY_HALO,
			AssemblyHaloItem::new, unstackable());
	public static final Item MANUFACTORY_HALO = make(LibItemNames.MANUFACTORY_HALO,
			ManufactoryHaloItem::new, unstackable()
					.component(BotaniaDataComponents.ACTIVE, Unit.INSTANCE));
	public static final Item SPELLBINDING_CLOTH = make(LibItemNames.SPELLBINDING_CLOTH,
			SpellbindingClothItem::new, XplatAbstractions.INSTANCE.noRepairOnNeoForge(unstackable()
					.durability(35)));
	public static final FlowerPouchItem FLOWER_POUCH = make(LibItemNames.FLOWER_POUCH,
			FlowerPouchItem::new, unstackable()
					.component(BotaniaDataComponents.ITEM_TAGS,
							List.of(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS, BotaniaTags.Items.TALL_MYSTICAL_FLOWERS)));
	public static final PetalPouchItem PETAL_POUCH = make(LibItemNames.PETAL_POUCH,
			PetalPouchItem::new, unstackable()
					.component(BotaniaDataComponents.ACTIVE, Unit.INSTANCE)
					.component(BotaniaDataComponents.ITEM_TAGS,
							List.of(BotaniaTags.Items.PETALS, BotaniaTags.Items.SHIMMERING_MUSHROOMS))
					.component(BotaniaDataComponents.CRAFTABLE_ITEM_TAGS,
							List.of(BotaniaTags.Items.SMALL_MYSTICAL_FLOWERS, BotaniaTags.Items.TALL_MYSTICAL_FLOWERS)));
	public static final Item BLACK_HOLE_TALISMAN = make(LibItemNames.BLACK_HOLE_TALISMAN,
			BlackHoleTalismanItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item STONE_OF_TEMPERANCE = make(LibItemNames.STONE_OF_TEMPERANCE,
			StoneOfTemperanceItem::new, unstackable());
	public static final Item WATER_BOWL = make(LibItemNames.WATER_BOWL,
			WaterBowlItem::new, unstackable().craftRemainder(Items.BOWL));
	public static final Item CACOPHONIUM = make(LibItemNames.CACOPHONIUM,
			CacophoniumItem::new, unstackable());
	public static final Item SLIME_IN_A_BOTTLE = make(LibItemNames.SLIME_IN_A_BOTTLE,
			SlimeInABottleItem::new, unstackable());
	public static final Item WORLDSHAPERS_SEXTANT = make(LibItemNames.WORLDSHAPERS_SEXTANT,
			SextantItem::new, unstackable());
	public static final Item WORLDSHAPERS_ASTROLABE = make(LibItemNames.WORLDSHAPERS_ASTROLABE,
			AstrolabeItem::new, unstackable().rarity(Rarity.RARE));
	public static final Item TRINKET_CASE = make(LibItemNames.TRINKET_CASE,
			BaubleBoxItem::new, unstackable());

	// Baubles / trinkets / curios / etc.
	public static final Item BAND_OF_MANA = make(LibItemNames.BAND_OF_MANA,
			BandOfManaItem::new, unstackable()
					.component(BotaniaDataComponents.MAX_MANA, BandOfManaItem.DEFAULT_MAX_MANA)
					.component(BotaniaDataComponents.CAN_PROVIDE_MANA_TO_ITEMS, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_DRAIN_MANA_TO_POOL, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_ACCEPT_MANA_FROM_ITEMS, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_RECEIVE_MANA_FROM_POOL, Unit.INSTANCE));
	public static final Item GREATER_BAND_OF_MANA = make(LibItemNames.GREATER_BAND_OF_MANA,
			BandOfManaItem::new, unstackable()
					.rarity(Rarity.UNCOMMON)
					.fireResistant()
					.component(BotaniaDataComponents.MAX_MANA, BandOfManaItem.DEFAULT_GREATER_MAX_MANA)
					.component(BotaniaDataComponents.CAN_PROVIDE_MANA_TO_ITEMS, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_DRAIN_MANA_TO_POOL, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_ACCEPT_MANA_FROM_ITEMS, Unit.INSTANCE)
					.component(BotaniaDataComponents.CAN_RECEIVE_MANA_FROM_POOL, Unit.INSTANCE));
	public static final Item BAND_OF_AURA = make(LibItemNames.BAND_OF_AURA,
			props -> new BandOfAuraItem(10, props), unstackable());
	public static final Item GREATER_BAND_OF_AURA = make(LibItemNames.GREATER_BAND_OF_AURA,
			props -> new BandOfAuraItem(2, props), unstackable()
					.rarity(Rarity.UNCOMMON)
					.fireResistant());
	public static final Item RING_OF_MAGNETIZATION = make(LibItemNames.RING_OF_MAGNETIZATION,
			RingOfMagnetizationItem::new, unstackable()
					.component(BotaniaDataComponents.RANGE, RingOfMagnetizationItem.DEFAULT_RANGE));
	public static final Item GREATER_RING_OF_MAGNETIZATION = make(LibItemNames.GREATER_RING_OF_MAGNETIZATION,
			RingOfMagnetizationItem::new, unstackable()
					.rarity(Rarity.UNCOMMON)
					.fireResistant()
					.component(BotaniaDataComponents.RANGE, RingOfMagnetizationItem.DEFAULT_GREATER_RANGE));
	public static final Item RING_OF_CHORDATA = make(LibItemNames.RING_OF_CHORDATA,
			RingOfChordataItem::new, unstackable()
					.rarity(Rarity.UNCOMMON));
	public static final Item RING_OF_CORRECTION = make(LibItemNames.RING_OF_CORRECTION,
			RingOfCorrectionItem::new, unstackable());
	public static final Item RING_OF_DEXTEROUS_MOTION = make(LibItemNames.RING_OF_DEXTEROUS_MOTION,
			RingOfDexterousMotionItem::new, unstackable());
	public static final Item RING_OF_THE_MANTLE = make(LibItemNames.RING_OF_THE_MANTLE,
			RingOfTheMantleItem::new, unstackable());
	public static final Item GREAT_FAIRY_RING = make(LibItemNames.GREAT_FAIRY_RING,
			GreatFairyRingItem::new, unstackable());
	public static final Item RING_OF_FAR_REACH = make(LibItemNames.RING_OF_FAR_REACH,
			RingOfFarReachItem::new, unstackable());
	public static final Item SOJOURNERS_SASH = make(LibItemNames.SOJOURNORS_SASH,
			SojournersSashItem::new, unstackable());
	public static final Item GLOBETROTTERS_SASH = make(LibItemNames.GLOBETROTTERS_SASH,
			GlobetrottersSashItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item PLANESTRIDERS_SASH = make(LibItemNames.PLANESTRIDERS_SASH,
			PlanestridersSashItem::new, unstackable());
	public static final Item TECTONIC_GIRDLE = make(LibItemNames.TECTONIC_GIRDLE,
			TectonicGirdleItem::new, unstackable());
	public static final Item SNOWFLAKE_PENDANT = make(LibItemNames.SNOWFLAKE_PENDANT,
			SnowflakePendantItem::new, unstackable());
	public static final Item PYROCLAST_PENDANT = make(LibItemNames.PYROCLAST_PENDANT,
			PyroclastPendantItem::new, unstackable());
	public static final Item CRIMSON_PENDANT = make(LibItemNames.CRIMSON_PENDANT,
			CrimsonPendantItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item CIRRUS_AMULET = make(LibItemNames.CIRRUS_AMULET,
			CirrusAmuletItem::new, unstackable());
	public static final Item NIMBUS_AMULET = make(LibItemNames.NIMBUS_AMULET,
			NimbusAmuletItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item CLOAK_OF_VIRTUE = make(LibItemNames.CLOAK_OF_VIRTUE,
			CloakOfVirtueItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item CLOAK_OF_SIN = make(LibItemNames.CLOAK_OF_SIN,
			CloakOfSinItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item CLOAK_OF_BALANCE = make(LibItemNames.CLOAK_OF_BALANCE,
			CloakOfBalanceItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item INVISIBILITY_CLOAK = make(LibItemNames.INVISIBILITY_CLOAK,
			InvisibilityCloakItem::new, unstackable());
	public static final Item THIRD_EYE = make(LibItemNames.THIRD_EYE,
			ThirdEyeItem::new, unstackable());
	public static final Item MANASEER_MONOCLE = make(LibItemNames.MANASEER_MONOCLE,
			ManaseerMonocleItem::new, unstackable());
	public static final Item TINY_PLANET = make(LibItemNames.TINY_PLANET,
			TinyPlanetItem::new, unstackable());
	public static final Item BENEVOLENT_GODDESS_CHARM = make(LibItemNames.BENEVOLENT_GODDESS_CHARM,
			BenevolentGoddessCharmItem::new, unstackable());
	public static final Item CHARM_OF_THE_DIVA = make(LibItemNames.CHARM_OF_THE_DIVA,
			CharmOfTheDivaItem::new, unstackable()
					.rarity(Rarity.RARE));
	public static final Item THE_SPECTATOR = make(LibItemNames.THE_SPECTATOR,
			SpectatorItem::new, unstackable());
	public static final Item FLUGEL_TIARA = make(LibItemNames.FLUGEL_TIARA,
			FlugelTiaraItem::new, unstackable()
					.rarity(Rarity.RARE));

	// Misc
	public static final Item BISCUIT_OF_TOTALITY = make(LibItemNames.BISCUIT_OF_TOTALITY,
			Item::new, defaultBuilder()
					// PORT-26.3: food effects are consume effects now
					.food(new FoodProperties.Builder()
							.nutrition(0)
							.saturationModifier(0.1F)
							.build(),
							Consumables.defaultFood()
									.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0), 1))
									.build()));
	public static final Item MANA_IN_A_BOTTLE = make(LibItemNames.MANA_IN_A_BOTTLE,
			BottledManaItem::new, unstackable()
					// Mark as food just to fool foxes into using it
					.food(new FoodProperties.Builder().alwaysEdible().build())
					.component(BotaniaDataComponents.REMAINING_USES, BottledManaItem.SWIGS));
	public static final Item SHARD_OF_LAPUTA = make(LibItemNames.SHARD_OF_LAPUTA,
			LaputaShardItem::new, unstackable()
					.component(BotaniaDataComponents.SHARD_LEVEL, 1)
					.rarity(Rarity.RARE));
	public static final Item NECRODERMAL_VIRUS = make(LibItemNames.NECRODERMAL_VIRUS,
			EquestrianVirusItem::new, defaultBuilder()
					.rarity(Rarity.UNCOMMON));
	public static final Item NULLODERMAL_VIRUS = make(LibItemNames.NULLODERMAL_VIRUS,
			EquestrianVirusItem::new, defaultBuilder()
					.rarity(Rarity.UNCOMMON));
	public static final Item MANA_SPARK = make(LibItemNames.MANA_SPARK, ManaSparkItem::new, defaultBuilder());
	public static final Item SPARK_AUGMENT_DISPERSIVE = make(LibItemNames.SPARK_AUGMENT + "_dispersive",
			Item::new, defaultBuilder()
					.component(BotaniaDataComponents.AUGMENT_ICON, botaniaRL("item/spark_augment_rune_dispersive"))
					.component(BotaniaDataComponents.AUGMENT_ID, (byte) 1));
	public static final Item SPARK_AUGMENT_DOMINANT = make(LibItemNames.SPARK_AUGMENT + "_dominant",
			Item::new, defaultBuilder()
					.component(BotaniaDataComponents.AUGMENT_ICON, botaniaRL("item/spark_augment_rune_dominant"))
					.component(BotaniaDataComponents.AUGMENT_ID, (byte) 2));
	public static final Item SPARK_AUGMENT_RECESSIVE = make(LibItemNames.SPARK_AUGMENT + "_recessive",
			Item::new, defaultBuilder()
					.component(BotaniaDataComponents.AUGMENT_ICON, botaniaRL("item/spark_augment_rune_recessive"))
					.component(BotaniaDataComponents.AUGMENT_ID, (byte) 3));
	public static final Item SPARK_AUGMENT_ISOLATED = make(LibItemNames.SPARK_AUGMENT + "_isolated",
			Item::new, defaultBuilder()
					.component(BotaniaDataComponents.AUGMENT_ICON, botaniaRL("item/spark_augment_rune_isolated"))
					.component(BotaniaDataComponents.AUGMENT_ID, (byte) 4));
	public static final Item CORPOREA_SPARK = make(LibItemNames.CORPOREA_SPARK,
			CorporeaSparkItem::new, defaultBuilder());
	public static final Item MASTER_CORPOREA_SPARK = make(LibItemNames.MASTER_CORPOREA_SPARK,
			CorporeaSparkItem::new, defaultBuilder());
	public static final Item CREATIVE_CORPOREA_SPARK = make(LibItemNames.CREATIVE_CORPOREA_SPARK,
			CorporeaSparkItem::new, defaultBuilder()
					.rarity(Rarity.EPIC));
	public static final Item BLACK_LOTUS = make(LibItemNames.BLACK_LOTUS,
			BlackLotusItem::new, defaultBuilder()
					.rarity(Rarity.UNCOMMON));
	public static final Item BLACKER_LOTUS = make(LibItemNames.BLACKER_LOTUS,
			BlackLotusItem::new, defaultBuilder()
					.rarity(Rarity.RARE));
	public static final Item WORLD_SEED = make(LibItemNames.WORLD_SEED,
			WorldSeedItem::new, defaultBuilder());
	public static final Item PHANTOM_INK = make(LibItemNames.PHANTOM_INK,
			PhantomInkItem::new, defaultBuilder());
	public static final Item MANA_POOL_MINECART = make(LibItemNames.MANA_POOL_MINECART,
			ManaPoolMinecartItem::new, unstackable());
	public static final Item RESOLUTE_IVY = make(LibItemNames.RESOLUTE_IVY,
			ResoluteIvyItem::new, defaultBuilder());
	public static final Item CRAFTING_PLACEHOLDER = make(LibItemNames.CRAFTING_PLACEHOLDER,
			SelfReturningItem::new, defaultBuilder());
	public static final Item CRAFTING_PATTERN_1_1 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "1_1",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_1_1, props), unstackable());
	public static final Item CRAFTING_PATTERN_2_2 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "2_2",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_2_2, props), unstackable());
	public static final Item CRAFTING_PATTERN_1_2 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "1_2",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_1_2, props), unstackable());
	public static final Item CRAFTING_PATTERN_2_1 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "2_1",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_2_1, props), unstackable());
	public static final Item CRAFTING_PATTERN_1_3 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "1_3",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_1_3, props), unstackable());
	public static final Item CRAFTING_PATTERN_3_1 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "3_1",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_3_1, props), unstackable());
	public static final Item CRAFTING_PATTERN_2_3 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "2_3",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_2_3, props), unstackable());
	public static final Item CRAFTING_PATTERN_3_2 = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "3_2",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_3_2, props), unstackable());
	public static final Item CRAFTING_PATTERN_DONUT = make(LibItemNames.CRAFTING_PATTERN_PREFIX + "donut",
			props -> new CraftingPatternItem(CraftyCratePattern.CRAFTY_DONUT, props), unstackable());

	// Guardian of Gaia drops
	public static final Item DICE_OF_FATE = make(LibItemNames.DICE_OF_FATE,
			DiceOfFateItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item FRUIT_OF_GRISAIA = make(LibItemNames.THE_FRUIT_OF_GRISAIA,
			FruitOfGrisaiaItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item KEY_OF_THE_KINGS_LAW = make(LibItemNames.KEY_OF_THE_KINGS_LAW,
			KeyOfTheKingsLawItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item EYE_OF_THE_FLUGEL = make(LibItemNames.EYE_OF_THE_FLUGEL,
			EyeOfTheFlugelItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item RING_OF_THOR = make(LibItemNames.RING_OF_THOR,
			RingOfThorItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item RING_OF_ODIN = make(LibItemNames.RING_OF_ODIN,
			RingOfOdinItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item RING_OF_LOKI = make(LibItemNames.RING_OF_LOKI,
			RingOfLokiItem::new, unstackable()
					.fireResistant()
					.rarity(Rarity.EPIC));
	public static final Item SCATHED_MUSIC_DISC_1 = make(LibItemNames.SCATHED_MUSIC_DISC_1,
			Item::new, unstackable()
					.rarity(Rarity.RARE)
					.jukeboxPlayable(BotaniaJukeboxSongs.GAIA_MUSIC_1));
	public static final Item SCATHED_MUSIC_DISC_2 = make(LibItemNames.SCATHED_MUSIC_DISC_2,
			Item::new, unstackable()
					.rarity(Rarity.RARE)
					.jukeboxPlayable(BotaniaJukeboxSongs.GAIA_MUSIC_2));
	public static final Item WILL_OF_AHRIM = make(LibItemNames.ANCIENT_WILL_PREFIX + "ahrim",
			props -> new AncientWillItem(AncientWillContainer.AncientWillType.AHRIM, props), unstackable()
					.rarity(Rarity.RARE));
	public static final Item WILL_OF_DHAROK = make(LibItemNames.ANCIENT_WILL_PREFIX + "dharok",
			props -> new AncientWillItem(AncientWillContainer.AncientWillType.DHAROK, props), unstackable()
					.rarity(Rarity.RARE));
	public static final Item WILL_OF_GUTHAN = make(LibItemNames.ANCIENT_WILL_PREFIX + "guthan",
			props -> new AncientWillItem(AncientWillContainer.AncientWillType.GUTHAN, props), unstackable()
					.rarity(Rarity.RARE));
	public static final Item WILL_OF_TORAG = make(LibItemNames.ANCIENT_WILL_PREFIX + "torag",
			props -> new AncientWillItem(AncientWillContainer.AncientWillType.TORAG, props), unstackable()
					.rarity(Rarity.RARE));
	public static final Item WILL_OF_VERAC = make(LibItemNames.ANCIENT_WILL_PREFIX + "verac",
			props -> new AncientWillItem(AncientWillContainer.AncientWillType.VERAC, props), unstackable()
					.rarity(Rarity.RARE));
	public static final Item WILL_OF_KARIL = make(LibItemNames.ANCIENT_WILL_PREFIX + "karil",
			props -> new AncientWillItem(AncientWillContainer.AncientWillType.KARIL, props), unstackable()
					.rarity(Rarity.RARE));
	public static final Item THE_PINKINATOR = make(LibItemNames.THE_PINKINATOR,
			PinkinatorItem::new, unstackable()
					.rarity(Rarity.RARE));

	// Brewing
	public static final Item MANAGLASS_VIAL = make(LibItemNames.MANAGLASS_VIAL,
			VialItem::new, defaultBuilder());
	public static final Item ALFGLASS_FLASK = make(LibItemNames.ALFGLASS_FLASK,
			FlaskItem::new, defaultBuilder());
	public static final BaseBrewItem BREW_VIAL = make(LibItemNames.BREW_VIAL,
			props -> new BaseBrewItem(32, () -> MANAGLASS_VIAL, props), unstackable()
					.component(BotaniaDataComponents.MAX_USES, BaseBrewItem.DEFAULT_USES_VIAL));
	public static final BaseBrewItem BREW_FLASK = make(LibItemNames.BREW_FLASK,
			props -> new BaseBrewItem(24, () -> ALFGLASS_FLASK, props), unstackable()
					.component(BotaniaDataComponents.MAX_USES, BaseBrewItem.DEFAULT_USES_FLASK));
	public static final Item TAINTED_BLOOD_PENDANT = make(LibItemNames.TAINTED_BLOOD_PENDANT,
			TaintedBloodPendantItem::new, unstackable());
	public static final Item INCENSE_STICK = make(LibItemNames.INCENSE_STICK,
			IncenseStickItem::new, unstackable());

	// Cosmetics
	public static final Item COSMETIC_BLACK_BOWTIE = make(LibItemNames.COSMETIC_PREFIX + "black_bowtie",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.BLACK_BOWTIE, props), unstackable());
	public static final Item COSMETIC_BLACK_TIE = make(LibItemNames.COSMETIC_PREFIX + "black_tie",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.BLACK_TIE, props), unstackable());
	public static final Item COSMETIC_RED_GLASSES = make(LibItemNames.COSMETIC_PREFIX + "red_glasses",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.RED_GLASSES, props), unstackable());
	public static final Item COSMETIC_PUFFY_SCARF = make(LibItemNames.COSMETIC_PREFIX + "puffy_scarf",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.PUFFY_SCARF, props), unstackable());
	public static final Item COSMETIC_ENGINEER_GOGGLES = make(LibItemNames.COSMETIC_PREFIX + "engineer_goggles",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.ENGINEER_GOGGLES, props), unstackable());
	public static final Item COSMETIC_EYEPATCH = make(LibItemNames.COSMETIC_PREFIX + "eyepatch",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.EYEPATCH, props), unstackable());
	public static final Item COSMETIC_WICKED_EYEPATCH = make(LibItemNames.COSMETIC_PREFIX + "wicked_eyepatch",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.WICKED_EYEPATCH, props), unstackable());
	public static final Item COSMETIC_RED_RIBBONS = make(LibItemNames.COSMETIC_PREFIX + "red_ribbons",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.RED_RIBBONS, props), unstackable());
	public static final Item COSMETIC_PINK_FLOWER_BUD = make(LibItemNames.COSMETIC_PREFIX + "pink_flower_bud",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.PINK_FLOWER_BUD, props), unstackable());
	public static final Item COSMETIC_POLKA_DOTTED_BOWS = make(LibItemNames.COSMETIC_PREFIX + "polka_dotted_bows",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.POLKA_DOTTED_BOWS, props), unstackable());
	public static final Item COSMETIC_BLUE_BUTTERFLY = make(LibItemNames.COSMETIC_PREFIX + "blue_butterfly",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.BLUE_BUTTERFLY, props), unstackable());
	public static final Item COSMETIC_CAT_EARS = make(LibItemNames.COSMETIC_PREFIX + "cat_ears",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.CAT_EARS, props), unstackable());
	public static final Item COSMETIC_WITCH_PIN = make(LibItemNames.COSMETIC_PREFIX + "witch_pin",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.WITCH_PIN, props), unstackable());
	public static final Item COSMETIC_DEVIL_TAIL = make(LibItemNames.COSMETIC_PREFIX + "devil_tail",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.DEVIL_TAIL, props), unstackable());
	public static final Item COSMETIC_KAMUI_EYE = make(LibItemNames.COSMETIC_PREFIX + "kamui_eye",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.KAMUI_EYE, props), unstackable());
	public static final Item COSMETIC_GOOGLY_EYES = make(LibItemNames.COSMETIC_PREFIX + "googly_eyes",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOOGLY_EYES, props), unstackable());
	public static final Item COSMETIC_FOUR_LEAFED_CLOVER = make(LibItemNames.COSMETIC_PREFIX + "four_leaf_clover",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.FOUR_LEAF_CLOVER, props), unstackable());
	public static final Item COSMETIC_CLOCK_EYE = make(LibItemNames.COSMETIC_PREFIX + "clock_eye",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.CLOCK_EYE, props), unstackable());
	public static final Item COSMETIC_UNICORN_HORN = make(LibItemNames.COSMETIC_PREFIX + "unicorn_horn",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.UNICORN_HORN, props), unstackable());
	public static final Item COSMETIC_DEVIL_HORNS = make(LibItemNames.COSMETIC_PREFIX + "devil_horns",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.DEVIL_HORNS, props), unstackable());
	public static final Item COSMETIC_HYPER_PLUS = make(LibItemNames.COSMETIC_PREFIX + "hyper_plus",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.HYPER_PLUS, props), unstackable());
	public static final Item COSMETIC_BOTANIST_EMBLEM = make(LibItemNames.COSMETIC_PREFIX + "botanist_emblem",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.BOTANIST_EMBLEM, props), unstackable());
	public static final Item COSMETIC_ANCIENT_MASK = make(LibItemNames.COSMETIC_PREFIX + "ancient_mask",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.ANCIENT_MASK, props), unstackable());
	public static final Item COSMETIC_EERIE_MASK = make(LibItemNames.COSMETIC_PREFIX + "eerie_mask",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.EERIE_MASK, props), unstackable());
	public static final Item COSMETIC_ALIEN_ANTENNA = make(LibItemNames.COSMETIC_PREFIX + "alien_antenna",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.ALIEN_ANTENNA, props), unstackable());
	public static final Item COSMETIC_ANAGLYPH_GLASSES = make(LibItemNames.COSMETIC_PREFIX + "anaglyph_glasses",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.ANAGLYPH_GLASSES, props), unstackable());
	public static final Item COSMETIC_ORANGE_SHADES = make(LibItemNames.COSMETIC_PREFIX + "orange_shades",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.ORANGE_SHADES, props), unstackable());
	public static final Item COSMETIC_GROUCHO_GLASSES = make(LibItemNames.COSMETIC_PREFIX + "groucho_glasses",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GROUCHO_GLASSES, props), unstackable());
	public static final Item COSMETIC_THICK_EYEBROWS = make(LibItemNames.COSMETIC_PREFIX + "thick_eyebrows",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.THICK_EYEBROWS, props), unstackable());
	public static final Item COSMETIC_LUSITANIC_SHIELD = make(LibItemNames.COSMETIC_PREFIX + "lusitanic_shield",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.LUSITANIC_SHIELD, props), unstackable());
	public static final Item COSMETIC_TINY_POTATO_MASK = make(LibItemNames.COSMETIC_PREFIX + "tiny_potato_mask",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.TINY_POTATO_MASK, props), unstackable());
	public static final Item COSMETIC_QUESTGIVER_MARK = make(LibItemNames.COSMETIC_PREFIX + "questgiver_mark",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.QUESTGIVER_MARK, props), unstackable());
	public static final Item COSMETIC_THINKING_HAND = make(LibItemNames.COSMETIC_PREFIX + "thinking_hand",
			props -> new CosmeticBaubleItem(CosmeticBaubleItem.Variant.THINKING_HAND, props), unstackable());

	// Banner patterns
	public static final Item BANNER_PATTERN_BOTANIA = make("botania_banner_pattern",
			Item::new, unstackable()
					.rarity(Rarity.UNCOMMON).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(BotaniaTags.BannerPatterns.PATTERN_ITEM_BOTANIA)));
	public static final Item BANNER_PATTERN_MATERIALS = make("materials_banner_pattern",
			Item::new, unstackable().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(BotaniaTags.BannerPatterns.PATTERN_ITEM_MATERIALS)));
	public static final Item BANNER_PATTERN_SPARK_AUGMENTS = make("spark_augments_banner_pattern",
			Item::new, unstackable().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(BotaniaTags.BannerPatterns.PATTERN_ITEM_SPARK_AUGMENTS)));
	public static final Item BANNER_PATTERN_TOOLS = make("tools_banner_pattern",
			Item::new, unstackable().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(BotaniaTags.BannerPatterns.PATTERN_ITEM_TOOLS)));

	public static final MenuType<TrinketCaseMenu> TRINKET_CASE_MENU_TYPE =
			XplatAbstractions.INSTANCE.createMenuType(TrinketCaseMenu::new, ByteBufCodecs.BOOL);
	public static final MenuType<ColoredContentsPouchMenu> COLORED_CONTENTS_POUCH_MENU_TYPE =
			XplatAbstractions.INSTANCE.createMenuType(ColoredContentsPouchMenu::new, ByteBufCodecs.BOOL);
	public static final MenuType<HandOfEnderMenu> HAND_OF_ENDER_MENU_TYPE =
			XplatAbstractions.INSTANCE.createMenuType(HandOfEnderMenu::new, ByteBufCodecs.BOOL);

	public static final CauldronInteraction MANA_POOL_INTERACTION = BotaniaItems::cauldronUndyeManaPool;
	public static final CauldronInteraction MANA_LENS_INTERACTION = BotaniaItems::cauldronUndyeManaLens;
	public static final CauldronInteraction PHANTOM_INK_INTERACTION = BotaniaItems::cauldronRemovePhantomInk;

	private static InteractionResult cauldronUndyeManaPool(BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, ItemStack stack) {
		Block block = Block.byItem(stack.getItem());
		if (!(block instanceof ManaPoolBlock poolBlock)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			ManaPoolBlock undyedPool = ManaPoolBlock.getUndyedBlock(poolBlock);
			ItemStack itemstack = stack.transmuteCopy(undyedPool, 1);
			player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, itemstack, false));
			player.awardStat(BotaniaStats.MANA_POOLS_CLEANED);
			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
		}
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult cauldronUndyeManaLens(BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, ItemStack stack) {
		if (!LensItem.isLensTinted(stack)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			stack.remove(BotaniaDataComponents.LENS_RAINBOW_TINT);
			stack.remove(BotaniaDataComponents.LENS_TINT);
			player.awardStat(BotaniaStats.MANA_LENSES_CLEANED);
			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
		}
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult cauldronRemovePhantomInk(BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, ItemStack stack) {
		if (!(stack.getItem() instanceof PhantomInkable inkable) || !inkable.hasPhantomInk(stack)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			inkable.setPhantomInk(stack, false);
			player.awardStat(BotaniaStats.PHANTOM_INK_CLEANED);
			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
		}
		return InteractionResult.SUCCESS;
	}

	private static <T extends Item> T make(String name, T item) {
		var old = ALL.put(name, item);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate name: " + name);
		}
		return item;
	}

	// PORT-26.3: 26.3 items need their registry id in the properties before construction (description id, model)
	private static <T extends Item> T make(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		return make(name, factory.apply(properties.setId(ResourceKey.create(Registries.ITEM, botaniaRL(name)))));
	}

	private static MysticalPetalItem makePetal(DyeColor dyeColor) {
		return make(dyeColor.getName() + LibItemNames.MYSTICAL_PETAL_SUFFIX,
				props -> new MysticalPetalItem(BotaniaBlocks.getBuriedPetal(dyeColor), dyeColor, props), defaultBuilder());
	}

	public static Item.Properties defaultBuilder() {
		return new Item.Properties();
	}

	public static Item.Properties manaRepairable(int manaPerDamage) {
		return defaultBuilder()
				.component(BotaniaDataComponents.MANA_REPAIR, new ManaRepair(manaPerDamage, 2 * manaPerDamage));
	}

	private static Item.Properties stackTo16() {
		return defaultBuilder().stacksTo(16);

	}

	private static Item.Properties unstackable() {
		return defaultBuilder().stacksTo(1);
	}

	public static void registerItems(BiConsumer<Item, Identifier> r) {
		for (var e : ALL.entrySet()) {
			r.accept(e.getValue(), botaniaRL(e.getKey()));
		}
	}

	public static void registerMenuTypes(BiConsumer<MenuType<?>, Identifier> consumer) {
		consumer.accept(TRINKET_CASE_MENU_TYPE, botaniaRL(LibItemNames.TRINKET_CASE));
		consumer.accept(COLORED_CONTENTS_POUCH_MENU_TYPE, botaniaRL(LibItemNames.FLOWER_POUCH));
		consumer.accept(HAND_OF_ENDER_MENU_TYPE, botaniaRL(LibItemNames.HAND_OF_ENDER));
	}

	// PORT-26.3: the water cauldron's interactions are a Dispatcher whose put is package-private.
	private static void waterCauldron(Item item, CauldronInteraction interaction) {
		((CauldronInteractionDispatcherAccessor) CauldronInteractions.WATER).botania_put(item, interaction);
	}

	public static void registerCauldronInteractions() {
		ColorHelper.supportedColors().forEach(color -> {
			waterCauldron(BotaniaBlocks.findOptionallyDyedBlock(BotaniaBlocks.MANA_POOL, color).asItem(), MANA_POOL_INTERACTION);
			waterCauldron(BotaniaBlocks.findOptionallyDyedBlock(BotaniaBlocks.CREATIVE_MANA_POOL, color).asItem(), MANA_POOL_INTERACTION);
			waterCauldron(BotaniaBlocks.findOptionallyDyedBlock(BotaniaBlocks.DILUTED_MANA_POOL, color).asItem(), MANA_POOL_INTERACTION);
			waterCauldron(BotaniaBlocks.findOptionallyDyedBlock(BotaniaBlocks.FABULOUS_MANA_POOL, color).asItem(), MANA_POOL_INTERACTION);
		});

		ALL.values().stream().filter(item -> item instanceof LensItem)
				.forEach(item -> waterCauldron(item, MANA_LENS_INTERACTION));
		ALL.values().stream().filter(item -> item instanceof PhantomInkable)
				.forEach(item -> waterCauldron(item, PHANTOM_INK_INTERACTION));
	}

	public static Item getPetal(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_MYSTICAL_PETAL;
			case ORANGE -> ORANGE_MYSTICAL_PETAL;
			case MAGENTA -> MAGENTA_MYSTICAL_PETAL;
			case LIGHT_BLUE -> LIGHT_BLUE_MYSTICAL_PETAL;
			case YELLOW -> YELLOW_MYSTICAL_PETAL;
			case LIME -> LIME_MYSTICAL_PETAL;
			case PINK -> PINK_MYSTICAL_PETAL;
			case GRAY -> GRAY_MYSTICAL_PETAL;
			case LIGHT_GRAY -> LIGHT_GRAY_MYSTICAL_PETAL;
			case CYAN -> CYAN_MYSTICAL_PETAL;
			case PURPLE -> PURPLE_MYSTICAL_PETAL;
			case BLUE -> BLUE_MYSTICAL_PETAL;
			case BROWN -> BROWN_MYSTICAL_PETAL;
			case GREEN -> GREEN_MYSTICAL_PETAL;
			case RED -> RED_MYSTICAL_PETAL;
			case BLACK -> BLACK_MYSTICAL_PETAL;
		};
	}

	public static boolean isNoDespawn(Item item) {
		return item instanceof ManaTabletItem || item instanceof BandOfManaItem || item instanceof TerraShattererItem
				|| item instanceof RelicItem || item instanceof RelicBaubleItem;
	}
}
