package net.mcreator.abyss.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import net.mcreator.abyss.AbyssMod;
import net.mcreator.abyss.block.BulkheadBlock;
import net.mcreator.abyss.block.entity.BulkheadBlockEntity;

public class BulkheadRenderer implements BlockEntityRenderer<BulkheadBlockEntity> {
 private static final ResourceLocation FRAME_MODEL = ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/bulkhead_frame");
 private static final ResourceLocation DOOR_MODEL = ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/bulkhead_door");
 private static final ResourceLocation HANDLE_MODEL = ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/bulkhead_handle");
 private static final float HANDLE_PIVOT_X = 2.0F / 16.0F;
 private static final float HANDLE_PIVOT_Y = 1.0F;
 private static final float HANDLE_PIVOT_Z = 15.99F / 16.0F;

 public BulkheadRenderer(BlockEntityRendererProvider.Context context) {
 }

 @Override
 public void render(BulkheadBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
  if (!(blockEntity.getBlockState().getBlock() instanceof BulkheadBlock)) {
   return;
  }
  if (blockEntity.getBlockState().getValue(BulkheadBlock.HALF) != DoubleBlockHalf.LOWER) {
   return;
  }

  float progress = blockEntity.getProgress(partialTick);
  Direction facing = blockEntity.getBlockState().getValue(BulkheadBlock.FACING);
  DoorHingeSide hinge = blockEntity.getBlockState().getValue(BulkheadBlock.HINGE);

  float handleT = Mth.clamp(progress / BulkheadBlockEntity.HANDLE_PHASE, 0.0F, 1.0F);
  float slideT = Mth.clamp((progress - BulkheadBlockEntity.HANDLE_PHASE) / (1.0F - BulkheadBlockEntity.HANDLE_PHASE), 0.0F, 1.0F);

  poseStack.pushPose();
  poseStack.translate(0.5D, 0.0D, 0.5D);
  poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
  if (hinge == DoorHingeSide.RIGHT) {
   poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
  }
  poseStack.translate(-0.5D, 0.0D, -0.5D);

  var dispatcher = Minecraft.getInstance().getBlockRenderer();
  renderBakedModel(dispatcher, poseStack, buffer, getModel(FRAME_MODEL), packedLight, packedOverlay);

  poseStack.pushPose();
  poseStack.translate(0.0F, 0.0F, -slideT * BulkheadBlockEntity.DOOR_SLIDE_BLOCKS);
  renderBakedModel(dispatcher, poseStack, buffer, getModel(DOOR_MODEL), packedLight, packedOverlay);
  poseStack.popPose();

  poseStack.pushPose();
  poseStack.translate(0.0F, 0.0F, -slideT * BulkheadBlockEntity.DOOR_SLIDE_BLOCKS);
  poseStack.translate(HANDLE_PIVOT_X, HANDLE_PIVOT_Y, HANDLE_PIVOT_Z);
  poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F * handleT));
  poseStack.translate(-HANDLE_PIVOT_X, -HANDLE_PIVOT_Y, -HANDLE_PIVOT_Z);
  renderBakedModel(dispatcher, poseStack, buffer, getModel(HANDLE_MODEL), packedLight, packedOverlay);
  poseStack.popPose();

  poseStack.popPose();
 }

 private static BakedModel getModel(ResourceLocation modelLocation) {
  return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelLocation));
 }

 private static void renderBakedModel(net.minecraft.client.renderer.block.BlockRenderDispatcher dispatcher, PoseStack poseStack, MultiBufferSource buffer, BakedModel model, int packedLight,
   int packedOverlay) {
  dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), null, model, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
 }
}
