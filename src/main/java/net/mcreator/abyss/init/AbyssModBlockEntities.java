package net.mcreator.abyss.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;

import net.mcreator.abyss.block.entity.AbyssalPressurizerBlockEntity;
import net.mcreator.abyss.block.entity.BulkheadBlockEntity;
import net.mcreator.abyss.AbyssMod;

public class AbyssModBlockEntities {
 public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AbyssMod.MODID);
 public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssalPressurizerBlockEntity>> ABYSSAL_PRESSURIZER;
 public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BulkheadBlockEntity>> BULKHEAD;

 static {
  ABYSSAL_PRESSURIZER = REGISTRY.register("abyssal_pressurizer",
    () -> BlockEntityType.Builder.of(AbyssalPressurizerBlockEntity::new, AbyssModBlocks.ABYSSAL_PRESSURIZER.get()).build(null));
  BULKHEAD = REGISTRY.register("bulkhead",
    () -> BlockEntityType.Builder.of(BulkheadBlockEntity::new, AbyssModBlocks.BULKHEAD.get()).build(null));
 }
}
