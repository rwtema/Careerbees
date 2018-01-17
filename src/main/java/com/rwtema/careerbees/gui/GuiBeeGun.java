package com.rwtema.careerbees.gui;

import com.google.common.collect.ImmutableList;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.handlers.SimpleFluidTank;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.Validate;

public class GuiBeeGun extends GuiContainer {
	public static final ResourceLocation BEEGUN_GUI_TEXTURE = new ResourceLocation(BeeMod.MODID, "textures/gui/gun.png");
	private ContainerBeeGun containerBeeGun;
	private EntityPlayer player;

	public GuiBeeGun(ContainerBeeGun containerBeeGun, EntityPlayer player) {
		super(containerBeeGun);
		this.containerBeeGun = containerBeeGun;
		this.player = player;
		this.xSize = 176;
		this.ySize = 178;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BEEGUN_GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		Slot hoveredSlot = getSlotUnderMouse();
		if (this.mc.player.inventory.getItemStack().isEmpty() && hoveredSlot != null && hoveredSlot.getHasStack()) {
			this.renderToolTip(hoveredSlot.getStack(), mouseX, mouseY);
		} else if (isPointInRegion(151, 20,16, 58, mouseX , mouseY)) {
			SimpleFluidTank capability = (SimpleFluidTank) Validate.notNull(containerBeeGun.stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));


			this.drawHoveringText(ImmutableList.of(capability.getAllowedFluid().getLocalizedName(capability.getFluid()),  capability.getFluidAmount() + " / " + capability.getCapacity()), mouseX, mouseY, fontRenderer);
		}
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(containerBeeGun.stack.getDisplayName(), 8, 6, 4210752);
		this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, 96 - 9 - 1, 4210752);


		IFluidTank capability = (IFluidTank) Validate.notNull(containerBeeGun.stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
		FluidStack fluid = capability.getFluid();
		if (fluid != null && fluid.amount > 0) {
			int capacity = capability.getCapacity();
			this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			TextureMap textureMapBlocks = mc.getTextureMapBlocks();
			ResourceLocation fluidStill = fluid.getFluid().getStill(fluid);
			TextureAtlasSprite fluidStillSprite = null;
			if (fluidStill != null) {
				fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
			}
			if (fluidStillSprite == null) {
				fluidStillSprite = textureMapBlocks.getMissingSprite();
			}

			int fluidColor = fluid.getFluid().getColor(fluid);

			int h = fluid.amount * 58 / capacity;


			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			float red = (fluidColor >> 16 & 0xFF) / 255.0F;
			float green = (fluidColor >> 8 & 0xFF) / 255.0F;
			float blue = (fluidColor & 0xFF) / 255.0F;

			GlStateManager.color(red, green, blue, 1.0F);

			drawTexturedModalRect(151, 20 + 58 - h, fluidStillSprite, 16, h);

			GlStateManager.color(red, green, blue, 1.0F);
			this.mc.getTextureManager().bindTexture(BEEGUN_GUI_TEXTURE);
			drawTexturedModalRect(151, 20, 240, 106, 16, 58);
		}

//		for (SlotItemHandler slot : containerBeeGun.beeSlots) {
//			if (this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY) && slot.isEnabled()) {
//				GlStateManager.disableLighting();
//				GlStateManager.disableDepth();
//				int j1 = slot.xPos;
//				int k1 = slot.yPos;
//				GlStateManager.colorMask(true, true, true, false);
//				drawTexturedModalRect(j1 - 6, k1 - 4, 0, 182, 26, 22);
//				GlStateManager.colorMask(true, true, true, true);
//				GlStateManager.enableLighting();
//				GlStateManager.enableDepth();
//			}
//		}
	}
}
