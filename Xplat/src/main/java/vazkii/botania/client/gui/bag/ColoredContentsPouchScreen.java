/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.gui.bag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix3x2fStack;

import vazkii.botania.api.internal.Colored;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.ColoredContentsPouchItem;

import java.util.List;

public class ColoredContentsPouchScreen extends AbstractContainerScreen<ColoredContentsPouchMenu> {

	private static final Identifier texture = Identifier.parse(ResourcesLib.GUI_FLOWER_BAG);

	public ColoredContentsPouchScreen(ColoredContentsPouchMenu container, Inventory playerInventory, Component title) {
		// PORT-26.3: the image size is final and set through the constructor
		super(container, playerInventory, title, 176, 166 + 36);

		// recompute, same as super
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		Matrix3x2fStack ms = guiGraphics.pose();
		Minecraft mc = Minecraft.getInstance();
		int k = (width - imageWidth) / 2;
		int l = (height - imageHeight) / 2;
		// TODO: adjust for other row counts
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, k, l, 0, 0, imageWidth, imageHeight, 256, 256);

		List<TagKey<Item>> itemTypes = ColoredContentsPouchItem.getStoredItemTypes(menu.getPouch());
		List<DyeColor> colors = ColorHelper.supportedColors().toList();
		for (Slot slot : menu.slots) {
			if (slot.container == menu.pouchInv) {
				int x = this.leftPos + slot.x;
				int y = this.topPos + slot.y;
				if (!slot.hasItem()) {
					int typeIndex = slot.index / colors.size();
					int colorIndex = slot.index % colors.size();
					var holder = BuiltInRegistries.ITEM.get(itemTypes.get(typeIndex));
					holder.flatMap(holders -> holders.stream().map(Holder::value)
							.filter(item -> item instanceof Colored colored &&
									colored.getColor() == colors.get(colorIndex))
							.findFirst())
							.ifPresent(item -> {
								ItemStack missingFlower = new ItemStack(item);
								RenderHelper.renderGuiItemAlpha(guiGraphics, missingFlower, x, y, 0x5F);
							});
				} else if (slot.getItem().getCount() == 1) {
					// Always draw the count even at 1
					ms.pushMatrix();
					// Same as how much vanilla offsets when drawing item decorations in gui slots
					guiGraphics.text(mc.font, "1", x + 11, y + 9, 0xFFFFFF);
					ms.popMatrix();
				}
			}
		}
	}
}
