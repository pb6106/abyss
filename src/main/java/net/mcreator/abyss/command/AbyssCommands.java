package net.mcreator.abyss.command;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import net.mcreator.abyss.world.AbyssWorldHelper;
import net.mcreator.abyss.AbyssMod;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = AbyssMod.MODID)
public class AbyssCommands {
	public static final ResourceKey<Level> ABYSS_LEVEL = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("abyss:abyss"));

	private static final ResourceLocation SEABASE_TEMPLATE = ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "seabase");
	private static final List<ResourceLocation> WRECK_TEMPLATES = List.of(
			ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck"),
			ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_graveyard"),
			ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_implosion"),
			ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_porthole"),
			ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_tube"));
	private static final Map<String, ResourceLocation> WRECK_TYPE_ALIASES = Map.of(
			"default", ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck"),
			"graveyard", ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_graveyard"),
			"implosion", ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_implosion"),
			"porthole", ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_porthole"),
			"tube", ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "submarinewreck_tube"));

	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("abyss").requires(source -> source.hasPermission(2))
				.then(Commands.literal("teleport").executes(context -> teleportToSeafloor(context.getSource())))
				.then(Commands.literal("exit").executes(context -> exitToWorldSpawn(context.getSource())))
				.then(Commands.literal("surface").executes(context -> teleportToSurface(context.getSource())))
				.then(Commands.literal("vision").executes(context -> grantVision(context.getSource())))
				.then(Commands.literal("placestructure")
						.then(Commands.literal("seabase").executes(context -> placeStructure(context.getSource(), SEABASE_TEMPLATE)))
						.then(Commands.literal("wreck")
								.executes(context -> placeRandomWreck(context.getSource()))
								.then(Commands.argument("type", StringArgumentType.word())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(WRECK_TYPE_ALIASES.keySet(), builder))
										.executes(context -> placeWreck(context.getSource(), StringArgumentType.getString(context, "type")))))));
	}

	private static int exitToWorldSpawn(CommandSourceStack source) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command can only be used by a player."));
			return 0;
		}

		ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
		if (overworld == null) {
			source.sendFailure(Component.literal("The Overworld is not loaded."));
			return 0;
		}

		BlockPos spawn = overworld.getSharedSpawnPos();
		player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
		source.sendSuccess(() -> Component.literal("Teleported to world spawn."), true);
		return 1;
	}

	private static int teleportToSurface(CommandSourceStack source) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command can only be used by a player."));
			return 0;
		}

		ServerLevel abyssLevel = source.getServer().getLevel(ABYSS_LEVEL);
		if (abyssLevel == null) {
			source.sendFailure(Component.literal("The Abyss dimension is not loaded."));
			return 0;
		}

		double x = 0.5;
		double y = AbyssWorldHelper.SEA_LEVEL;
		double z = 0.5;
		player.teleportTo(abyssLevel, x, y, z, player.getYRot(), player.getXRot());
		source.sendSuccess(() -> Component.literal("Teleported to the Abyss surface at 0, " + AbyssWorldHelper.SEA_LEVEL + ", 0."), true);
		return 1;
	}

	private static int teleportToSeafloor(CommandSourceStack source) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command can only be used by a player."));
			return 0;
		}

		ServerLevel abyssLevel = source.getServer().getLevel(ABYSS_LEVEL);
		if (abyssLevel == null) {
			source.sendFailure(Component.literal("The Abyss dimension is not loaded."));
			return 0;
		}

		BlockPos target = AbyssWorldHelper.findSafeTeleportPos(abyssLevel, BlockPos.ZERO);
		player.teleportTo(abyssLevel, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, player.getYRot(), player.getXRot());
		source.sendSuccess(() -> Component.literal("Teleported to the Abyss near 0, 0 (Y=" + target.getY() + ")."), true);
		return 1;
	}

	private static int grantVision(CommandSourceStack source) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command can only be used by a player."));
			return 0;
		}

		player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, MobEffectInstance.INFINITE_DURATION, 255, false, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 255, false, false, false));
		source.sendSuccess(() -> Component.literal("Granted Conduit Power and Night Vision."), true);
		return 1;
	}

	private static int placeRandomWreck(CommandSourceStack source) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command can only be used by a player."));
			return 0;
		}

		ResourceLocation templateId = WRECK_TEMPLATES.get(player.serverLevel().getRandom().nextInt(WRECK_TEMPLATES.size()));
		return placeStructure(source, templateId);
	}

	private static int placeWreck(CommandSourceStack source, String type) {
		ResourceLocation templateId = WRECK_TYPE_ALIASES.get(type);
		if (templateId == null) {
			source.sendFailure(Component.literal("Unknown wreck type: " + type + ". Options: " + String.join(", ", WRECK_TYPE_ALIASES.keySet())));
			return 0;
		}

		return placeStructure(source, templateId);
	}

	private static int placeStructure(CommandSourceStack source, ResourceLocation templateId) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("This command can only be used by a player."));
			return 0;
		}

		ServerLevel level = player.serverLevel();
		StructureTemplate template = level.getStructureManager().get(templateId).orElse(null);
		if (template == null) {
			source.sendFailure(Component.literal("Structure template not found: " + templateId));
			return 0;
		}

		BlockPos origin = player.blockPosition();
		StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(Rotation.NONE);
		if (SEABASE_TEMPLATE.equals(templateId)) {
			StructureProcessorList processors = level.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST)
					.get(ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "seabase_chests"));
			if (processors != null) {
				processors.list().forEach(settings::addProcessor);
			}
		}

		template.placeInWorld(level, origin, origin, settings, level.getRandom(), Block.UPDATE_ALL);
		source.sendSuccess(() -> Component.literal("Placed " + templateId + " at " + origin.toShortString() + " (size " + template.getSize().toShortString() + ")."), true);
		return 1;
	}
}
