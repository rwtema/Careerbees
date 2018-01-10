package com.rwtema.careerbees.helpers;

import forestry.api.apiculture.*;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IEffectData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ParticleHelper {

	@Nullable
	public static final BeeHiveFX BEE_HIVE_FX;

	static {
		BeeHiveFX temp;
		try {
			temp = forestry.core.render.ParticleRender::addBeeHiveFX;
		} catch (Throwable err) {
			err.printStackTrace();
			temp = (housing, genome, flowerPositions) -> {
				IAlleleBeeEffect allele = (IAlleleBeeEffect) AlleleManager.alleleRegistry.getAllele("forestry.effect.none");
				allele.doFX(genome, new IEffectData() {
					@Override
					public void setInteger(int index, int val) {

					}

					@Override
					public void setBoolean(int index, boolean val) {

					}

					@Override
					public int getInteger(int index) {
						return 0;
					}

					@Override
					public boolean getBoolean(int index) {
						return false;
					}

					@Override
					public void readFromNBT(@Nonnull NBTTagCompound nbt) {

					}

					@Nonnull
					@Override
					public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
						return nbt;
					}
				}, housing);
			};
		}
		BEE_HIVE_FX = temp;
	}

	public interface BeeHiveFX {
		void addBeeHiveFX(IBeeHousing housing, IBeeGenome genome, List<BlockPos> flowerPositions);
	}
}
