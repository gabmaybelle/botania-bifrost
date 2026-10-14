/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import vazkii.botania.network.clientbound.ArenaIndicatorEffectPacket;
import vazkii.botania.network.clientbound.AvatarSkiesRodEffectPacket;
import vazkii.botania.network.clientbound.AvatarSkiesRodUpdatePacket;
import vazkii.botania.network.clientbound.BlackLotusDissolveEffectPacket;
import vazkii.botania.network.clientbound.DivaCharmEffectPacket;
import vazkii.botania.network.clientbound.EnchanterDestroyEffectPacket;
import vazkii.botania.network.clientbound.FlowerTakeItemEffectPacket;
import vazkii.botania.network.clientbound.FlugelEyeEffectPacket;
import vazkii.botania.network.clientbound.GogWorldPacket;
import vazkii.botania.network.clientbound.GrassSeedsEffectPacket;
import vazkii.botania.network.clientbound.HaloCraftEffectPacket;
import vazkii.botania.network.clientbound.ItemLifeTimePacket;
import vazkii.botania.network.clientbound.ItemSmokeEffectPacket;
import vazkii.botania.network.clientbound.PaintLensEffectPacket;
import vazkii.botania.network.clientbound.ParticleBeamEffectPacket;
import vazkii.botania.network.clientbound.RodOfThePlentifulMantleEffectPacket;
import vazkii.botania.network.clientbound.RodOfTheUnstableReservoirEffectPacket;
import vazkii.botania.network.clientbound.SparkManaFlowEffectPacket;
import vazkii.botania.network.clientbound.SparkNetIndicatorEffectPacket;
import vazkii.botania.network.clientbound.ThundercallerEffectPacket;
import vazkii.botania.network.clientbound.UpdateItemsRemainingPacket;
import vazkii.botania.network.serverbound.DodgePacket;
import vazkii.botania.network.serverbound.IndexKeybindRequestPacket;
import vazkii.botania.network.serverbound.IndexStringRequestPacket;
import vazkii.botania.network.serverbound.JumpPacket;
import vazkii.botania.network.serverbound.LeftClickPacket;

import java.util.function.BiConsumer;

public final class FabricPacketHandler {
	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(DodgePacket.ID, DodgePacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(IndexKeybindRequestPacket.ID, IndexKeybindRequestPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(IndexStringRequestPacket.ID, IndexStringRequestPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(JumpPacket.ID, JumpPacket.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(LeftClickPacket.ID, LeftClickPacket.STREAM_CODEC);

		PayloadTypeRegistry.clientboundPlay().register(ArenaIndicatorEffectPacket.ID, ArenaIndicatorEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(AvatarSkiesRodUpdatePacket.ID, AvatarSkiesRodUpdatePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(AvatarSkiesRodEffectPacket.ID, AvatarSkiesRodEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(BlackLotusDissolveEffectPacket.ID, BlackLotusDissolveEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(DivaCharmEffectPacket.ID, DivaCharmEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EnchanterDestroyEffectPacket.ID, EnchanterDestroyEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FlowerTakeItemEffectPacket.ID, FlowerTakeItemEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FlowerTakeItemEffectPacket.FIRE_ID, FlowerTakeItemEffectPacket.STREAM_CODEC_FIRE);
		PayloadTypeRegistry.clientboundPlay().register(FlugelEyeEffectPacket.ID, FlugelEyeEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GogWorldPacket.ID, GogWorldPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GrassSeedsEffectPacket.ID, GrassSeedsEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HaloCraftEffectPacket.ID, HaloCraftEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ItemLifeTimePacket.ID, ItemLifeTimePacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ItemSmokeEffectPacket.ID, ItemSmokeEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(PaintLensEffectPacket.ID, PaintLensEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ParticleBeamEffectPacket.ID, ParticleBeamEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RodOfThePlentifulMantleEffectPacket.ID, RodOfThePlentifulMantleEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RodOfTheUnstableReservoirEffectPacket.ID, RodOfTheUnstableReservoirEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SparkManaFlowEffectPacket.ID, SparkManaFlowEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SparkNetIndicatorEffectPacket.ID, SparkNetIndicatorEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ThundercallerEffectPacket.ID, ThundercallerEffectPacket.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(UpdateItemsRemainingPacket.ID, UpdateItemsRemainingPacket.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(DodgePacket.ID, makeServerBoundHandler(DodgePacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(IndexKeybindRequestPacket.ID, makeServerBoundHandler(IndexKeybindRequestPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(IndexStringRequestPacket.ID, makeServerBoundHandler(IndexStringRequestPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(JumpPacket.ID, makeServerBoundHandler(JumpPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(LeftClickPacket.ID, makeServerBoundHandler(LeftClickPacket::handle));
	}

	private static <T extends CustomPacketPayload> ServerPlayNetworking.PlayPayloadHandler<T> makeServerBoundHandler(BiConsumer<T, ServerPlayer> handle) {
		return (payload, context) -> handle.accept(payload, context.player());
	}

	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(ArenaIndicatorEffectPacket.ID, makeClientBoundHandler(ArenaIndicatorEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(AvatarSkiesRodUpdatePacket.ID, makeClientBoundHandler(AvatarSkiesRodUpdatePacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(AvatarSkiesRodEffectPacket.ID, makeClientBoundHandler(AvatarSkiesRodEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(BlackLotusDissolveEffectPacket.ID, makeClientBoundHandler(BlackLotusDissolveEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(DivaCharmEffectPacket.ID, makeClientBoundHandler(DivaCharmEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(EnchanterDestroyEffectPacket.ID, makeClientBoundHandler(EnchanterDestroyEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(FlowerTakeItemEffectPacket.ID, makeClientBoundHandler(FlowerTakeItemEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(FlowerTakeItemEffectPacket.FIRE_ID, makeClientBoundHandler(FlowerTakeItemEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(FlugelEyeEffectPacket.ID, makeClientBoundHandler(FlugelEyeEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(GogWorldPacket.ID, makeClientBoundHandler(GogWorldPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(GrassSeedsEffectPacket.ID, makeClientBoundHandler(GrassSeedsEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(HaloCraftEffectPacket.ID, makeClientBoundHandler(HaloCraftEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ItemLifeTimePacket.ID, makeClientBoundHandler(ItemLifeTimePacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ItemSmokeEffectPacket.ID, makeClientBoundHandler(ItemSmokeEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(PaintLensEffectPacket.ID, makeClientBoundHandler(PaintLensEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ParticleBeamEffectPacket.ID, makeClientBoundHandler(ParticleBeamEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(RodOfThePlentifulMantleEffectPacket.ID, makeClientBoundHandler(RodOfThePlentifulMantleEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(
				RodOfTheUnstableReservoirEffectPacket.ID, makeClientBoundHandler(RodOfTheUnstableReservoirEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(SparkManaFlowEffectPacket.ID, makeClientBoundHandler(SparkManaFlowEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(SparkNetIndicatorEffectPacket.ID, makeClientBoundHandler(SparkNetIndicatorEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ThundercallerEffectPacket.ID, makeClientBoundHandler(ThundercallerEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(UpdateItemsRemainingPacket.ID, makeClientBoundHandler(UpdateItemsRemainingPacket.Handler::handle));
	}

	private static <T extends CustomPacketPayload> ClientPlayNetworking.PlayPayloadHandler<T> makeClientBoundHandler(BiConsumer<T, Player> handler) {
		return (payload, context) -> handler.accept(payload, context.player());
	}

	private FabricPacketHandler() {}

}
