/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.model;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.entity.GaiaGuardianRenderState;

// [VanillaCopy] PlayerModel, only the parts relevant to the Gaia Guardian
public class GaiaGuardianModel extends HumanoidModel<GaiaGuardianRenderState> {
	public final ModelPart leftSleeve;
	public final ModelPart rightSleeve;
	public final ModelPart leftPants;
	public final ModelPart rightPants;
	public final ModelPart jacket;
	private final boolean slim;

	public GaiaGuardianModel(ModelPart root, boolean slim) {
		super(root, RenderHelper::getGaiaNoiseDynamicLayer);
		// PORT-26.3: the overlay parts hang off the limbs now (PlayerModel.createMesh), not off the root
		this.leftSleeve = this.leftArm.getChild("left_sleeve");
		this.rightSleeve = this.rightArm.getChild("right_sleeve");
		this.leftPants = this.leftLeg.getChild("left_pants");
		this.rightPants = this.rightLeg.getChild("right_pants");
		this.jacket = this.body.getChild("jacket");
		this.slim = slim;
	}

	@Override
	public void setupAnim(GaiaGuardianRenderState state) {
		// PORT-26.3: part visibility comes from the render state now (the renderer used to set it before drawing)
		this.hat.visible = state.showHat;
		this.jacket.visible = state.showJacket;
		this.leftPants.visible = state.showLeftPants;
		this.rightPants.visible = state.showRightPants;
		this.leftSleeve.visible = state.showLeftSleeve;
		this.rightSleeve.visible = state.showRightSleeve;
		super.setupAnim(state);
		this.leftPants.loadPose(this.leftLeg.storePose());
		this.rightPants.loadPose(this.rightLeg.storePose());
		this.leftSleeve.loadPose(this.leftArm.storePose());
		this.rightSleeve.loadPose(this.rightArm.storePose());
		this.jacket.loadPose(this.body.storePose());
	}

	@Override
	public void translateToHand(HumanoidRenderState state, HumanoidArm side, PoseStack poseStack) {
		this.root().translateAndRotate(poseStack);
		ModelPart modelpart = this.getArm(side);
		if (this.slim) {
			float f = 0.5F * (float) (side == HumanoidArm.RIGHT ? 1 : -1);
			modelpart.x += f;
			modelpart.translateAndRotate(poseStack);
			modelpart.x -= f;
		} else {
			modelpart.translateAndRotate(poseStack);
		}
	}

}
