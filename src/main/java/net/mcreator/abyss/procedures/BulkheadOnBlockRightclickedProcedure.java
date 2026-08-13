package net.mcreator.abyss.procedures;

import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;

import net.mcreator.abyss.block.BulkheadBlock;

public class BulkheadOnBlockRightclickedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		if (!(blockstate.getBlock() instanceof BulkheadBlock) || !(world instanceof Level level) || level.isClientSide()) {
			return;
		}

		BlockPos pos = BlockPos.containing(x, y, z);
		if (blockstate.getValue(BulkheadBlock.HALF) == DoubleBlockHalf.UPPER) {
			pos = pos.below();
			blockstate = level.getBlockState(pos);
			if (!(blockstate.getBlock() instanceof BulkheadBlock)) {
				return;
			}
		}

		boolean open = !blockstate.getValue(BulkheadBlock.OPEN);
		BulkheadBlock.setOpen(level, pos, blockstate, open);
		level.playSound(null, pos, open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}
}
