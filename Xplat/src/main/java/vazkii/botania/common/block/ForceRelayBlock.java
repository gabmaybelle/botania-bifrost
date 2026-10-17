/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.common.datafix.LegacySavedData;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.ForcePushHelper;
import vazkii.botania.common.item.lens.ForceLens;
import vazkii.botania.network.clientbound.ParticleBeamEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class ForceRelayBlock extends BotaniaBlock {

	public final Map<UUID, GlobalPos> activeBindingAttempts = new HashMap<>();

	public ForceRelayBlock(Properties builder) {
		super(builder.pushReaction(PushReaction.PUSH));
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
		// PORT-26.3: this replaces onRemove; the new state is already in the world when it runs
		BlockState newState = world.getBlockState(pos);
		{
			var data = WorldData.get(world);

			Direction movementContextDirection = ForcePushHelper.getMovementContextDirection();
			if (isMoving && (movementContextDirection != null || newState.is(Blocks.MOVING_PISTON))) {
				var pistonDirection = movementContextDirection != null
						? movementContextDirection
						: newState.getValue(MovingPistonBlock.FACING);
				// if being moved as part of a retracting sticky piston's block structure, reverse movement direction
				var moveDirection = ForcePushHelper.isExtendingMovementContext() ? pistonDirection : pistonDirection.getOpposite();

				var destPos = data.mapping.get(pos);
				if (destPos != null) {
					BlockPos newSrcPos = pos.relative(moveDirection);

					{
						// Move source side of our binding along
						data.mapping.remove(pos);
						data.mapping.put(newSrcPos, destPos);
						data.setDirty();
					}

					if (!newState.is(Blocks.MOVING_PISTON) || newState.getValue(MovingPistonBlock.TYPE) == PistonType.DEFAULT) {
						// Move the actual bound blocks
						if (ForceLens.moveBlocks(world, destPos.relative(moveDirection.getOpposite()), moveDirection, pos)) {
							// Move dest side of our binding
							data.mapping.put(newSrcPos, data.mapping.get(newSrcPos).relative(moveDirection));
						}
					}
				}
			} else {
				if (data.mapping.remove(pos) != null) {
					data.setDirty();
				}
			}
		}
	}

	public static Wandable createWandable(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be, Direction side) {
		return new ForceRelayWandable(level, pos);
	}

	public record ForceRelayWandable(Level level, BlockPos pos) implements Wandable {
		@Override
		public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
			if (player == null || level.isClientSide()) {
				return false;
			}

			var data = WorldData.get(level);
			if (XplatAbstractions.INSTANCE.isDevEnvironment()) {
				BotaniaAPI.LOGGER.info("PistonRelay pairs");
				for (var e : data.mapping.entrySet()) {
					BotaniaAPI.LOGGER.info("{} -> {}", e.getKey(), e.getValue());
				}
			}
			BlockPos dest = data.mapping.get(pos);
			if (dest != null) {
				XplatAbstractions.INSTANCE.sendToNear(level, pos, new ParticleBeamEffectPacket(pos, dest));
			}

			return true;
		}
	}

	public static WandBindable createWandBindable(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be, Direction side) {
		return new ForceRelayBindable(level, pos);
	}

	public record ForceRelayBindable(Level level, BlockPos sourcePos) implements WandBindable {
		@Override
		public boolean bindTo(Player player, ItemStack wand, BlockPos targetPos, Direction side) {
			if (!level.isClientSide()) {
				addBinding(level, sourcePos, targetPos);

				XplatAbstractions.INSTANCE.sendToNear(level, targetPos,
						new ParticleBeamEffectPacket(sourcePos, targetPos));
			}
			level.playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.DING, SoundSource.PLAYERS, 1F, 1F);
			return true;
		}

	}

	public static void addBinding(Level level, BlockPos sourcePos, BlockPos targetPos) {
		if (level.isClientSide()) {
			BotaniaAPI.LOGGER.warn("Tried to bind Force Relay on client: {}/{}", sourcePos, targetPos);
			return;
		}
		WorldData data = WorldData.get(level);
		data.mapping.put(sourcePos, targetPos.immutable());
		data.setDirty();
	}

	public static class WorldData extends SavedData {

		// PORT-26.3: saved data is described by a type with a codec, and the file is named by an identifier now
		// (data/botania/piston_relay_pairs.dat; the old PistonRelayPairs name is not a valid identifier)
		private static final Identifier ID = botaniaRL("piston_relay_pairs");
		private static final String LEGACY_ID = "PistonRelayPairs";
		private static final Codec<WorldData> CODEC = BlockPos.CODEC.listOf().fieldOf("list").codec()
				.xmap(WorldData::new, WorldData::toList);
		public static final SavedDataType<WorldData> TYPE = new SavedDataType<>(ID, WorldData::new, CODEC, DataFixTypes.LEVEL);
		public final Map<BlockPos, BlockPos> mapping = new HashMap<>();

		public WorldData() {
			// initialize with empty data
		}

		private WorldData(List<BlockPos> list) {
			for (int i = 0; i < list.size(); i += 2) {
				mapping.put(list.get(i), list.get(i + 1));
			}
		}

		private List<BlockPos> toList() {
			List<BlockPos> list = new ArrayList<>();
			for (Map.Entry<BlockPos, BlockPos> e : mapping.entrySet()) {
				list.add(e.getKey());
				list.add(e.getValue());
			}
			return list;
		}

		public static WorldData get(Level world) {
			return LegacySavedData.get((ServerLevel) world, TYPE, LEGACY_ID);
		}
	}
}
