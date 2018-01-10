package com.rwtema.careerbees.helpers;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.ClientRunnable;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class NameHelper {
	public static final NameHelper QUEEN_NAMES = new NameHelper("carrerbees.queen.names.");
	@Nonnull
	public static NameHelper TAXES = new NameHelper("beemod.text.taxpayer.tax.");
	private final String prefix;
	int numNames = -1;

	{
		BeeMod.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(NameHelper.this::resetNum);
			}
		});
	}

	public void resetNum(@SuppressWarnings("unused") Object dummy){
		numNames = -1;
	}

	public NameHelper(String prefix) {
		this.prefix = prefix;
	}


	public static short getQueenNameSeed(@Nonnull IBeeHousing housing) {
		ItemStack queen = housing.getBeeInventory().getQueen();
		if (queen.isEmpty()) return newSeed(housing);

		NBTTagCompound compound = queen.getTagCompound();
		if (compound == null) return newSeed(housing);
		if (compound.hasKey("name_seed", Constants.NBT.TAG_SHORT)) {
			return compound.getShort("name_seed");
		}
		short value = newSeed(housing);
		compound.setShort("name_seed", value);
		queen.setTagCompound(compound);
		housing.getBeeInventory().setQueen(queen);
		return value;
	}

	private static short newSeed(@Nonnull IBeeHousing housing) {
		return (short) housing.getWorldObj().rand.nextInt(16384);
	}

	@Nonnull
	public String getName(int seed) {
		if (numNames == -1) {
			int i = 0;
			while (I18n.canTranslate(prefix + i)) {
				i++;
			}
			if (i == 0) i = 1;
			numNames = i;
		}

		int i = Math.abs(seed) % numNames;
		return I18n.translateToLocal(prefix + i);
	}
}
