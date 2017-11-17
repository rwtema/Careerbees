package com.rwtema.careerbees.effects;

import forestry.api.genetics.IEffectData;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public abstract class BaseEffectDataMap implements IEffectData {

	@Override
	public void setBoolean(int index, boolean val) {
		setInteger(index, val ? 1 : 0);
	}

	@Override
	public boolean getBoolean(int index) {
		return getInteger(index) != 0;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt) {

	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {

		return nbt;
	}

	public static class SingleInt extends BaseEffectDataMap {
		int data;

		@Override
		public void setInteger(int index, int val) {
			Validate.isTrue(index == 0);
			data = val;
		}

		@Override
		public int getInteger(int index) {
			Validate.isTrue(index == 0);
			return data;
		}
	}



	public static class IntMap extends BaseEffectDataMap {
		TIntIntHashMap map;

		public IntMap() {
			map = new TIntIntHashMap(1, 0.5F, 0, 0);
		}

		@Override
		public void setInteger(int index, int val) {
			map.put(index, val);
		}

		@Override
		public int getInteger(int index) {
			return map.get(index);
		}
	}

	public static class IntArray extends BaseEffectDataMap {
		int[] data;

		public IntArray(int[] data) {
			this.data = data;
		}

		@Override
		public void setInteger(int index, int val) {
			data[index] = val;
		}

		@Override
		public int getInteger(int index) {
			return data[index];
		}
	}

	public static class None extends BaseEffectDataMap{
		public static final None INSTANCE = new None();
		@Override
		public void setInteger(int index, int val) {

		}

		@Override
		public int getInteger(int index) {
			return 0;
		}
	}
}
