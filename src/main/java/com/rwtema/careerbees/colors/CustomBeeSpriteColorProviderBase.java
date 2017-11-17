package com.rwtema.careerbees.colors;

import forestry.api.apiculture.IBeeSpriteColourProvider;

public abstract class CustomBeeSpriteColorProviderBase implements IBeeSpriteColourProvider {
	@Override
	public int getSpriteColour(int renderPass) {
		if (renderPass == 0) {
			return getPrimaryColour();
		}
		if (renderPass == 1) {
			return getSecondaryColour();
		}
		return 0xffffff;
	}


	protected int getSecondaryColour(){
		return 0xffffff;
	}


	protected int getPrimaryColour(){
		return 0xffffff;
	}


}
