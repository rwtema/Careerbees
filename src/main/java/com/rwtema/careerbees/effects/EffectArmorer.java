package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class EffectArmorer extends EffectAttributeBoost {

	public static final EffectArmorer INSTANCE = new EffectArmorer();

	static {
		test(getDoubleUnaryOperator(1.0, 50.0, 1000.0), 0);
	}

	public EffectArmorer() {
		super("armorer", false, false, 20 * 8 / 10, 0.2F, UUID.fromString("65C761AF-1772-437F-903B-1CCA6371D572"));
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, @Nonnull ItemStack stack, IBeeHousing housing) {
		return modifyStack(stack);
	}

	@Override
	public boolean acceptItemStack(@Nonnull ItemStack stack) {
		return modifyStack(stack.copy()) != null;
	}

	private ItemStack modifyStack(@Nonnull ItemStack stack) {
		EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
		if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
			return boostStat(
					stack,
					SharedMonsterAttributes.ARMOR,
					slot, 1,
					getDoubleUnaryOperator(1.0, 50.0, 1000.0),
					0
			);
		}
		return null;
	}
}
