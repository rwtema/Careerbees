package com.rwtema.careerbees.effects;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.IBeeGenome;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;

public class EffectHoneyGlaze extends EffectFoodModify {

	public static final EffectHoneyGlaze INSTANCE = new EffectHoneyGlaze("glazing", 20 * 30 / 10);

	public EffectHoneyGlaze(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectHoneyGlaze(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}


	@Override
	protected boolean shouldBeginEattingOverride(@Nonnull EntityPlayer entityPlayer, ItemStack itemStack, @Nonnull NBTTagCompound compoundTag) {
		return compoundTag.getFloat("current") > entityPlayer.getAbsorptionAmount();
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void addTooltip(ItemStack itemStack, @Nonnull NBTTagCompound tag, @Nonnull List<String> toolTip) {
		float current = tag.getFloat("current");
		if (current > 0) {
			toolTip.add(ChatFormatting.YELLOW + Lang.translateArgs("Honey Glazing: %s Absorbtion Hearts", NumberFormat.getInstance(Locale.UK).format(current / 2)));
		}
	}

	@Override
	public void callback(@Nonnull EntityPlayer player, @Nonnull NBTTagCompound tag, ItemStack item) {
		float max = tag.getFloat("current");
		float absorptionAmount = player.getAbsorptionAmount();
		if (absorptionAmount < max) {
			player.setAbsorptionAmount(max);
		}
	}

	@Override
	protected boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, @Nonnull NBTTagCompound curTag) {
		return curTag.getFloat("current") >= curTag.getFloat("max");
	}

	@Nullable
	@Override
	protected NBTTagCompound addData(IBeeGenome genome, @Nonnull ItemStack stack, @Nullable NBTTagCompound prevTag) {
		float current = prevTag != null ? prevTag.getFloat("current") : 0;
		ItemFood food = (ItemFood) stack.getItem();
		float max = Math.min(getMax(genome), calcMaxUnbound(stack, food));
		float newCurrent = Math.min(current + 1, max);
		if (current >= max) {
			return null;
		}
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		nbtTagCompound.setFloat("current", newCurrent);
		nbtTagCompound.setFloat("max", max);
		return nbtTagCompound;
	}

	static {
		if(BeeMod.deobf) {
			StreamSupport.stream(ItemFood.REGISTRY.spliterator(), false)
					.filter(item -> item instanceof ItemFood)
					.flatMap(item -> {
						NonNullList<ItemStack> list = NonNullList.create();
						item.getSubItems(CreativeTabs.SEARCH, list );
						return list.stream();
					})
					.forEach(stack -> BeeMod.logger.info(stack.getDisplayName() + " " + calcMaxUnbound(stack, ((ItemFood) stack.getItem()))));
		}

	}

	private static float calcMaxUnbound(ItemStack stack, @Nonnull ItemFood food) {
		return Math.round(25F * food.getHealAmount(stack) * food.getSaturationModifier(stack) / 2F) / 4F;
	}

	@Override
	public boolean acceptItemStack(ItemStack stack, @Nullable NBTTagCompound curTag) {
		return curTag == null || curTag.getFloat("current") < curTag.getFloat("max");
	}

	protected int getMax(IBeeGenome genome) {
		return 20;
	}
}
