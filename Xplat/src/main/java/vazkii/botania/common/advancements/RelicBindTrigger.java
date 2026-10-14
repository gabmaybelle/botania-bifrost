/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class RelicBindTrigger extends SimpleCriterionTrigger<RelicBindTrigger.Instance> {
	public static final Identifier ID = botaniaRL("relic_bind");
	public static final RelicBindTrigger INSTANCE = new RelicBindTrigger();

	private RelicBindTrigger() {}

	public void trigger(ServerPlayer player, ItemStack relic) {
		trigger(player, instance -> instance.test(relic));
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public record Instance(Optional<Holder<LootItemCondition>> player, Optional<ItemPredicate> relic) implements SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player),
				ItemPredicate.CODEC.optionalFieldOf("relic").forGetter(Instance::relic)
		).apply(instance, Instance::new));

		public static Criterion<Instance> bound(ItemLike relicItem) {
			return INSTANCE.createCriterion(new Instance(Optional.empty(),
					Optional.of(ItemPredicate.Builder.item().of(relicItem).build())));
		}

		boolean test(ItemStack stack) {
			return this.relic.isEmpty() || this.relic.get().test(stack);
		}
	}
}
