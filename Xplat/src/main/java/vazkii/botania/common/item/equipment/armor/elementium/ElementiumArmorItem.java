/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.armor.elementium;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ElementiumArmorItem extends ManasteelArmorItem {
	public ElementiumArmorItem(ArmorType type, ArmorMaterial material, double pixieChance, Properties properties) {
		super(type, material, properties);
	}

	// PORT-26.3: Item#getDefaultAttributeModifiers is gone (attributes are a component), so BotaniaItems puts these on.
	public static ItemAttributeModifiers createAttributes(ArmorType type, ArmorMaterial material, double pixieChance) {
		return material.createAttributes(type)
				.withModifierAdded(PixieHandler.PIXIE_SPAWN_CHANCE, PixieHandler.makeModifier(
						botaniaRL("armor." + type.getName()), pixieChance),
						EquipmentSlotGroup.bySlot(type.getSlot()));
	}

	@Override
	public Identifier getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return Identifier.parse(ResourcesLib.MODEL_ELEMENTIUM_NEW);
	}

	private static final Supplier<ItemStack[]> armorSet = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(BotaniaItems.ELEMENTIUM_HELMET),
			new ItemStack(BotaniaItems.ELEMENTIUM_CHESTPLATE),
			new ItemStack(BotaniaItems.ELEMENTIUM_LEGGINGS),
			new ItemStack(BotaniaItems.ELEMENTIUM_BOOTS)
	});

	@Override
	public ItemStack[] getArmorSetStacks() {
		return armorSet.get();
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null) {
			return false;
		}

		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) {
			return false;
		}

		return switch (slot) {
			case HEAD -> stack.is(BotaniaItems.ELEMENTIUM_HELMET);
			case CHEST -> stack.is(BotaniaItems.ELEMENTIUM_CHESTPLATE);
			case LEGS -> stack.is(BotaniaItems.ELEMENTIUM_LEGGINGS);
			case FEET -> stack.is(BotaniaItems.ELEMENTIUM_BOOTS);
			default -> false;
		};

	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("botania.armorset.elementium.name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, Consumer<Component> list) {
		super.addArmorSetDescription(stack, list);
		list.accept(Component.translatable("botania.armorset.elementium.desc").withStyle(ChatFormatting.GRAY));
	}

}
