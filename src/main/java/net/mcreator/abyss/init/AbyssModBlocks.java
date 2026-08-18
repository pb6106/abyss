package net.mcreator.abyss.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.mcreator.abyss.block.ReinforcedAbysstoneBlock;
import net.mcreator.abyss.block.GrateBlock;
import net.mcreator.abyss.block.BulkheadBlock;
import net.mcreator.abyss.block.AbysstoneBlock;
import net.mcreator.abyss.AbyssMod;

public class AbyssModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(AbyssMod.MODID);
	public static final DeferredBlock<Block> BULKHEAD;
	public static final DeferredBlock<Block> REINFORCED_ABYSSTONE;
	public static final DeferredBlock<Block> ABYSSTONE;
	public static final DeferredBlock<Block> GRATE;
	static {
		BULKHEAD = REGISTRY.register("bulkhead", BulkheadBlock::new);
		REINFORCED_ABYSSTONE = REGISTRY.register("reinforced_abysstone", ReinforcedAbysstoneBlock::new);
		ABYSSTONE = REGISTRY.register("abysstone", AbysstoneBlock::new);
		GRATE = REGISTRY.register("grate", GrateBlock::new);
	}
}