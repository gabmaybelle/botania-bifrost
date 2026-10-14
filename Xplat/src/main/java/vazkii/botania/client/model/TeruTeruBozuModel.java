/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

public class TeruTeruBozuModel extends Model<Unit> {

	private final ModelPart thread;
	private final ModelPart cloth;
	private final ModelPart happyFace;
	private final ModelPart sadFace;

	public TeruTeruBozuModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
		sadFace = root.getChild("sad_face");
		happyFace = root.getChild("happy_face");
		thread = root.getChild("thread");
		cloth = root.getChild("cloth");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("sad_face", CubeListBuilder.create().texOffs(32, 0)
				.addBox(-4.0F, -6.0F, -4.0F, 8, 8, 8),
				PartPose.offsetAndRotation(0.0F, 14.5F, 0.0F, 0.1745F, 0.0F, 0.0F));
		root.addOrReplaceChild("happy_face", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -6.0F, -4.0F, 8, 8, 8),
				PartPose.offsetAndRotation(0.0F, 14.5F, 0.0F, -0.1745F, 0.0F, 0.0F));
		root.addOrReplaceChild("thread", CubeListBuilder.create().texOffs(32, 16)
				.addBox(-3.0F, 2.0F, -3.0F, 6, 1, 6),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("cloth", CubeListBuilder.create().texOffs(0, 16)
				.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8),
				PartPose.offsetAndRotation(0.0F, 21.5F, -1.0F, 0.7854F, 2.2689F, 1.5708F));
		return mesh;
	}

	// PORT-26.3: the model can't pick its face while drawing any more (renderToBuffer is final); the renderer sets it
	public void setRaining(boolean raining) {
		sadFace.visible = raining;
		happyFace.visible = !raining;
	}
}
