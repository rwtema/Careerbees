package com.rwtema.careerbees.colors;

public class DebugBeeSpriteColors  extends CustomBeeSpriteColorProviderBase {
	public int primaryColour;
	public int secondaryColour;

	public DebugBeeSpriteColors(int primaryColour, int secondaryColour) {
		this.primaryColour = primaryColour;
		this.secondaryColour = secondaryColour;
	}

	@Override
	protected int getSecondaryColour() {
		return secondaryColour;
	}

	@Override
	protected int getPrimaryColour() {
		return primaryColour;
	}
}
