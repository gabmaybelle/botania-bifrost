/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.armor.manasteel;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.helper.DataComponentHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.proxy.Proxy;

import java.util.List;
import java.util.function.Supplier;

// PORT-26.3: ArmorItem is gone; being armor is a matter of item properties (Item.Properties#humanoidArmor in
// BotaniaItems). The piece and material are kept here for the set logic and the subclasses.
public class ManasteelArmorItem extends Item implements PhantomInkable {
	protected final ArmorType type;
	protected final ArmorMaterial material;

	public ManasteelArmorItem(ArmorType type, ArmorMaterial material, Properties properties) {
		super(properties);
		this.type = type;
		this.material = material;
	}

	public ArmorType getType() {
		return type;
	}

	public ArmorMaterial getMaterial() {
		return material;
	}

	public EquipmentSlot getEquipmentSlot() {
		return type.getSlot();
	}

	@SoftImplement("IItemExtension")
	// PORT-26.3: ArmorMaterial.Layer is gone; the renderers ask for the texture directly once they are ported.
	public final Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, boolean innerModel) {
		return hasPhantomInk(stack) ? Identifier.parse(ResourcesLib.MODEL_INVISIBLE_ARMOR) : getArmorTextureAfterInk(stack, slot);
	}

	public Identifier getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		return Identifier.parse(ResourcesLib.MODEL_MANASTEEL_NEW);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags) {
		TooltipHandler.addOnShift(list, flags, () -> addInformationAfterShift(stack, context, list, flags));
	}

	public void addInformationAfterShift(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags) {
		Player player = Proxy.INSTANCE.getClientPlayer();
		list.add(getArmorSetTitle(player));
		addArmorSetDescription(stack, list);
		ItemStack[] stacks = getArmorSetStacks();
		for (ItemStack armor : stacks) {
			EquipmentSlot slot = ((ManasteelArmorItem) armor.getItem()).getEquipmentSlot();
			MutableComponent cmp = Component.translatable("botaniamisc.template.tooltip_list", armor.getHoverName())
					.withStyle(hasArmorSetItem(player, slot) ? ChatFormatting.GREEN : ChatFormatting.GRAY);
			list.add(cmp);
		}
		if (hasPhantomInk(stack)) {
			list.add(Component.translatable("botaniamisc.hasPhantomInk").withStyle(ChatFormatting.GRAY));
		}
	}

	private static final Supplier<ItemStack[]> armorSet = Suppliers.memoize(() -> new ItemStack[] {
			new ItemStack(BotaniaItems.MANASTEEL_HELMET),
			new ItemStack(BotaniaItems.MANASTEEL_CHESTPLATE),
			new ItemStack(BotaniaItems.MANASTEEL_LEGGINGS),
			new ItemStack(BotaniaItems.MANASTEEL_BOOTS)
	});

	public ItemStack[] getArmorSetStacks() {
		return armorSet.get();
	}

	public boolean hasArmorSet(Player player) {
		return hasArmorSetItem(player, EquipmentSlot.HEAD) && hasArmorSetItem(player, EquipmentSlot.CHEST) && hasArmorSetItem(player, EquipmentSlot.LEGS) && hasArmorSetItem(player, EquipmentSlot.FEET);
	}

	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null) {
			return false;
		}

		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) {
			return false;
		}

		return switch (slot) {
			case HEAD -> stack.is(BotaniaItems.MANASTEEL_HELMET);
			case CHEST -> stack.is(BotaniaItems.MANASTEEL_CHESTPLATE);
			case LEGS -> stack.is(BotaniaItems.MANASTEEL_LEGGINGS);
			case FEET -> stack.is(BotaniaItems.MANASTEEL_BOOTS);
			default -> false;
		};

	}

	private int getSetPiecesEquipped(Player player) {
		int pieces = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && hasArmorSetItem(player, slot)) {
				pieces++;
			}
		}

		return pieces;
	}

	public MutableComponent getArmorSetName() {
		return Component.translatable("botania.armorset.manasteel.name");
	}

	private Component getArmorSetTitle(Player player) {
		Component pieces = Component.translatable("botaniamisc.template.n_of_m",
				getSetPiecesEquipped(player), getArmorSetStacks().length);
		Component setInfo = Component.translatable("botaniamisc.template.parenthesis_suffix", getArmorSetName(), pieces)
				.withStyle(ChatFormatting.GRAY);
		return Component.translatable("botaniamisc.armorset", setInfo);
	}

	public void addArmorSetDescription(ItemStack stack, List<Component> list) {
		list.add(Component.translatable("botania.armorset.manasteel.desc").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean hasPhantomInk(ItemStack stack) {
		return stack.has(BotaniaDataComponents.PHANTOM_INKED);
	}

	@Override
	public void setPhantomInk(ItemStack stack, boolean ink) {
		DataComponentHelper.setFlag(stack, BotaniaDataComponents.PHANTOM_INKED, ink);
	}
}
