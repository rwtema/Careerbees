package com.rwtema.careerbees;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class BeeModCreativeTab extends CreativeTabs {
	public BeeModCreativeTab(String label) {
		super(label);
	}

	@Nonnull
	@Override
	public ItemStack getIconItemStack() {
		NonNullList<ItemStack> list = NonNullList.create();
		displayAllRelevantItems(list);
		int i = MCTimer.clientTimer / 1024;
		return list.get(i % list.size());
	}

	@Nonnull
	@Override
	public ItemStack getTabIconItem() {
		return ItemStack.EMPTY;
	}
}
