/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import vazkii.botania.common.item.BotaniaItems;

public class EnderEssenceCloudEntity extends Entity {
	private static final String TAG_AGE = "Age";
	private static final String TAG_FROM_ENDERMAN = "FromEnderman";
	private static final int MAX_AGE = 3 * 20;
	private static final int MAX_AGE_ENDERMAN = 5 * 20;
	private static final float TWO_PI = (float) (2 * Math.PI);

	/**
	 * Pure and diluted clouds are created as separate entity types,
	 * but we implement them with a single class and provide a distinction via this flag.
	 */
	private final boolean diluted;
	private boolean fromEnderman;

	public static EnderEssenceCloudEntity createPure(EntityType<?> entityType, Level level) {
		return new EnderEssenceCloudEntity(entityType, level, false);
	}

	public static EnderEssenceCloudEntity createDiluted(EntityType<?> entityType, Level level) {
		return new EnderEssenceCloudEntity(entityType, level, true);
	}

	public EnderEssenceCloudEntity(EntityType<?> entityType, Level level, boolean diluted) {
		super(entityType, level);
		this.diluted = diluted;
	}

	public static void spawnForEnderman(LivingEntity entity) {
		EntityType<EnderEssenceCloudEntity> type = entity.level().dimension() == Level.END
				? BotaniaEntities.PURE_ENDER_ESSENCE_CLOUD
				: BotaniaEntities.DILUTED_ENDER_ESSENCE_CLOUD;
		EnderEssenceCloudEntity cloud = type.create(entity.level(), EntitySpawnReason.EVENT);
		if (cloud != null) {
			cloud.snapTo(entity.position(), entity.getYRot(), 0);
			cloud.setFromEnderman(true);
			entity.level().addFreshEntity(cloud);
		}
	}

	public boolean isDiluted() {
		return diluted;
	}

	public boolean isFromEnderman() {
		return fromEnderman;
	}

	public void setFromEnderman(boolean fromEnderman) {
		this.fromEnderman = fromEnderman;
		refreshDimensions();
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide()) {
			if (tickCount > (isFromEnderman() ? MAX_AGE_ENDERMAN : MAX_AGE) || isInWaterOrRain()) {
				discard();
			}
		} else {
			// heavily based on AreaEffectCloud::tick
			int particleCount;
			float particleRadius;
			if (fromEnderman) {
				particleCount = 7;
				particleRadius = 1.1f;
			} else {
				particleCount = 3;
				particleRadius = 0.55f;
			}
			if (diluted) {
				particleCount /= 2;
			}
			for (int i = 0; i < particleCount; i++) {
				float angle = this.random.nextFloat() * TWO_PI;
				float distance = Mth.sqrt(this.random.nextFloat()) * particleRadius;
				double x = this.getX() + Mth.cos(angle) * distance;
				double y = this.getY() + this.random.nextFloat() * 0.5f;
				double z = this.getZ() + Mth.sin(angle) * distance;
				level().addAlwaysVisibleParticle(
						ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, EnderEssenceFlaskEntity.PARTICLE_COLOR),
						x, y, z, 0, 0, 0
				);
			}
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected void readAdditionalSaveData(ValueInput tag) {
		tickCount = tag.getIntOr(TAG_AGE, 0);
		setFromEnderman(tag.getBooleanOr(TAG_FROM_ENDERMAN, false));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput tag) {
		tag.putInt(TAG_AGE, tickCount);
		tag.putBoolean(TAG_FROM_ENDERMAN, isFromEnderman());
	}

	public ItemStack getBottledItem() {
		return new ItemStack(isDiluted() ? BotaniaItems.DILUTED_ENDER_ESSENCE : BotaniaItems.PURE_ENDER_ESSENCE);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityDimensions.scalable(isFromEnderman() ? 2.0f : 1.0f, 1.0f);
	}

	// [VanillaCopy] AreaEffectCloud::refreshDimensions()
	@Override
	public void refreshDimensions() {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		super.refreshDimensions();
		this.setPos(x, y, z);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
		return new ClientboundAddEntityPacket(this, entity, isFromEnderman() ? 1 : 0);
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket packet) {
		super.recreateFromPacket(packet);
		setFromEnderman((packet.getData() & 1) != 0);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}
}
