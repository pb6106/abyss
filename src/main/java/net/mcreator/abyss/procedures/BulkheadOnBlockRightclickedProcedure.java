package net.mcreator.abyss.procedures;

import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import net.mcreator.abyss.block.BulkheadBlock;

@Deprecated
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

  BulkheadBlock.toggleOpen(level, pos, blockstate, !blockstate.getValue(BulkheadBlock.OPEN));
 }
}
