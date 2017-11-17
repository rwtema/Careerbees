package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;

public class EffectSoaring extends EffectBase {
	public static final EffectSoaring INSTANCE = new EffectSoaring("soaring");

	public EffectSoaring(String rawname) {
		super(rawname);
	}

	public EffectSoaring(String rawname, boolean isDominant, boolean isCombinable) {
		super(rawname, isDominant, isCombinable);
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		Item value = ForgeRegistries.ITEMS.getValue(new ResourceLocation("extrautils2", "fakecopy"));
		if(value != null){
			housing.getBeeInventory().addProduct(new ItemStack(value, 1, 1), false);
		}

		return storedData;
	}
}
