/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.Map;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: armor materials are no longer a registry, just values the item properties apply
 * ({@code Item.Properties#humanoidArmor}). The numbers are upstream's: the per-piece durability factors
 * that BotaniaItems used to pass to {@code ArmorItem.Type#getDurability}, the defense per piece,
 * enchantability, equip sound and toughness. Repairing is by item tag now. Each material names an
 * equipment asset ({@code assets/botania/equipment/<name>.json}); Botania's own armor models are drawn by
 * its renderers, so those files are empty until the rendering port.
 */
public final class BotaniaArmorMaterials {
	public static final ArmorMaterial MANASTEEL = create("manasteel", 18,
			Map.of(
					ArmorType.BOOTS, 2,
					ArmorType.LEGGINGS, 5,
					ArmorType.CHESTPLATE, 6,
					ArmorType.HELMET, 2
			),
			18, BotaniaSounds.EQUIP_MANASTEEL, 0, commonTag("ingots/manasteel"));
	public static final ArmorMaterial MANAWEAVE = create("manaweave", 5,
			Map.of(
					ArmorType.BOOTS, 1,
					ArmorType.LEGGINGS, 2,
					ArmorType.CHESTPLATE, 3,
					ArmorType.HELMET, 1
			),
			18, BotaniaSounds.EQUIP_MANAWEAVE, 0, BotaniaTags.Items.MANAWEAVE_ARMOR_REPAIR);
	public static final ArmorMaterial ELEMENTIUM = create("elementium", 18,
			Map.of(
					ArmorType.BOOTS, 2,
					ArmorType.LEGGINGS, 5,
					ArmorType.CHESTPLATE, 6,
					ArmorType.HELMET, 2
			),
			18, BotaniaSounds.EQUIP_ELEMENTIUM, 0, commonTag("ingots/elementium"));
	public static final ArmorMaterial TERRASTEEL = create("terrasteel", 34,
			Map.of(
					ArmorType.BOOTS, 3,
					ArmorType.LEGGINGS, 6,
					ArmorType.CHESTPLATE, 8,
					ArmorType.HELMET, 3
			),
			26, BotaniaSounds.EQUIP_TERRASTEEL, 3, commonTag("ingots/terrasteel"));

	private BotaniaArmorMaterials() {}

	private static ArmorMaterial create(
			String name,
			int durabilityFactor,
			Map<ArmorType, Integer> defense,
			int enchantmentValue,
			Holder<SoundEvent> equipSound,
			float toughness,
			TagKey<Item> repairIngredient) {
		ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, botaniaRL(name));
		return new ArmorMaterial(durabilityFactor, defense, enchantmentValue, equipSound, toughness, 0, repairIngredient, asset);
	}

	private static TagKey<Item> commonTag(String path) {
		return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
	}
}
