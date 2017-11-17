package com.rwtema.careerbees.colors;

import com.rwtema.careerbees.MCTimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Color;


public class RainbowBeeColors extends CustomBeeSpriteColorProviderBase {
	@Override
	@SideOnly(Side.CLIENT)
	protected int getSecondaryColour() {
		return Color.HSBtoRGB(MCTimer.renderTimer / 64F + 0.05F, 1, 0.5F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected int getPrimaryColour() {
		return Color.HSBtoRGB(MCTimer.renderTimer / 256F  , 1, 1);
	}


}
