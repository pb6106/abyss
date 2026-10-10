package net.mcreator.abyss.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import net.mcreator.abyss.AbyssMod;
import net.mcreator.abyss.init.AbyssModBlocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Compartment depth boost for Create: Deep Seas when an Abyssal Pressurizer is inside a sealed room.
 */
public final class DeepSeasPressurizerHelper {
 public static final int DEPTH_MULTIPLIER = 10;
 private static final String DEEP_SEAS_MOD_ID = "create_submarine";

 private static Method getCompartments;
 private static Method getActualBlockState;
 private static Method hullConfigGetFor;
 private static Method isCompromised;
 private static Method findSubLevelId;
 private static Constructor<?> hullPropertyCtor;
 private static Constructor<?> weakPointCtor;
 private static Class<?> componentClass;

 private DeepSeasPressurizerHelper() {
 }

 public static boolean isDeepSeasLoaded() {
  return ModList.get().isLoaded(DEEP_SEAS_MOD_ID);
 }

 public static boolean hooksReady() {
  return isDeepSeasLoaded() && ensureReflection();
 }

 /** Active only inside a sealed submarine compartment on a Sable sublevel (same idea as the oxygen diffuser). */
 public static boolean isOnSubLevel(Level level, BlockPos pos) {
  if (!ensureReflection()) {
   return false;
  }
  try {
   return findSubLevelId.invoke(null, level, pos) != null;
  } catch (ReflectiveOperationException exception) {
   return false;
  }
 }

 /** 0.1 off sublevel, 1.0 on Sable sublevel, 3.0 when boosting a sealed Deep Seas compartment. */
 public static float pressurizerArmSpeedMultiplier(Level level, BlockPos pos) {
  if (isActiveInSealedCompartment(level, pos)) {
   return 3.0F;
  }
  if (isOnSubLevel(level, pos)) {
   return 1.0F;
  }
  return 0.1F;
 }

 public static boolean isActiveInSealedCompartment(Level level, BlockPos pos) {
  if (!level.getBlockState(pos).is(AbyssModBlocks.ABYSSAL_PRESSURIZER.get())) {
   return false;
  }
  if (!ensureReflection()) {
   return false;
  }
  try {
   UUID subId = (UUID) findSubLevelId.invoke(null, level, pos);
   if (subId == null) {
    return false;
   }
   Object comp = compartmentContaining(pos, subId);
   return comp != null && isUsableCompartment(subId, comp);
  } catch (ReflectiveOperationException exception) {
   return false;
  }
 }

 public static Object scaleHullProperty(Object hullProperty, UUID subId, Level plotLevel, BlockPos plotPos) {
  if (hullProperty == null || !isDeepSeasLoaded() || !compartmentHasPressurizer(subId, plotLevel, plotPos)) {
   return hullProperty;
  }
  return scaleHullProperty(hullProperty, DEPTH_MULTIPLIER);
 }

 public static int effectiveMaxDepth(UUID subId, Level plotLevel, BlockPos hullPos, int baseDepth) {
  if (!isDeepSeasLoaded()) {
   return baseDepth;
  }
  Object comp = compartmentForHull(subId, hullPos);
  if (comp == null || !compartmentHasPressurizer(comp, plotLevel)) {
   return baseDepth;
  }
  return Math.max(1, baseDepth * DEPTH_MULTIPLIER);
 }

 public static Object weakestHull(UUID subId, Level plotLevel) {
  if (!ensureReflection()) {
   return null;
  }
  try {
   Class<?> tracker = Class.forName("com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker");
   Class<?> pressure = Class.forName("com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem");
   @SuppressWarnings("unchecked")
   List<Object> comps = (List<Object>) getCompartments.invoke(null, subId);
   Object weakest = null;
   int weakestDepth = Integer.MAX_VALUE;
   for (Object comp : comps) {
    if (!isUsableCompartment(subId, comp)) {
     continue;
    }
    @SuppressWarnings("unchecked")
    Set<BlockPos> hull = (Set<BlockPos>) componentClass.getMethod("hull").invoke(comp);
    boolean boosted = compartmentHasPressurizer(comp, plotLevel);
    for (BlockPos bp : hull) {
     if (!facesExterior(subId, bp)) {
      continue;
     }
     BlockState state = plotLevel.getBlockState(bp);
     if (state.isAir()) {
      continue;
     }
     BlockState strengthState = (BlockState) getActualBlockState.invoke(null, plotLevel, bp, state);
     Optional<?> propOpt = (Optional<?>) hullConfigGetFor.invoke(null, strengthState);
     if (propOpt.isEmpty()) {
      continue;
     }
     int depth = (int) propOpt.get().getClass().getMethod("maxWaterDepth").invoke(propOpt.get());
     if (boosted) {
      depth = Math.max(1, depth * DEPTH_MULTIPLIER);
     }
     if (depth < weakestDepth) {
      weakestDepth = depth;
      weakest = weakPointCtor.newInstance(depth, strengthState, bp.immutable());
     }
    }
   }
   return weakest;
  } catch (ReflectiveOperationException exception) {
   AbyssMod.LOGGER.warn("Failed to compute pressurized weakest hull for submarine {}", subId, exception);
   return null;
  }
 }

