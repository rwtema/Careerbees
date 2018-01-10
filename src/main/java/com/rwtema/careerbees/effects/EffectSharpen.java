package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class EffectSharpen extends EffectAttributeBoost {
	public static final EffectSharpen INSTANCE = new EffectSharpen();

//	static {
//		test(getDoubleUnaryOperator(5.0, 8.0, 100.0), 0);
//	}

	public EffectSharpen() {
		super("sharpen", false, false, 20, 0.2F, UUID.fromString("78EE8D4A-80DC-4639-9A4E-266B7591794D"));
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, ItemStack stack, IBeeHousing housing) {
		return modifyStack(stack);
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		ItemStack stack1 = modifyStack(stack);
		return stack1 != null && (!stack1.isItemEqual(stack) || !Objects.equals(stack.getTagCompound(), stack1.getTagCompound()));
	}

	private ItemStack modifyStack(ItemStack stack) {
		if (stack.getItem() instanceof ItemSword || stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
			return boostStat(
					stack,
					SharedMonsterAttributes.ATTACK_DAMAGE,
					EntityEquipmentSlot.MAINHAND, 0, getDoubleUnaryOperator(5.0, 8.0, 100.0), 0
			);
		}
		return null;
	}

}
