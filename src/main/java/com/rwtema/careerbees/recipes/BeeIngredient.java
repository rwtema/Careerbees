package com.rwtema.careerbees.recipes;

import com.google.gson.JsonObject;
import forestry.api.apiculture.*;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IChromosome;
import forestry.core.config.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BeeIngredient extends Ingredient {
	public static final Random rand = new Random();
	final Set<EnumBeeType> allowedTypes;
	final IAllele[] requirements;
	ItemStack[] stacks;

	public BeeIngredient(Set<EnumBeeType> allowedTypes, IAllele[] requirements) {
		super(createMatchingStacks(allowedTypes));
		this.allowedTypes = allowedTypes;
		this.requirements = requirements;
	}

	private static ItemStack[] createMatchingStacks(Set<EnumBeeType> allowedTypes) {
		return allowedTypes.stream()
				.map(t -> {
					switch (t) {
						case DRONE:
							return "bee_drone_ge";
						case PRINCESS:
							return "bee_princess_ge";
						case QUEEN:
							return "bee_queen_ge";
						case LARVAE:
							return "bee_larvae_ge";
					}
					throw new IllegalStateException();
				})
				.map(s -> new ResourceLocation(Constants.MOD_ID, s))
				.map(ForgeRegistries.ITEMS::getValue)
				.map(ItemStack::new)
				.toArray(ItemStack[]::new);
	}

	public static ItemStack[] createRandomBees(int n, Collection<EnumBeeType> allowedTypes, IAllele[] requirements, Random random) {
		ItemStack[] stacks = new ItemStack[n];
		IAllele[][][] template = new IAllele[n][2][requirements.length];
		EnumBeeChromosome[] values = EnumBeeChromosome.values();
		for (int i = 0; i < values.length; i++) {
			IAllele requirement = requirements[i];
			if (requirement != null && (values[i] == EnumBeeChromosome.SPECIES || !requirement.isDominant())) {
				for (int j = 0; j < n; j++) {
					template[j][0][i] = template[j][1][i] = requirement;
				}
			} else if (requirement != null && requirement.isDominant()) {
				EnumBeeChromosome enumBeeChromosome = values[i];
				ArrayList<IAllele> iAlleles = new ArrayList<>(AlleleManager.alleleRegistry.getRegisteredAlleles(enumBeeChromosome));
				if (iAlleles.isEmpty())
					return new ItemStack[0];
				for (int j = 0; j < n; j++) {
					int k = random.nextInt(2);
					template[j][k][i] = requirement;
					template[j][1 - k][i] = iAlleles.get(random.nextInt(iAlleles.size()));
				}
			} else {
				EnumBeeChromosome enumBeeChromosome = values[i];
				ArrayList<IAllele> iAlleles = new ArrayList<>(AlleleManager.alleleRegistry.getRegisteredAlleles(enumBeeChromosome));
				if (iAlleles.isEmpty())
					return new ItemStack[0];
				for (int j = 0; j < n; j++) {
					template[j][0][i] = iAlleles.get(random.nextInt(iAlleles.size()));
					template[j][1][i] = iAlleles.get(random.nextInt(iAlleles.size()));
				}
			}
		}

		ArrayList<EnumBeeType> beeTypes = new ArrayList<>(allowedTypes);

		for (int j = 0; j < n; j++) {
			IBeeGenome genome = BeeManager.beeRoot.templateAsGenome(template[j][0], template[j][1]);
			IBee bee = BeeManager.beeRoot.getBee(genome);
			EnumBeeType enumBeeType = beeTypes.get(random.nextInt(beeTypes.size()));
			bee.analyze();
			ItemStack beeStack = BeeManager.beeRoot.getMemberStack(bee, enumBeeType);
			stacks[j] = beeStack;
		}

		return stacks;
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks() {
		if (stacks == null) {
			stacks = createRandomBees(24, allowedTypes, requirements, rand);
		}
		return stacks;
	}

	@Override
	public boolean apply(@Nullable ItemStack p_apply_1_) {
		if (p_apply_1_ == null) return false;
		EnumBeeType type = BeeManager.beeRoot.getType(p_apply_1_);
		if (type == null || !allowedTypes.contains(type)) return false;

		IBee member = BeeManager.beeRoot.getMember(p_apply_1_);
		if (member == null) return false;

		IBeeGenome genome = member.getGenome();
		IChromosome[] chromosomes = genome.getChromosomes();
		for (int i = 0; i < requirements.length; i++) {
			if (requirements[i] != null) {
				if (!chromosomes[i].getActiveAllele().equals(requirements[i])) {
					return false;
				}
			}
		}

		return super.apply(p_apply_1_);
	}

	public static class Factory implements IIngredientFactory {
		public Factory() {
			super();
		}

		@Nonnull
		@Override
		public Ingredient parse(JsonContext context, JsonObject json) {

			String type = JsonUtils.getString(json, "beetype", "any");
			EnumSet<EnumBeeType> allowedTypes;

			if (Objects.equals(type, "any")) {
				allowedTypes = EnumSet.allOf(EnumBeeType.class);
			} else {
				allowedTypes = EnumSet.noneOf(EnumBeeType.class);
				for (String s : type.split(",")) {
					EnumBeeType enumBeeType = EnumBeeType.valueOf(s.toUpperCase(Locale.ENGLISH));
					allowedTypes.add(enumBeeType);
				}
			}

			JsonUtils.getString(json, "species");

			IAllele[] requirements = new IAllele[EnumBeeChromosome.values().length];
			for (EnumBeeChromosome enumBeeChromosome : EnumBeeChromosome.values()) {
				String name = enumBeeChromosome.getName();
				if (JsonUtils.hasField(json, name)) {
					String string = JsonUtils.getString(json, name);
					IAllele allele = AlleleManager.alleleRegistry.getAllele(string);
					requirements[enumBeeChromosome.ordinal()] = allele;
				}
			}

			return new BeeIngredient(allowedTypes, requirements);
		}
	}
}
