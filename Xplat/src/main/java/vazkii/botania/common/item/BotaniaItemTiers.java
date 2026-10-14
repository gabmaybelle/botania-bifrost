/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * PORT-26.3: {@code Tier} is gone. A tool's material is a {@link ToolMaterial} value (the same numbers as
 * before: durability, mining speed, attack damage bonus, enchantability) that the item properties apply
 * ({@code Item.Properties#pickaxe} and friends). Repairing is by item tag now, so the metals repair with
 * their {@code c:ingots/...} tags. The Vitreous Pickaxe's own mining rules moved to
 * {@link vazkii.botania.common.item.equipment.tool.VitreousPickaxeItem}.
 */
public final class BotaniaItemTiers {
	public static final ToolMaterial MANASTEEL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
			300, 6.2F, 2, 20, commonTag("ingots/manasteel"));
	public static final ToolMaterial ELEMENTIUM = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
			720, 6.2F, 2, 20, commonTag("ingots/elementium"));
	public static final ToolMaterial TERRASTEEL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
			2300, 9, 4, 26, commonTag("ingots/terrasteel"));
	public static final ToolMaterial VITREOUS = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL,
			125, 3.9F, 0, 10, commonTag("glass_blocks/colorless"));

	private BotaniaItemTiers() {}

	private static TagKey<Item> commonTag(String path) {
		return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
	}
}
