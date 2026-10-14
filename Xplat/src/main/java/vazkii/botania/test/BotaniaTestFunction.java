/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Consumer;

/**
 * PORT-26.3: stands in for vanilla's removed {@code TestFunction} (the seven-argument form upstream used),
 * for tests made by a {@link BotaniaGameTestGenerator}.
 */
public record BotaniaTestFunction(String batchName, String testName, String structureName, int maxTicks, long setupTicks,
		boolean required, Consumer<GameTestHelper> function) {
}
