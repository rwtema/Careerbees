package com.rwtema.careerbees.colors;

import com.rwtema.careerbees.MCTimer;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class QuantumBeeColors extends CustomBeeSpriteColorProviderBase {
	final float offset, hue, sat;

	public QuantumBeeColors(float offset, float hue, float sat) {
		this.offset = offset;
		this.hue = hue;
		this.sat = sat;
	}

	@Override
	protected int getSecondaryColour() {
		return Color.HSBtoRGB(hue, sat, 0.5F* (1+0.7F *MathHelper.cos(MCTimer.renderTimer / 64F * 6F + offset)));
	}

	@Override
	protected int getPrimaryColour() {
		return Color.HSBtoRGB(hue, sat, 0.5F* (1+ 0.5F * MathHelper.cos(MCTimer.renderTimer / 64F * 6F + offset + 3F)));
	}
}
