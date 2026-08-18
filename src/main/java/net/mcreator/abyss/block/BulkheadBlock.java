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
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3f, 1000f).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK)
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
		VoxelShape shape = open ? nativeOpenShape() : nativeClosedShape();
		int turns = (facing.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
		for (int i = 0; i < turns; i++) {
			shape = rotateYClockwise(shape);
		}
		return shape;
	}

	private static VoxelShape nativeClosedShape() {
		return Shapes.or(box(-1, 31, 0, 17, 32, 16), box(-1, 0, -1, 17, 32, 1), box(-1, 0, 16, 17, 32, 17), box(-1, 0, -1, 17, 1, 17), box(3, 0, 0, 13, 31, 12),
				box(3, 0, 13, 13, 12, 16), box(3, 18, 12, 13, 31, 16), box(3, 0, 12, 13, 13, 13), box(5, 12, 12, 11, 18, 16));
	}

	private static VoxelShape nativeOpenShape() {
		return Shapes.or(box(-1, 31, 0, 17, 32, 16), box(-1, 0, -1, 17, 32, 1), box(-1, 0, 16, 17, 32, 17), box(-1, 0, -1, 17, 1, 17), box(3, 0, 0, 13, 12, 1),
				box(3, 18, 0, 13, 31, 1), box(5, 12, 0, 11, 18, 1));
	}

	private static VoxelShape rotateYClockwise(VoxelShape shape) {
		VoxelShape[] rotated = new VoxelShape[]{Shapes.empty()};
		shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> rotated[0] = Shapes.or(rotated[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
		return rotated[0];
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
		if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
			return null;
		}

		Direction facing = context.getHorizontalDirection();
		if (context.getClickedFace().getAxis().isHorizontal()) {
			facing = context.getClickedFace().getOpposite();
		}

		return this.defaultBlockState().setValue(FACING, facing.getClockWise()).setValue(OPEN, false).setValue(HALF, DoubleBlockHalf.LOWER);
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
		if (!level.isClientSide && player.isCreative()) {
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
