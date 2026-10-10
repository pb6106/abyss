package net.mcreator.abyss.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.mcreator.abyss.compat.DeepSeasPressurizerHelper;
import net.mcreator.abyss.init.AbyssModBlockEntities;

public class AbyssalPressurizerBlockEntity extends BlockEntity {
 private float armSpeedMultiplier = 0.1F;

 public AbyssalPressurizerBlockEntity(BlockPos pos, BlockState state) {
  super(AbyssModBlockEntities.ABYSSAL_PRESSURIZER.get(), pos, state);
 }

 public float getArmSpeedMultiplier() {
  return armSpeedMultiplier;
 }

 public static void serverTick(Level level, BlockPos pos, BlockState state, AbyssalPressurizerBlockEntity blockEntity) {
  float multiplier = DeepSeasPressurizerHelper.pressurizerArmSpeedMultiplier(level, pos);
  if (Math.abs(multiplier - blockEntity.armSpeedMultiplier) > 1.0E-4F) {
   blockEntity.armSpeedMultiplier = multiplier;
   blockEntity.setChanged();
   level.sendBlockUpdated(pos, state, state, 3);
  }
 }

 public static void clientTick(Level level, BlockPos pos, BlockState state, AbyssalPressurizerBlockEntity blockEntity) {
  if (!DeepSeasPressurizerHelper.isActiveInSealedCompartment(level, pos)) {
   return;
  }
  if (level.random.nextInt(4) != 0) {
   return;
  }
  double x = pos.getX() + 0.25 + level.random.nextDouble() * 0.5;
  double y = pos.getY() + 0.55 + level.random.nextDouble() * 0.35;
  double z = pos.getZ() + 0.25 + level.random.nextDouble() * 0.5;
  level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.04, 0.0);
 }

 @Override
 protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
  super.saveAdditional(tag, registries);
  tag.putFloat("ArmSpeedMultiplier", armSpeedMultiplier);
 }

 @Override
 protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
  super.loadAdditional(tag, registries);
  armSpeedMultiplier = tag.contains("ArmSpeedMultiplier") ? tag.getFloat("ArmSpeedMultiplier") : 0.1F;
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
