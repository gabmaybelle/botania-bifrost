/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.gui.monocle;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.block.MonocleHud;
import vazkii.botania.client.core.helper.RenderHelper;

/**
 * Template for a simple icon+text monocle HUD overlay to the right of the crosshair.
 */
public interface SimpleTextAndIconMonocleHud extends MonocleHud {
	/**
	 * The item stack to use as icon.
	 */
	ItemStack getDisplayStack();

	/**
	 * The text to display.
	 */
	Component getDisplayString();

	@Override
	default void renderHUD(GuiGraphicsExtractor gui, Window window, Font font, float partialTick) {
		int x = window.getGuiScaledWidth() / 2 + 15;
		int y = window.getGuiScaledHeight() / 2 - 8;

		Component text = getDisplayString();
		int textWidth = font.width(text.getVisualOrderText());
		RenderHelper.renderHUDBox(gui, x - 4, y - 4, x + textWidth + 24, y + 20);
		gui.item(getDisplayStack(), x, y);
		gui.text(font, text, x + 20, y + 4, 0xFFFFFF);
	}
}
