/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.integration.trinkets;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.ResoluteIvyItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.proxy.Proxy;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import eu.pb4.trinkets.api.event.TrinketDropCallback;

// PORT-26.3: Trinkets ends at 1.21.1; this uses Trinkets Updated (eu.pb4.trinkets), which replaces Trinket with
// TrinketCallback, SlotReference with TrinketSlotAccess and the trinket component with TrinketAttachment, and
// equips any item whose callback allows it on right-click (so the old UseItemCallback is gone).
public class TrinketsIntegration extends EquipmentHandler {
	public static void init() {
		TrinketDropCallback.EVENT.register(TrinketsIntegration::keepAccessoryDrops);
	}

	private static TrinketDropRule keepAccessoryDrops(TrinketDropRule oldRule,
			ItemStack stack, TrinketSlotAccess ref, LivingEntity livingEntity) {
		//TODO make this less hacky
		if (ResoluteIvyItem.hasIvy(stack)) {
			stack.remove(BotaniaDataComponents.RESOLUTE_IVY);
			return TrinketDropRule.KEEP;
		}
		return oldRule;
	}

	@Override
	protected Container getAllWornItems(LivingEntity living) {
		return new SimpleContainer(TrinketsApi.getAttachment(living).allEquipped(true).stream()
				.map(TrinketSlotAccess::get)
				.toArray(ItemStack[]::new));
	}

	@Override
	protected ItemStack findItem(Item item, LivingEntity living) {
		return findItem(stack -> stack.is(item), living);
	}

	@Override
	protected ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living) {
		return TrinketsApi.getAttachment(living).findFirst(pred, true)
				.map(TrinketSlotAccess::get)
				.orElse(ItemStack.EMPTY);
	}

	@Override
	public void onInit(Item item) {
		TrinketCallback.setCallback(item, WRAPPER);
		// PORT-26.3: the renderer lives in its own class, since Trinkets Updated strips its client classes on servers
		Proxy.INSTANCE.runOnClient(() -> () -> TrinketsClientIntegration.registerRenderer(item));
	}

	@Override
	public boolean isAccessory(ItemStack stack) {
		return super.isAccessory(stack)
				|| TrinketCallback.getCallback(stack) != TrinketCallback.DEFAULT;
	}

	public static final TrinketCallback WRAPPER = new TrinketCallback() {
		private BaubleItem getItem(ItemStack stack) {
			return (BaubleItem) stack.getItem();
		}

		@Override
		public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				getItem(stack).onWornTick(stack, entity);
			}
		}

		@Override
		public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				getItem(stack).onEquipped(stack, entity);
			}
		}

		@Override
		public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				getItem(stack).onUnequipped(stack, entity);
			}
		}

		@Override
		public boolean canEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				return getItem(stack).canEquip(stack, entity);
			}
			return false;
		}

		@Override
		public boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
			return !stack.isEmpty() && getItem(stack).canEquip(stack, entity);
		}

		@Override
		public void forEachTrinketModifier(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity,
				Identifier slotIdentifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
			if (!stack.isEmpty()) {
				getItem(stack).getEquippedAttributeModifiers(stack, slotIdentifier).forEach(consumer);
			}
		}
	};
}