 private static boolean ensureReflection() {
  if (!isDeepSeasLoaded()) {
   return false;
  }
  if (getCompartments != null) {
   return true;
  }
  try {
   Class<?> tracker = Class.forName("com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker");
   Class<?> pressure = Class.forName("com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem");
   Class<?> hullConfig = Class.forName("com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig");
   componentClass = Class.forName("com.maxenonyme.createsubmarine.submarine.compartment.CompartmentDetector$Component");
   getCompartments = tracker.getMethod("getCompartments", UUID.class);
   isCompromised = tracker.getMethod("isCompromised", UUID.class, BlockPos.class);
   Class<?> subLevelRegistry = Class.forName("com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry");
   findSubLevelId = subLevelRegistry.getMethod("findUUID", Level.class, BlockPos.class);
   getActualBlockState = pressure.getMethod("getActualBlockState", Level.class, BlockPos.class, BlockState.class);
   hullConfigGetFor = hullConfig.getMethod("getFor", BlockState.class);
   hullPropertyCtor = findRecordConstructor(hullConfig, "HullProperty", int.class, float.class);
   weakPointCtor = findRecordConstructor(pressure, "WeakPoint", int.class, BlockState.class, BlockPos.class);
   return true;
  } catch (ReflectiveOperationException exception) {
   AbyssMod.LOGGER.error("Could not bind Create: Deep Seas pressurizer compat", exception);
   return false;
  }
 }

 private static Object scaleHullProperty(Object hullProperty, int multiplier) {
  try {
   int depth = (int) hullProperty.getClass().getMethod("maxWaterDepth").invoke(hullProperty);
   float chance = (float) hullProperty.getClass().getMethod("implosionChance").invoke(hullProperty);
   return hullPropertyCtor.newInstance(Math.max(1, depth * multiplier), chance);
  } catch (ReflectiveOperationException exception) {
   return hullProperty;
  }
 }

 private static boolean compartmentHasPressurizer(UUID subId, Level plotLevel, BlockPos plotPos) {
  Object comp = compartmentAdjacent(subId, plotPos);
  return comp != null && compartmentHasPressurizer(comp, plotLevel);
 }

 private static Object compartmentForHull(UUID subId, BlockPos hullPos) {
  if (!ensureReflection()) {
   return null;
  }
  try {
   @SuppressWarnings("unchecked")
   List<Object> comps = (List<Object>) getCompartments.invoke(null, subId);
   for (Object comp : comps) {
    if (!isUsableCompartment(subId, comp)) {
     continue;
    }
    @SuppressWarnings("unchecked")
    Set<BlockPos> hull = (Set<BlockPos>) componentClass.getMethod("hull").invoke(comp);
    if (hull.contains(hullPos)) {
     return comp;
    }
   }
  } catch (ReflectiveOperationException ignored) {
  }
  return null;
 }

 private static Object compartmentAdjacent(UUID subId, BlockPos plotPos) {
  if (!ensureReflection()) {
   return null;
  }
  try {
   Class<?> tracker = Class.forName("com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker");
   Method find = tracker.getMethod("findCompartmentAdjacent", UUID.class, BlockPos.class);
   return find.invoke(null, subId, plotPos);
  } catch (ReflectiveOperationException exception) {
   return null;
  }
 }

 private static boolean compartmentHasPressurizer(Object comp, Level plotLevel) {
  try {
   @SuppressWarnings("unchecked")
   Set<BlockPos> internal = (Set<BlockPos>) componentClass.getMethod("internal").invoke(comp);
   for (BlockPos pos : internal) {
    if (isActiveInSealedCompartment(plotLevel, pos)) {
     return true;
    }
   }
  } catch (ReflectiveOperationException ignored) {
  }
  return false;
 }

 private static Object compartmentContaining(BlockPos internalPos, UUID subId) throws ReflectiveOperationException {
  @SuppressWarnings("unchecked")
  List<Object> comps = (List<Object>) getCompartments.invoke(null, subId);
  for (Object comp : comps) {
   @SuppressWarnings("unchecked")
   Set<BlockPos> internal = (Set<BlockPos>) componentClass.getMethod("internal").invoke(comp);
   if (internal.contains(internalPos)) {
    return comp;
   }
  }
  return null;
 }

 private static boolean isUsableCompartment(UUID subId, Object comp) throws ReflectiveOperationException {
  if (!(boolean) componentClass.getMethod("sealed").invoke(comp)) {
   return false;
  }
  BlockPos anchor = (BlockPos) componentClass.getMethod("anchor").invoke(comp);
  return anchor == null || !(boolean) isCompromised.invoke(null, subId, anchor);
 }

 private static Constructor<?> findRecordConstructor(Class<?> owner, String simpleName, Class<?>... parameterTypes)
   throws NoSuchMethodException {
  for (Class<?> inner : owner.getDeclaredClasses()) {
   if (inner.getSimpleName().equals(simpleName)) {
    return inner.getConstructor(parameterTypes);
   }
  }
  throw new NoSuchMethodException(simpleName);
 }

 private static boolean facesExterior(UUID subId, BlockPos bp) {
  try {
   Class<?> tracker = Class.forName("com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker");
   Method withinShip = tracker.getMethod("isWithinShip", UUID.class, BlockPos.class);
   for (Direction dir : Direction.values()) {
    if (!(boolean) withinShip.invoke(null, subId, bp.relative(dir))) {
     return true;
    }
   }
  } catch (ReflectiveOperationException ignored) {
  }
  return false;
 }
}
