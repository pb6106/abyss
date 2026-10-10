package net.mcreator.abyss.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import net.mcreator.abyss.AbyssMod;
import net.mcreator.abyss.block.entity.AbyssalPressurizerBlockEntity;
import net.mcreator.abyss.compat.DeepSeasPressurizerHelper;

public class AbyssalPressurizerRenderer implements BlockEntityRenderer<AbyssalPressurizerBlockEntity> {
 public static final ResourceLocation BASE_MODEL = ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/abyssal_pressurizer_base");
 public static final ResourceLocation ARM_MODEL = ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/abyssal_pressurizer_arm");

 /** Turntable center (block space): arm spans in X/Z around here, not the BB group origin at x=22. */
 private static final float PIVOT_X = 8.0F / 16.0F;
 private static final float PIVOT_Y = 10.0F / 16.0F;
 private static final float PIVOT_Z = 8.0F / 16.0F;

 /** One full turn every 3 seconds (60 ticks). */
 public static final float ARM_RADIANS_PER_TICK = (float) (Math.PI * 2.0 / 60.0);

 public AbyssalPressurizerRenderer(BlockEntityRendererProvider.Context context) {
 }

 @Override
 public void render(AbyssalPressurizerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
  if (blockEntity.getLevel() == null) {
   return;
  }

  float speed = ARM_RADIANS_PER_TICK * blockEntity.getArmSpeedMultiplier();
  float angle = (blockEntity.getLevel().getGameTime() + partialTick) * speed;
  var dispatcher = Minecraft.getInstance().getBlockRenderer();

  renderBakedModel(dispatcher, poseStack, buffer, getModel(BASE_MODEL), packedLight, packedOverlay);

  poseStack.pushPose();
  poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
  poseStack.mulPose(Axis.YP.rotation(angle));
  poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);
  renderBakedModel(dispatcher, poseStack, buffer, getModel(ARM_MODEL), packedLight, packedOverlay);
  poseStack.popPose();
 }

 private static BakedModel getModel(ResourceLocation modelLocation) {
  return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelLocation));
 }

 private static void renderBakedModel(net.minecraft.client.renderer.block.BlockRenderDispatcher dispatcher, PoseStack poseStack, MultiBufferSource buffer, BakedModel model,
   int packedLight, int packedOverlay) {
  dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), null, model, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
 }
}
