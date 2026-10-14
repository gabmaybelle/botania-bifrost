/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PORT-26.3: vanilla's {@code @GameTest} annotation is gone; since 1.21.5 game tests are data-driven
 * test instances. This annotation carries the same information vanilla's did, with the same defaults,
 * so the test classes stay as upstream wrote them. Each loader turns these into test instances when the
 * tests are registered (see the loaders' game test registration).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BotaniaGameTest {
	int DEFAULT_TIMEOUT_TICKS = 100;
	String DEFAULT_BATCH = "defaultBatch";

	String template();

	String batch() default DEFAULT_BATCH;

	int timeoutTicks() default DEFAULT_TIMEOUT_TICKS;

	long setupTicks() default 0L;

	boolean required() default true;
}
