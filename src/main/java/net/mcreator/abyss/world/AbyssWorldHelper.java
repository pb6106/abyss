package net.mcreator.abyss.world;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public final class AbyssWorldHelper {
	public static final int SEA_LEVEL = 1000;
	public static final int FOG_DEPTH = 800;
	public static final int MIN_MOB_SPAWN_Y = 6;
	public static final int TARGET_SEAFLOOR_Y = 63;

	private AbyssWorldHelper() {
	}

	public static boolean isOpenWater(ServerLevel level, BlockPos pos) {
		return pos.getY() >= MIN_MOB_SPAWN_Y && pos.getY() < SEA_LEVEL && level.getFluidState(pos).is(FluidTags.WATER);
	}

	public static BlockPos findWaterAboveFloor(ServerLevel level, int x, int z) {
		level.getChunk(new BlockPos(x, 0, z));

		BlockPos best = null;
		for (int y = MIN_MOB_SPAWN_Y; y < SEA_LEVEL; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (!level.getFluidState(pos).is(FluidTags.WATER)) {
				continue;
			}

			BlockState below = level.getBlockState(pos.below());
			if (!below.is(Blocks.BEDROCK) && below.blocksMotion()) {
				best = pos;
			}
		}

		if (best != null) {
			return best;
		}

		for (int y = MIN_MOB_SPAWN_Y; y < SEA_LEVEL; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (level.getFluidState(pos).is(FluidTags.WATER)) {
				return pos;
			}
		}

		return null;
	}

	public static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos origin) {
		int originX = origin.getX();
		int originZ = origin.getZ();

		for (int radius = 0; radius <= 48; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
						continue;
					}

					BlockPos pos = findWaterAboveFloor(level, originX + dx, originZ + dz);
					if (pos != null) {
						return pos;
					}
				}
			}
		}

		return new BlockPos(originX, TARGET_SEAFLOOR_Y + 1, originZ);
	}
}
