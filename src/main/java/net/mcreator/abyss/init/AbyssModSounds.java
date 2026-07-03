/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.abyss.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.mcreator.abyss.AbyssMod;

public class AbyssModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, AbyssMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> SN = REGISTRY.register("sn", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("abyss", "sn")));
}