package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.recipes.OreRecipes;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectOreCrushing extends EffectItemModification {
	public static final EffectOreCrushing INSTANCE = new EffectOreCrushing("crusher", 20 * 10 / 10 * 20);

	public EffectOreCrushing(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, ItemStack stack, @Nonnull IBeeHousing housing) {
		ItemStack itemStack = OreRecipes.ORE_TO_DUST.get(stack);
		if (itemStack.isEmpty()) return null;
		itemStack.setCount(2 + housing.getWorldObj().rand.nextInt(3));
		return itemStack;
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		return OreRecipes.ORE_TO_DUST.isValid(stack);
	}
}
