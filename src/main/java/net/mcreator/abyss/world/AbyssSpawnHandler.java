package net.mcreator.abyss.world;

import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import net.mcreator.abyss.AbyssMod;

@EventBusSubscriber(modid = AbyssMod.MODID)
public class AbyssSpawnHandler {
	public static final ResourceLocation ABYSS_DIMENSION = ResourceLocation.parse("abyss:abyss");
	private static final int TARGET_FISH_NEAR_PLAYER = 12;
	private static final int TARGET_DOLPHINS_NEAR_PLAYER = 1;
	private static final int FISH_RADIUS = 96;
	private static final int DOLPHIN_RADIUS = 160;

	private static boolean isAbyssLevel(ServerLevelAccessor level) {
		Level world = level instanceof ServerLevel serverLevel ? serverLevel : level.getLevel();
		return world.dimension().location().equals(ABYSS_DIMENSION);
	}

	@SubscribeEvent
	public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
		EntityType<?> type = event.getEntityType();
		if (type != EntityType.TROPICAL_FISH && type != EntityType.DOLPHIN) {
			return;
		}

		if (!isAbyssLevel(event.getLevel())) {
			return;
		}

		if (AbyssWorldHelper.isOpenWater(event.getLevel().getLevel(), event.getPos())) {
			event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
		}
	}

	@SubscribeEvent
	public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
		EntityType<?> type = event.getEntity().getType();
		if (type != EntityType.TROPICAL_FISH && type != EntityType.DOLPHIN) {
			return;
		}

		if (!isAbyssLevel(event.getLevel())) {
			return;
		}

		int spawnY = Mth.floor(event.getY());
		if (spawnY < AbyssWorldHelper.MIN_MOB_SPAWN_Y || spawnY >= AbyssWorldHelper.SEA_LEVEL) {
			event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
		}
	}

	@SubscribeEvent
	public static void onSurfaceFreeze(BlockEvent.NeighborNotifyEvent event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
			return;
		}

		if (!serverLevel.dimension().location().equals(ABYSS_DIMENSION)) {
			return;
		}

		BlockPos pos = event.getPos();
		if (pos.getY() < AbyssWorldHelper.SEA_LEVEL - 4) {
			return;
		}

		BlockState state = serverLevel.getBlockState(pos);
		if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
			serverLevel.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
		} else if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
			serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel level)) {
			return;
		}

		if (!level.dimension().location().equals(ABYSS_DIMENSION)) {
			return;
		}

		long gameTime = level.getGameTime();
		if (gameTime % 80 == 0) {
			clearSurfaceIceAndSnow(level);
		}

		if (gameTime % 40 == 0) {
			for (ServerPlayer player : level.players()) {
				spawnAmbientSeaLifeNear(level, player);
			}
		}
	}

	private static void clearSurfaceIceAndSnow(ServerLevel level) {
		for (ServerPlayer player : level.players()) {
			BlockPos center = player.blockPosition();
			BlockPos.betweenClosedStream(center.offset(-24, -12, -24), center.offset(24, 24, 24)).forEach(pos -> {
				if (pos.getY() < AbyssWorldHelper.SEA_LEVEL - 2) {
					return;
				}

				BlockState state = level.getBlockState(pos);
				if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
					level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
				} else if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
				}
			});
		}
	}

	private static void spawnAmbientSeaLifeNear(ServerLevel level, ServerPlayer player) {
		BlockPos center = player.blockPosition();
		int fishNearby = level.getEntitiesOfClass(TropicalFish.class, new net.minecraft.world.phys.AABB(center).inflate(FISH_RADIUS), fish -> true).size();
		int dolphinsNearby = level.getEntitiesOfClass(Dolphin.class, new net.minecraft.world.phys.AABB(center).inflate(DOLPHIN_RADIUS), dolphin -> true).size();

		RandomSource random = level.random;
		int fishToSpawn = Math.min(4, TARGET_FISH_NEAR_PLAYER - fishNearby);
		for (int i = 0; i < fishToSpawn; i++) {
			trySpawnAmbientMob(level, random, center, EntityType.TROPICAL_FISH, 4, 64);
		}

		if (dolphinsNearby < TARGET_DOLPHINS_NEAR_PLAYER && random.nextInt(2) == 0) {
			trySpawnAmbientMob(level, random, center, EntityType.DOLPHIN, 8, 96);
		}
	}

	private static void trySpawnAmbientMob(ServerLevel level, RandomSource random, BlockPos center, EntityType<? extends Mob> type, int minRange, int maxRange) {
		for (int attempt = 0; attempt < 40; attempt++) {
			int x = center.getX() + random.nextInt(maxRange * 2 + 1) - maxRange;
			int z = center.getZ() + random.nextInt(maxRange * 2 + 1) - maxRange;
			int y = center.getY() + random.nextInt(49) - 24;

			if (y < AbyssWorldHelper.MIN_MOB_SPAWN_Y) {
				y = AbyssWorldHelper.MIN_MOB_SPAWN_Y + random.nextInt(64);
			}
			if (y >= AbyssWorldHelper.SEA_LEVEL) {
				y = AbyssWorldHelper.SEA_LEVEL - 1 - random.nextInt(64);
			}

			BlockPos pos = new BlockPos(x, y, z);
			if (center.distSqr(pos) < (long) minRange * minRange) {
				continue;
			}

			if (!AbyssWorldHelper.isOpenWater(level, pos)) {
				continue;
			}

			level.getChunk(pos);
			Mob mob = type.create(level);
			if (mob == null) {
				continue;
			}

			mob.setPos(x + 0.5, y, z + 0.5);
			mob.setYRot(random.nextFloat() * 360.0F);
			if (level.noCollision(mob) && level.addFreshEntity(mob)) {
				return;
			}
		}
	}
}
