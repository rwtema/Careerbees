package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.items.ItemIngredients;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class EffectHoneyCombIngotConvert extends EffectItemModification {
	public static final EffectHoneyCombIngotConvert INSTANCE = new EffectHoneyCombIngotConvert("honey_comb", 200);

	public EffectHoneyCombIngotConvert(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, ItemStack stack, @Nullable IBeeHousing housing) {
		NonNullList<ItemStack> ingotIron = OreDictionary.getOres("ingotIron");
		for (ItemStack target : ingotIron) {
			if (OreDictionary.itemMatches(target, stack, false)) {
				return ItemIngredients.IngredientType.INGOTHONEYCOLM.get();
			}
		}
		return null;
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		NonNullList<ItemStack> ingotIron = OreDictionary.getOres("ingotIron");
		for (ItemStack target : ingotIron) {
			if (OreDictionary.itemMatches(target, stack, false)) {
				return true;
			}
		}
		return false;
	}
}
