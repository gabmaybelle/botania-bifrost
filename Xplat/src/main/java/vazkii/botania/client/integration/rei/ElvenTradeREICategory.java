/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.List;

public class ElvenTradeREICategory implements DisplayCategory<ElvenTradeREIDisplay> {
	private final EntryStack<ItemStack> gateway = EntryStacks.of(new ItemStack(BotaniaBlocks.ELVEN_GATEWAY_CORE));
	private final Identifier TRADE_OVERLAY = BotaniaAPI.botaniaRL("textures/gui/elven_trade_overlay.png");

	@Override
	public CategoryIdentifier<ElvenTradeREIDisplay> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.ELVEN_TRADE;
	}

	@Override
	public Renderer getIcon() {
		return gateway;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("botania.nei.elvenTrade");
	}

	@Override
	public List<Widget> setupDisplay(ElvenTradeREIDisplay display, Rectangle bounds) {
		List<Widget> widgets = new ArrayList<>();
		Point center = new Point(bounds.getCenterX() - 8, bounds.getCenterY() - 4);
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createDrawableWidget((gui, mouseX, mouseY, delta) -> {
			CategoryUtils.drawOverlay(gui, TRADE_OVERLAY, center.x - 45, center.y - 34, 20, 19, 71, 75);
			drawPortal(gui, center);
		}));
		int x = center.x - 20;
		for (EntryIngredient o : display.getInputEntries()) {
			widgets.add(Widgets.createSlot(new Point(x, center.y - 40)).entries(o).disableBackground());
			x += 18;
		}
		x = center.x + 28;
		for (EntryIngredient o : display.getOutputEntries()) {
			widgets.add(Widgets.createSlot(new Point(x, center.y)).entries(o).disableBackground());
			x += 18;
		}
		return widgets;
	}

	@Override
	public int getDisplayHeight() {
		return 100;
	}

	// PORT-26.3: GUI drawing goes through GuiGraphicsExtractor's pipelines; the portal swirl is a block atlas sprite
	void drawPortal(GuiGraphicsExtractor gui, Point point) {
		TextureAtlasSprite sprite = gui.getSprite(new SpriteId(TextureAtlas.LOCATION_BLOCKS, BotaniaAPI.botaniaRL("block/alfheim_portal_swirl")));
		int startX = point.x - 43;
		int startY = point.y - 17;
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, startX, startY, 48, 48);
	}
}
