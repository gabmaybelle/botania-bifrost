/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.util.IntrinsicTagsProvider;

import java.util.concurrent.CompletableFuture;

public class EntityTagProvider extends IntrinsicTagsProvider<EntityType<?>> {
	public EntityTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, Registries.ENTITY_TYPE, lookupProvider,
				entityType -> BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).orElseThrow());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(BotaniaTags.Entities.COCOON_COMMON).add(
				EntityTypes.PIG, EntityTypes.COW, EntityTypes.CHICKEN, EntityTypes.RABBIT, EntityTypes.SHEEP
		);
		tag(BotaniaTags.Entities.COCOON_RARE)
				.add(EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.WOLF, EntityTypes.OCELOT,
						EntityTypes.CAT, EntityTypes.PARROT, EntityTypes.LLAMA, EntityTypes.FOX,
						EntityTypes.PANDA, EntityTypes.TURTLE, EntityTypes.GOAT, EntityTypes.CAMEL,
						EntityTypes.ARMADILLO);

		tag(BotaniaTags.Entities.COCOON_COMMON_AQUATIC)
				.add(EntityTypes.COD, EntityTypes.SALMON, EntityTypes.TROPICAL_FISH, EntityTypes.PUFFERFISH, EntityTypes.SQUID, EntityTypes.FROG)
				.addOptional(Identifier.fromNamespaceAndPath("quark", "crab"));
		tag(BotaniaTags.Entities.COCOON_RARE_AQUATIC).add(EntityTypes.DOLPHIN, EntityTypes.GLOW_SQUID, EntityTypes.AXOLOTL);

		tag(BotaniaTags.Entities.DRUM_MILKABLE).add(EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.GOAT);
		tag(BotaniaTags.Entities.DRUM_NO_SHEARING).add(EntityTypes.MOOSHROOM);

		tag(BotaniaTags.Entities.SHADED_MESA_NO_PICKUP).add(EntityTypes.ENDER_DRAGON, EntityTypes.WITHER,
				EntityTypes.ITEM_FRAME, EntityTypes.GLOW_ITEM_FRAME, EntityTypes.END_CRYSTAL, EntityTypes.PAINTING,
				EntityTypes.COMMAND_BLOCK_MINECART, EntityTypes.MARKER, EntityTypes.AREA_EFFECT_CLOUD,
				EntityTypes.EVOKER_FANGS, EntityTypes.LEASH_KNOT, EntityTypes.BLOCK_DISPLAY, EntityTypes.ITEM_DISPLAY,
				EntityTypes.TEXT_DISPLAY, EntityTypes.INTERACTION, EntityTypes.MARKER,
				BotaniaEntities.CORPOREA_SPARK, BotaniaEntities.GAIA_GUARDIAN, BotaniaEntities.FLAME_RING, BotaniaEntities.GAIA_TRAP,
				BotaniaEntities.MAGIC_MISSILE, BotaniaEntities.MANA_BURST, BotaniaEntities.PINK_WITHER, BotaniaEntities.MANA_SPARK, BotaniaEntities.LUMINIZER_BEAM
		);

		tag(BotaniaTags.Entities.KEY_IMMUNE).add(EntityTypes.ITEM, EntityTypes.ITEM_FRAME, EntityTypes.GLOW_ITEM_FRAME,
				EntityTypes.PAINTING, EntityTypes.EXPERIENCE_ORB);
		tag(BotaniaTags.Entities.PORTAL_BREAD_IMMUNE).add(EntityTypes.ITEM, EntityTypes.EXPERIENCE_ORB);

		tag(BotaniaTags.Entities.ENDER_ESSENCE_CLOUDS)
				.add(BotaniaEntities.DILUTED_ENDER_ESSENCE_CLOUD, BotaniaEntities.PURE_ENDER_ESSENCE_CLOUD);
	}
}
