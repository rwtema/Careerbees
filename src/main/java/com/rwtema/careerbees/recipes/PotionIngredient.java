package com.rwtema.careerbees.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class PotionIngredient extends Ingredient {
	final Potion potion;
	final int minLevel;

	public PotionIngredient(Potion potion, int minLevel) {
		super(getValidPotions(potion, minLevel));
		this.potion = potion;
		this.minLevel = minLevel;
	}

	public static Stream<ItemPotion> getPotions() {
		return Stream.of(Items.POTIONITEM, Items.LINGERING_POTION, Items.SPLASH_POTION);
	}

	public static ItemStack[] getValidPotions(Potion potion, int level) {
		boolean foundExact = false;
		List<PotionType> potiontypes = Streams.stream(PotionType.REGISTRY)
				.filter(s -> {
					if (s.getEffects().size() != 1) return false;
					PotionEffect potionEffect = s.getEffects().get(0);
					return potionEffect.getPotion() == potion && potionEffect.getAmplifier() >= level;
				}).collect(Collectors.toList());


		List<ItemStack> stacks = potiontypes.stream()
				.flatMap(t -> getPotions()
						.map(ItemStack::new)
						.map(s -> PotionUtils.addPotionToItemStack(s, t)))
				.collect(Collectors.toList());

		if (potiontypes.stream().map(PotionType::getEffects).map(t -> t.get(0)).noneMatch(s -> s.getAmplifier() == level)) {
			getPotions().map(ItemStack::new)
					.map(s -> PotionUtils.appendEffects(s, ImmutableList.of(new PotionEffect(potion, 20 * 10, level))))
					.forEach(stacks::add);
		}

		return stacks.stream().toArray(ItemStack[]::new);
	}

	@Override
	public boolean apply(@Nullable ItemStack stack) {
		if (stack != null && stack.hasTagCompound()) {
			for (PotionEffect effect : PotionUtils.getEffectsFromStack(stack)) {
				if (effect.getPotion() == potion && effect.getAmplifier() >= minLevel) {
					return true;
				}
			}

		}
		return false;
	}

	public static class Factory implements IIngredientFactory {
		@Nonnull
		@Override
		public Ingredient parse(JsonContext context, JsonObject json) {
			String potionName = JsonUtils.getString(json, "potion");
			Potion potion = Potion.REGISTRY.getObject(new ResourceLocation(potionName));
			if (potion == null) {
				throw new JsonSyntaxException("Unable to find Potion - " + potionName);
			}

			int level = JsonUtils.getInt(json, "level");

			return new PotionIngredient(potion, level);
		}
	}
}
