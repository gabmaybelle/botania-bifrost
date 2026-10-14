/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.gui.box;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import vazkii.botania.client.lib.ResourcesLib;

public class TrinketCaseScreen extends AbstractContainerScreen<TrinketCaseMenu> {

	private static final Identifier texture = Identifier.parse(ResourcesLib.GUI_BAUBLE_BOX);
	private int mouseX;
	private int mouseY;

	public TrinketCaseScreen(TrinketCaseMenu container, Inventory playerInventory, Component title) {
		super(container, playerInventory, title);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		// No-op, there's no space for gui titles
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
		InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics,
				leftPos + 8, topPos + 8, leftPos + 57, topPos + 78,
				30, 0.0625F, this.mouseX, this.mouseY, minecraft.player);
	}

}
