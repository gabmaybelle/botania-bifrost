/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool.elementium;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import vazkii.botania.common.handler.PixieHandler;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

// PORT-26.3: SwordItem is gone; BotaniaItems gives this the sword properties, then these attributes on top.
public class ElementiumSwordItem extends Item {
	public ElementiumSwordItem(Properties properties) {
		super(properties);
	}

	// [VanillaCopy] ToolMaterial#createSwordAttributes, which is private
	public static ItemAttributeModifiers.Builder swordAttributes(ToolMaterial material, float attackDamage, float attackSpeed) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
						attackDamage + material.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
						attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
	}

	public static ItemAttributeModifiers createAttributes(ToolMaterial material, int attackDamage, float attackSpeed) {
		return swordAttributes(material, attackDamage, attackSpeed).build()
				.withModifierAdded(
						PixieHandler.PIXIE_SPAWN_CHANCE,
						PixieHandler.makeModifier(botaniaRL("sword_modifier"), 0.05),
						EquipmentSlotGroup.MAINHAND
				);
	}
}
