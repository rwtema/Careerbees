package com.rwtema.careerbees.items;

import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBeeGenome;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemEternalFrame extends ItemBaseFrame {
	public static final ThreadLocal<Boolean> checkProduction = ThreadLocal.withInitial(() -> false);
	public ItemEternalFrame() {
		super(new DefaultBeeModifier(){
			@Override
			public float getProductionModifier(IBeeGenome genome, float currentModifier) {
				if(checkProduction.get()) return 1;
				return 0;
			}

			@Override
			public float getLifespanModifier(IBeeGenome genome, @Nullable IBeeGenome mate, float currentModifier) {
				return Math.max(currentModifier, 1000000);
			}
		}, 0);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(Lang.translateArgs("Production: x%s", 0));
		tooltip.add(Lang.translateArgs("Lifespan: x%s", 1000000));
	}
}
