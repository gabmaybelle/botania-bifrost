/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorModel extends HumanoidModel<LivingEntity> {
	protected final EquipmentSlot slot;

	public ArmorModel(ModelPart root, EquipmentSlot slot) {
		super(root);
		this.slot = slot;
	}

	// [VanillaCopy] ArmorStandArmorModel.setupAnim because armor stands are dumb
	// This fixes the armor "breathing" and helmets always facing south on armor stands
	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!(entity instanceof ArmorStand entityIn)) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			return;
		}

		this.head.xRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().x();
		this.head.yRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().y();
		this.head.zRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().z();
		this.head.setPos(0.0F, 1.0F, 0.0F);
		this.body.xRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().x();
		this.body.yRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().y();
		this.body.zRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().z();
		this.leftArm.xRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().x();
		this.leftArm.yRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().y();
		this.leftArm.zRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().z();
		this.rightArm.xRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().x();
		this.rightArm.yRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().y();
		this.rightArm.zRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().z();
		this.leftLeg.xRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().x();
		this.leftLeg.yRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().y();
		this.leftLeg.zRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().z();
		this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
		this.rightLeg.xRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().x();
		this.rightLeg.yRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().y();
		this.rightLeg.zRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().z();
		this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
		this.hat.copyFrom(this.head);
	}

	@Override
	public void renderToBuffer(PoseStack ms, VertexConsumer buffer, int light, int overlay, int color) {
		setPartVisibility(slot);
		super.renderToBuffer(ms, buffer, light, overlay, color);
	}

	// [VanillaCopy] HumanoidArmorLayer
	private void setPartVisibility(EquipmentSlot slot) {
		setAllVisible(false);
		switch (slot) {
			case HEAD -> {
				head.visible = true;
				hat.visible = true;
			}
			case CHEST -> {
				body.visible = true;
				rightArm.visible = true;
				leftArm.visible = true;
			}
			case LEGS -> {
				body.visible = true;
				rightLeg.visible = true;
				leftLeg.visible = true;
			}
			case FEET -> {
				rightLeg.visible = true;
				leftLeg.visible = true;
			}
		}
	}
}
