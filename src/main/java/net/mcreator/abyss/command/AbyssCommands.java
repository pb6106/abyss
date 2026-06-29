package net.mcreator.abyss.command;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import net.mcreator.abyss.world.AbyssWorldHelper;
import net.mcreator.abyss.AbyssMod;

@EventBusSubscriber(modid = AbyssMod.MODID)
public class AbyssCommands {
	public static final ResourceKey<Level> ABYSS_LEVEL = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("abyss:abyss"));

	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("abyss").requires(source -> source.hasPermission(2)).then(Commands.literal("teleport").executes(context -> teleportToSeafloor(context.getSource()))));
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
}
