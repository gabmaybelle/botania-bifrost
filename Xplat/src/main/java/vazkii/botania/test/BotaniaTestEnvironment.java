/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;

/**
 * PORT-26.3: tests are batched by their environment now, so each of Botania's batches is an environment of its own,
 * and the batch's {@link BotaniaBeforeBatch} and {@link BotaniaAfterBatch} methods run as its setup and teardown.
 */
public record BotaniaTestEnvironment(String batch) implements TestEnvironmentDefinition<Unit> {
	public static final MapCodec<BotaniaTestEnvironment> CODEC = Codec.STRING.fieldOf("batch")
			.xmap(BotaniaTestEnvironment::new, BotaniaTestEnvironment::batch);

	@Override
	public Unit setup(ServerLevel level) {
		BotaniaGameTests.runBatchHooks(batch, true, level);
		return Unit.INSTANCE;
	}

	@Override
	public void teardown(ServerLevel level, Unit saveData) {
		BotaniaGameTests.runBatchHooks(batch, false, level);
	}

	@Override
	public MapCodec<BotaniaTestEnvironment> codec() {
		return CODEC;
	}
}
