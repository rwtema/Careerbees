package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.blocks.BlockFlowerPedastal;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import javax.annotation.Nullable;

public class EffectSmelt extends EffectItemModification {
	public static EffectSmelt INSTANCE = new EffectSmelt("smelt", 20 * 10 / 10);

	public EffectSmelt(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, TileFlowerPedastal frame, ItemStack stack, IBeeHousing housing) {
		ItemStack smeltingResult = FurnaceRecipes.instance().getSmeltingResult(stack);
		if (smeltingResult.isEmpty()) return null;
		return smeltingResult.copy();
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
	}

	@Override
	protected BlockFlowerPedastal.ParticleType getParticleType(IBeeGenome genome, TileFlowerPedastal plantFrame, ItemStack stack, ItemStack itemStack) {
		return BlockFlowerPedastal.ParticleType.FIRE;
	}
}
