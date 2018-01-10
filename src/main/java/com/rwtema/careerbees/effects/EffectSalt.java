package com.rwtema.careerbees.effects;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.IBeeGenome;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EffectSalt extends EffectFoodModify {
	public EffectSalt(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectSalt(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Override
	protected boolean shouldBeginEattingOverride(EntityPlayer entityPlayer, ItemStack itemStack, NBTTagCompound compoundTag) {
		return false;
	}

	@Override
	protected void addTooltip(ItemStack itemStack, @Nonnull NBTTagCompound compoundTag, @Nonnull List<String> toolTip) {
		float current = compoundTag.getFloat("current");
		if (current > 0) {
			toolTip.add(ChatFormatting.YELLOW + Lang.translateArgs("Salted: +%s Hunger Filled, -%s Saturation",
					NumberFormat.getPercentInstance(Locale.UK).format(current),
					NumberFormat.getPercentInstance(Locale.UK).format(current / 2)
			));
		}
	}

	@Override
	public void callback(@Nonnull EntityPlayer player, @Nonnull NBTTagCompound tag, @Nonnull ItemStack stack) {
		float current = tag.getFloat("current");
		if (!(stack.getItem() instanceof ItemFood)) {
			return;
		}

		ItemFood food = (ItemFood) stack.getItem();
		int healAmount = food.getHealAmount(stack);
		float saturationModifier = food.getSaturationModifier(stack);

		FoodStats foodStats = player.getFoodStats();
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		foodStats.writeNBT(nbtTagCompound);
		int curFoodLevel = nbtTagCompound.getInteger("foodLevel");
		float curSaturation = nbtTagCompound.getFloat("foodSaturationLevel");
		int newFoodLevel = Math.min(Math.round(healAmount * current + curFoodLevel), 20);
		float newFoodSaturationLevel = Math.max(curSaturation - curFoodLevel * saturationModifier * current, 0);
		nbtTagCompound.setInteger("foodLevel", newFoodLevel);
		nbtTagCompound.setFloat("foodSaturationLevel", newFoodSaturationLevel);
		foodStats.readNBT(nbtTagCompound);
	}

	@Override
	protected boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, @Nonnull NBTTagCompound curTag) {
		return curTag.getFloat("current") >= MAX;
	}

	@Nullable
	@Override
	protected NBTTagCompound addData(IBeeGenome genome, ItemStack stack, @Nullable NBTTagCompound prevTag) {
		float current = prevTag != null ? prevTag.getFloat("current") : 0;
		if (current >= MAX) {
			return null;
		}
		float newCurrent = Math.min(current * 0.75F + MAX * 0.25F + 0.05F, MAX);
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		nbtTagCompound.setFloat("current", newCurrent);
		return nbtTagCompound;
	}

	@Override
	public boolean acceptItemStack(ItemStack stack, @Nullable NBTTagCompound curTag) {
		return curTag == null || curTag.getFloat("current") < MAX;
	}

	final float MAX = 2;
}
