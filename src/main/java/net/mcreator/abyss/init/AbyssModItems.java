/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.abyss.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.abyss.AbyssMod;

public class AbyssModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(AbyssMod.MODID);
	public static final DeferredItem<Item> BULKHEAD;
	public static final DeferredItem<Item> REINFORCED_ABYSSTONE;
	public static final DeferredItem<Item> ABYSSTONE;
	public static final DeferredItem<Item> GRATE;
	static {
		BULKHEAD = block(AbyssModBlocks.BULKHEAD);
		REINFORCED_ABYSSTONE = block(AbyssModBlocks.REINFORCED_ABYSSTONE);
		ABYSSTONE = block(AbyssModBlocks.ABYSSTONE);
		GRATE = block(AbyssModBlocks.GRATE);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}