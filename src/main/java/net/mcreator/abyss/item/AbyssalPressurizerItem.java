package net.mcreator.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class AbyssalPressurizerItem extends BlockItem {
 public AbyssalPressurizerItem(Block block, Properties properties) {
  super(block, properties);
 }

 @Override
 public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
  super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
  tooltipComponents.add(Component.translatable("tooltip.abyss.abyssal_pressurizer.prefix").withStyle(ChatFormatting.GRAY)
    .append(Component.literal("Create: Deep Seas").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
    .append(Component.translatable("tooltip.abyss.abyssal_pressurizer.suffix").withStyle(ChatFormatting.GRAY)));
 }
}
