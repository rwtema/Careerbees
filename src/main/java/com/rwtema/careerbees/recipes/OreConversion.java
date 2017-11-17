package com.rwtema.careerbees.recipes;

import com.google.common.cache.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rwtema.careerbees.BeeMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.StreamSupport;

public abstract class OreConversion {


	LoadingCache<Item, Map<Integer, ItemStack>> cache = CacheBuilder.<Item, Map<Integer, ItemStack>>newBuilder()
			.maximumSize(60)
			.build(new CacheLoader<Item, Map<Integer, ItemStack>>() {
		@Override
		public Map<Integer, ItemStack> load(@Nonnull Item item) throws Exception {
			List<ItemStack> stacks = getStacks(item);
			if (stacks.isEmpty()) {
				stacks = ImmutableList.of(new ItemStack(item));
			}
			ImmutableSet<Integer> metas;
			if (item.getHasSubtypes()) {
				metas = stacks.stream().filter((stack) -> !stack.hasTagCompound()).map(ItemStack::getMetadata).collect(ImmutableSet.toImmutableSet());
				if (metas.isEmpty()) metas = ImmutableSet.of(0);
			} else {
				metas = ImmutableSet.of(0);
			}

			ImmutableMap.Builder<Integer, ItemStack> builder = ImmutableMap.builder();

			for (Integer meta : metas) {
				int[] oreIDs = OreDictionary.getOreIDs(new ItemStack(item, 1, meta));
				if (oreIDs.length == 0) continue;

				Arrays.stream(oreIDs)
						.mapToObj(OreDictionary::getOreName)
						.map(OreConversion.this::getOreMapping)
						.filter(Objects::nonNull)
						.distinct()
						.flatMap(t -> OreDictionary.getOres(t).stream())
						.sorted(Comparator.comparing(t -> !Objects.equals(
								Validate.notNull(t.getItem().getRegistryName()).getResourceDomain(),
								Validate.notNull(item.getRegistryName()).getResourceDomain())))
						.findFirst()
						.ifPresent(s -> builder.put(meta, s.copy()));
			}

			return builder.build();
		}
	});

	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void run(OreDictionary.OreRegisterEvent event) {
		cache.invalidateAll();
	}

	public boolean isValid(ItemStack itemStack) {
		Map<Integer, ItemStack> map = cache.getUnchecked(itemStack.getItem());
		return map.containsKey(itemStack.getMetadata());
	}

	public ItemStack get(ItemStack itemStack) {
		return get(itemStack, ItemStack.EMPTY);
	}

	public ItemStack get(ItemStack itemStack, ItemStack emptyReturn) {
		Map<Integer, ItemStack> map = cache.getUnchecked(itemStack.getItem());

		if (map.isEmpty())
			return emptyReturn;

		ItemStack stack = map.get(itemStack.getMetadata());
//		if (stack == null) stack = map.get(OreDictionary.WILDCARD_VALUE);
		if (stack == null) return emptyReturn;
		return stack.copy();
	}

	@Nullable
	public abstract String getOreMapping(String input);

	public NonNullList<ItemStack> getStacks(Item item) {
		NonNullList<ItemStack> objects = NonNullList.create();
		item.getSubItems(CreativeTabs.SEARCH, objects);
		return objects;
	}

	public void test() {
		cache.invalidateAll();
		StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false)
				.map(this::getStacks)
				.flatMap(Collection::stream)
				.forEach(stack -> {
					ItemStack itemStack = get(stack);
					if (!itemStack.isEmpty()) {
						BeeMod.logger.info("Conv " + stack.getDisplayName() + " -> " + itemStack.getDisplayName());
					}
				});
		cache.invalidateAll();
	}

	public static class PrefixReplace extends OreConversion {
		final String inPrefix, outPrefix;

		public PrefixReplace(String inPrefix, String outPrefix) {
			this.inPrefix = inPrefix;
			this.outPrefix = outPrefix;
		}

		@Override
		public String getOreMapping(String input) {
			if (input.length() > inPrefix.length() && input.startsWith(inPrefix) && Character.isUpperCase(input.charAt(inPrefix.length()))) {
				return outPrefix + input.substring(inPrefix.length());
			}
			return null;
		}
	}
}
