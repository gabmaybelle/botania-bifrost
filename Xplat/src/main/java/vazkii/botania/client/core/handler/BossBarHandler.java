/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.entity.GaiaGuardianEntity;

import java.util.Collections;
import java.util.OptionalInt;
import java.util.Set;
import java.util.WeakHashMap;

public final class BossBarHandler {
	private BossBarHandler() {}

	// Only access on the client thread!
	public static final Set<GaiaGuardianEntity> bosses = Collections.newSetFromMap(new WeakHashMap<>());
	private static final Identifier BAR_TEXTURE = Identifier.parse(ResourcesLib.GUI_BOSS_BAR);

	public static OptionalInt onBarRender(GuiGraphicsExtractor gui, int x, int y, BossEvent bossEvent, boolean drawName) {
		for (GaiaGuardianEntity currentBoss : bosses) {
			if (currentBoss.getBossInfoUuid().equals(bossEvent.getId())) {
				Minecraft mc = Minecraft.getInstance();
				// todo boss_bar.png has textures for different colors, respect bossEvent's getColor()?
				int frameU = 0, frameV = 0;
				int frameWidth = 185, frameHeight = 15;
				int healthU = 0, healthV = frameV + frameHeight;
				int healthWidth = 181, healthHeight = 7;
				int healthX = x + (frameWidth - healthWidth) / 2;
				int healthY = y + (frameHeight - healthHeight) / 2;

				int playerCountHeight = drawInitialPlayerCount(currentBoss.getInitialPlayerCount(), gui, x, y);
				RenderHelper.drawTexturedModalRect(gui, BAR_TEXTURE, x, y, frameU, frameV,
						frameWidth, frameHeight);
				drawHealthBar(gui, currentBoss, healthX, healthY, healthU, healthV,
						healthWidth, healthHeight,
						bossEvent.getProgress());

				if (drawName) {
					Component name = bossEvent.getName();
					int centerX = mc.getWindow().getGuiScaledWidth() / 2;
					gui.centeredText(mc.font, name, centerX, y - 10, 0xFFA2018C);
				}

				return OptionalInt.of(frameHeight + playerCountHeight + (drawName ? mc.font.lineHeight : 0));
			}
		}
		return OptionalInt.empty();
	}

	private static int drawInitialPlayerCount(int playerCount, GuiGraphicsExtractor gui, int x, int y) {
		int px = x + 160;
		int py = y + 12;
		Minecraft mc = Minecraft.getInstance();
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		gui.item(stack, px, py);
		gui.text(mc.font, Integer.toString(playerCount), px + 15, py + 4, 0xFFFFFFFF);
		return 5;
	}

	private static void drawHealthBar(GuiGraphicsExtractor gui, GaiaGuardianEntity currentBoss, int x, int y, int u, int v, int w, int h, float progress) {
		// PORT-26.3: the grain and health fraction were shader uniforms; the GUI pipeline reads them off the tint
		// (red = grain, alpha = health, see CoreShaders.GAIA_BOSS_BAR). Without the shader the bar is a plain blit.
		RenderPipeline shader = CoreShaders.gaiaBossBar();
		int barW = (int) (w * progress);
		if (shader != null) {
			float time = currentBoss.getInvulTime();
			float grainIntensity = time > 20 ? 1F : Math.max(currentBoss.isHardMode() ? 0.5F : 0F, time / 20F);
			gui.blit(shader, BAR_TEXTURE, x, y, u, v, barW, h, 256, 256, ARGB.colorFromFloat(progress, grainIntensity, 1F, 1F));
		} else {
			RenderHelper.drawTexturedModalRect(gui, BAR_TEXTURE, x, y, u, v, barW, h);
		}
	}
}
