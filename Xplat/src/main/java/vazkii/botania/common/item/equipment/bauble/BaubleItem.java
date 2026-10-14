/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import vazkii.botania.api.item.CosmeticAttachable;
import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.DataComponentHelper;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.function.Consumer;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public abstract class BaubleItem extends Item implements CosmeticAttachable, PhantomInkable {

	public BaubleItem(Properties properties) {
		super(properties);
		EquipmentHandler.instance.onInit(this);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flags) {
		ItemStack cosmetic = getCosmeticItem(stack);
		if (!cosmetic.isEmpty()) {
			tooltip.accept(Component.translatable("botaniamisc.hasCosmetic", cosmetic.getHoverName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}

		if (hasPhantomInk(stack)) {
			tooltip.accept(Component.translatable("botaniamisc.hasPhantomInk").withStyle(ChatFormatting.AQUA));
		}
	}

	@Override
	public ItemStack getCosmeticItem(ItemStack stack) {
		return DataComponentHelper.getSingleItem(stack, BotaniaDataComponents.COSMETIC_OVERRIDE);
	}

	@Override
	public void setCosmeticItem(ItemStack stack, ItemStack cosmetic) {
		DataComponentHelper.setNonEmpty(stack, BotaniaDataComponents.COSMETIC_OVERRIDE, cosmetic);
	}

	@Override
	public boolean hasPhantomInk(ItemStack stack) {
		return stack.has(BotaniaDataComponents.PHANTOM_INKED);
	}

	@Override
	public void setPhantomInk(ItemStack stack, boolean ink) {
		DataComponentHelper.setFlag(stack, BotaniaDataComponents.PHANTOM_INKED, ink);
	}

	public void onWornTick(ItemStack stack, LivingEntity entity) {}

	public void onEquipped(ItemStack stack, LivingEntity entity) {
		if (!entity.level().isClientSide() && entity instanceof ServerPlayer player) {
			PlayerHelper.grantCriterion(player, botaniaRL("main/bauble_wear"), "code_triggered");
		}
	}

	public void onUnequipped(ItemStack stack, LivingEntity entity) {}

	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return true;
	}

	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, Identifier slotId) {
		return HashMultimap.create();
	}

	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return !hasPhantomInk(stack)
				&& BotaniaConfig.client().renderAccessories()
				&& !living.isInvisible();
	}
}
