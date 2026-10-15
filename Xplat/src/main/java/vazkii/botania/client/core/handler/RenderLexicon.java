/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.model.BotaniaBookModel;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.LexicaBotaniaItem;

import java.util.ArrayList;
import java.util.List;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

// Hacky way to render 3D lexicon, will be reevaluated in the future.
// PORT-26.3: drawn by LexiconSpecialRenderer from the item definition; the model is posed through its render state
// and the text is submitted to the collector.
public class RenderLexicon {
	private static final boolean SHOULD_MISSPELL = Math.random() < 0.004;
	// PORT-26.3: the 3D book textures sit in the block atlas now that items have their own atlas
	public static final SpriteId TEXTURE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, botaniaRL("block/lexica_botania_3d"));
	public static final SpriteId ELVEN_TEXTURE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, botaniaRL("block/lexica_botania_elven_3d"));

	private static final String[] QUOTES = new String[] {
			"\"Neat!\" - Direwolf20",
			"\"It's pretty ledge.\" - Haighyorkie",
			"\"I don't really like it.\" - CrustyMustard",
			"\"It's a very thinky mod.\" - AdamG3691",
			"\"You must craft the tiny potato.\" - TheFractangle",
			"\"Vazkii did a thing.\" - cpw"
	};
	private static final String[] MISSPELLINGS = {
			"Bonito", "Bonita", "Bonitia", "Botnaia", "Bontonio",
			"Botnia", "Bonitaaaaaaaaaa", "Botonio", "Botonia", "Botnetia",
			"Banana", "Brotania", "Botanica", "Boat", "Batania", "Bosnia"
	};
	private static int quote = -1;
	private static int misspelling = -1;

	public static void render(BotaniaBookModel model, SpriteGetter sprites, ItemStack stack, boolean leftHanded, PoseStack ms, SubmitNodeCollector collector, int light) {
		Minecraft mc = Minecraft.getInstance();
		ms.pushPose();
		float ticks = ClientTickHandler.getTicksWithLexicaOpen();
		if (!leftHanded) {
			ms.translate(0.3F + 0.02F * ticks, 0.125F + 0.01F * ticks, -0.2F - 0.035F * ticks);
			ms.rotate(VecHelper.rotateY(180F + ticks * 6));
		} else {
			ms.translate(0.1F - 0.02F * ticks, 0.125F + 0.01F * ticks, -0.2F - 0.035F * ticks);
			ms.rotate(VecHelper.rotateY(200F + ticks * 10));
		}
		ms.rotate(VecHelper.rotateZ(-0.3F + ticks * 2.85F));
		float opening = Math.clamp(ticks / 12F, 0, 1);

		float pageFlipTicks = ClientTickHandler.getPageFlipTicks();
		float pageFlip = pageFlipTicks / 5F;

		float leftPageAngle = Mth.frac(pageFlip + 0.25F) * 1.6F - 0.3F;
		float rightPageAngle = Mth.frac(pageFlip + 0.75F) * 1.6F - 0.3F;

		BotaniaBookModel.State state = new BotaniaBookModel.State(
				ClientTickHandler.getPlayerTicksInGame() + ClientTickHandler.getPartialPlayerTick(),
				Math.clamp(leftPageAngle, 0.0F, 1.0F), Math.clamp(rightPageAngle, 0.0F, 1.0F), opening);
		SpriteId mat = LexicaBotaniaItem.isElven(stack) ? ELVEN_TEXTURE : TEXTURE;
		collector.submitModel(model, state, ms, light, OverlayTexture.NO_OVERLAY, -1, mat, sprites, 0);

		if (ticks < 3) {
			Font font = Minecraft.getInstance().font;
			ms.rotate(VecHelper.rotateZ(180F));
			ms.translate(-0.30F, -0.24F, -0.07F);
			ms.scale(0.0030F, 0.0030F, -0.0030F);

			if (misspelling == -1 && mc.level != null) {
				misspelling = mc.level.getRandom().nextInt(MISSPELLINGS.length);
			}

			String title = LexicaBotaniaItem.getTitle(stack).getString();
			if (SHOULD_MISSPELL) {
				title = title.replaceAll("Botania", MISSPELLINGS[misspelling]);
			}

			text(collector, ms, font.plainSubstrByWidth(title, 80), 0, 0, 0xD69700, light);
			ms.translate(0F, 10F, 0F);
			ms.scale(0.6F, 0.6F, 0.6F);
			Component edition = LexicaBotaniaItem.getEdition().copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.BOLD);
			collector.submitText(ms, 0, 0, edition.getVisualOrderText(), false, Font.DisplayMode.NORMAL, light, 0xA07100, 0, 0);

			if (quote == -1 && mc.level != null) {
				quote = mc.level.getRandom().nextInt(QUOTES.length);
			}

			String quoteStr = QUOTES[quote];
			ms.translate(-5F, 15F, 0F);
			renderText(0, 0, 140, 0, 0x79ff92, quoteStr, ms, collector, light);
			ms.translate(8F, 110F, 0F);

			String blurb = I18n.get("botaniamisc.lexiconcover0");
			text(collector, ms, blurb, 0, 0, 0x79ff92, light);
			ms.translate(0F, 10F, 0F);
			String blurb2 = ChatFormatting.UNDERLINE + "" + ChatFormatting.ITALIC + I18n.get("botaniamisc.lexiconcover1");
			text(collector, ms, blurb2, 0, 0, 0x79ff92, light);
			ms.translate(0F, -30F, 0F);

			String authorTitle = I18n.get("botaniamisc.lexiconcover2");
			int len = font.width(authorTitle);
			text(collector, ms, authorTitle, 58 - len / 2F, -8, 0xD69700, light);
		}
		ms.popPose();
	}

	private static void text(SubmitNodeCollector collector, PoseStack ms, String str, float x, float y, int color, int light) {
		collector.submitText(ms, x, y, FormattedCharSequence.forward(str, Style.EMPTY), false, Font.DisplayMode.NORMAL, light, color, 0, 0);
	}

	@SuppressWarnings("SameParameterValue")
	private static void renderText(int x, int y, int width, int paragraphSize, int color, String unlocalizedText, PoseStack ms, SubmitNodeCollector collector, int light) {
		x += 2;
		y += 10;
		width -= 4;

		Font font = Minecraft.getInstance().font;
		String text = I18n.get(unlocalizedText).replace("&", "\u00a7");
		String[] textEntries = text.split("<br>");

		List<List<String>> lines = new ArrayList<>();

		String controlCodes;
		for (String s : textEntries) {
			List<String> words = new ArrayList<>();
			String lineStr = "";
			String[] tokens = s.split(" ");
			for (String token : tokens) {
				String prev = lineStr;
				String spaced = token + " ";
				lineStr += spaced;

				controlCodes = toControlCodes(getControlCodes(prev));
				if (font.width(lineStr) > width) {
					lines.add(words);
					lineStr = controlCodes + spaced;
					words = new ArrayList<>();
				}

				words.add(controlCodes + token);
			}

			if (!lineStr.isEmpty()) {
				lines.add(words);
			}
			lines.add(new ArrayList<>());
		}

		for (List<String> words : lines) {
			int xi = x;
			int spacing = 4;
			for (String s : words) {
				int extra = 0;
				text(collector, ms, s, xi, y, color, light);
				xi += font.width(s) + spacing + extra;
			}

			y += words.isEmpty() ? paragraphSize : 10;
		}
	}

	private static String getControlCodes(String s) {
		String controls = s.replaceAll("(?<!\u00a7)(.)", "");
		return controls.replaceAll(".*r", "r");
	}

	private static String toControlCodes(String s) {
		return s.replaceAll(".", "\u00a7$0");
	}
}
