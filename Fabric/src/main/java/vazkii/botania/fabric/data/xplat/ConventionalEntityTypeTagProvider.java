/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.fabric.data.xplat;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityTypes;

import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.EntityTagProvider;

import java.util.concurrent.CompletableFuture;

public class ConventionalEntityTypeTagProvider extends EntityTagProvider {
	public ConventionalEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(output, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		// PORT-26.3: vanilla names tag providers by registry (final), so the conventional tags ride on Botania's own provider
		super.addTags(wrapperLookup);
		tag(ConventionalEntityTypeTags.BOSSES).add(BotaniaEntities.GAIA_GUARDIAN);

		tag(BotaniaTags.Entities.NOT_CHARMABLE)
				.add(EntityTypes.WARDEN)
				.addTag(ConventionalEntityTypeTags.BOSSES);

		tag(ConventionalEntityTypeTags.MINECARTS).add(BotaniaEntities.MANA_POOL_MINECART);
	}

}
