/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.CorporeaSparkEntity;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class CorporeaSparkRenderer extends BaseSparkRenderer<CorporeaSparkEntity> {
	private final TextureAtlasSprite corporeaWorldSprite;
	private final TextureAtlasSprite corporeaMasterWorldSprite;
	private final TextureAtlasSprite corporeaCreativeWorldSprite;

	public CorporeaSparkRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		// PORT-26.3: the spark sprites are drawn from the block atlas; items have their own atlas now, so these are copies under textures/block
		this.corporeaWorldSprite = Objects.requireNonNull(RenderHelper.getBlockSprite(this.sprites, botaniaRL("block/corporea_spark")));
		this.corporeaMasterWorldSprite = Objects.requireNonNull(RenderHelper.getBlockSprite(this.sprites, botaniaRL("block/master_corporea_spark")));
		this.corporeaCreativeWorldSprite = Objects.requireNonNull(RenderHelper.getBlockSprite(this.sprites, botaniaRL("block/creative_corporea_spark")));
	}

	@Override
	public TextureAtlasSprite getBaseIcon(CorporeaSparkEntity entity) {
		if (entity.isCreative()) {
			return this.corporeaCreativeWorldSprite;
		} else if (entity.isMaster()) {
			return this.corporeaMasterWorldSprite;
		} else {
			return this.corporeaWorldSprite;
		}
	}

}
