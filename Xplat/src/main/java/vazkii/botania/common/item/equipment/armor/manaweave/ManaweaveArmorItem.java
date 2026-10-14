/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.armor.manaweave;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ManaweaveArmorItem extends ManasteelArmorItem {

	public ManaweaveArmorItem(ArmorType type, ArmorMaterial material, Properties properties) {
		super(type, material, properties);
	}

	@Override
	public Identifier getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return Identifier.parse(ClientProxy.jingleTheBells ? ResourcesLib.MODEL_MANAWEAVE_NEW_HOLIDAY : ResourcesLib.MODEL_MANAWEAVE_NEW);
	}

	@Override
	public Component getName(ItemStack stack) {
		// PORT-26.3: the translation key is final now, so the festive rename is done on the name
		if (XplatAbstractions.INSTANCE.isPhysicalClient() && ClientProxy.jingleTheBells) {
			return Component.translatable(getDescriptionId().replace("manaweave", "santaweave"));
		}
		return super.getName(stack);
	}

	private static final Supplier<ItemStack[]> armorSet = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(BotaniaItems.MANAWEAVE_HELMET),
			new ItemStack(BotaniaItems.MANAWEAVE_CHESTPLATE),
			new ItemStack(BotaniaItems.MANAWEAVE_LEGGINGS),
			new ItemStack(BotaniaItems.MANAWEAVE_BOOTS)
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
			case HEAD -> stack.is(BotaniaItems.MANAWEAVE_HELMET);
			case CHEST -> stack.is(BotaniaItems.MANAWEAVE_CHESTPLATE);
			case LEGS -> stack.is(BotaniaItems.MANAWEAVE_LEGGINGS);
			case FEET -> stack.is(BotaniaItems.MANAWEAVE_BOOTS);
			default -> false;
		};

	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("botania.armorset.manaweave.name");
	}

	@Override
	public void addInformationAfterShift(ItemStack stack, TooltipContext context, Consumer<Component> list, TooltipFlag flags) {
		if (XplatAbstractions.INSTANCE.isPhysicalClient() && ClientProxy.jingleTheBells) {
			list.accept(Component.translatable("botaniamisc.santaweaveInfo"));
			list.accept(Component.literal(""));
		}

		super.addInformationAfterShift(stack, context, list, flags);
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, Consumer<Component> list) {
		list.accept(Component.translatable("botania.armorset.manaweave.desc0").withStyle(ChatFormatting.GRAY));
		list.accept(Component.translatable("botania.armorset.manaweave.desc1").withStyle(ChatFormatting.GRAY));
	}

	@SoftImplement("IItemExtension")
	public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
		return stack.is(BotaniaItems.MANAWEAVE_BOOTS);
	}
}
