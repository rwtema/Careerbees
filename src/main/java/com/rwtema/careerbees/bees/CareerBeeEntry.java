package com.rwtema.careerbees.bees;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.colors.DebugBeeSpriteColors;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.helpers.StringHelper;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.*;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleSpeciesBuilder;
import forestry.api.genetics.IClassification;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.rwtema.careerbees.bees.CareerBeeSpecies.BEE_YELLOW;

public class CareerBeeEntry implements Supplier<IAlleleBeeSpecies>, BeeMutationTree.SpeciesEntry {
	public static final List<CareerBeeEntry> BEE_ENTRIES = new ArrayList<>();
	final List<Consumer<CustomBeeFactory>> speciesInstructions = new ArrayList<>();
	private final String name;
	private final boolean dominant;
	private final String branchName;
	private final int primaryColor;
	private final int secondaryColor;
	public IAlleleBeeSpecies species;
	public String modelName;
	boolean shouldAddVanillaProducts = true;
	private boolean isSecret;

	public CareerBeeEntry(String name, boolean dominant, String branchName, int primaryColor) {
		this(name, dominant, branchName, primaryColor, CareerBeeSpecies.BEE_YELLOW);
	}

	public CareerBeeEntry(String name, boolean dominant, String branchName, int primaryColor, int secondaryColor) {
		BEE_ENTRIES.add(this);
		this.name = name;
		this.dominant = dominant;
		this.branchName = branchName;
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
		this.modelName = name;
	}

