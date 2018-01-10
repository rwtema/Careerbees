package com.rwtema.careerbees.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public interface BeeCraftingInputEntry extends Predicate<ItemStack> {
	@Nonnull
	@SideOnly(Side.CLIENT)
	List<ItemStack> getJEIInputs();
}
