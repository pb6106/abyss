package net.mcreator.abyss.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class AbysstoneBlock extends Block {
	public AbysstoneBlock() {
		super(BlockBehaviour.Properties.of().strength(1.5f, 11.5f).requiresCorrectToolForDrops().sound(SoundType.STONE));
	}
}