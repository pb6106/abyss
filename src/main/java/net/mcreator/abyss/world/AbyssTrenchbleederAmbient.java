package net.mcreator.abyss.world;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import net.mcreator.abyss.init.AbyssModSounds;
import net.mcreator.abyss.AbyssMod;

@EventBusSubscriber(modid = AbyssMod.MODID, value = Dist.CLIENT)
public class AbyssTrenchbleederAmbient {
	private static final ResourceLocation ABYSS_DIMENSION = ResourceLocation.parse("abyss:abyss");
	private static final int MIN_DELAY_TICKS = 15 * 60 * 20;
	private static final int MAX_DELAY_TICKS = 45 * 60 * 20;
	private static final float MIN_VOLUME = 0.05F;
	private static final float MAX_VOLUME = 0.15F;
	private static final double MIN_DISTANCE = 24.0;
	private static final double MAX_DISTANCE = 64.0;

	private static int ticksUntilNext = -1;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientLevel level = minecraft.level;

		if (player == null || level == null || minecraft.isPaused()) {
			return;
		}

		if (!level.dimension().location().equals(ABYSS_DIMENSION)) {
			ticksUntilNext = -1;
			return;
		}

		if (ticksUntilNext < 0) {
			ticksUntilNext = nextDelay(level.random);
		}

		if (--ticksUntilNext > 0) {
			return;
		}

		playDistantCue(level, player, level.random);
		ticksUntilNext = nextDelay(level.random);
	}

	private static int nextDelay(RandomSource random) {
		return Mth.nextInt(random, MIN_DELAY_TICKS, MAX_DELAY_TICKS);
	}

	private static void playDistantCue(ClientLevel level, LocalPlayer player, RandomSource random) {
		double angle = random.nextDouble() * Math.PI * 2.0;
		double distance = Mth.nextDouble(random, MIN_DISTANCE, MAX_DISTANCE);
		double x = player.getX() + Math.cos(angle) * distance;
		double y = player.getY() + Mth.nextDouble(random, -8.0, 8.0);
		double z = player.getZ() + Math.sin(angle) * distance;
		float volume = Mth.nextFloat(random, MIN_VOLUME, MAX_VOLUME);

		level.playLocalSound(x, y, z, AbyssModSounds.TRENCHBLEEDER.get(), SoundSource.AMBIENT, volume, 1.0F, false);
	}
}
