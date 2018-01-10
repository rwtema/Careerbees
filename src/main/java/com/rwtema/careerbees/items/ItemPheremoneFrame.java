package com.rwtema.careerbees.items;

import forestry.api.apiculture.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nonnull;
import java.util.Random;

public class ItemPheremoneFrame extends ItemBaseFrame {
	public ItemPheremoneFrame() {
		super(new DefaultBeeModifier(), 80);
	}

	@Nonnull
	@Override
	public ItemStack frameUsed(@Nonnull IBeeHousing housing, @Nonnull ItemStack frame, @Nonnull IBee queen, int wear) {
		IBeeHousingInventory beeInventory = housing.getBeeInventory();
		Random rand = housing.getWorldObj().rand;
		if (rand.nextInt(4) == 0) {
			IAlleleBeeSpecies species = rand.nextBoolean() ?  queen.getGenome().getPrimary() : queen.getGenome().getSecondary();
			ItemStack product = getPheremoneStack(species);
			beeInventory.addProduct(product, false);
		}
		return super.frameUsed(housing, frame, queen, wear);
	}

	@Nonnull
	public static ItemStack getPheremoneStack(@Nonnull IAlleleBeeSpecies species) {
		ItemStack product = ItemIngredients.IngredientType.PHEREMONES.get();
		product.setTagInfo("species", new NBTTagString(species.getUID()));
		return product;
	}
}
