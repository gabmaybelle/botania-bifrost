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
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ManaBlasterTrigger extends SimpleCriterionTrigger<ManaBlasterTrigger.Instance> {
	public static final Identifier ID = botaniaRL("fire_mana_blaster");
	public static final ManaBlasterTrigger INSTANCE = new ManaBlasterTrigger();

	private ManaBlasterTrigger() {}

	public void trigger(ServerPlayer player, ItemStack stack) {
		trigger(player, instance -> instance.test(stack, player));
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public record Instance(Optional<Holder<LootItemCondition>> player, Optional<ItemPredicate> item, Optional<EntityPredicate> user)
			implements
				SimpleInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player),
				ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item),
				EntityPredicate.CODEC.optionalFieldOf("user").forGetter(Instance::user)
		).apply(instance, Instance::new));

		public static Criterion<Instance> shoot() {
			return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		boolean test(ItemStack stack, ServerPlayer entity) {
			return (this.item.isEmpty() || this.item.get().test(stack))
					&& (this.user.isEmpty() || this.user.get().matches(entity, entity));
		}
	}
}
