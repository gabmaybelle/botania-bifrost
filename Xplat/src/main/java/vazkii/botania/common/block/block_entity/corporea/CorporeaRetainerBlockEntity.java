/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity.corporea;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.corporea.*;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.helper.NbtHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class CorporeaRetainerBlockEntity extends BlockEntity implements Wandable {
	private static final String TAG_REQUEST_X = "requestX";
	private static final String TAG_REQUEST_Y = "requestY";
	private static final String TAG_REQUEST_Z = "requestZ";
	private static final String TAG_REQUEST_TYPE = "requestType";
	private static final String TAG_REQUEST_COUNT = "requestCount";

	private static final Map<Identifier, BiFunction<CompoundTag, HolderLookup.Provider, ? extends CorporeaRequestMatcher>> corporeaMatcherDeserializers = new ConcurrentHashMap<>();
	private static final Map<Class<? extends CorporeaRequestMatcher>, Identifier> corporeaMatcherSerializers = new ConcurrentHashMap<>();

	@Nullable
	private BlockPos requestPos;

	@Nullable
	private CorporeaRequestMatcher request;
	private int requestCount;

	public CorporeaRetainerBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CORPOREA_RETAINER, pos, state);
	}

	public boolean shouldRetainMissing() {
		return getBlockState().getValue(BotaniaStateProperties.RETAIN_MISSING);
	}

	public void remember(BlockPos pos, CorporeaRequestMatcher request, int count, int missing) {
		this.requestPos = pos;
		this.request = request;
		this.requestCount = shouldRetainMissing() ? missing : count;

		setChanged();
	}

	public void forget() {
		requestPos = null;
		request = null;
		requestCount = 0;
	}

	public int getComparatorValue() {
		return CorporeaHelper.instance().signalStrengthForRequestSize(requestCount);
	}

	public boolean hasPendingRequest() {
		return request != null;
	}

	public void fulfilRequest() {
		if (!hasPendingRequest()) {
			return;
		}

		CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(level, requestPos);
		if (spark != null) {
			BlockEntity te = spark.getSparkNode().getWorld().getBlockEntity(spark.getSparkNode().getPos());
			if (te instanceof CorporeaRequestor requestor) {
				requestor.doCorporeaRequest(request, requestCount, spark, null);

				forget();
				setChanged();
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput cmp) {
		if (requestPos != null) {
			cmp.putInt(TAG_REQUEST_X, requestPos.getX());
			cmp.putInt(TAG_REQUEST_Y, requestPos.getY());
			cmp.putInt(TAG_REQUEST_Z, requestPos.getZ());
		}
		Identifier reqType = request != null ? corporeaMatcherSerializers.get(request.getClass()) : null;

		if (reqType != null) {
			cmp.putString(TAG_REQUEST_TYPE, reqType.toString());
			// PORT-26.3: the matcher API still writes a CompoundTag; its keys are copied into the save data as they are
			CompoundTag requestTag = new CompoundTag();
			request.writeToNBT(requestTag, level.registryAccess());
			cmp.store(NbtHelper.COMPOUND_PASSTHROUGH_CODEC, requestTag);
			cmp.putInt(TAG_REQUEST_COUNT, requestCount);
		}
	}

	@Override
	protected void loadAdditional(ValueInput cmp) {
		if (cmp.getInt(TAG_REQUEST_X).isPresent()) {
			requestPos = new BlockPos(
					cmp.getIntOr(TAG_REQUEST_X, 0),
					cmp.getIntOr(TAG_REQUEST_Y, 0),
					cmp.getIntOr(TAG_REQUEST_Z, 0));
		} else {
			requestPos = null;
		}
		Identifier reqType = Identifier.tryParse(cmp.getStringOr(TAG_REQUEST_TYPE, ""));
		if (reqType != null && corporeaMatcherDeserializers.containsKey(reqType)) {
			request = corporeaMatcherDeserializers.get(reqType)
					.apply(cmp.read(NbtHelper.COMPOUND_PASSTHROUGH_CODEC).orElseGet(CompoundTag::new), cmp.lookup());
		} else {
			request = null;
		}
		requestCount = cmp.getIntOr(TAG_REQUEST_COUNT, 0);
	}

	public static <T extends CorporeaRequestMatcher> void addCorporeaRequestMatcher(Identifier id, Class<T> clazz, BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer) {
		corporeaMatcherSerializers.put(clazz, id);
		corporeaMatcherDeserializers.put(id, deserializer);
	}

	public static class WandHud implements WandHUD {
		private final CorporeaRetainerBlockEntity retainer;

		public WandHud(CorporeaRetainerBlockEntity retainer) {
			this.retainer = retainer;
		}

		@Override
		public void renderHUD(GuiGraphicsExtractor gui, Window window, Font font, float partialTick) {
			String mode = I18n.get("botaniamisc.retainer." + (retainer.shouldRetainMissing() ? "retain_missing" : "retain_all"));
			int strWidth = font.width(mode);
			int x = (window.getGuiScaledWidth() - strWidth) / 2;
			int y = window.getGuiScaledHeight() / 2 + 8;

			RenderHelper.renderHUDBox(gui, x - 2, y, x + strWidth + 2, y + 12);
			gui.text(font, mode, x, y + 2, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean onUsedByWand(Player player, ItemStack stack, Direction side) {
		if (!level.isClientSide()) {
			level.setBlock(getBlockPos(), getBlockState().cycle(BotaniaStateProperties.RETAIN_MISSING),
					Block.UPDATE_CLIENTS);
		}
		return true;
	}
}
