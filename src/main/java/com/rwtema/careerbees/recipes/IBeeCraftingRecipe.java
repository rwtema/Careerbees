package com.rwtema.careerbees.recipes;

import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public interface IBeeCraftingRecipe {
	ItemStack getOutput(Map<BeeCraftingInputEntry, ItemStack> inputs);

	List<BeeCraftingInputEntry> getInputs();

	default boolean consumeStack(BeeCraftingInputEntry slot, Map<BeeCraftingInputEntry, ItemStack> inputs){
		return true;
	}
}
