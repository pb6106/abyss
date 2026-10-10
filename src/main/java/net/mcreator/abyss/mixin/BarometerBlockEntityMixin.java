package net.mcreator.abyss.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.mcreator.abyss.compat.DeepSeasPressurizerHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(targets = "com.maxenonyme.createsubmarine.submarine.block.entity.BarometerBlockEntity", remap = false)
public abstract class BarometerBlockEntityMixin {
 @Inject(method = "getWeakestHullPressure", at = @At("RETURN"), cancellable = true, remap = false)
 private void abyss$boostBarometerDisplay(UUID subId, CallbackInfoReturnable<Integer> callback) {
  int weakest = callback.getReturnValue();
  if (weakest <= 0) {
   return;
  }
  Level level = ((BlockEntity) (Object) this).getLevel();
  if (level == null || !DeepSeasPressurizerHelper.submarineHasSealedPressurizer(subId, level)) {
   return;
  }
  callback.setReturnValue(Math.max(1, weakest * DeepSeasPressurizerHelper.DEPTH_MULTIPLIER));
 }
}
