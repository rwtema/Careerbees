package com.rwtema.careerbees.jei;

import com.rwtema.careerbees.items.ItemIngredients;
import com.rwtema.careerbees.lang.Lang;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class BeeModJEI implements IModPlugin {
	@Override
	public void register(IModRegistry registry) {
		addLocalization(registry, ItemIngredients.IngredientType.INGOTHONEYCOLM.get(), "Made by the \"Honey-Smelter Bees\" special effect from iron ingots.");
	}

	public void addLocalization(IModRegistry registry, ItemStack stack, String text) {
		registry.addIngredientInfo(stack, ItemStack.class, Lang.getKey(text));
	}
}
