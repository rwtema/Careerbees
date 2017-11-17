package com.rwtema.careerbees.items;

import com.rwtema.careerbees.BeeMod;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IHiveFrame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class ItemBaseFrame extends Item implements IHiveFrame {
	protected final IBeeModifier beeModifier;

	public ItemBaseFrame(IBeeModifier beeModifier, int maxDamageIn) {
		this.beeModifier = beeModifier;
		this.setMaxStackSize(1);
		this.setMaxDamage(maxDamageIn);
		this.setCreativeTab(BeeMod.creativeTab);
	}

	@Nonnull
	@Override
	public IBeeModifier getBeeModifier() {
		return beeModifier;
	}

	@Override
	@Nonnull
	public IBeeModifier getBeeModifier(ItemStack frame) {
		return beeModifier;
	}

	@Nonnull
	@Override
	public ItemStack frameUsed(@Nonnull IBeeHousing housing, @Nonnull ItemStack frame, @Nonnull IBee queen, int wear) {
		frame.attemptDamageItem(wear, housing.getWorldObj().rand, null);
		if (frame.getItemDamage() >= frame.getMaxDamage()) {
			return ItemStack.EMPTY;
		} else {
			return frame;
		}
	}
}
