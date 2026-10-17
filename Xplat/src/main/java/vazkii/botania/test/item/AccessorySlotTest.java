/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test.item;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.test.BotaniaGameTest;
import vazkii.botania.test.TestingUtil;

import java.util.function.BiConsumer;
import java.util.function.ToDoubleBiFunction;

// PORT-26.3: checks the accessory integration (Trinkets Updated on Fabric, Curios on NeoForge, both in the dev runs):
// a ring put in an accessory slot is found there by Botania, and its attribute bonus applies.
public class AccessorySlotTest {
	/**
	 * Puts a ring into the player's accessory slot. By default it right-clicks it, which Trinkets Updated turns into
	 * an equip. Curios equips on right-click only through a server player, whose mock connection can't take Curios'
	 * sync payloads, so the NeoForge game test setup replaces this with a direct Curios equip.
	 */
	public static BiConsumer<Player, ItemStack> equip = (player, stack) -> {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		stack.use(player.level(), player, InteractionHand.MAIN_HAND);
	};

	/**
	 * The player's block reach with the ring worn. By default the player is ticked the way the server would (Trinkets
	 * Updated applies slot modifiers from the entity's own tick) and the attribute is read. Curios skips its slot tick
	 * on a game test server, so the NeoForge game test setup adds the modifiers Curios would apply instead.
	 */
	public static ToDoubleBiFunction<Player, ItemStack> wornReach = (player, stack) -> {
		player.tick();
		player.tick();
		return player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
	};

	@BotaniaGameTest(template = TestingUtil.EMPTY_STRUCTURE)
	public void testRingEquipsIntoAccessorySlot(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		double baseReach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
		ItemStack ring = new ItemStack(BotaniaItems.RING_OF_FAR_REACH);
		equip.accept(player, ring);

		ItemStack worn = EquipmentHandler.findOrEmpty(BotaniaItems.RING_OF_FAR_REACH, player);
		TestingUtil.assertThat(!worn.isEmpty(), () -> "The ring isn't in an accessory slot");
		TestingUtil.assertThat(EquipmentHandler.getAllWorn(player).countItem(BotaniaItems.RING_OF_FAR_REACH) == 1,
				() -> "The worn accessories don't list the ring once");
		double reach = wornReach.applyAsDouble(player, worn);
		TestingUtil.assertEquals(reach, baseReach + 3.5,
				() -> "Expected block reach " + (baseReach + 3.5) + " with the ring worn, was " + reach);
		helper.succeed();
	}
}
