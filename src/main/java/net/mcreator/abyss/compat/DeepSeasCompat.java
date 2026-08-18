package net.mcreator.abyss.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import net.mcreator.abyss.AbyssMod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@EventBusSubscriber(modid = AbyssMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DeepSeasCompat {
	private static final String DEEP_SEAS_MOD_ID = "create_submarine";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final Map<String, HullEntry> ABYSS_HULL_DEFAULTS = Map.of(
			"abyss:abysstone", new HullEntry(100, 0.12f),
			"abyss:grate", new HullEntry(69, 0.15f),
			"abyss:reinforced_abysstone", new HullEntry(150, 0.08f),
			"abyss:bulkhead", new HullEntry(120, 0.06f));

	private record HullEntry(int maxWaterDepth, float implosionChance) {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		if (!ModList.get().isLoaded(DEEP_SEAS_MOD_ID)) {
			return;
		}
		event.enqueueWork(DeepSeasCompat::applyHullDefaults);
	}

	private static void applyHullDefaults() {
		Path configPath = FMLPaths.CONFIGDIR.get().resolve("submarine_hull.json");
		JsonObject root = readConfig(configPath);
		boolean changed = false;

		for (Map.Entry<String, HullEntry> entry : ABYSS_HULL_DEFAULTS.entrySet()) {
			JsonObject desired = toJson(entry.getValue());
			if (!root.has(entry.getKey()) || !desired.equals(root.getAsJsonObject(entry.getKey()))) {
				root.add(entry.getKey(), desired);
				changed = true;
			}
		}

		if (!changed) {
			return;
		}

		try {
			Files.createDirectories(configPath.getParent());
			Files.writeString(configPath, GSON.toJson(root));
			reloadDeepSeasHullConfig();
			AbyssMod.LOGGER.info("Updated Abyss hull defaults for Create: Deep Seas in {}", configPath);
		} catch (IOException exception) {
			AbyssMod.LOGGER.error("Failed to write Create: Deep Seas hull defaults", exception);
		}
	}

	private static void reloadDeepSeasHullConfig() {
		try {
			Class<?> configClass = Class.forName("com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig");
			Method load = configClass.getDeclaredMethod("load");
			load.invoke(null);
		} catch (ReflectiveOperationException exception) {
			AbyssMod.LOGGER.warn("Patched submarine_hull.json but could not reload Create: Deep Seas hull config", exception);
		}
	}

	private static JsonObject readConfig(Path configPath) {
		if (!Files.exists(configPath)) {
			return new JsonObject();
		}
		try {
			JsonObject root = GSON.fromJson(Files.readString(configPath), JsonObject.class);
			return root == null ? new JsonObject() : root;
		} catch (IOException exception) {
			AbyssMod.LOGGER.warn("Could not read {}, creating fresh Abyss hull entries", configPath);
			return new JsonObject();
		}
	}

	private static JsonObject toJson(HullEntry entry) {
		JsonObject object = new JsonObject();
		object.addProperty("maxWaterDepth", entry.maxWaterDepth());
		object.addProperty("implosionChance", entry.implosionChance());
		return object;
	}
}
