/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.DataComponentHelper;

import java.util.List;

public class ManufactoryHaloItem extends AssemblyHaloItem {

	private static final Identifier glowTexture = Identifier.parse(ResourcesLib.MISC_GLOW_CYAN);

	public ManufactoryHaloItem(Item.Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot pos) {
		// PORT-26.3: inventoryTick gets the equipment slot now (null in the inventory), not a selected flag.
		boolean equipped = pos == EquipmentSlot.MAINHAND;
		super.inventoryTick(stack, world, entity, pos);

		if (!world.isClientSide() && entity instanceof Player player && !equipped && isActive(stack)) {

			for (int i = 1; i < SEGMENTS; i++) {
				tryCraft(player, stack, i, false);
			}
		}
	}

	@Override
	public Identifier getGlowResource(ItemStack stack) {
		return isActive(stack) ? glowTexture : super.getGlowResource(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> stacks, TooltipFlag flags) {
		if (isActive(stack)) {
			stacks.add(Component.translatable("botaniamisc.active"));
		} else {
			stacks.add(Component.translatable("botaniamisc.inactive"));
		}
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (getSegmentLookedAt(stack, player) == 0 && player.isSecondaryUseActive()) {
			togglePassive(stack, player, world);
			return InteractionResult.SUCCESS;
		}

		return super.use(world, player, hand);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack cursor, Slot slot,
			ClickAction click, Player player, SlotAccess access) {
		Level world = player.level();
		if (click == ClickAction.SECONDARY && slot.allowModification(player) && cursor.isEmpty()) {
			togglePassive(stack, player, world);
			access.set(cursor);
			return true;
		}
		return false;
	}

	private void togglePassive(ItemStack stack, LivingEntity living, Level world) {
		DataComponentHelper.setFlag(stack, BotaniaDataComponents.ACTIVE, !isActive(stack));
		if (living instanceof Player player) {
			world.playSound(player, player.getX(), player.getY(), player.getZ(), BotaniaSounds.MANUFACTORY_HALO_CONFIGURE, SoundSource.NEUTRAL, 1F, 1F);
		}
	}

	private static boolean isActive(ItemStack stack) {
		return stack.has(BotaniaDataComponents.ACTIVE);
	}
}
