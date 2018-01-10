package com.rwtema.careerbees.helpers;

import com.google.common.base.Supplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;

public class NBTHelper {
	public static <T extends NBTBase> Collector<T, ?, NBTTagList> toNBTTagList() {
		return Collector.of((Supplier<NBTTagList>) NBTTagList::new,
				NBTTagList::appendTag,
				(nbtBases, nbtBases2) -> {
					for (int i = 0; i < nbtBases2.tagCount(); i++) {
						nbtBases.appendTag(nbtBases2.get(i));
					}
					return nbtBases;
				}
		);
	}

	@SuppressWarnings("unchecked")
	public static <K, V, KT extends NBTBase, VT extends NBTBase> void deserializeListIntoMap(@Nonnull Map<K,V> map, @Nonnull NBTTagList list, @Nonnull String keyName, @Nonnull String valName, @Nonnull Function<KT, K> nbtToKey, @Nonnull Function<VT, V> nbtToVal  ){
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


	public static <K, V> NBTTagList serializeMapToList(@Nonnull Map<K, V> map, @Nonnull String keyName, @Nonnull String valName, @Nonnull Function<K, ? extends NBTBase> keyToNBT, @Nonnull Function<V, ? extends NBTBase> valToNBT ){
		return map.entrySet().stream()
				.map(e -> builder()
						.setTag(keyName, keyToNBT.apply(e.getKey()))
						.setTag(valName, valToNBT.apply(e.getValue()))
						.build()
				).collect(toNBTTagList());
	}

	@Nullable
	public static <T extends NBTBase> List<T> wrapList(@Nonnull NBTTagList list) {
		return new AbstractList<T>() {
			@Override
			public int size() {
				return list.tagCount();
			}

			@Nullable
			@Override
			public T get(int index) {
				NBTBase nbtBase = list.get(index);
				if (nbtBase.getId() != list.getTagType()) return null;
				return (T) nbtBase;
			}
		};
	}

	@Nonnull
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

		@Nonnull
		public NBTChainBuilder setTag(@Nonnull String key, @Nonnull NBTBase value) {
			tag.setTag(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setByte(@Nonnull String key, byte value) {
			tag.setByte(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setShort(@Nonnull String key, short value) {
			tag.setShort(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setInteger(@Nonnull String key, int value) {
			tag.setInteger(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setLong(@Nonnull String key, long value) {
			tag.setLong(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setUniqueId(String key, @Nonnull UUID value) {
			tag.setUniqueId(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setFloat(@Nonnull String key, float value) {
			tag.setFloat(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setDouble(@Nonnull String key, double value) {
			tag.setDouble(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setString(@Nonnull String key, @Nonnull String value) {
			tag.setString(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setByteArray(@Nonnull String key, @Nonnull byte[] value) {
			tag.setByteArray(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setIntArray(@Nonnull String key, @Nonnull int[] value) {
			tag.setIntArray(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setBoolean(@Nonnull String key, boolean value) {
			tag.setBoolean(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder merge(@Nonnull NBTTagCompound other) {
			tag.merge(other);
			return this;
		}
	}


	@Nonnull
	public static GameProfile profileFromNBT(@Nonnull NBTTagCompound tag){
		return new GameProfile(new UUID(tag.getLong("upper"), tag.getLong("lower")), tag.getString("name") ) ;
	}

	public static NBTTagCompound profileToNBT(@Nonnull GameProfile profile){
		return builder()
				.setLong("lower", profile.getId().getLeastSignificantBits())
				.setLong("upper", profile.getId().getMostSignificantBits())
				.setString("name", profile.getName())
				.build();


	}

	public static <NBT extends NBTBase, T extends INBTSerializable<NBT>> Function<NBT,  T> deserializer(@Nonnull java.util.function.Supplier<T> blank){
		return tag -> {
			T t = blank.get();
			t.deserializeNBT(tag);
			return t;
		};
	}
}
