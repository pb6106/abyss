package net.mcreator.abyss.mixin;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class AbyssMixinPlugin implements IMixinConfigPlugin {
 private static final String DEEP_SEAS_MOD_ID = "create_submarine";

 @Override
 public void onLoad(String mixinPackage) {
 }

 @Override
 public String getRefMapperConfig() {
  return null;
 }

 @Override
 public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
  if (!mixinClassName.contains("SubmarinePressureSystemMixin") && !mixinClassName.contains("BarometerBlockEntityMixin")) {
   return true;
  }
  return isDeepSeasLoaded(targetClassName);
 }

 private static boolean isDeepSeasLoaded(String targetClassName) {
  ModList modList = ModList.get();
  if (modList != null) {
   return modList.isLoaded(DEEP_SEAS_MOD_ID);
  }

  LoadingModList loading = LoadingModList.get();
  if (loading != null && loading.getModFileById(DEEP_SEAS_MOD_ID) != null) {
   return true;
  }

  try {
   Class.forName(targetClassName, false, AbyssMixinPlugin.class.getClassLoader());
   return true;
  } catch (ClassNotFoundException ignored) {
   return false;
  }
 }

 @Override
 public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
 }

 @Override
 public List<String> getMixins() {
  return null;
 }

 @Override
 public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
 }

 @Override
 public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
 }
}
