/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.RecipeBookAccess;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.mixin.client.AbstractContainerScreenAccessor;
import vazkii.botania.mixin.client.AbstractRecipeBookScreenAccessor;
import vazkii.botania.mixin.client.RecipeBookComponentAccessor;
import vazkii.botania.mixin.client.RecipeBookPageAccessor;
import vazkii.botania.network.serverbound.IndexKeybindRequestPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CorporeaInputHandler {

	public static final List<Supplier<ItemStack>> hoveredStackGetters = new ArrayList<>();
	public static Predicate<Screen> supportedGuiFilter = gui -> gui instanceof AbstractContainerScreen;

	public static boolean buttonPressed(KeyEvent event) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null || !supportedGuiFilter.test(mc.gui.screen())
				|| !ClientProxy.CORPOREA_REQUEST.matches(event)
				|| CorporeaIndexBlockEntity.getNearbyValidIndexes(mc.player).isEmpty()) {
			return false;
		}

		ItemStack stack = getStackUnderMouse();
		if (!stack.isEmpty()) {
			int count = 1;
			int max = stack.getMaxStackSize();

			if (event.hasShiftDown()) {
				count = max;
				if (event.hasControlDown()) {
					count /= 4;
				}
			} else if (event.hasControlDown()) {
				count = max / 2;
			}

			if (count > 0) {
				ItemStack requested = stack.copyWithCount(count);
				ClientXplatAbstractions.INSTANCE.sendToServer(new IndexKeybindRequestPacket(requested));
				return true;
			}
		}
		return false;
	}

	private static ItemStack getStackUnderMouse() {
		Screen screen = Minecraft.getInstance().gui.screen();
		if (screen instanceof AbstractContainerScreen) {
			Slot slotUnderMouse = ((AbstractContainerScreenAccessor) screen).botania_getHoveredSlot();
			if (slotUnderMouse != null && slotUnderMouse.hasItem()) {
				return slotUnderMouse.getItem().copy();
			}

			// PORT-26.3: the recipe book lives on AbstractRecipeBookScreen now
			if (screen instanceof AbstractRecipeBookScreen<?> recipeScreen
					&& ((AbstractRecipeBookScreenAccessor) recipeScreen).botania_getRecipeBookComponent().isVisible()) {
				RecipeBookComponent<?> recipeBook = ((AbstractRecipeBookScreenAccessor) recipeScreen).botania_getRecipeBookComponent();
				RecipeBookPage page = ((RecipeBookComponentAccessor) recipeBook).botania_getRecipeBookPage();
				RecipeButton widget = ((RecipeBookPageAccessor) page).botania_getHoveredButton();
				if (widget != null) {
					return widget.getDisplayStack();
				}
				ItemStack stack = ((RecipeBookAccess) recipeBook).botania_getHoveredGhostRecipeStack();
				if (stack != null) {
					return stack;
				}
			}
		}

		for (var getter : CorporeaInputHandler.hoveredStackGetters) {
			ItemStack stack = getter.get();
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
