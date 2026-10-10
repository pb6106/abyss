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
import java.util.Set;
import java.util.UUID;

/**
 * Compartment depth boost for Create: Deep Seas when an Abyssal Pressurizer is inside a sealed room.
 */
public final class DeepSeasPressurizerHelper {
 public static final int DEPTH_MULTIPLIER = 10;
 private static final String DEEP_SEAS_MOD_ID = "create_submarine";

 private static Method getCompartments;
 private static Method isCompromised;
 private static Method findSubLevelId;
 private static Constructor<?> hullPropertyCtor;
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
  if (hullProperty == null || !isDeepSeasLoaded()) {
   return hullProperty;
  }
  Object comp = compartmentForHull(subId, plotPos);
  if (comp == null) {
   comp = compartmentAdjacent(subId, plotPos);
  }
  if (comp == null || !compartmentHasPressurizerForSub(subId, comp, plotLevel)) {
   return hullProperty;
  }
  return scaleHullProperty(hullProperty, DEPTH_MULTIPLIER);
 }

 /** True when an abyssal pressurizer sits in any sealed, uncompromised compartment on this sub. */
 public static boolean submarineHasSealedPressurizer(UUID subId, Level plotLevel) {
  if (!ensureReflection()) {
   return false;
  }
  try {
   @SuppressWarnings("unchecked")
   List<Object> comps = (List<Object>) getCompartments.invoke(null, subId);
   for (Object comp : comps) {
    if (!isUsableCompartment(subId, comp)) {
     continue;
    }
    if (compartmentContainsPressurizer(comp, plotLevel)) {
     return true;
    }
   }
  } catch (ReflectiveOperationException ignored) {
  }
  return false;
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
   Class<?> hullConfig = Class.forName("com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig");
   componentClass = Class.forName("com.maxenonyme.createsubmarine.submarine.compartment.CompartmentDetector$Component");
   getCompartments = tracker.getMethod("getCompartments", UUID.class);
   isCompromised = tracker.getMethod("isCompromised", UUID.class, BlockPos.class);
   Class<?> subLevelRegistry = Class.forName("com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry");
   findSubLevelId = subLevelRegistry.getMethod("findUUID", Level.class, BlockPos.class);
   hullPropertyCtor = findRecordConstructor(hullConfig, "HullProperty", int.class, float.class);
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

 private static boolean compartmentHasPressurizerForSub(UUID subId, Object comp, Level plotLevel) {
  try {
   return isUsableCompartment(subId, comp) && compartmentContainsPressurizer(comp, plotLevel);
  } catch (ReflectiveOperationException exception) {
   return false;
  }
 }

 private static Object compartmentForHull(UUID subId, BlockPos hullPos) {
  if (!ensureReflection()) {
   return null;
  }
  try {
   @SuppressWarnings("unchecked")
   List<Object> comps = (List<Object>) getCompartments.invoke(null, subId);
   for (Object comp : comps) {
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

 private static boolean compartmentContainsPressurizer(Object comp, Level plotLevel) {
  if (!ensureReflection()) {
   return false;
  }
  try {
   @SuppressWarnings("unchecked")
   Set<BlockPos> internal = (Set<BlockPos>) componentClass.getMethod("internal").invoke(comp);
   for (BlockPos pos : internal) {
    if (isPressurizerBlock(plotLevel, pos)) {
     return true;
    }
    for (Direction direction : Direction.values()) {
     if (isPressurizerBlock(plotLevel, pos.relative(direction))) {
      return true;
     }
    }
   }
   @SuppressWarnings("unchecked")
   Set<BlockPos> hull = (Set<BlockPos>) componentClass.getMethod("hull").invoke(comp);
   for (BlockPos pos : hull) {
    if (isPressurizerBlock(plotLevel, pos)) {
     return true;
    }
   }
  } catch (ReflectiveOperationException ignored) {
  }
  return false;
 }

 private static boolean isPressurizerBlock(Level plotLevel, BlockPos pos) {
  return plotLevel.getBlockState(pos).is(AbyssModBlocks.ABYSSAL_PRESSURIZER.get());
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

}
