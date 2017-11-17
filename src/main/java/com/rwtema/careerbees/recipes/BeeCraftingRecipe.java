package com.rwtema.careerbees.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BeeCraftingRecipe implements IBeeCraftingRecipe {

	public static final List<IBeeCraftingRecipe> RECIPES = new ArrayList<>();
	private final List<BeeCraftingInputEntry> inputs;
	private final ItemStack output;

	public BeeCraftingRecipe(ItemStack output, Object... inputObjects) {
		this.inputs = new ArrayList<>();
		this.output = output;

		for (Object input : inputObjects) {
			if (input instanceof Item) {
				this.inputs.add(createWrapper((Item) input));
			} else if (input instanceof ItemStack) {
				int metadata = ((ItemStack) input).getMetadata();
				if (metadata == OreDictionary.WILDCARD_VALUE) {
					this.inputs.add(createWrapper(((ItemStack) input).getItem()));
				} else {
					this.inputs.add(createWrapper(((ItemStack) input).getItem(), metadata));
				}
			} else if (input instanceof List) {
				this.inputs.add(createWrapper((List) input));
			} else if (input instanceof String) {
				this.inputs.add(createWrapper((String) input));
			}
		}
	}

	protected BeeCraftingRecipe(List<BeeCraftingInputEntry> inputs, ItemStack output) {
		this.inputs = inputs;
		this.output = output;
	}

	public static BeeCraftingInputEntry createWrapper(Item item) {
		return new BeeCraftingInputEntry() {
			@Override
			public boolean test(ItemStack stack) {
				return stack.getItem() == item;
			}

			@Override
			public List<ItemStack> getJEIInputs() {
				NonNullList<ItemStack> list = NonNullList.create();
				item.getSubItems(item.getCreativeTab(), list);
				return list;
			}
		};
	}

	public static BeeCraftingInputEntry createWrapper(String oreName) {
		return createWrapper(OreDictionary.getOres(oreName));
	}

	public static BeeCraftingInputEntry createWrapper(List<ItemStack> list) {
		return new BeeCraftingInputEntry() {

			@Override
			public boolean test(ItemStack stack) {
				for (ItemStack matchStack : list) {
					if (OreDictionary.itemMatches(matchStack, stack, false)) {
						return true;
					}
				}

				return false;
			}

			@Override
			public List<ItemStack> getJEIInputs() {
				return list;
			}
		};
	}

	public static BeeCraftingInputEntry createWrapper(Item item, int meta) {
		return new BeeCraftingInputEntry() {
			@Override
			public boolean test(ItemStack stack) {
				return stack.getItem() == item && stack.getMetadata() == meta;
			}

			@Override
			public List<ItemStack> getJEIInputs() {
				return Collections.singletonList(new ItemStack(item, 1, meta));
			}
		};
	}

	@Override
	public ItemStack getOutput(Map<BeeCraftingInputEntry, ItemStack> inputs) {
		return output;
	}

	@Override
	public List<BeeCraftingInputEntry> getInputs() {
		return inputs;
	}
}
