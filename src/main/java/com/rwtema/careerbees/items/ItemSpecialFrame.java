package com.rwtema.careerbees.items;

import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBeeModifier;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class ItemSpecialFrame<T extends IBeeModifier> extends ItemBaseFrame {
	public ItemSpecialFrame(int maxDamageIn) {
		super(new DefaultBeeModifier(), maxDamageIn);
	}

	@Nonnull
	@Override
	public abstract T getBeeModifier(ItemStack frame) ;
}
