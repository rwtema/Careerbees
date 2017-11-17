package com.rwtema.careerbees.items;

import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeModifier;

import javax.annotation.Nullable;

public class ItemStationaryFrame extends ItemBaseFrame {
	public ItemStationaryFrame(IBeeModifier beeModifier, int maxDamageIn) {
		super(new DefaultBeeModifier(){
			@Override
			public float getProductionModifier(IBeeGenome genome, float currentModifier) {
				return 0;
			}

			@Override
			public float getLifespanModifier(IBeeGenome genome, @Nullable IBeeGenome mate, float currentModifier) {
				return 100;
			}
		}, maxDamageIn);
	}
}
