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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

@Mixin(SubmarinePressureSystem.class)
public class SubmarinePressureSystemMixin {
 @ModifyVariable(method = "applyPressure", at = @At("HEAD"), ordinal = 0, remap = false)
 private static HullProperty abyss$scaleHullForPressurizer(HullProperty hullAtInjection, UUID id, Level plotLevel, Level oceanLevel, SubLevelAccess subLevelAccess, BlockPos plotPos,
   BlockState state, HullProperty prop, int surfaceY, boolean[] creakPlayed) {
  Object scaled = DeepSeasPressurizerHelper.scaleHullProperty(hullAtInjection, id, plotLevel, plotPos);
  return scaled instanceof HullProperty hull ? hull : hullAtInjection;
 }
}
