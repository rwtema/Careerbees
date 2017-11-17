package com.rwtema.careerbees.helpers;

import com.google.common.base.Supplier;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.AbstractList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;

public class NBTHelper {
	public static <T extends NBTBase> Collector<T, ?, NBTTagList> toNBTTagList() {
		return Collector.<T, NBTTagList>of((Supplier<NBTTagList>) NBTTagList::new,
				NBTTagList::appendTag,
				(nbtBases, nbtBases2) -> {
					for (int i = 0; i < nbtBases2.tagCount(); i++) {
						nbtBases.appendTag(nbtBases2.get(i));
					}
					return nbtBases;
				}
		);
	}

	public static <T extends NBTBase> List<T> wrapList(NBTTagList list) {
		return new AbstractList<T>() {
			@Override
			public int size() {
				return list.tagCount();
			}

			@Override
			public T get(int index) {
				NBTBase nbtBase = list.get(index);
				if (nbtBase.getId() != list.getTagType()) return null;
				return (T) nbtBase;
			}
		};
	}

	public static NBTChainBuilder builder() {
		return new NBTChainBuilder(new NBTTagCompound());
	}

	public static class NBTChainBuilder {
		final NBTTagCompound tag;

		public NBTTagCompound build(){
			return tag;
		}

		public NBTChainBuilder(NBTTagCompound tag) {
			this.tag = tag;
		}

		public NBTChainBuilder setTag(String key, NBTBase value) {
			tag.setTag(key, value);
			return this;
		}

		public NBTChainBuilder setByte(String key, byte value) {
			tag.setByte(key, value);
			return this;
		}

		public NBTChainBuilder setShort(String key, short value) {
			tag.setShort(key, value);
			return this;
		}

		public NBTChainBuilder setInteger(String key, int value) {
			tag.setInteger(key, value);
			return this;
		}

		public NBTChainBuilder setLong(String key, long value) {
			tag.setLong(key, value);
			return this;
		}

		public NBTChainBuilder setUniqueId(String key, UUID value) {
			tag.setUniqueId(key, value);
			return this;
		}

		public NBTChainBuilder setFloat(String key, float value) {
			tag.setFloat(key, value);
			return this;
		}

		public NBTChainBuilder setDouble(String key, double value) {
			tag.setDouble(key, value);
			return this;
		}

		public NBTChainBuilder setString(String key, String value) {
			tag.setString(key, value);
			return this;
		}

		public NBTChainBuilder setByteArray(String key, byte[] value) {
			tag.setByteArray(key, value);
			return this;
		}

		public NBTChainBuilder setIntArray(String key, int[] value) {
			tag.setIntArray(key, value);
			return this;
		}

		public NBTChainBuilder setBoolean(String key, boolean value) {
			tag.setBoolean(key, value);
			return this;
		}

		public NBTChainBuilder merge(NBTTagCompound other) {
			tag.merge(other);
			return this;
		}
	}


}
