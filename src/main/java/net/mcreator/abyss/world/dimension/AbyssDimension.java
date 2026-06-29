package net.mcreator.abyss.world.dimension;

import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;

import org.joml.Vector3f;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer.FogMode;

import net.mcreator.abyss.world.AbyssWorldHelper;

public class AbyssDimension {
	public static final ResourceLocation ABYSS_DIMENSION = ResourceLocation.parse("abyss:abyss");

	public static float getDepthFactor(double y) {
		return Mth.clamp((float) (AbyssWorldHelper.SEA_LEVEL - y) / AbyssWorldHelper.FOG_DEPTH, 0.0F, 1.0F);
	}

	private static boolean isAbyss(ClientLevel level) {
		return level != null && level.dimension().location().equals(ABYSS_DIMENSION);
	}

	private static Vector3f getAbyssFogColor(float depth) {
		float surfaceR = 0.02F;
		float surfaceG = 0.035F;
		float surfaceB = 0.07F;
		float deepR = 0.006F;
		float deepG = 0.01F;
		float deepB = 0.022F;

		return new Vector3f(Mth.lerp(depth, surfaceR, deepR), Mth.lerp(depth, surfaceG, deepG), Mth.lerp(depth, surfaceB, deepB));
	}

	private static void applyAbyssFogDistance(float depth, float renderDistance) {
		float maxView = renderDistance * 16.0F;
		float fogStart = Mth.lerp(depth, maxView * 0.05F, maxView * 0.01F);
		float fogEnd = Mth.lerp(depth, maxView * 0.8F, maxView * 0.35F);
		RenderSystem.setShaderFogStart(fogStart);
		RenderSystem.setShaderFogEnd(fogEnd);
	}

	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerFluidType(new IClientFluidTypeExtensions() {
			@Override
			public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
				if (!isAbyss(level)) {
					return fluidFogColor;
				}

				return getAbyssFogColor(getDepthFactor(camera.getPosition().y));
			}

			@Override
			public void modifyFogRender(Camera camera, FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
				ClientLevel level = Minecraft.getInstance().level;
				if (!isAbyss(level)) {
					IClientFluidTypeExtensions.DEFAULT.modifyFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
					return;
				}

				applyAbyssFogDistance(getDepthFactor(camera.getPosition().y), renderDistance);
			}
		}, Fluids.WATER.getFluidType());
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class AbyssSpecialEffectsHandler {
		@SubscribeEvent
		public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
			DimensionSpecialEffects customEffect = new DimensionSpecialEffects(1256f, false, DimensionSpecialEffects.SkyType.NONE, false, false) {
				@Override
				public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
					return new Vec3(0.012, 0.02, 0.04);
				}

				@Override
				public boolean isFoggyAt(int x, int y) {
					return true;
				}
			};
			event.register(ABYSS_DIMENSION, customEffect);
		}

		@SubscribeEvent
		public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
			ClientLevel level = Minecraft.getInstance().level;
			if (!isAbyss(level)) {
				return;
			}

			Vector3f fogColor = getAbyssFogColor(getDepthFactor(event.getCamera().getPosition().y));
			event.setRed(fogColor.x());
			event.setGreen(fogColor.y());
			event.setBlue(fogColor.z());
		}
	}
}
