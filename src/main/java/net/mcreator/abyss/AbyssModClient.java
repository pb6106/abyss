package net.mcreator.abyss;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

import net.mcreator.abyss.client.AbyssalPressurizerRenderer;
import net.mcreator.abyss.client.BulkheadRenderer;
import net.mcreator.abyss.init.AbyssModBlockEntities;

@EventBusSubscriber(modid = AbyssMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AbyssModClient {
 @SubscribeEvent
 public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
  event.registerBlockEntityRenderer(AbyssModBlockEntities.BULKHEAD.get(), BulkheadRenderer::new);
  event.registerBlockEntityRenderer(AbyssModBlockEntities.ABYSSAL_PRESSURIZER.get(), AbyssalPressurizerRenderer::new);
 }

 @SubscribeEvent
 public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
  event.register(ModelResourceLocation.standalone(AbyssalPressurizerRenderer.BASE_MODEL));
  event.register(ModelResourceLocation.standalone(AbyssalPressurizerRenderer.ARM_MODEL));
  event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/bulkhead_frame")));
  event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/bulkhead_door")));
  event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(AbyssMod.MODID, "block/bulkhead_handle")));
 }
}
