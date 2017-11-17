package com.rwtema.careerbees.items;

import com.rwtema.careerbees.BeeMod;
import forestry.api.apiculture.IArmorApiarist;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class ItemBeeArmor extends ItemArmor implements IArmorApiarist {
	private static final ArmorMaterial HONEY_MATERIAL = EnumHelper.addArmorMaterial("honeycomb", BeeMod.MODID + ":beearmor", 33, new int[]{4, 7, 9, 4}, 64, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 2);


	public ItemBeeArmor(EntityEquipmentSlot equipmentSlotIn) {
		super(HONEY_MATERIAL, 0, equipmentSlotIn);
		String name = BeeMod.MODID + ":bee.armor." + equipmentSlotIn.getName().toLowerCase(Locale.ENGLISH);
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	@Nullable
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return String.format("%s:textures/armor/%s_layer_%d%s.png", BeeMod.MODID, "beearmor", (slot == EntityEquipmentSlot.LEGS ? 2 : 1), type == null ? "" : String.format("_%s", type));
	}

	@Override
	public boolean protectEntity(@Nonnull EntityLivingBase entity, @Nonnull ItemStack armor, @Nullable String cause, boolean doProtect) {
		return true;
	}
}
