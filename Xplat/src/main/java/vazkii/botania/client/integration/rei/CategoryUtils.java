/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

interface CategoryUtils {
	/**
	 * Widgets#createTexturedWidget doesn't allow partial transparency, so this is called in createDrawableWidget
	 * instead.
	 */
	static void drawOverlay(GuiGraphicsExtractor gui, Identifier texture, int x, int y, int u, int v, int width, int height) {
		// PORT-26.3: the GUI pipeline blends by itself
		gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
	}

	/**
	 * spin the wheel and laugh at god! Used for creating the Apothecary and Rune Altar categories.
	 */
	static FloatingPoint rotatePointAbout(FloatingPoint in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new FloatingPoint(newX, newY);
	}
}
