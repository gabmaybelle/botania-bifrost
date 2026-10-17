/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import vazkii.botania.test.block.*;
import vazkii.botania.test.item.*;
import vazkii.botania.test.item.lens.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: game tests are registry entries since 1.21.5: a test function (code, in a built-in registry) and a
 * test instance (structure, timeout, environment, in a data-driven registry). This turns the test classes'
 * {@link BotaniaGameTest}, {@link BotaniaGameTestGenerator}, {@link BotaniaBeforeBatch} and {@link BotaniaAfterBatch}
 * methods into those entries, the way vanilla's old annotation scanner did. Each batch becomes a
 * {@link BotaniaTestEnvironment}, which runs the batch's before and after methods. The loaders call this only when
 * the game tests run, so the tests never show up in a normal game.
 */
public final class BotaniaGameTests {
	public static final List<Class<?>> TEST_CLASSES = List.of(
			ApothecaryRecipeTest.class,
			BindingTest.class,
			DandelifeonTest.class,
			DrumBlockTest.class,
			EntropinnyumUnethicalTntDetectionTest.class,
			ForceRelayTest.class,
			HopperhockTest.class,
			IdleFlowerTickingTest.class,
			InterceptorTest.class,
			ManaSpreaderTest.class,
			MultiblockTest.class,
			OpenCrateTest.class,
			RannuncarpusTest.class,
			SaveReloadTest.class,
			TargetBlockTest.class,
			AstrolabeTest.class,
			CacophoniumTest.class,
			FlowerPouchTest.class,
			GrassSeedDispensingTest.class,
			LifeAggregatorTest.class,
			RarityConsistencyTest.class,
			SpectatorScanTest.class,
			AccessorySlotTest.class,
			BoreLensTest.class,
			BoreWarpLensTest.class,
			EntropicWarpLensTest.class,
			PaintslingerLensTest.class,
			WarpForceTest.class,
			TranslationKeysTest.class
	);

	private static final Map<String, List<Consumer<ServerLevel>>> BEFORE_BATCH = new HashMap<>();
	private static final Map<String, List<Consumer<ServerLevel>>> AFTER_BATCH = new HashMap<>();
	private static final Supplier<List<BotaniaTestFunction>> FUNCTIONS = Suppliers.memoize(BotaniaGameTests::collect);

	private BotaniaGameTests() {}

	/** Registers the environment type; call while the built-in registries are still open. */
	public static void registerEnvironmentType(Registry<MapCodec<? extends TestEnvironmentDefinition<?>>> registry) {
		Registry.register(registry, botaniaRL("batch"), BotaniaTestEnvironment.CODEC);
	}

	/** Registers every test function; call after Botania's blocks and items are registered. */
	public static void registerFunctions(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> register) {
		for (BotaniaTestFunction function : FUNCTIONS.get()) {
			register.accept(functionKey(function), function.function());
		}
	}

	/** Registers a test instance for every test function (the test instance registry is filled as a world loads). */
	public static void registerInstances(BiConsumer<Identifier, GameTestInstance> register) {
		for (BotaniaTestFunction function : FUNCTIONS.get()) {
			Holder<TestEnvironmentDefinition<?>> environment = Holder.direct(new BotaniaTestEnvironment(function.batchName()));
			TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(environment, Level.OVERWORLD,
					Identifier.parse(function.structureName()), function.maxTicks(), (int) function.setupTicks(),
					function.required(), Rotation.NONE, false, 1, 1, false, 0);
			register.accept(functionKey(function).identifier(), new FunctionGameTestInstance(functionKey(function), data));
		}
	}

	static void runBatchHooks(String batch, boolean before, ServerLevel level) {
		(before ? BEFORE_BATCH : AFTER_BATCH).getOrDefault(batch, List.of()).forEach(hook -> hook.accept(level));
	}

	private static ResourceKey<Consumer<GameTestHelper>> functionKey(BotaniaTestFunction function) {
		return ResourceKey.create(Registries.TEST_FUNCTION, botaniaRL(function.testName().toLowerCase(Locale.ROOT)));
	}

	private static List<BotaniaTestFunction> collect() {
		List<BotaniaTestFunction> functions = new ArrayList<>();
		for (Class<?> testClass : TEST_CLASSES) {
			for (Method method : testClass.getDeclaredMethods()) {
				BotaniaGameTest test = method.getAnnotation(BotaniaGameTest.class);
				if (test != null) {
					String name = testClass.getSimpleName() + "." + method.getName();
					functions.add(new BotaniaTestFunction(test.batch(), name, test.template(), test.timeoutTicks(),
							test.setupTicks(), test.required(), helper -> invoke(testClass, method, helper)));
				}
				if (method.isAnnotationPresent(BotaniaGameTestGenerator.class)) {
					@SuppressWarnings("unchecked")
					Collection<BotaniaTestFunction> generated = (Collection<BotaniaTestFunction>) invoke(testClass, method);
					functions.addAll(generated);
				}
				BotaniaBeforeBatch before = method.getAnnotation(BotaniaBeforeBatch.class);
				if (before != null) {
					BEFORE_BATCH.computeIfAbsent(before.batch(), b -> new ArrayList<>()).add(level -> invoke(testClass, method, level));
				}
				BotaniaAfterBatch after = method.getAnnotation(BotaniaAfterBatch.class);
				if (after != null) {
					AFTER_BATCH.computeIfAbsent(after.batch(), b -> new ArrayList<>()).add(level -> invoke(testClass, method, level));
				}
			}
		}
		return List.copyOf(functions);
	}

	// Like vanilla's old scanner: a fresh instance of the test class for each call, and the test's own exception
	// (usually a failed assertion) passed on unwrapped, so the test report shows it rather than the reflection wrapper
	@SuppressWarnings("PMD.PreserveStackTrace")
	private static Object invoke(Class<?> testClass, Method method, Object... args) {
		try {
			return method.invoke(testClass.getConstructor().newInstance(), args);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException runtime) {
				throw runtime;
			}
			throw new RuntimeException(e);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
