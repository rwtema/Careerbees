package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class EffectRepair extends EffectItemModification {
	public static final EffectRepair INSTANCE = new EffectRepair();

	public EffectRepair() {
		super("repair", false, false, 20, 0.2F);
	}

	@Override
	public ItemStack modifyStack(IBeeGenome genome, @Nonnull ItemStack stack, IBeeHousing housing) {
		if (!stack.getItem().isRepairable()) {
			return null;
		}

		int metadata = stack.getMetadata();
		if (metadata <= 0) {
			return null;
		}

		ItemStack copy = stack.copy();
		copy.setItemDamage(metadata - 1);
		return copy;
	}

	@Override
	public boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, ItemStack oldStack, @Nonnull ItemStack newStack, IBeeHousing housing) {
		return newStack.getMetadata() == 0;
	}

	@Override
	public boolean acceptItemStack(@Nonnull ItemStack stack) {
		return stack.getItem().isRepairable() && stack.getMetadata() > 0;
	}
}
