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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.util.IntrinsicTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ConventionalEntityTypeTagProvider extends IntrinsicTagsProvider<EntityType<?>> {
	public ConventionalEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(output, Registries.ENTITY_TYPE, completableFuture,
				entityType -> BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).orElseThrow());
	}

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		tag(ConventionalEntityTypeTags.BOSSES).add(BotaniaEntities.GAIA_GUARDIAN);

		tag(BotaniaTags.Entities.NOT_CHARMABLE)
				.add(EntityTypes.WARDEN)
				.addTag(ConventionalEntityTypeTags.BOSSES);

		tag(ConventionalEntityTypeTags.MINECARTS).add(BotaniaEntities.MANA_POOL_MINECART);
	}

}
