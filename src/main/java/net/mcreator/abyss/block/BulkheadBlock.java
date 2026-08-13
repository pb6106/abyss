package net.mcreator.abyss.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.abyss.procedures.BulkheadOnBlockRightclickedProcedure;

import com.google.common.collect.ImmutableMap;

public class BulkheadBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	private final ImmutableMap<BlockState, VoxelShape> shapes;

	public BulkheadBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WEEPING_VINES).strength(1.15f, 1000f).noOcclusion().pushReaction(PushReaction.BLOCK)
				.isRedstoneConductor((bs, br, bp) -> false).instrument(NoteBlockInstrument.DIDGERIDOO));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(HALF, DoubleBlockHalf.LOWER));
		this.shapes = this.makeShapes();
	}

	private ImmutableMap<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			VoxelShape full = makeFullShape(state.getValue(OPEN), state.getValue(FACING));
			if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
				return Shapes.join(full.move(0.0D, -1.0D, 0.0D), Shapes.block(), BooleanOp.AND);
			}
			return Shapes.join(full, Shapes.block(), BooleanOp.AND);
		});
	}

	private static VoxelShape makeFullShape(boolean open, Direction facing) {
		if (open) {
			return switch (facing) {
				default -> Shapes.or(box(2, 1, 13, 14, 32, 29), box(0, 18, 14, 16, 19, 17), box(0, 14, 14, 16, 15, 17), box(0, 14, 17, 16, 19, 18), box(1, 15, 15, 15, 18, 16), box(-2, 1, 16, 17, 32, 17), box(-1, 1, 0, 16, 2, 16),
						box(-2, 1, -1, 17, 32, 0), box(-2, 31, 0, 17, 32, 16));
				case NORTH -> Shapes.or(box(2, 1, -13, 14, 32, 3), box(0, 18, -1, 16, 19, 2), box(0, 14, -1, 16, 15, 2), box(0, 14, -2, 16, 19, -1), box(1, 15, 0, 15, 18, 1), box(-1, 1, -1, 18, 32, 0), box(0, 1, 0, 17, 2, 16),
						box(-1, 1, 16, 18, 32, 17), box(-1, 31, 0, 18, 32, 16));
				case EAST -> Shapes.or(box(13, 1, 2, 29, 32, 14), box(14, 18, 0, 17, 19, 16), box(14, 14, 0, 17, 15, 16), box(17, 14, 0, 18, 19, 16), box(15, 15, 1, 16, 18, 15), box(16, 1, -1, 17, 32, 18), box(0, 1, 0, 16, 2, 17),
						box(-1, 1, -1, 0, 32, 18), box(0, 31, -1, 16, 32, 18));
				case WEST -> Shapes.or(box(-13, 1, 2, 3, 32, 14), box(-1, 18, 0, 2, 19, 16), box(-1, 14, 0, 2, 15, 16), box(-2, 14, 0, -1, 19, 16), box(0, 15, 1, 1, 18, 15), box(-1, 1, -2, 0, 32, 17), box(0, 1, -1, 16, 2, 16),
						box(16, 1, -2, 17, 32, 17), box(0, 31, -2, 16, 32, 17));
			};
		}
		return switch (facing) {
			default -> Shapes.or(box(2, 1, 0, 14, 32, 16), box(0, 18, 1, 16, 19, 4), box(0, 14, 1, 16, 15, 4), box(0, 14, 4, 16, 19, 5), box(1, 15, 2, 15, 18, 3), box(-2, 1, 16, 17, 32, 17), box(-1, 1, 0, 16, 2, 16), box(-2, 1, -1, 17, 32, 0),
					box(-2, 31, 0, 17, 32, 16));
			case NORTH -> Shapes.or(box(2, 1, 0, 14, 32, 16), box(0, 18, 12, 16, 19, 15), box(0, 14, 12, 16, 15, 15), box(0, 14, 11, 16, 19, 12), box(1, 15, 13, 15, 18, 14), box(-1, 1, -1, 18, 32, 0), box(0, 1, 0, 17, 2, 16),
					box(-1, 1, 16, 18, 32, 17), box(-1, 31, 0, 18, 32, 16));
			case EAST -> Shapes.or(box(0, 1, 2, 16, 32, 14), box(1, 18, 0, 4, 19, 16), box(1, 14, 0, 4, 15, 16), box(4, 14, 0, 5, 19, 16), box(2, 15, 1, 3, 18, 15), box(16, 1, -1, 17, 32, 18), box(0, 1, 0, 16, 2, 17), box(-1, 1, -1, 0, 32, 18),
					box(0, 31, -1, 16, 32, 18));
			case WEST -> Shapes.or(box(0, 1, 2, 16, 32, 14), box(12, 18, 0, 15, 19, 16), box(12, 14, 0, 15, 15, 16), box(11, 14, 0, 12, 19, 16), box(13, 15, 1, 14, 18, 15), box(-1, 1, -2, 0, 32, 17), box(0, 1, -1, 16, 2, 16),
					box(16, 1, -2, 17, 32, 17), box(0, 31, -2, 16, 32, 17));
		};
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shapes.get(state);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shapes.get(state);
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
		return shapes.get(state);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? RenderShape.INVISIBLE : RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, OPEN, HALF);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
			// Face the player, then rotate 90° to match the Blockbench model orientation.
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite().getClockWise()).setValue(OPEN, false).setValue(HALF, DoubleBlockHalf.LOWER);
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
			return neighborState.is(this) && neighborState.getValue(HALF) != half
					? state.setValue(FACING, neighborState.getValue(FACING)).setValue(OPEN, neighborState.getValue(OPEN))
					: Blocks.AIR.defaultBlockState();
		}
		if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(state))) {
			preventCreativeDropFromBottomPart(level, pos, state, player);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	protected static void preventCreativeDropFromBottomPart(Level level, BlockPos pos, BlockState state, Player player) {
		DoubleBlockHalf half = state.getValue(HALF);
		if (half == DoubleBlockHalf.UPPER) {
			BlockPos below = pos.below();
			BlockState belowState = level.getBlockState(below);
			if (belowState.is(state.getBlock()) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER) {
				level.setBlock(below, Blocks.AIR.defaultBlockState(), 35);
				level.levelEvent(player, 2001, below, Block.getId(belowState));
			}
		}
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return true;
	}

	@Override
	public void neighborChanged(BlockState blockstate, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
		super.neighborChanged(blockstate, world, pos, neighborBlock, fromPos, moving);
		if (blockstate.getValue(HALF) != DoubleBlockHalf.LOWER || world.isClientSide) {
			return;
		}

		boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
		boolean open = blockstate.getValue(OPEN);
		if (powered == open) {
			return;
		}

		setOpen(world, pos, blockstate, powered);
		world.playSound(null, pos, powered ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public static void setOpen(Level world, BlockPos lowerPos, BlockState lowerState, boolean open) {
		world.setBlock(lowerPos, lowerState.setValue(OPEN, open), 2);
		BlockPos upperPos = lowerPos.above();
		BlockState upperState = world.getBlockState(upperPos);
		if (upperState.getBlock() instanceof BulkheadBlock && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
			world.setBlock(upperPos, upperState.setValue(OPEN, open).setValue(FACING, lowerState.getValue(FACING)), 2);
		}
	}

	@Override
	public InteractionResult useWithoutItem(BlockState blockstate, Level world, BlockPos pos, Player entity, BlockHitResult hit) {
		BlockPos lowerPos = blockstate.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
		BlockState lowerState = world.getBlockState(lowerPos);
		if (!(lowerState.getBlock() instanceof BulkheadBlock)) {
			return InteractionResult.PASS;
		}
		BulkheadOnBlockRightclickedProcedure.execute(world, lowerPos.getX(), lowerPos.getY(), lowerPos.getZ(), lowerState);
		return InteractionResult.SUCCESS;
	}
}
