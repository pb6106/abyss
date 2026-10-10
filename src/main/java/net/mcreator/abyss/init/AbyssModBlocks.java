package net.mcreator.abyss.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

import net.mcreator.abyss.block.ReinforcedAbysstoneBlock;
import net.mcreator.abyss.block.GrateBlock;
import net.mcreator.abyss.block.BulkheadBlock;
import net.mcreator.abyss.block.AbyssalPressurizerBlock;
import net.mcreator.abyss.block.AbysstoneBlock;
import net.mcreator.abyss.AbyssMod;

public class AbyssModBlocks {
 public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(AbyssMod.MODID);
 public static final DeferredBlock<Block> BULKHEAD;
 public static final DeferredBlock<Block> REINFORCED_ABYSSTONE;
 public static final DeferredBlock<Block> ABYSSTONE;
 public static final DeferredBlock<Block> GRATE;
 public static final DeferredBlock<Block> ABYSSAL_PRESSURIZER;
 static {
  BULKHEAD = REGISTRY.register("bulkhead", () -> new BulkheadBlock());
  REINFORCED_ABYSSTONE = REGISTRY.register("reinforced_abysstone", ReinforcedAbysstoneBlock::new);
  ABYSSTONE = REGISTRY.register("abysstone", AbysstoneBlock::new);
  GRATE = REGISTRY.register("grate", GrateBlock::new);
  ABYSSAL_PRESSURIZER = REGISTRY.register("abyssal_pressurizer",
    () -> new AbyssalPressurizerBlock(BlockBehaviour.Properties.of().strength(3.5f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
 }
}
