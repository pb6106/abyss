package net.mcreator.abyss.mixin;

import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig.HullProperty;
import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;

import dev.ryanhcode.sable.companion.SubLevelAccess;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.mcreator.abyss.compat.DeepSeasPressurizerHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(SubmarinePressureSystem.class)
public class SubmarinePressureSystemMixin {
 @ModifyVariable(method = "applyPressure", at = @At("HEAD"), ordinal = 0, remap = false)
 private static HullProperty abyss$scaleHullForPressurizer(HullProperty prop, UUID id, Level level, Level plotLevel, SubLevelAccess subLevelAccess, BlockPos plotPos,
   BlockState state, HullProperty hullPropertyArg, int pressureArg, boolean[] flags) {
  Object scaled = DeepSeasPressurizerHelper.scaleHullProperty(prop, id, plotLevel, plotPos);
  return scaled instanceof HullProperty hull ? hull : prop;
 }

 @Inject(method = "weakestHull", at = @At("HEAD"), cancellable = true, remap = false)
 private static void abyss$weakestHull(UUID subId, Level plotLevel, CallbackInfoReturnable<SubmarinePressureSystem.WeakPoint> callback) {
  if (!DeepSeasPressurizerHelper.hooksReady()) {
   return;
  }
  Object weakest = DeepSeasPressurizerHelper.weakestHull(subId, plotLevel);
  if (weakest instanceof SubmarinePressureSystem.WeakPoint weakPoint) {
   callback.setReturnValue(weakPoint);
   callback.cancel();
  }
 }
}
