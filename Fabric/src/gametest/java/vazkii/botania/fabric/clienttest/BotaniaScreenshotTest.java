package vazkii.botania.fabric.clienttest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;

import vazkii.patchouli.api.PatchouliAPI;

/**
 * PORT-26.3: walks a real client through Botania's screens, renderers and HUD and takes a screenshot of
 * each, so rendering bugs (invisible text, missing textures, crashes inside a render pass) show up
 * without anyone playing. Run with `./gradlew :Fabric:runClientGameTest`; screenshots land in
 * run/clientGameTest/screenshots (CI uploads them).
 */
public class BotaniaScreenshotTest implements FabricClientGameTest {
	private static final String[] HOTBAR = {
			"wand_of_the_forest", "lexica_botania", "mana_tablet", "ring_of_magnetization",
			"terra_blade", "mana_spreader", "mana_pool", "pure_daisy", "runic_altar"
	};

	@Override
	public void runTest(ClientGameTestContext ctx) {
		try (TestSingleplayerContext world = ctx.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			server.runCommand("time set day");
			server.runCommand("weather clear");
			server.runCommand("gamerule doDaylightCycle false");
			server.runCommand("gamerule doWeatherCycle false");
			server.runCommand("gamerule doMobSpawning false");
			server.runCommand("gamemode creative @a");
			server.runCommand("clear @a");

			buildScene(server);
			server.runCommand("tp @a 0 101 0 facing 0 101 6");
			world.getConnection().waitForChunksRender();
			ctx.waitTicks(40);

			hotbar(ctx, server);
			ctx.takeScreenshot("botania_scene");

			wandHud(ctx, server);
			lexica(ctx);
			inventory(ctx);
		}
	}

	/** A stone platform with a mana pool, spreaders, an apothecary, a runic altar and flowers. */
	private static void buildScene(TestServerContext server) {
		server.runCommand("fill -8 100 -2 8 100 12 minecraft:stone");
		server.runCommand("fill -8 101 -2 8 106 12 minecraft:air");
		server.runCommand("setblock 0 101 6 botania:creative_mana_pool");
		server.runCommand("setblock -3 101 6 botania:mana_spreader");
		server.runCommand("setblock 3 101 6 botania:pulse_mana_spreader");
		server.runCommand("setblock -2 101 9 botania:petal_apothecary");
		server.runCommand("setblock 2 101 9 botania:runic_altar");
		server.runCommand("setblock 0 101 9 botania:terrestrial_agglomeration_plate");
		server.runCommand("setblock -4 100 3 minecraft:grass_block");
		server.runCommand("setblock -4 101 3 botania:pure_daisy");
		server.runCommand("setblock 4 100 3 minecraft:grass_block");
		server.runCommand("setblock 4 101 3 botania:endoflame");
	}

	/** Botania item icons in the hotbar. */
	private static void hotbar(ClientGameTestContext ctx, TestServerContext server) {
		for (int i = 0; i < HOTBAR.length; i++) {
			server.runCommand("item replace entity @a hotbar." + i + " with botania:" + HOTBAR[i]);
		}
		ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
		ctx.waitTicks(10);
	}

	/** Holding the Wand of the Forest while looking at a pool draws the mana bar HUD. */
	private static void wandHud(ClientGameTestContext ctx, TestServerContext server) {
		server.runCommand("tp @a 0 101 3 facing 0 101.3 6.5");
		ctx.waitTicks(20);
		ctx.takeScreenshot("botania_wand_hud");
	}

	/** The Lexica Botania opens as a Patchouli book. */
	private static void lexica(ClientGameTestContext ctx) {
		ctx.runOnClient(mc -> PatchouliAPI.get().openBookGUI(Identifier.fromNamespaceAndPath("botania", "lexica_botania")));
		ctx.waitTicks(20);
		String screen = ctx.computeOnClient(mc -> mc.gui.screen() == null ? "none" : mc.gui.screen().getClass().getName());
		if (!screen.startsWith("vazkii.patchouli")) {
			throw new AssertionError("the Lexica did not open, the screen is " + screen);
		}
		ctx.takeScreenshot("botania_lexica");
		ctx.setScreen(() -> null);
		ctx.waitTicks(5);
	}

	/** The inventory, with the accessory slots and the recipe viewer's item list beside it. */
	private static void inventory(ClientGameTestContext ctx) {
		ctx.setScreen(() -> new InventoryScreen(ctx.computeOnClient(mc -> mc.player)));
		ctx.waitTicks(20);
		ctx.takeScreenshot("botania_inventory");
		ctx.setScreen(() -> null);
	}
}
