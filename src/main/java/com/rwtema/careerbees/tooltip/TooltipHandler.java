package com.rwtema.careerbees.tooltip;

import com.google.common.collect.ImmutableList;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.*;
import forestry.api.genetics.IAllele;
import forestry.core.gui.GuiAlyzer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

public class TooltipHandler {
	Class<? extends GuiContainer> guiLyzer;

	public void init() {
		try {
			//noinspection unchecked
			guiLyzer = (Class<? extends GuiContainer>) Class.forName("forestry.core.gui.GuiAlyzer");
		} catch (ClassNotFoundException e) {
			return;
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tooltipAdd(@Nonnull ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		if (!itemStack.isEmpty()) {
			EnumBeeType type = BeeManager.beeRoot.getType(itemStack);
			IBee member = BeeManager.beeRoot.getMember(itemStack);
			if (type != null && member != null) {
				if (BeeMod.deobf && BeeMod.MODID.equals(member.getGenome().getPrimary().getModID())) {
					event.getToolTip().add("CareerBee");
				}

				if (type == EnumBeeType.DRONE && member.getGenome().getPrimary() == CareerBeeSpecies.STUDENT.get() && member.getGenome().getSecondary() == CareerBeeSpecies.STUDENT.get()) {
					event.getToolTip().add(net.minecraft.util.text.translation.I18n.translateToLocal("careerbees.message.student.bee"));
				}
			}
		}
	}

	@SubscribeEvent
	public void drawTooltip(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player == null || !player.inventory.getItemStack().isEmpty()) return;

		GuiScreen gui = event.getGui();
		if (guiLyzer.isInstance(gui)) {
			GuiContainer container = guiLyzer.cast(gui);
			NonNullList<ItemStack> inventory = container.inventorySlots.getInventory();
			if (inventory.size() >= 3) {
				ItemStack itemStack = inventory.get(inventory.size() - 5);
				IBee bee = BeeManager.beeRoot.getMember(itemStack);
				if (bee == null) {
					return;
				}

				int x = event.getMouseX() - container.getGuiLeft();
				int y = event.getMouseY() - container.getGuiTop();
				int dy = 12 + 12 * 10;

				IBeeGenome genome = bee.getGenome();
				if (x > GuiAlyzer.COLUMN_1 && x < GuiAlyzer.COLUMN_2 && y >= dy && y < (dy + 12)) {
					IAllele effect = genome.getChromosomes()[EnumBeeChromosome.EFFECT.ordinal()].getPrimaryAllele();
					addTooltip(event, container, effect, genome);
				}
				if (x > GuiAlyzer.COLUMN_2 && x < (GuiAlyzer.COLUMN_2 + (GuiAlyzer.COLUMN_2 - GuiAlyzer.COLUMN_1)) && y >= dy && y < (dy + 12)) {
					IAllele effect = genome.getChromosomes()[EnumBeeChromosome.EFFECT.ordinal()].getSecondaryAllele();
					addTooltip(event, container, effect, genome);
				}

			}
		}
	}

	private void addTooltip(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event, @Nonnull GuiContainer container, @Nonnull IAllele effect, @Nonnull IBeeGenome genome) {
		String key = effect.getUnlocalizedName() + ".desc";

		ImmutableList<String> list;
		if (I18n.hasKey(key)) {
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
			if (effect instanceof EffectBase && !((EffectBase) effect).isValidSpecies(genome)) {
				Set<IAlleleBeeSpecies> validSpecies = ((EffectBase) effect).validSpecies;
				if (validSpecies.size() == 0) {
					list = ImmutableList.of(I18n.format(key));
				} else {
					list = ImmutableList.of(I18n.format(key), TextFormatting.RED.toString() + TextFormatting.BOLD.toString() + Lang.translateArgs("Only the bee species, %s, can use this effect!", validSpecies.stream().map(IAllele::getAlleleName).map(s -> '"' + s + '"').collect(Collectors.joining(","))));
				}
			} else
				list = ImmutableList.of(I18n.format(key));

			GuiUtils.drawHoveringText(list, event.getMouseX(), event.getMouseY(), container.width, container.height, -1, fontRenderer);
		}
	}

}
