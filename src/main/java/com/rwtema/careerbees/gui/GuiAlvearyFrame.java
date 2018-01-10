package com.rwtema.careerbees.gui;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.blocks.TileAlvearyHiveFrameHolder;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiAlvearyFrame extends GuiContainer {
	private static final ResourceLocation SETTINGS_GUI_TEXTURE = new ResourceLocation(BeeMod.MODID, "textures/gui/frame_holder.png");
	private final EntityPlayer player;

	public GuiAlvearyFrame(TileAlvearyHiveFrameHolder tile, EntityPlayer player) {
		super(new ContainerAlvearyFrame(tile, player));
		this.player = player;
		this.xSize = 176;
		this.ySize = 162;
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(SETTINGS_GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}


	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(new ItemStack(BeeMod.instance.alvearyHiveFrameHolder).getDisplayName(), 8, 6, 4210752);
		this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, 64, 4210752);
	}
}
