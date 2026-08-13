package net.mcreator.abyss.procedures;

import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import net.mcreator.abyss.block.BulkheadBlock;

public class BulkheadRedstoneOnProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!(world instanceof Level level) || level.isClientSide()) {
			return;
		}

		BlockPos pos = BlockPos.containing(x, y, z);
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof BulkheadBlock)) {
			return;
		}
		if (state.getValue(BulkheadBlock.HALF) == DoubleBlockHalf.UPPER) {
			pos = pos.below();
			state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof BulkheadBlock)) {
				return;
			}
		}
		if (!state.getValue(BulkheadBlock.OPEN)) {
			BulkheadBlock.setOpen(level, pos, state, true);
		}
	}
}
