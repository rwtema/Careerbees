package com.rwtema.careerbees.items;

import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemCoalFrame extends ItemSpecialFrame<ItemCoalFrame.CoalModifier> {

	public ItemCoalFrame(int maxDamageIn) {
		super(maxDamageIn);
	}

	@Nonnull
	@Override
	public CoalModifier getBeeModifier(ItemStack frame) {
		return new CoalModifier(frame);
	}

	@Nonnull
	@Override
	public ItemStack frameUsed(@Nonnull IBeeHousing housing, @Nonnull ItemStack frame, @Nonnull IBee queen, int wear) {
		return frame;
	}

	public static class CoalModifier extends DefaultBeeModifier {
		final ItemStack stack;
		public CoalModifier(ItemStack stack) {
			this.stack = stack;
		}
	}
}
