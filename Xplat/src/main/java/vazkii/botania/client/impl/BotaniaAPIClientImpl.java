/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.impl;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.client.gui.HUDHandler;

public class BotaniaAPIClientImpl implements BotaniaAPIClient {

	@Override
	public void drawSimpleManaHUD(GuiGraphicsExtractor gui, Window window, Font font, int color, int mana, int maxMana, String name) {
		HUDHandler.drawSimpleManaHUD(gui, window, font, color, mana, maxMana, name);
	}

	@Override
	public void drawComplexManaHUD(GuiGraphicsExtractor gui, Window window, Font font, int color, int mana, int maxMana, String name, ItemStack bindDisplay, boolean properlyBound) {
		HUDHandler.drawComplexManaHUD(color, gui, window, font, mana, maxMana, name, bindDisplay, properlyBound
		);
	}

}
