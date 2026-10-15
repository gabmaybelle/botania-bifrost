/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.SyncedRecipes;

import java.util.ArrayList;
import java.util.List;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: Fabric has no recipe sync of its own, so the server sends each player its copy of the recipe types in
 * {@link BotaniaRecipeTypes#SYNCED_TYPES} on join and after a reload (see {@link SyncedRecipes}): one type per
 * packet, in slices that stay well under the payload size limit. NeoForge does the same through its datapack sync
 * event, so this packet is Fabric-only.
 */
public record UpdateRecipesPacket(RecipeType<?> recipeType, boolean append, List<RecipeHolder<?>> recipes) implements CustomPacketPayload {

	public static final Type<UpdateRecipesPacket> ID = new Type<>(botaniaRL("update_recipes"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRecipesPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.registry(Registries.RECIPE_TYPE), UpdateRecipesPacket::recipeType,
			ByteBufCodecs.BOOL, UpdateRecipesPacket::append,
			RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateRecipesPacket::recipes,
			UpdateRecipesPacket::new);
	private static final int RECIPES_PER_PACKET = 256;

	@Override
	public Type<UpdateRecipesPacket> type() {
		return ID;
	}

	public static void sendAll(ServerPlayer player) {
		RecipeManager manager = BotaniaRecipeTypes.recipeManager(player.level());
		for (RecipeType<?> type : BotaniaRecipeTypes.SYNCED_TYPES) {
			List<RecipeHolder<?>> recipes = new ArrayList<>(BotaniaRecipeTypes.getAllRecipesFor(manager, type));
			int from = 0;
			do {
				int to = Math.min(from + RECIPES_PER_PACKET, recipes.size());
				ServerPlayNetworking.send(player, new UpdateRecipesPacket(type, from > 0, recipes.subList(from, to)));
				from = to;
			} while (from < recipes.size());
		}
	}

	public static class Handler {
		public static void handle(UpdateRecipesPacket packet, Player player) {
			SyncedRecipes.update(packet.recipeType(), packet.recipes(), packet.append());
		}
	}
}
