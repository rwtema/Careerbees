package com.rwtema.careerbees.flowerproviders;

import com.rwtema.careerbees.BeeMod;
import forestry.api.apiculture.FlowerManager;

public class FloweringCrafting extends FloweringBase {
	public final static FloweringCrafting INSTANCE = new FloweringCrafting();

	public FloweringCrafting() {
		super("crafting", false);
		FlowerManager.flowerRegistry.registerAcceptableFlower(BeeMod.instance.plantFrame, getFlowerType());
	}
}
