package net.mcreator.abyss.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcreator.abyss.AbyssMod;

public class AbyssModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AbyssMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ABYSS_TAB = REGISTRY.register("abyss_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.abyss.abyss_tab")).icon(() -> new ItemStack(Items.WATER_BUCKET)).displayItems((parameters, tabData) -> {
				tabData.accept(AbyssModBlocks.BULKHEAD.get().asItem());
				tabData.accept(AbyssModBlocks.REINFORCED_ABYSSTONE.get().asItem());
				tabData.accept(AbyssModBlocks.ABYSSTONE.get().asItem());
				tabData.accept(AbyssModBlocks.GRATE.get().asItem());
			}).build());
}