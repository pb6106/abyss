package net.mcreator.abyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;

import net.mcreator.abyss.block.entity.AbyssalPressurizerBlockEntity;
import net.mcreator.abyss.init.AbyssModBlockEntities;

import org.jetbrains.annotations.Nullable;

public class AbyssalPressurizerBlock extends BaseEntityBlock {
 public static final MapCodec<AbyssalPressurizerBlock> CODEC = simpleCodec(AbyssalPressurizerBlock::new);

 public AbyssalPressurizerBlock(BlockBehaviour.Properties properties) {
  super(properties);
 }

 @Override
 protected MapCodec<? extends BaseEntityBlock> codec() {
  return CODEC;
 }

 @Override
 public RenderShape getRenderShape(BlockState state) {
  return RenderShape.INVISIBLE;
 }

 @Nullable
 @Override
 public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
  return new AbyssalPressurizerBlockEntity(pos, state);
 }

 @Nullable
 @Override
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
  return createTickerHelper(type, AbyssModBlockEntities.ABYSSAL_PRESSURIZER.get(),
    level.isClientSide() ? AbyssalPressurizerBlockEntity::clientTick : AbyssalPressurizerBlockEntity::serverTick);
 }
}
