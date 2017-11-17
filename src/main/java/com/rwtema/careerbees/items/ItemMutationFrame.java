package com.rwtema.careerbees.items;

import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBeeGenome;

public class ItemMutationFrame extends ItemBaseFrame {
	public ItemMutationFrame() {
		super(new DefaultBeeModifier() {
			@Override
			public float getMutationModifier(IBeeGenome genome, IBeeGenome mate, float currentModifier) {
				return 10;
			}

			@Override
			public float getGeneticDecay(IBeeGenome genome, float currentModifier) {
				return 3;
			}
		}, 20);
	}
}