	public static float roundSig(float k, float n) {
		if (k <= 0 || n <= 0) return 0;
		if (k < 1) return roundSig(k * n, n) / n;
		return Math.round(k);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CareerBeeEntry)) return false;

		CareerBeeEntry that = (CareerBeeEntry) o;

		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Nonnull
	@Override
	public String toString() {
		return "CareerBeeEntry{" +
				"name='" + name + '\'' +
				'}';
	}

	@Nonnull
	public final CareerBeeEntry setTemperature(@Nonnull EnumTemperature temperature) {
		speciesInstructions.add(c -> c.setTemperature(temperature));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setHumidity(@Nonnull EnumHumidity humidity) {
		speciesInstructions.add(c -> c.setHumidity(humidity));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setHasEffect() {
		speciesInstructions.add(IAlleleSpeciesBuilder::setHasEffect);
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setIsSecret() {
		speciesInstructions.add(IAlleleSpeciesBuilder::setIsSecret);
		isSecret = true;
		return this;
	}

	public final boolean isSecret() {
		return isSecret;
	}

	@Nonnull
	public final CareerBeeEntry setIsNotCounted() {
		speciesInstructions.add(CustomBeeFactory::setIsNotCounted);
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setComplexity(int complexity) {
		speciesInstructions.add(c -> c.setComplexity(complexity));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry addProduct(@Nonnull ItemStack product, @Nonnull Float chance) {
		speciesInstructions.add(c -> c.addProduct(product, chance));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry addSpecialty(@Nonnull ItemStack specialty, @Nonnull Float chance) {
		speciesInstructions.add(c -> c.addSpecialty(specialty, chance));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setJubilanceProvider(@Nonnull IJubilanceProvider provider) {
		speciesInstructions.add(c -> c.setJubilanceProvider(provider));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setNocturnal() {
		speciesInstructions.add(IAlleleBeeSpeciesBuilder::setNocturnal);
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setCustomBeeModelProvider(IBeeModelProvider beeIconProvider) {
		if (beeIconProvider instanceof CustomBeeModel) {
			modelName = ((CustomBeeModel) beeIconProvider).suffix;
		}
		speciesInstructions.add(c -> c.setCustomBeeModelProvider(beeIconProvider));
		return this;
	}

	public final CareerBeeEntry removeVanillaProducts(){
		shouldAddVanillaProducts  = false;
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setCustomBeeSpriteColourProvider(@Nonnull IBeeSpriteColourProvider beeIconColourProvider) {
		speciesInstructions.add(c -> c.setCustomBeeSpriteColourProvider(beeIconColourProvider));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setTemplateAllelleBool(@Nonnull EnumBeeChromosome effect, boolean val) {
		return setTemplateAllelleString(effect, val ? "forestry.boolTrue" : "forestry.boolFalse");
	}

	@Nonnull
	public final CareerBeeEntry setTemplateAllelleString(@Nonnull EnumBeeChromosome effect, String value) {
		speciesInstructions.add(c -> c.setTemplateAllelle(effect, value));
		return this;
	}

	@Nonnull
	public final CareerBeeEntry setTemplateEffect(@Nonnull Supplier<? extends EffectBase> supplier) {
		return setTemplate(EnumBeeChromosome.EFFECT, supplier);
	}

	@Nonnull
	public final CareerBeeEntry setTemplate(@Nonnull EnumBeeChromosome effect, @Nonnull Supplier<? extends IAllele> supplier) {
		speciesInstructions.add(c -> c.setTemplateAllelle(effect, supplier.get()));
		return this;
	}

	public final void build() {
		CustomBeeFactory beeFactory = CustomBeeFactory.factory(name, dominant, branchName, primaryColor, secondaryColor);
		speciesInstructions.forEach(c -> c.accept(beeFactory));
		if (shouldAddVanillaProducts)
			addVanillaProducts(beeFactory);
		species = beeFactory.build();
	}

	private void addVanillaProducts(@Nonnull CustomBeeFactory beeFactory) {
		List<BeeMutationTree.SpeciesEntry> vanillaParents = CareerBeeSpecies.tree.getVanillaParents(this);
		HashMap<ItemStack, Pair<Float, Float>> sumMap = new HashMap<>();
		for (BeeMutationTree.SpeciesEntry entry : vanillaParents) {
			Map<ItemStack, Float> productChances = entry.get().getProductChances();
			mainLoop:
			for (Map.Entry<ItemStack, Float> inEntry : productChances.entrySet()) {
				for (Map.Entry<ItemStack, Pair<Float, Float>> existEntry : sumMap.entrySet()) {
					if (ItemHandlerHelper.canItemStacksStack(existEntry.getKey(), inEntry.getKey())) {
						existEntry.setValue(Pair.of(inEntry.getValue() + existEntry.getValue().getLeft(), Math.max(inEntry.getValue(), existEntry.getValue().getRight())));
						continue mainLoop;
					}
				}
				sumMap.put(inEntry.getKey().copy(), Pair.of(inEntry.getValue(), inEntry.getValue()));
			}
		}
		for (Map.Entry<ItemStack, Pair<Float, Float>> itemStackFloatEntry : sumMap.entrySet()) {
			float meanChance = itemStackFloatEntry.getValue().getLeft() / vanillaParents.size();
			Float maxChance = itemStackFloatEntry.getValue().getRight();
			float finalChance = (meanChance + maxChance) / 2;
			float chance = roundSig(finalChance, 20);
			if (chance > 0) {
				beeFactory.addProduct(itemStackFloatEntry.getKey(), chance);
			}
		}
	}

	@Override
	public final IAlleleBeeSpecies get() {
		return species;
	}

	public void preInit() {

	}

	public void init() {

	}

	@Override
	public boolean isVanilla() {
		return false;
	}

	@Nonnull
	public String getAlleleName() {
		return species.getAlleleName();
	}


	public static class CustomBeeFactory implements IAlleleBeeSpeciesBuilder {
		public static final HashMap<IAlleleBeeSpecies, IAlleleBeeEffect> SPECIES_EFFECT_MAP = new HashMap<>();
		public static final HashMap<String, IAlleleBeeSpecies> STRING_SPECIES_MAP = new HashMap<>();
		static final HashSet<String> assignedUUIDs = new HashSet<>();
		final IAlleleBeeSpeciesBuilder species;
		@Nonnull
		final AlleleTemplate template;
		final List<Pair<ItemStack, Float>> products = new ArrayList<>();

		public CustomBeeFactory(IAlleleBeeSpeciesBuilder species) {
			this.species = species;
			template = AlleleTemplate.createAlleleTemplate(null);
		}

		@Nonnull
		public static CustomBeeFactory factory(String name,
											   boolean dominant,
											   String branchName,
											   int primaryColor) {
			return factory(name, dominant, branchName, primaryColor, BEE_YELLOW);
		}

		@Nonnull
		public static CustomBeeFactory factory(String name,
											   boolean dominant,
											   String branchName,
											   int primaryColor,
											   int secondaryColor) {
			branchName = new ResourceLocation(BeeMod.MODID, new ResourceLocation(branchName).getResourcePath()).toString();
			IClassification branch = CareerBeeSpecies.classificationHashMap.computeIfAbsent(branchName, s -> {
				ResourceLocation resourceLocation = new ResourceLocation(s);
				IClassification classification = BeeManager.beeFactory.createBranch(resourceLocation.getResourcePath(), resourceLocation.getResourceDomain());
				AlleleManager.alleleRegistry.getClassification("family.apidae").addMemberGroup(classification);
				return classification;
			});
			String uid = BeeMod.MODID + "." + name;

			if (!assignedUUIDs.add(uid))
				throw new IllegalStateException(uid + " is already registered");
			String speciesKey = BeeMod.MODID + ".bees.species." + uid;
			String speciesDescKey = BeeMod.MODID + ".description." + uid;
			String speciesBinomKey = BeeMod.MODID + ".bees.binomial.species." + uid;
			if (BeeMod.deobf_folder) {
				String s = StringHelper.capFirstMulti(name);
				Lang.translate(speciesKey, s);
				Lang.translate(speciesDescKey, s + " Description");
				Lang.translate(speciesBinomKey, s);
			}
			IAlleleBeeSpeciesBuilder species = BeeManager.beeFactory.createSpecies(BeeMod.MODID, uid, dominant, "RWTema",
					speciesKey,
					speciesDescKey,
					branch, speciesBinomKey, primaryColor, secondaryColor);
			if (BeeMod.deobf) {
				species.setCustomBeeSpriteColourProvider(new DebugBeeSpriteColors(primaryColor, secondaryColor));
			}
			return new CustomBeeFactory(species);
		}

		@Nonnull
		public CustomBeeFactory setTemplateAllelle(@Nonnull EnumBeeChromosome chromosome, Object value) {
			template.setTemplateAllelle(chromosome, value);
			return this;
		}

		@Nonnull
		@Override
		public IAlleleBeeSpecies build() {
			products.forEach(pair -> this.species.addProduct(pair.getKey(), pair.getValue()));

			IAlleleBeeSpecies species = this.species.build();
			IAlleleBeeEffect effect = template.getEffect();
			if (effect instanceof EffectBase) {
				((EffectBase) effect).addSpecies(species);
			}
			template.setTemplateAllelle(EnumBeeChromosome.SPECIES, species);
			template.register();
			STRING_SPECIES_MAP.put(species.getUID(), species);
			SPECIES_EFFECT_MAP.put(species, template.getEffect());
			return species;
		}

		@Nonnull
		@Override
		public CustomBeeFactory addProduct(@Nonnull ItemStack product, @Nonnull Float chance) {
			products.add(Pair.of(product, chance));
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory addSpecialty(@Nonnull ItemStack specialty, @Nonnull Float chance) {
			species.addSpecialty(specialty, chance);
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setJubilanceProvider(@Nonnull IJubilanceProvider provider) {
			species.setJubilanceProvider(provider);
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setNocturnal() {
			species.setNocturnal();
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setCustomBeeModelProvider(@Nonnull IBeeModelProvider beeIconProvider) {
			species.setCustomBeeModelProvider(beeIconProvider);
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setCustomBeeSpriteColourProvider(@Nonnull IBeeSpriteColourProvider beeIconColourProvider) {
			species.setCustomBeeSpriteColourProvider(beeIconColourProvider);
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setTemperature(@Nonnull EnumTemperature temperature) {
			species.setTemperature(temperature);
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setHumidity(@Nonnull EnumHumidity humidity) {
			species.setHumidity(humidity);
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setHasEffect() {
			species.setHasEffect();
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setIsSecret() {
			species.setIsSecret();
			setIsNotCounted();
			return this;
		}

		@Nonnull
		@Override
		public CustomBeeFactory setIsNotCounted() {
			species.setIsNotCounted();
			return this;
		}

		@Override
		public void setComplexity(int complexity) {
			species.setComplexity(complexity);
		}

		@Nonnull
		public CustomBeeFactory setComplexityRet(int complexity) {
			setComplexity(complexity);
			return this;
		}

		@Nonnull
		public CustomBeeFactory clearDefaultProducts() {
			products.clear();
			return this;
		}
	}
}
