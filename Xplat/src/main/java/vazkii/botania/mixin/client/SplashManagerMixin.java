/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.client.integration.shared.LocaleHelper;
import vazkii.botania.common.block.block_entity.TinyPotatoBlockEntity;
import vazkii.botania.xplat.BotaniaConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle Botania's splash texts.
 *
 * @see BotaniaConfig.ClientConfigAccess#splashesEnabled()
 */
@Mixin(SplashManager.class)
public class SplashManagerMixin {
	// PORT-26.3: the splash list is an immutable list of components now, replaced on every resource reload
	@Shadow
	private List<Component> splashes;

	@Shadow
	private static Component literalSplash(String text) {
		throw new AssertionError();
	}

	@Unique
	private boolean botania_alreadyAdded;

	@Inject(
		method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At("HEAD")
	)
	private void resetSelf(List<Component> object, ResourceManager resourceManager, ProfilerFiller profiler,
			CallbackInfo ci) {
		botania_alreadyAdded = false;
	}

	// PORT-26.3: vanilla no longer reads a Calendar in getSplash (it uses SpecialDates), so hook the start of it
	@Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
	private void handleBotaniaSplashes(CallbackInfoReturnable<SplashRenderer> cir) {
		var config = BotaniaConfig.client();
		if (config == null || !config.splashesEnabled()) {
			return;
		}

		// override splash for tater's birthday
		LocalDate today = LocalDate.now();
		if (today.getMonth() == TinyPotatoBlockEntity.BIRTHDAY.getMonth()
				&& today.getDayOfMonth() == TinyPotatoBlockEntity.BIRTHDAY.getDayOfMonth()) {
			int age = today.getYear() - TinyPotatoBlockEntity.BIRTHDAY.getYear();
			cir.setReturnValue(new SplashRenderer(literalSplash("Happy %s birthday, Tiny Potato!"
					.formatted(LocaleHelper.formatAsEnglishOrdinal(age)))));
			return;
		}

		// otherwise add our regular splashes to the list, if that didn't already happen
		if (botania_alreadyAdded) {
			return;
		}
		botania_addSplashes();
		botania_alreadyAdded = true;
	}

	@Unique
	private void botania_addSplashes() {
		List<Component> list = new ArrayList<>(splashes);
		for (String splash : List.of(
				"Do not feed bread to elves!",
				"Drum wild!",
				"Edible textures!",
				"Music by Firel!"
		)) {
			list.add(literalSplash(splash));
		}
		splashes = List.copyOf(list);
	}
}
