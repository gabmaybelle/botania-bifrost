/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.gui.enderhand;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// [VanillaCopy] ContainerScreen with 3 rows for a player's Ender Chest inventory
public class HandOfEnderScreen extends AbstractContainerScreen<HandOfEnderMenu> implements MenuAccess<HandOfEnderMenu> {
	private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final int CONTAINER_ROWS = 3;

	public HandOfEnderScreen(HandOfEnderMenu menu, Inventory playerInventory, Component title) {
		// PORT-26.3: the image size is final and set through the constructor
		super(menu, playerInventory, title, 176, 114 + CONTAINER_ROWS * 18);
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		int xo = (this.width - this.imageWidth) / 2;
		int yo = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, xo, yo, 0, 0, this.imageWidth, CONTAINER_ROWS * 18 + 17, 256, 256);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, xo, yo + CONTAINER_ROWS * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
	}
}
