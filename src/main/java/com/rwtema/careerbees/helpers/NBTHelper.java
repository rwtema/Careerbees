package com.rwtema.careerbees.helpers;

import com.google.common.base.Supplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
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

	public static <K, V, KT extends NBTBase, VT extends NBTBase> void deserializeListIntoMap(Map<K,V> map, NBTTagList list, String keyName, String valName, Function<KT, K> nbtToKey, Function<VT, V> nbtToVal  ){
		map.clear();
		NBTHelper.<NBTTagCompound>wrapList(list).forEach(
				t -> {
					if(t.hasKey(keyName) && t.hasKey(valName)) {
						K key = nbtToKey.apply((KT) t.getTag(keyName));
						V value = nbtToVal.apply((VT) t.getTag(valName));
						map.put(key, value);
					}
				}
		);
	}


	public static <K, V> NBTTagList serializeMapToList(Map<K, V> map, String keyName, String valName, Function<K, ? extends NBTBase> keyToNBT, Function<V, ? extends NBTBase> valToNBT ){
		return map.entrySet().stream()
				.map(e -> builder()
						.setTag(keyName, keyToNBT.apply(e.getKey()))
						.setTag(valName, valToNBT.apply(e.getValue()))
						.build()
				).collect(toNBTTagList());
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


		public NBTChainBuilder() {
			this(new NBTTagCompound());
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


	public static GameProfile profileFromNBT(NBTTagCompound tag){
		return new GameProfile(new UUID(tag.getLong("upper"), tag.getLong("lower")), tag.getString("name") ) ;
	}

	public static NBTTagCompound profileToNBT(GameProfile profile){
		return builder()
				.setLong("lower", profile.getId().getLeastSignificantBits())
				.setLong("upper", profile.getId().getMostSignificantBits())
				.setString("name", profile.getName())
				.build();


	}

	public static <NBT extends NBTBase, T extends INBTSerializable<NBT>> Function<NBT,  T> deserializer(java.util.function.Supplier<T> blank){
		return tag -> {
			T t = blank.get();
			t.deserializeNBT(tag);
			return t;
		};
	}
}
