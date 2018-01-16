package com.rwtema.careerbees.effects.settings;

import com.google.common.collect.ImmutableSet;
import com.rwtema.careerbees.effects.EffectBase;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Filter {
	@Nonnull
	public static final Predicate<ItemStack> DEFAULT_MATCHER = s -> true;
	@Nonnull
	final Setting.Choice<FilterType> filterType;
	@Nonnull
	final Setting.Stack stack;
	@Nonnull
	final Setting.YesNo ignoreMeta;
	@Nonnull
	final Setting.YesNo ignoreNBT;
	@Nonnull
	final Setting.OreDicText oreDicText;

	public Filter(EffectBase parent) {
		this(parent, "Filter");
	}

	public Filter(EffectBase parent, String name) {
		filterType = new Setting.Choice<>(parent, name + "Type", FilterType.ITEMSTACK);

		stack = new Setting.Stack(parent, name + "Itemstack") {
			@Override
			public boolean shouldBeVisible(IEffectSettingsHolder settingsHolder) {
				return filterType.getValue(settingsHolder) == FilterType.ITEMSTACK;
			}

			@Nonnull
			@Override
			public ItemStack overrideInput(@Nonnull ItemStack input) {
				if(input.getCount() > 1){
					ItemStack copy = input.copy();
					copy.setCount(1);
					return copy;
				}
				return input;
			}
		};
		ignoreMeta = new Setting.YesNo(parent, name + "Meta", false) {
			@Override
			public boolean shouldBeVisible(IEffectSettingsHolder settingsHolder) {
				return filterType.getValue(settingsHolder) == FilterType.ITEMSTACK;
			}
		};
		ignoreNBT = new Setting.YesNo(parent, name + "NBT", true) {
			@Override
			public boolean shouldBeVisible(IEffectSettingsHolder settingsHolder) {
				return filterType.getValue(settingsHolder) == FilterType.ITEMSTACK;
			}
		};

		oreDicText = new Setting.OreDicText(parent, name + "OreDic") {
			@Override
			public boolean shouldBeVisible(IEffectSettingsHolder settingsHolder) {
				return filterType.getValue(settingsHolder) == FilterType.OREDICTIONARY;
			}
		};
	}

	public boolean matches(@Nonnull IBeeHousing housing, @Nonnull ItemStack stack) {
		for (IBeeModifier modifier : housing.getBeeModifiers()) {
			if (modifier instanceof IEffectSettingsHolder) {
				return matches((IEffectSettingsHolder) modifier, stack);
			}
		}
		return matches(IEffectSettingsHolder.DEFAULT_INSTANCE, stack);
	}

	public boolean matches(IEffectSettingsHolder settings, @Nonnull ItemStack stack) {
		if (stack.isEmpty()) return false;
		FilterType value = filterType.getValue(settings);
		switch (value) {
			case ITEMSTACK: {
				ItemStack target = this.stack.getValue(settings);
				return target.getItem() == stack.getItem()
						&& (ignoreMeta.getValue(settings) || target.getMetadata() == stack.getMetadata())
						&& (ignoreNBT.getValue(settings) || Objects.equals(target.getTagCompound(), stack.getTagCompound()));
			}
			case OREDICTIONARY: {
				for (ItemStack itemStack : OreDictionary.getOres(oreDicText.getValue(settings), false)) {
					if (OreDictionary.itemMatches(itemStack, stack, false)) {
						return true;
					}
				}
			}
		}

		return true;
	}

	@Nonnull
	public Predicate<ItemStack> getMatcher(IEffectSettingsHolder settings) {
		if (settings == IEffectSettingsHolder.DEFAULT_INSTANCE) return DEFAULT_MATCHER;

		FilterType value = filterType.getValue(settings);
		switch (value) {
			case ITEMSTACK: {
				ItemStack target = this.stack.getValue(settings);
				return createSingleStackPredicate(target, ignoreMeta.getValue(settings), ignoreNBT.getValue(settings));
			}
			case OREDICTIONARY: {
				NonNullList<ItemStack> ores = OreDictionary.getOres(oreDicText.getValue(settings), false);
				if (ores.isEmpty()) {
					return t -> false;
				} else if (ores.size() == 1) {
					ItemStack target = ores.get(0);
					return createSingleStackPredicate(target, target.getMetadata() == OreDictionary.WILDCARD_VALUE, true);
				} else {
					ImmutableSet<Item> basicItems = ores.stream()
							.filter(s -> s.getMetadata() == OreDictionary.WILDCARD_VALUE)
							.map(ItemStack::getItem)
							.collect(ImmutableSet.toImmutableSet());

					Map<Item, ImmutableSet<Integer>> advItems = ores.stream()
							.filter(s -> s.getMetadata() != OreDictionary.WILDCARD_VALUE)
							.collect(Collectors.groupingBy(
									ItemStack::getItem,
									Collectors.mapping(ItemStack::getMetadata, ImmutableSet.toImmutableSet())
							));
					Predicate<ItemStack> basicPredicate = basicItems.isEmpty() ? null : s -> basicItems.contains(s.getItem());
					Predicate<ItemStack> advPrediacte = advItems.isEmpty() ? null : s -> advItems.getOrDefault(s.getItem(), ImmutableSet.of()).contains(s.getItemDamage());
					if (basicPredicate == null) return advPrediacte == null ? (s -> false) : advPrediacte;
					if (advPrediacte == null) return basicPredicate;
					return basicPredicate.and(advPrediacte);
				}
			}
		}

		return s -> false;
	}

	private Predicate<ItemStack> createSingleStackPredicate(@Nonnull ItemStack target, boolean ignoreMetaValue, boolean ignoreNBTValue) {
		if (target.isEmpty()) return s -> true;
		Item item = target.getItem();
		Predicate<ItemStack> predicate = stack -> stack.getItem() == item;
		if (!ignoreMetaValue)
			predicate = predicate.and(stack -> stack.getMetadata() == target.getMetadata());
		if (!ignoreNBTValue)
			predicate = predicate.and(stack -> Objects.equals(stack.getTagCompound(), target.getTagCompound()));

		return predicate;
	}

	public enum FilterType {
		ITEMSTACK,
		OREDICTIONARY
	}
}
