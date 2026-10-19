/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.fabric.BotaniaFabricCapabilities;
import vazkii.botania.client.BotaniaClientCapabilities;
import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.CorporeaInputHandler;
import vazkii.botania.client.core.handler.DebugHandler;
import vazkii.botania.client.core.handler.KonamiHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.gui.ManaBarTooltipComponent;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.client.gui.bag.ColoredContentsPouchScreen;
import vazkii.botania.client.gui.box.TrinketCaseScreen;
import vazkii.botania.client.gui.enderhand.HandOfEnderScreen;
import vazkii.botania.client.integration.iris.IrisIntegration;
import vazkii.botania.client.model.BotaniaLayerDefinitions;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.client.render.ColorHandler;
import vazkii.botania.client.render.entity.*;
import vazkii.botania.client.render.item.BotaniaSpecialModelRenderers;
import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.fabric.network.FabricPacketHandler;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.BookDrawScreenCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class FabricClientInitializer implements ClientModInitializer {
	private static final Identifier EXTRA_MODELS_LISTENER_ID = botaniaRL("extra_models");

	// PORT-26.3: the F3 screen is built from registered entries; this one stays off until the player turns it on in
	// the debug options, because vanilla's profiles only list vanilla entries.
	private static final DebugScreenEntry DEBUG_ENTRY = (displayer, level, clientChunk, serverChunk) -> {
		List<String> lines = new ArrayList<>();
		DebugHandler.onDrawDebugText(lines);
		lines.forEach(displayer::addLine);
	};

	public FabricClientInitializer() {
		// This happens before all mods' client initializations.
		// It doesn't involve "real" registries, so doing this here should hopefully be fine.
		BotaniaClientCapabilities.registerCapabilityTypes(BotaniaFabricCapabilities.getLookupRegistration());
	}

	@Override
	public void onInitializeClient() {
		// ensure API implementations are loaded
		BotaniaAPI.LOGGER.debug("Client API instances: {}",
				List.of(BotaniaAPIClient.instance(), ClientXplatAbstractions.instance()));

		// PORT-26.3: shaders are RenderPipelines now (CoreShaders); Fabric needs no registration for them
		FabricPacketHandler.initClient();
		if (XplatAbstractions.INSTANCE.isModLoaded("iris")) {
			IrisIntegration.init();
		}

		// Guis (MenuScreens#register is opened up by Fabric's transitive access widener)
		MenuScreens.register(BotaniaItems.COLORED_CONTENTS_POUCH_MENU_TYPE, ColoredContentsPouchScreen::new);
		MenuScreens.register(BotaniaItems.TRINKET_CASE_MENU_TYPE, TrinketCaseScreen::new);
		MenuScreens.register(BotaniaItems.HAND_OF_ENDER_MENU_TYPE, HandOfEnderScreen::new);

		// Blocks and Items
		// PORT-26.3: resource models are Fabric extra models (FabricExtraModels), handed to MiscellaneousModels once the
		// model manager has reloaded; the floating flower loader is an unbaked model deserializer plus a block state
		// resolver (FabricFloatingFlowerModel); the platform camouflage wraps the baked block model (FabricPlatformModel)
		UnbakedModelDeserializer.register(ClientXplatAbstractions.FLOATING_FLOWER_MODEL_LOADER_ID, FabricFloatingFlowerModel::deserialize);
		ModelLoadingPlugin.register(pluginContext -> {
			MiscellaneousModels.INSTANCE.onModelRegister(Minecraft.getInstance().getResourceManager(),
					id -> FabricExtraModels.register(pluginContext, id));
			for (Block block : BuiltInRegistries.BLOCK) {
				if (block instanceof FloatingFlowerBaseBlock) {
					pluginContext.registerBlockStateResolver(block, new FabricFloatingFlowerModel.Resolver(block));
				}
			}
			pluginContext.modifyBlockModelAfterBake().register(
					(model, context) -> context.state().getBlock() instanceof PlatformBlock ? new FabricPlatformModel(model) : model);
		});
		ResourceLoader clientResources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		clientResources.registerReloadListener(EXTRA_MODELS_LISTENER_ID, new SimpleReloadListener<Unit>() {
			@Override
			protected Unit prepare(PreparableReloadListener.SharedState state) {
				return Unit.INSTANCE;
			}

			@Override
			protected void apply(Unit unit, PreparableReloadListener.SharedState state) {
				MiscellaneousModels.INSTANCE.onModelsLoaded(FabricExtraModels::get);
			}
		});
		clientResources.addListenerOrdering(ResourceReloaderKeys.Client.MODELS, EXTRA_MODELS_LISTENER_ID);
		// PORT-26.3: the item model properties, tint sources and special renderers go into vanilla's id mappers, which
		// Fabric's transitive access widener opens up
		BotaniaItemProperties.registerTests(ConditionalItemModelProperties.ID_MAPPER::put);
		BotaniaItemProperties.registerRanges(RangeSelectItemModelProperties.ID_MAPPER::put);
		ColorHandler.submitItemTintSources(ItemTintSources.ID_MAPPER::put);
		BotaniaSpecialModelRenderers.register(SpecialModelRenderers.ID_MAPPER::put);
		// PORT-26.3: block render layers are read off the textures since 26.1, BlockRenderLayers is gone

		// BE/Entity Renderer
		BotaniaLayerDefinitions.init((loc, supplier) -> ModelLayerRegistry.registerModelLayer(loc, supplier::get));
		EntityRenderers.registerBlockEntityRenderers(BlockEntityRendererRegistry::register);
		// PORT-26.3: block items drawn by their block entity renderer are special model renderers now (BotaniaSpecialModelRenderers)
		EntityRenderers.registerEntityRenderers(EntityRendererRegistry::register);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register(this::initAuxiliaryRender);

		BotaniaParticles.FactoryHandler.registerFactories(new BotaniaParticles.FactoryHandler.Consumer() {
			@Override
			public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> constructor) {
				ParticleProviderRegistry.getInstance().register(type, (ParticleProviderRegistry.PendingParticleProvider<T>) constructor::apply);
			}
		});

		// Events
		BookDrawScreenCallback.EVENT.register(KonamiHandler::renderBook);
		ClientLifecycleEvents.CLIENT_STARTED.register(this::loadComplete);
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::clientTickEnd);
		ClientTickEvents.END_CLIENT_TICK.register(KonamiHandler::clientTick);
		// PORT-26.3: a layer attached after a vanilla element inherits that element's render condition (the experience
		// level only draws at level 1 and up), so the main HUD goes last, as HudRenderCallback drew it; it checks the
		// hidden-HUD state itself
		HudElementRegistry.addLast(botaniaRL("mana_hud"), HUDHandler::onDrawExperienceBarPost);
		HudElementRegistry.addLast(botaniaRL("air_level_hud"), HUDHandler::onDrawAirLevelPost);
		ItemTooltipCallback.EVENT.register(TooltipHandler::onTooltipEvent);
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> ScreenKeyboardEvents.beforeKeyPress(screen)
				.register((screen2, event) -> CorporeaInputHandler.buttonPressed(event)));
		ClientTooltipComponentCallback.EVENT.register(ManaBarTooltipComponent::tryConvert);
		DebugScreenEntries.register(botaniaRL("debug"), DEBUG_ENTRY);

		// Etc
		ClientProxy.initSeasonal();
		ClientProxy.initKeybindings(KeyMappingHelper::registerKeyMapping);

		registerArmors();
		registerCapabilities();

		// PORT-26.3: disabled. Ears isn't checked for 26.3 yet (Xplat/src/disabled/integration/ears).
	}

	private static void registerCapabilities() {
		BotaniaClientCapabilities.registerCapabilityProviders(BotaniaFabricCapabilities.getProviderRegistration());
		BotaniaClientCapabilities.registerCapabilityFallbackProviders(BotaniaFabricCapabilities.getProviderRegistration());
	}

	private static void registerArmors() {
		List<Item> armors = new ArrayList<>();
		for (var entry : BuiltInRegistries.ITEM.entrySet()) {
			Item item = entry.getValue();
			Identifier id = entry.getKey().identifier();
			if (item instanceof ManasteelArmorItem
					&& id.getNamespace().equals(BotaniaAPI.MODID)) {
				armors.add(item);
			}
		}

		// PORT-26.3: armour is submitted with the wearer's render state; ArmorModel#setupAnim poses it the way the
		// wearer's model is posed, so nothing is copied from the context model. The texture does not depend on the wearer.
		ArmorRenderer renderer = (ms, collector, stack, state, slot, light, contextModel) -> {
			ManasteelArmorItem armor = (ManasteelArmorItem) stack.getItem();
			var model = ArmorModels.get(stack);
			if (model != null) {
				var texture = armor.getArmorTexture(stack, null, slot, false);
				collector.submitModel(model, state, ms, RenderTypes.armorCutoutNoCull(texture), light, OverlayTexture.NO_OVERLAY, 0);
			}
		};
		ArmorRenderer.register(renderer, armors.toArray(Item[]::new));
	}

	private void loadComplete(Minecraft mc) {
		ColorHandler.submitBlocks((source, blocks) -> BlockColorRegistry.register(List.of(source), blocks));
	}

	private void initAuxiliaryRender(EntityType<? extends LivingEntity> type, LivingEntityRenderer<?, ?, ?> renderer,
			LivingEntityRenderLayerRegistrationCallback.RegistrationHelper helper, EntityRendererProvider.Context ctx) {
		if (type == EntityTypes.PLAYER && renderer instanceof AvatarRenderer<?> avatarRenderer) {
			EntityRenderers.addAuxiliaryPlayerRenders(avatarRenderer, helper::register);
		}
	}
}
