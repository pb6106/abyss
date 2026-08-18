package net.mcreator.abyss.world;

import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.sounds.Music;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import net.mcreator.abyss.init.AbyssModSounds;
import net.mcreator.abyss.AbyssMod;

@EventBusSubscriber(modid = AbyssMod.MODID, value = Dist.CLIENT)
public class AbyssMusicHandler {
	private static final ResourceLocation ABYSS_DIMENSION = ResourceLocation.parse("abyss:abyss");
	private static Music abyssMusic;

	private static Music abyssMusic() {
		if (abyssMusic == null) {
			abyssMusic = new Music(AbyssModSounds.SN, 2000, 6000, true);
		}
		return abyssMusic;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onSelectMusic(SelectMusicEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || !minecraft.level.dimension().location().equals(ABYSS_DIMENSION)) {
			return;
		}
		event.overrideMusic(abyssMusic());
	}
}
