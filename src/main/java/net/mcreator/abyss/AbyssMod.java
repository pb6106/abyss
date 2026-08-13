package net.mcreator.abyss;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.TickTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.Registries;

import net.mcreator.abyss.init.AbyssModTabs;
import net.mcreator.abyss.init.AbyssModSounds;
import net.mcreator.abyss.init.AbyssModItems;
import net.mcreator.abyss.init.AbyssModBlocks;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

@Mod("abyss")
public class AbyssMod {
	public static final Logger LOGGER = LogManager.getLogger(AbyssMod.class);
	public static final String MODID = "abyss";

	public AbyssMod(IEventBus modEventBus) {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		NeoForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::registerNetworking);
		AbyssModSounds.REGISTRY.register(modEventBus);
		AbyssModBlocks.REGISTRY.register(modEventBus);
		AbyssModItems.REGISTRY.register(modEventBus);
		AbyssModTabs.REGISTRY.register(modEventBus);
		// Start of user code block mod init
		// End of user code block mod init
	}

	// Start of user code block mod methods
	@SubscribeEvent
	public void validateStructureTemplates(ServerStartedEvent event) {
		ServerLevel level = event.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(MODID, "abyss")));
		if (level == null) {
			LOGGER.warn("Abyss dimension not loaded; skipping structure template validation");
			return;
		}
		StructureTemplateManager manager = level.getStructureManager();
		for (String name : new String[]{"seabase", "submarinewreck", "submarinewreck_graveyard", "submarinewreck_implosion", "submarinewreck_porthole", "submarinewreck_tube"}) {
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
			manager.get(id).ifPresentOrElse(template -> {
				var size = template.getSize();
				LOGGER.info("Loaded structure template {} ({}x{}x{})", id, size.getX(), size.getY(), size.getZ());
			}, () -> LOGGER.error("Failed to load structure template {} from data/{}/structure/{}.nbt", id, MODID, name));
		}
	}

	// End of user code block mod methods
	private static boolean networkingRegistered = false;
	private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

	private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
	}

	public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
		if (networkingRegistered)
			throw new IllegalStateException("Cannot register new network messages after networking has been registered");
		MESSAGES.put(id, new NetworkMessage<>(reader, handler));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void registerNetworking(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(MODID);
		MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
		networkingRegistered = true;
	}

	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	@SubscribeEvent
	public void tick(ServerTickEvent.Post event) {
		int currentTick = event.getServer().getTickCount();
		IntObjectPair<Runnable> work;
		while ((work = workToBeScheduled.poll()) != null) {
			workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
		}
		while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
			workQueue.poll().run();
		}
	}
}