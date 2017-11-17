package com.rwtema.careerbees.blocks;

import com.rwtema.careerbees.MCTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.Validate;

public class TESRPlantFrame extends TileEntitySpecialRenderer<TileFlowerPedastal> {
	private RenderEntityItem travelingItemRender;
	private EntityItem travelingEntityItem = new EntityItem(null);


	{
		Minecraft minecraft = Minecraft.getMinecraft();
		travelingItemRender = new RenderEntityItem(Validate.notNull(minecraft.getRenderManager()), Validate.notNull(minecraft.getRenderItem())) {

			@Override
			public boolean shouldBob() {

				return true;
			}

			@Override
			public boolean shouldSpreadItems() {

				return false;
			}
		};
		travelingEntityItem.hoverStart = 0;
	}

	@Override
	public void render(TileFlowerPedastal te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		ItemStack stack = te.getStack();
		if (stack.isEmpty()) return;

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(x + 0.5, y + 0.9, z + 0.5);

			short positionRandom = (short)MathHelper.getPositionRandom(te.getPos());
			travelingEntityItem.hoverStart = MCTimer.renderTimer / 64F + positionRandom;
			travelingEntityItem.setItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
			travelingItemRender.doRender(travelingEntityItem, 0, -0.3F, 0, 0, 0);
		}
		GlStateManager.popMatrix();
	}
}
