package com.rwtema.careerbees.effects.settings;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rwtema.careerbees.effects.EffectBase;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Setting<V, NBT extends NBTBase> {
	static ImmutableBiMap<Class<? extends NBTBase>, Integer> classIds = ImmutableBiMap.<Class<? extends NBTBase>, Integer>builder()
			.put(NBTTagByte.class, Constants.NBT.TAG_BYTE)
			.put(NBTTagShort.class, Constants.NBT.TAG_SHORT)
			.put(NBTTagInt.class, Constants.NBT.TAG_INT)
			.put(NBTTagLong.class, Constants.NBT.TAG_LONG)
			.put(NBTTagFloat.class, Constants.NBT.TAG_FLOAT)
			.put(NBTTagDouble.class, Constants.NBT.TAG_DOUBLE)
			.put(NBTTagByteArray.class, Constants.NBT.TAG_BYTE_ARRAY)
			.put(NBTTagString.class, Constants.NBT.TAG_STRING)
			.put(NBTTagList.class, Constants.NBT.TAG_LIST)
			.put(NBTTagCompound.class, Constants.NBT.TAG_COMPOUND)
			.put(NBTTagIntArray.class, Constants.NBT.TAG_INT_ARRAY)
			.put(NBTTagLongArray.class, 12)
			.build();

	final V _default;
	final String keyname;
	final int expectedType;

	public Setting(EffectBase parent, String keyname, V _default, Class<NBT> clazz) {
		this._default = _default;
		this.keyname = keyname;
		this.expectedType = classIds.get(clazz);
		parent.addSetting(this);
	}

	public String getKeyname() {
		return keyname;
	}

	public int getExpectedType() {
		return expectedType;
	}

	public V getDefault() {
		return _default;
	}

	public V getValue(IBeeHousing housing) {
		for (IBeeModifier iBeeModifier : housing.getBeeModifiers()) {
			if (iBeeModifier instanceof IEffectSettingsHolder) {
				return getValue((IEffectSettingsHolder) iBeeModifier);
			}
		}
		return _default;
	}

	public V getValue(IEffectSettingsHolder settingsHolder) {
		return settingsHolder.getValue(this);
	}

	public boolean shouldBeVisible(IEffectSettingsHolder settingsHolder) {
		return true;
	}

	public boolean shouldBeEnabled(IEffectSettingsHolder settingsHolder) {
		return true;
	}

	public abstract NBT toNBT(V value);

	public abstract V fromNBT(NBT value);

	public String format(V value) {
		return String.valueOf(value);
	}

	public boolean isAcceptable(V value) {
		return true;
	}

	public List<V> getEntries() {
		if (getType() != Entry_Type.BUTTON)
			return ImmutableList.of();

		throw new UnsupportedOperationException();
	}

	public V nextEntry(V input) {
		List<V> entries = getEntries();
		for (int i = 0; i < entries.size(); i++) {
			V v = entries.get(i);
			if (Objects.equals(input, v)) {
				return entries.get((i + 1) % entries.size());
			}
		}
		return entries.get(0);
	}

	public abstract Entry_Type getType();

	public V overrideInput(V input) {
		return input;
	}


	public V onClickWithItemStack(V currentValue, ItemStack stack, IEffectSettingsHolder holder) {
		return currentValue;
	}

	public int priority() {
		return 0;
	}

	@Nullable
	public List<String> getTooltip(V value) {
		return null;
	}

	public enum Entry_Type {
		TEXT(1),
		BUTTON(2),
		ITEMSTACK(2),
		SCROLL_BAR(1);

		public final int height;

		Entry_Type(int height) {
			this.height = height;
		}
	}

	interface IScrollBar {
		float getMinValue();

		float getMaxValue();
	}

	public static abstract class SettingFunc<V, NBT extends NBTBase> extends Setting<V, NBT> {
		final Function<V, NBT> toNBT;
		final Function<NBT, V> fromNBT;

		public SettingFunc(EffectBase parent, String keyname, V _default, Function<V, NBT> toNBT, Function<NBT, V> fromNBT, Class<NBT> clazz) {
			super(parent, keyname, _default, clazz);
			this.toNBT = toNBT;
			this.fromNBT = fromNBT;
		}

		@Override
		public NBT toNBT(V value) {
			return toNBT.apply(value);
		}

		@Override
		public V fromNBT(NBT value) {
			return fromNBT.apply(value);
		}
	}

	public static class ChoiceOptional<E extends Enum<E>> extends SettingFunc<E, NBTTagByte> {

		private Class<E> clazz;

		public ChoiceOptional(EffectBase parent, String keyname, Class<E> clazz) {
			super(parent, keyname, null, t -> new NBTTagByte(t == null ? (byte) 255 : (byte) t.ordinal()), t -> t.getByte() == (byte) 255 ? null : clazz.getEnumConstants()[t.getInt()], NBTTagByte.class);
			this.clazz = clazz;
		}

		@Override
		public Entry_Type getType() {
			return Entry_Type.BUTTON;
		}

		@Override
		public List<E> getEntries() {
			ArrayList<E> list = Lists.<E>newArrayList((E) null);
			Collections.addAll(list, clazz.getEnumConstants());
			return list;
		}
	}

	public static class Choice<E extends Enum<E>> extends SettingFunc<E, NBTTagByte> {

		public Choice(EffectBase parent, String keyname, E _default) {
			super(parent, keyname, _default, t -> new NBTTagByte((byte) t.ordinal()), t -> _default.getDeclaringClass().getEnumConstants()[t.getInt()], NBTTagByte.class);
		}

		@Override
		public Entry_Type getType() {
			return Entry_Type.BUTTON;
		}

		@Override
		public List<E> getEntries() {
			return ImmutableList.copyOf(_default.getDeclaringClass().getEnumConstants());
		}
	}

	public static class Stack extends SettingFunc<ItemStack, NBTTagCompound> {
		final Predicate<ItemStack> isValid;

		public Stack(EffectBase parent, String keyname) {
			this(parent, keyname, ItemStack.EMPTY);
		}

		public Stack(EffectBase parent, String keyname, ItemStack _default) {
			this(parent, keyname, _default, t -> true);
		}

		public Stack(EffectBase parent, String keyname, ItemStack _default, Predicate<ItemStack> isValid) {
			super(parent, keyname, _default, t -> t.writeToNBT(new NBTTagCompound()), ItemStack::new, NBTTagCompound.class);
			this.isValid = isValid;
		}

		@Override
		public ItemStack onClickWithItemStack(ItemStack currentValue, ItemStack stack, IEffectSettingsHolder holder) {
			return stack.copy();
		}

		@Override
		public String format(ItemStack value) {
			if (value.isEmpty()) return "";
			return value.getDisplayName();
		}

		@Override
		public boolean isAcceptable(ItemStack value) {
			return isValid.test(value);
		}

		@Override
		public Entry_Type getType() {
			return Entry_Type.ITEMSTACK;
		}
	}

	public static class YesNo extends SettingFunc<Boolean, NBTTagByte> {
		public YesNo(EffectBase parent, String keyname, boolean _default) {
			super(parent, keyname, _default, t -> new NBTTagByte(t ? (byte) 1 : (byte) 0), t -> t.getInt() != 0, NBTTagByte.class);
		}

		@Override
		public List<Boolean> getEntries() {
			return ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
		}

		@Override
		public Entry_Type getType() {
			return Entry_Type.BUTTON;
		}
	}

	public static class Slider extends SettingFunc<Float, NBTTagFloat> implements IScrollBar {

		private final float minValue;
		private final float maxValue;

		public Slider(EffectBase parent, String keyname, float _default, float minValue, float maxValue) {
			super(parent, keyname, _default, NBTTagFloat::new, NBTTagFloat::getFloat, NBTTagFloat.class);
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		@Override
		public Entry_Type getType() {
			return Entry_Type.SCROLL_BAR;
		}

		public float getMinValue() {
			return minValue;
		}

		public float getMaxValue() {
			return maxValue;
		}

		@Override
		public Float overrideInput(Float input) {
			return MathHelper.clamp(input, minValue, maxValue);
		}
	}

	public static class Text extends SettingFunc<String, NBTTagString> {
		public Text(EffectBase parent, String keyname) {
			super(parent, keyname, "", NBTTagString::new, NBTTagString::getString, NBTTagString.class);
		}

		@Override
		public Entry_Type getType() {
			return Entry_Type.TEXT;
		}
	}

	public abstract static class TextRestricted extends Text {
		public TextRestricted(EffectBase parent, String keyname) {
			super(parent, keyname);
		}

		@Override
		public boolean isAcceptable(String value) {
			return value.isEmpty() || getEntries().parallelStream().map(s -> s.toLowerCase(Locale.ENGLISH)).anyMatch(s -> s.startsWith(value));
		}

		@Override
		public abstract ArrayList<String> getEntries();

		@Override
		public String overrideInput(String input) {
			if (input.isEmpty()) return input;
			String toLowerCase = input.toLowerCase(Locale.ENGLISH);
			return getEntries().parallelStream()
					.map(s -> s.toLowerCase(Locale.ENGLISH))
					.filter(s -> s.startsWith(toLowerCase))
					.map(s -> s.substring(0, toLowerCase.length()))
					.findAny()
					.map(s -> s + toLowerCase.substring(s.length()))
					.orElse(input);
		}
	}

	public static class OreDicText extends TextRestricted {
		ArrayList<String> ores = null;

		public OreDicText(EffectBase parent, String keyname) {
			super(parent, keyname);
			MinecraftForge.EVENT_BUS.register(this);
		}

		@Override
		public ArrayList<String> getEntries() {
			ArrayList<String> ores = this.ores;
			if (ores == null) {
				this.ores = ores = Lists.newArrayList(OreDictionary.getOreNames());
			}
			return ores;
		}

		@SubscribeEvent
		public void onOreRegister(OreDictionary.OreRegisterEvent event) {
			ores = null;
		}

		@Override
		public String onClickWithItemStack(String currentValue, ItemStack stack, IEffectSettingsHolder holder) {
			int[] oreIDs = OreDictionary.getOreIDs(stack);
			if (oreIDs.length == 0) return currentValue;
			for (int i = 0; i < (oreIDs.length - 1); i++) {
				String oreName = OreDictionary.getOreName(i);
				if (oreName.equals(currentValue)) {
					return OreDictionary.getOreName(i + 1);
				}
			}
			return OreDictionary.getOreName(oreIDs[0]);
		}
	}
}
