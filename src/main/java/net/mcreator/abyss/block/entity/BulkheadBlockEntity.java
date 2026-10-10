package net.mcreator.abyss.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.mcreator.abyss.block.BulkheadBlock;
import net.mcreator.abyss.init.AbyssModBlockEntities;

public class BulkheadBlockEntity extends BlockEntity {
 public static final int ANIMATION_TICKS = 10;
 public static final float HANDLE_PHASE = 0.35F;
 public static final float DOOR_SLIDE_BLOCKS = 11.0F / 16.0F;

 private float progress;
 private float prevProgress;
 private int animTick;
 private boolean animating;
 private boolean opening;

 public BulkheadBlockEntity(BlockPos pos, BlockState state) {
  super(AbyssModBlockEntities.BULKHEAD.get(), pos, state);
 }

 public void syncFromBlockState(BlockState state) {
  if (!animating) {
   progress = state.getValue(BulkheadBlock.OPEN) ? 1.0F : 0.0F;
   prevProgress = progress;
  }
 }

 public static void tick(Level level, BlockPos pos, BlockState state, BulkheadBlockEntity blockEntity) {
  if (!blockEntity.animating) {
   return;
  }
  blockEntity.tickAnimation();
  if (!level.isClientSide() && !blockEntity.animating) {
   BulkheadBlock.setOpen(level, pos, state, blockEntity.opening);
  }
 }

 public void startAnimation(boolean open) {
  this.animating = true;
  this.opening = open;
  this.animTick = 0;
  this.prevProgress = this.progress;
  this.progress = open ? 0.0F : 1.0F;
  setChanged();
  if (level != null && !level.isClientSide()) {
   level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }
 }

 public void tickAnimation() {
  if (!animating || level == null) {
   return;
  }
  prevProgress = progress;
  animTick++;
  float target = opening ? 1.0F : 0.0F;
  float start = opening ? 0.0F : 1.0F;
  progress = start + (target - start) * (animTick / (float) ANIMATION_TICKS);
  if (animTick >= ANIMATION_TICKS) {
   animating = false;
   progress = target;
   prevProgress = progress;
  }
  setChanged();
 }

 public float getProgress(float partialTick) {
  if (!animating) {
   return progress;
  }
  return prevProgress + (progress - prevProgress) * partialTick;
 }

 public float getProgress() {
  return progress;
 }

 public boolean isAnimating() {
  return animating;
 }

 public boolean isOpening() {
  return opening;
 }

 public boolean shouldBlockPassage() {
  if (!animating) {
   return false;
  }
  float p = progress;
  if (opening) {
   return p < HANDLE_PHASE;
  }
  return p > (1.0F - HANDLE_PHASE);
 }

 @Override
 protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
  super.saveAdditional(tag, registries);
  tag.putFloat("Progress", progress);
  tag.putFloat("PrevProgress", prevProgress);
  tag.putInt("AnimTick", animTick);
  tag.putBoolean("Animating", animating);
  tag.putBoolean("Opening", opening);
 }

 @Override
 protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
  super.loadAdditional(tag, registries);
  progress = tag.getFloat("Progress");
  prevProgress = tag.getFloat("PrevProgress");
  animTick = tag.getInt("AnimTick");
  animating = tag.getBoolean("Animating");
  opening = tag.getBoolean("Opening");
 }

 @Override
 public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
  return saveWithoutMetadata(registries);
 }

 @Override
 public Packet<ClientGamePacketListener> getUpdatePacket() {
  return ClientboundBlockEntityDataPacket.create(this);
 }
}
