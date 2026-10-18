/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;

import org.joml.Matrix3x2fStack;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.LexicaBotaniaItem;

public class KonamiHandler {
	private static final int[] KONAMI_CODE = {
			InputConstants.KEY_UP, InputConstants.KEY_UP,
			InputConstants.KEY_DOWN, InputConstants.KEY_DOWN,
			InputConstants.KEY_LEFT, InputConstants.KEY_RIGHT,
			InputConstants.KEY_LEFT, InputConstants.KEY_RIGHT,
			InputConstants.KEY_B, InputConstants.KEY_A,
	};
	public static final int TOTAL_KONAMI_TIME = 240;
	private static int nextLetter = 0;
	private static int konamiTime = 0;

	public static void clientTick(Minecraft client) {
		if (konamiTime > 0) {
			konamiTime--;
		}

		if (!LexicaBotaniaItem.isOpen()) {
			nextLetter = 0;
		}
	}

	public static void handleInput(int key, int action, int modifiers) {
		Minecraft mc = Minecraft.getInstance();
		if (modifiers == 0 && action == InputConstants.PRESS && LexicaBotaniaItem.isOpen()) {
			if (konamiTime == 0 && key == KONAMI_CODE[nextLetter]) {
				nextLetter++;
				if (nextLetter >= KONAMI_CODE.length) {
					mc.getSoundManager().play(SimpleSoundInstance.forUI(BotaniaSounds.way, 1.0F));
					nextLetter = 0;
					konamiTime = TOTAL_KONAMI_TIME;
				}
			} else {
				nextLetter = 0;
			}
		}
	}

	public static void renderBook(Identifier book, Screen gui, int mouseX, int mouseY, float partialTicks, GuiGraphicsExtractor guiGraphics) {
		Matrix3x2fStack ms = guiGraphics.pose();
		if (konamiTime > 0) {
			String meme = I18n.get("botania.subtitle.way");
			ms.pushMatrix();
			int fullWidth = Minecraft.getInstance().font.width(meme);
			int left = gui.width;
			double widthPerTick = (double) (fullWidth + gui.width) / TOTAL_KONAMI_TIME;
			double currWidth = left - widthPerTick * (TOTAL_KONAMI_TIME - (konamiTime - partialTicks)) * 3.2;

			ms.translate((float) currWidth, gui.height / 2 - 10);
			ms.scale(4, 4);
			guiGraphics.text(Minecraft.getInstance().font, meme, 0, 0, 0xFFFFFFFF);
			ms.popMatrix();
		}
	}
}
