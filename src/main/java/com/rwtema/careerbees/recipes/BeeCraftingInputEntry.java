package com.rwtema.careerbees.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Predicate;

public interface BeeCraftingInputEntry extends Predicate<ItemStack> {
	@SideOnly(Side.CLIENT)
	List<ItemStack> getJEIInputs();
}
