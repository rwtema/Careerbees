package com.rwtema.careerbees.bees;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.colors.QuantumBeeColors;
import com.rwtema.careerbees.colors.RainbowBeeColors;
import com.rwtema.careerbees.effects.*;
import com.rwtema.careerbees.helpers.StringHelper;
import com.rwtema.careerbees.items.ItemIngredients;
import com.rwtema.careerbees.mutations.MutationRecentExplosion;
import com.rwtema.careerbees.recipes.PlayerSpawnHandler;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static com.rwtema.careerbees.effects.EffectBase.registeredEffectSpecies;

public class CareerBeeSpecies {
	public final static int BEE_YELLOW = 0xffdc16;
	public static final Set<IAlleleBeeSpecies> registeredSpecies = new HashSet<>();
	public static final HashMap<String, IClassification> classificationHashMap = new HashMap<>();

	//	public static CustomBeeEntry CRAFTING = new CustomBeeEntry("crafting", false, "crafting:crafticus", col(188, 150, 98), col(180, 119, 75))
//			.setTemplateEffect(() -> EffectCrafting.INSTANCE)
//			.setTemplateAllelleBool(EnumBeeChromosome.NEVER_SLEEPS, true)
//			.setCustomBeeModelProvider(new CustomBeeModel("crafting"));
	public static final CareerBeeEntry STUDENT = new CareerBeeEntry("student", false, ":discipulus", col(90, 43, 25))
			.setCustomBeeModelProvider(new CustomBeeModel("student"))
//			.setTemplateEffect(() -> EffectResearcher.INSTANCE)
			;
	public static final CareerBeeEntry VOCATIONAL = new CareerBeeEntry("graduate", false, "consilium:graduati", col(60, 60, 80))
			.setTemplateEffect(() -> EffectEffection.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("graduate"));
	public static final CareerBeeEntry ARTIST = new CareerBeeEntry("artist", false, "artifex", col(40, 40, 40))
			.setTemplateEffect(() -> EffectPainting.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("artist"));
	public static final CareerBeeEntry BUISNESS = new CareerBeeEntry("buisnessman", false, "negotiator", col(40, 40, 40), col(80, 80, 80))
			.setCustomBeeModelProvider(new CustomBeeModel("tophat"));
	public static final CareerBeeEntry DIGGING_BASE = new CareerBeeEntry("digging", false, "digging:diggicus", col(222, 122, 20))
			.setTemplateAllelleBool(EnumBeeChromosome.CAVE_DWELLING, true)
			.setTemplateEffect(() -> EffectDigging.INSTANCE_NORMAL)
			.setCustomBeeModelProvider(new CustomBeeModel("mining"));
	public static final CareerBeeEntry DIGGING_FORTUNE = new CareerBeeEntry("digging.fortune", false, "digging:diggicus", col(175, 50, 20))
			.setTemplateAllelleBool(EnumBeeChromosome.CAVE_DWELLING, true)
			.setTemplateEffect(() -> EffectDigging.INSTANCE_FORTUNE)
			.setCustomBeeModelProvider(new CustomBeeModel("mining"));
	public static final CareerBeeEntry DIGGING_SILKY = new CareerBeeEntry("digging.silky", false, "digging:diggicus", col(122, 102, 200))
			.setTemplateAllelleBool(EnumBeeChromosome.CAVE_DWELLING, true)
			.setTemplateEffect(() -> EffectDigging.INSTANCE_SILKY)
			.setCustomBeeModelProvider(new CustomBeeModel("mining"));
	public static final CareerBeeEntry THIEF = new CareerBeeEntry("thief", false, "thief:cleptus", col(23, 23, 23), col(100, 100, 100))
			.setNocturnal()
			.setTemplateEffect(() -> EffectStealPlayer.PLAYER)
			.setCustomBeeModelProvider(new CustomBeeModel("thief"));
	public static final CareerBeeEntry POLICE = new CareerBeeEntry("police", false, "thief:cleptus", col(100, 140, 150), col(22, 67, 165))
			.setTemplateEffect(() -> EffectStealMob.MOB)
			.setCustomBeeModelProvider(new CustomBeeModel("police"));
	public static final CareerBeeEntry TAXCOLLECTOR = new CareerBeeEntry("taxcollector", false, "thief:cleptus", col(110, 110, 119), col(200, 200, 200))
			.setTemplateEffect(() -> EffectStealMobTaxation.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("tax_collector"));
	public static final CareerBeeEntry ACCELERATION = new CareerBeeEntry("acceleration", false, "tempus:fugit", col(140, 50, 40))
			.setTemplateEffect(() -> EffectAcceleration.INSTANCE)
			.setTemplate(EnumBeeChromosome.LIFESPAN, () -> SpecialProperties.ETERNAL)
			.setCustomBeeModelProvider(new CustomBeeModel("acceleration"));
	public static final CareerBeeEntry REPAIR = new CareerBeeEntry("repair", false, "smithing:faber", col(122, 122, 190), col(224, 210, 255))
			.setTemplateEffect(() -> EffectRepair.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("hammer"));
	public static final CareerBeeEntry SHARPENER = new CareerBeeEntry("sharpening", false, "smithing:faber", col(200, 200, 200), col(120, 120, 120))
			.setTemplateEffect(() -> EffectSharpen.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("hammer"));
	public static final CareerBeeEntry ARMORER = new CareerBeeEntry("armorer", false, "smithing:faber", col(100, 200, 200), col(100, 120, 120))
			.setTemplateEffect(() -> EffectArmorer.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("hammer"));
	public static final CareerBeeEntry LUMBER = new CareerBeeEntry("lumber", false, "ligna:choppicus", col(96, 71, 0))
			.setTemplateEffect(() -> EffectLumber.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("axe"));
	public static final CareerBeeEntry HUSBANDRYIST = new CareerBeeEntry("husbandry", false, "medicus:easpariunt", col(122, 122, 190), col(224, 210, 255))
			.setTemplateEffect(() -> EffectHusbandry.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("midwife"));
	public static final CareerBeeEntry BUTCHER = new CareerBeeEntry("butcher", false, "macello:caedes", col(250, 150, 150))
			.setTemplateEffect(() -> EffectButcher.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("butcher"));
	public static final CareerBeeEntry WOMBLING = new CareerBeeEntry("collectors", false, "lectica:coactore", col(197, 60, 60))
			.setCustomBeeModelProvider(new CustomBeeModel("wombling"))
			.setTemplateEffect(() -> EffectPickup.INSTANCE);
	public static final CareerBeeEntry COOK = new CareerBeeEntry("cook", false, "cook:cookus", col(255, 255, 255))
			.setCustomBeeModelProvider(new CustomBeeModel("chef"))
			.setTemplateEffect(() -> EffectHoneyGlaze.INSTANCE);
	public static final CareerBeeEntry YENTE = new CareerBeeEntry("yente", false, "matchmaker", col(44, 44, 51))
			.setCustomBeeModelProvider(new CustomBeeModel("yente"));
	public static final CareerBeeEntry RAINBOW = new CareerBeeEntry("rainbow", false, "rain:eritque.arcus", col(255, 255, 255), col(255, 255, 255)) {
		{
			LinkedHashMultimap<EnumDyeColor, EnumDyeColor> dyeRecipes = LinkedHashMultimap.create();
			dyeRecipes.putAll(EnumDyeColor.ORANGE, ImmutableList.of(EnumDyeColor.RED, EnumDyeColor.YELLOW));
			dyeRecipes.putAll(EnumDyeColor.LIGHT_BLUE, ImmutableList.of(EnumDyeColor.WHITE, EnumDyeColor.BLUE));
			dyeRecipes.putAll(EnumDyeColor.PINK, ImmutableList.of(EnumDyeColor.WHITE, EnumDyeColor.RED));
			dyeRecipes.putAll(EnumDyeColor.GRAY, ImmutableList.of(EnumDyeColor.WHITE, EnumDyeColor.BLACK));
			dyeRecipes.putAll(EnumDyeColor.SILVER, ImmutableList.of(EnumDyeColor.WHITE, EnumDyeColor.GRAY));
			dyeRecipes.putAll(EnumDyeColor.LIME, ImmutableList.of(EnumDyeColor.WHITE, EnumDyeColor.GREEN));
			dyeRecipes.putAll(EnumDyeColor.PURPLE, ImmutableList.of(EnumDyeColor.RED, EnumDyeColor.BLUE));
			dyeRecipes.putAll(EnumDyeColor.CYAN, ImmutableList.of(EnumDyeColor.GREEN, EnumDyeColor.BLUE));
			dyeRecipes.putAll(EnumDyeColor.MAGENTA, ImmutableList.of(EnumDyeColor.PURPLE, EnumDyeColor.PINK));

			HashMap<EnumDyeColor, Double> probabilities = new HashMap<>();
			probabilities.put(EnumDyeColor.RED, 5.0);
			probabilities.put(EnumDyeColor.WHITE, 1.0);
			probabilities.put(EnumDyeColor.GREEN, 5.0);
			probabilities.put(EnumDyeColor.BLUE, 0.05);
			probabilities.put(EnumDyeColor.BLACK, 2.0);
			probabilities.put(EnumDyeColor.YELLOW, 5.0);
			probabilities.put(EnumDyeColor.BROWN, 0.5);

			for (EnumDyeColor enumDyeColor : dyeRecipes.keySet()) {
				Set<EnumDyeColor> enumDyeColors = dyeRecipes
						.get(enumDyeColor);
				probabilities.put(enumDyeColor,
						enumDyeColors.stream()
								.map(probabilities::get)
								.reduce(0.0, Double::sum)
				);
			}
			double sum = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
			double sum2 = probabilities.values().stream().mapToDouble(Double::doubleValue).map(t -> t * t).sum();
			@SuppressWarnings("UnnecessaryLocalVariable")
			double a = sum2 / 2, b = sum, c = -Math.log(0.5);
			double k = (b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
			for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
				float chance = CareerBeeEntry.roundSig((float) (probabilities.get(enumDyeColor) * k), 20);
				addProduct(new ItemStack(Items.DYE, 1, enumDyeColor.getDyeDamage()), chance);
			}
		}
	}
			.setCustomBeeSpriteColourProvider(new RainbowBeeColors());
	public static final CareerBeeEntry DIRE = new CareerBeeEntry("dire", false, "dire", 0x00AFAF, 0xff463AA5)
			.setTemplateEffect(() -> EffectDire.INSTANCE)
			.setIsSecret();
	public static final CareerBeeEntry SOARYN = new CareerBeeEntry("soaring", false, "soaryn", col(0, 0, 16), col(80, 68, 99))
			.setTemplateEffect(() -> EffectSoaring.INSTANCE)
			.setIsSecret();
	public static final CareerBeeEntry MASON = new CareerBeeEntry("mason", false, "mason:mason", col(120, 120, 120))
			.setTemplateEffect(() -> EffectMason.INSTANCE);
	public static final CareerBeeEntry SMELTER = new CareerBeeEntry("smelter", false, "smelter:smelter", col(81, 72, 64), col(109, 81, 53))
			.setCustomBeeModelProvider(new CustomBeeModel("smelter"))
			.setTemplateEffect(() -> EffectSmelt.INSTANCE);

	public static final CareerBeeEntry HONEY_SMELTER = new CareerBeeEntry("honey_smelter", false, "smelter:smelter", col(230,160, 10), col(120, 50, 0))
			.setCustomBeeModelProvider(new CustomBeeModel("smelter"))
			.setTemplateEffect(() -> EffectHoneyCombIngotConvert.INSTANCE);

	public static final CareerBeeEntry ORE_CRUSHER = new CareerBeeEntry("crusher", false, "crusher:crusher", col(120, 120, 120), col(80, 80, 80))
			.setCustomBeeModelProvider(new CustomBeeModel("saw"))
			.setTemplateEffect(() -> EffectOreCrushing.INSTANCE);
	public static final CareerBeeEntry SCIENTIST = new CareerBeeEntry("science", false, "science", col(158, 154, 159))
			.setCustomBeeModelProvider(new CustomBeeModel("einstein"));
	public static final CareerBeeEntry DOCTOR = new CareerBeeEntry("doctor", false, "science", col(255, 255, 255))
			.setTemplateEffect(() -> EffectHeal.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("white_on_pinkish_cross"));
	public static final CareerBeeEntry PLAGUE_DOCTOR = new CareerBeeEntry("plaguedoctor", false, "medicine", col(25, 25, 25), col(140, 140, 140))
			.setTemplateEffect(() -> EffectCurative.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("plague_doctor"));
	public static final CareerBeeEntry PHD = new CareerBeeEntry("phd", false, "consilium:graduati", col(160, 60, 80))
			.setTemplateEffect(() -> EffectEffection.INSTANCE)
			.setCustomBeeModelProvider(new CustomBeeModel("graduate"));
	public static final CareerBeeEntry CLOCKWORK = new CareerBeeEntry("clock", false, "clockwork", col(188, 181, 128), col(78, 70, 57))
			.setCustomBeeModelProvider(new CustomBeeModel("clockwork"))
			.setTemplateEffect(() -> EffectClockwork.INSTANCE);
	public static final CareerBeeEntry ENERGY = new CareerBeeEntry("energy", false, "redstoneflux", col(255, 0, 0), col(143, 0, 0))
			.setCustomBeeModelProvider(new CustomBeeModel("energy"))
			.setTemplateEffect(() -> EffectPower.INSTANCE);
	public static final CareerBeeEntry ENGINEER = new CareerBeeEntry("engineer", false, "engineer", col(48, 35, 188))
			.setCustomBeeModelProvider(new CustomBeeModel("engineer"));

	public static final CareerBeeEntry QUANTUM_CHARM = new CareerBeeEntry("quantum_charm", false, "science", col(255, 255, 255), col(0, 0, 0))
			.setCustomBeeSpriteColourProvider(new QuantumBeeColors(0, 0.666F, 0.5F))
			.setTemplateEffect(() -> EffectRandomSwap.INSTANCE)
			.setTemplateAllelleString(EnumBeeChromosome.SPEED, "forestry.speedFast")
			.setTemplateAllelleString(EnumBeeChromosome.FERTILITY, "forestry.fertilityHigh")
			.setTemplateAllelleString(EnumBeeChromosome.TEMPERATURE_TOLERANCE, "forestry.toleranceDown1")
			.setTemplateAllelleString(EnumBeeChromosome.HUMIDITY_TOLERANCE, "forestry.toleranceUp1")
			.setTemplateAllelleBool(EnumBeeChromosome.NEVER_SLEEPS, true)
			.setTemplateAllelleBool(EnumBeeChromosome.TOLERATES_RAIN, false)
			.setTemplateAllelleBool(EnumBeeChromosome.CAVE_DWELLING, true)
			.setTemplateAllelleString(EnumBeeChromosome.FLOWERING, "forestry.floweringFast")
			.setTemplateAllelleString(EnumBeeChromosome.TERRITORY, "forestry.territoryAverage")
			.setTemplateAllelleString(EnumBeeChromosome.SPEED, "forestry.speedFast")
			.removeVanillaProducts()
			.addProduct(ItemIngredients.IngredientType.YING.get(), 0.1F)
			;

	public static final CareerBeeEntry QUANTUM_STRANGE = new CareerBeeEntry("quantum_strange", false, "science", col(0, 0, 0), col(255, 255, 255))
			.setCustomBeeSpriteColourProvider(new QuantumBeeColors(3.141F, 0F, 0.5F))
			.setTemplateEffect(() -> EffectRandomSwap.INSTANCE)
			.setTemplateAllelleString(EnumBeeChromosome.SPEED, "forestry.speedSlow")
			.setTemplateAllelleString(EnumBeeChromosome.FERTILITY, "forestry.fertilityHigh")
			.setTemplateAllelleString(EnumBeeChromosome.TEMPERATURE_TOLERANCE, "forestry.toleranceUp1")
			.setTemplateAllelleString(EnumBeeChromosome.HUMIDITY_TOLERANCE, "forestry.toleranceDown1")
			.setTemplateAllelleBool(EnumBeeChromosome.NEVER_SLEEPS, false)
			.setTemplateAllelleBool(EnumBeeChromosome.TOLERATES_RAIN, true)
			.setTemplateAllelleBool(EnumBeeChromosome.CAVE_DWELLING, false)
			.setTemplateAllelleString(EnumBeeChromosome.FLOWERING, "forestry.floweringSlow")
			.setTemplateAllelleString(EnumBeeChromosome.TERRITORY, "forestry.territoryAverage")
			.setTemplateAllelleString(EnumBeeChromosome.SPEED, "forestry.speedSlow")
			.removeVanillaProducts()
			.addProduct(ItemIngredients.IngredientType.YANG.get(), 0.1F)
			;

	public static final CareerBeeEntry MAD_SCIENTIST = new CareerBeeEntry("mad_scientist", false, "science", col(84, 121, 132))
			.setCustomBeeModelProvider(new CustomBeeModel("mad_scientist"))
			.setTemplateEffect(() -> EffectExplosion.INSTANCE);


	static final BeeMutationTree tree = new BeeMutationTree();
	public static ArrayList<CareerBeeEntry> sorted_bee_entries;

	public static void register() {
		SpecialProperties.init();

		sorted_bee_entries = Lists.newArrayList(CareerBeeEntry.BEE_ENTRIES);
		Map<CareerBeeEntry, Integer> bee_complexity = sorted_bee_entries.stream().collect(Collectors.toMap(t -> t, s -> tree.getLeastParents(s).stream().mapToInt(Set::size).min().orElse(0)));
		Map<String, Double> bee_model_complexity = sorted_bee_entries.stream().collect(
				Collectors.groupingBy(
						s -> s.modelName,
						Collectors.collectingAndThen(
								Collectors.mapping(bee_complexity::get, Collectors.toList()),
								s -> s.stream().mapToInt(Integer::intValue).average().orElseThrow(RuntimeException::new))));
		sorted_bee_entries.sort(Comparator
				.comparing(CareerBeeEntry::isSecret)
				.thenComparingDouble(t -> bee_model_complexity.get(t.modelName))
				.thenComparingInt(bee_complexity::get));
		sorted_bee_entries.forEach(CareerBeeEntry::build);

		CareerBeeEntry.BEE_ENTRIES.forEach(CareerBeeEntry::init);

		registeredSpecies.addAll(sorted_bee_entries.stream().map(t -> t.species).filter(Objects::nonNull).collect(Collectors.toSet()));

		PlayerSpawnHandler.registerBeeSpawn("dire", UUID.fromString("BBB87DBE-690F-4205-BDC5-72FFB8EBC29D"), DIRE);
		PlayerSpawnHandler.registerBeeSpawn("soaryn", UUID.fromString("4F3A8D1E-33C1-44E7-BCE8-E683027C7DAC"), SOARYN);
		PlayerSpawnHandler.registerBeeSpawn("rwtema", UUID.fromString("72DDAA05-7BBE-4AE2-9892-2C8D90EA0AD8"), ACCELERATION);

		if (BeeMod.deobf_folder) {
			StringBuilder[] strings = new StringBuilder[EnumBeeChromosome.values().length];
			for (int i = 0; i < EnumBeeChromosome.values().length; i++) {
				strings[i] = new StringBuilder();
			}
			List<IAllele> registeredAlleles = Lists.newArrayList(AlleleManager.alleleRegistry.getRegisteredAlleles(EnumBeeChromosome.SPECIES));
			registeredAlleles.sort(Comparator.comparing(IAllele::getModID).thenComparing(IAllele::getUID));
			//noinspection unchecked
			for (IAlleleBeeSpecies iAllele : (Collection<IAlleleBeeSpecies>) ((Collection) registeredAlleles)) {
				IAllele[] template = BeeManager.beeRoot.getTemplate(iAllele);
				for (int i = 0; i < EnumBeeChromosome.values().length; i++) {
					EnumBeeChromosome chromosome = EnumBeeChromosome.values()[i];
					HashMap<String, String> abbreviate = StringHelper.abbreviate(AlleleManager.alleleRegistry.getRegisteredAlleles(chromosome).stream().map(IAllele::getAlleleName)::iterator);
					strings[i].append("\t").append(abbreviate.get(template[i].getAlleleName()));
				}
			}
			for (StringBuilder string : strings) {
				BeeMod.logger.info(string.toString());
			}
		}
		tree.registerMutations();

		if (BeeMod.deobf_folder) {
			BeeMod.logger.info(sorted_bee_entries.stream().map(CareerBeeEntry::get).map(IAllele::getAlleleName).collect(Collectors.joining("\n")));


			BeeMod.logger.info(sorted_bee_entries.stream().map(c -> {
				IAlleleBeeSpecies t = c.get();
				String alleleName = t.getAlleleName();

				Collection<? extends IMutation> paths = BeeManager.beeRoot.getPaths(t, EnumBeeChromosome.SPECIES);
				if (paths.isEmpty()) return alleleName;
				String mutationString = paths.stream().map(
						t2 -> {
							String s = t2.getAllele0().getAlleleName() + " + " + t2.getAllele1().getAlleleName();
							if (t2.getSpecialConditions().isEmpty()) {
								return s;
							}
							return s + " " + t2.getSpecialConditions().stream().collect(Collectors.joining(", ", "(", ")"));
						}

				).collect(Collectors.joining(" "));

				String s = alleleName + " Bees:  " + mutationString + "\n - " + t.getDescription();

				EffectBase effectBase = registeredEffectSpecies.get(t);
				if (effectBase != null) {
					s = s + "\n" + effectBase.getAlleleName() + ": " + I18n.format(effectBase.getUnlocalizedName() + ".desc");
				}

				return s;
			}).collect(Collectors.joining("\n\n")));
		}
	}

	public static void init() {
		CareerBeeEntry.BEE_ENTRIES.forEach(CareerBeeEntry::preInit);
		buildMutationList();
	}

	public static void buildMutationList() {

		BeeMutationTree.SpeciesEntry COMMON = getSpecies("forestry.speciesCommon");
		BeeMutationTree.SpeciesEntry CULTIVATED = getSpecies("forestry.speciesCultivated");
		BeeMutationTree.SpeciesEntry NOBLE = getSpecies("forestry.speciesNoble");

		tree.add(COMMON, CULTIVATED, STUDENT, 1, v -> v.requireResource(Blocks.BOOKSHELF.getDefaultState()));
		tree.add(STUDENT, COMMON, VOCATIONAL, 0.5);

		tree.add(VOCATIONAL, ENGINEER, DIGGING_BASE, 0.1, v -> v.requireResource("blockIron"));
		tree.add(ENGINEER, DIGGING_BASE, DIGGING_SILKY, 0.1, v -> v.requireResource("blockGold"));
		tree.add(DIGGING_BASE, DIGGING_SILKY, DIGGING_FORTUNE, 0.1, v -> v.requireResource("blockDiamond"));

		tree.add(VOCATIONAL, getFSpecies("Valiant"), POLICE, 0.1);
		tree.add(getFSpecies("Sinister"), POLICE, THIEF, 0.1);

		tree.add(VOCATIONAL, VOCATIONAL, ARMORER, 0.1, v -> v.requireResource(Blocks.ANVIL.getBlockState().getValidStates().stream().toArray(IBlockState[]::new)));
		tree.add(VOCATIONAL, ARMORER, SHARPENER, 0.1);
		tree.add(ARMORER, SHARPENER, REPAIR, 0.05);

		tree.add(VOCATIONAL, getFSpecies("Forest"), LUMBER, 0.5);

		tree.add(VOCATIONAL, LUMBER, BUTCHER, 0.5);

		tree.add(VOCATIONAL, getFSpecies("Meadows"), HUSBANDRYIST, 0.5);
		tree.add(STUDENT, HUSBANDRYIST, YENTE, 0.5);

		tree.add(STUDENT, COMMON, WOMBLING, 0.5);

		tree.add(VOCATIONAL, SMELTER, MASON, 0.5);
		tree.add(SMELTER, ARTIST, COOK, 0.2);

		tree.add(SMELTER, VOCATIONAL, HONEY_SMELTER, 0.2);

		tree.add(VOCATIONAL, getFSpecies("Modest"), SMELTER, 0.5, v -> v.requireResource(BlockStateList.of(Blocks.FURNACE, Blocks.LIT_FURNACE)));

		tree.add(MASON, SHARPENER, ORE_CRUSHER, 0.1);

		tree.add(STUDENT, VOCATIONAL, PHD, 0.3);
		tree.add(PHD, getFSpecies("Industrious"), SCIENTIST, 0.2);

		tree.add(PHD, getFSpecies("Imperial"), BUISNESS, 0.2);

		tree.add(PHD, getFSpecies("Noble"), ENGINEER, 0.3);

		tree.add(ENGINEER, SMELTER, CLOCKWORK, 0.2);

		tree.add(REPAIR, CLOCKWORK, ENERGY, 0.05);

		tree.add(PHD, getFSpecies("Majestic"), DOCTOR, 0.3);
		tree.add(DOCTOR, getFSpecies("Sinister"), PLAGUE_DOCTOR, 0.3);

//		tree.add(SCIENTIST, , ACCELERATION, 0.05);

		tree.add(VOCATIONAL, getFSpecies("Cultivated"), ARTIST, 0.4);

		tree.add(ARTIST, PHD, RAINBOW, 0.1);

		tree.add(POLICE, THIEF, TAXCOLLECTOR, 0.2);

		tree.add(MAD_SCIENTIST, getFSpecies("Phantasmal"), QUANTUM_STRANGE, 0.5);
		tree.add(MAD_SCIENTIST, getFSpecies("Phantasmal"), QUANTUM_CHARM, 0.5);
		tree.add(QUANTUM_CHARM, QUANTUM_CHARM, QUANTUM_STRANGE, 0.5);
		tree.add(QUANTUM_STRANGE, QUANTUM_STRANGE, QUANTUM_CHARM, 0.5);
		tree.add(QUANTUM_STRANGE, QUANTUM_CHARM, ACCELERATION, 0.01);

		tree.add(SCIENTIST, ENGINEER, MAD_SCIENTIST, 1, iBeeMutationBuilder -> iBeeMutationBuilder.addMutationCondition(new MutationRecentExplosion().forceMutation(MAD_SCIENTIST)));

		if (BeeMod.deobf) {
			HashSet<BeeMutationTree.SpeciesEntry> entries = Sets.newHashSet(CareerBeeEntry.BEE_ENTRIES);
			entries.removeAll(tree.recipes.keySet());
			entries.remove(STUDENT);
			entries.removeIf(BeeMutationTree.SpeciesEntry::isVanilla);
			BeeMod.logger.info(entries.stream().map(Object::toString).collect(Collectors.joining("\n")));


		}
	}

	@Nonnull
	private static BeeMutationTree.SpeciesEntry getFSpecies(String uuid) {
		return getSpecies("forestry.species" + uuid);
	}

	@Nonnull
	private static BeeMutationTree.SpeciesEntry getSpecies(String uuid) {
		return new BeeMutationTree.VanillaEntry(uuid);
	}

	public static IMutationBuilder register(@Nonnull IAlleleBeeSpecies inA, @Nonnull IAlleleBeeSpecies inB, @Nonnull IAlleleBeeSpecies out, int chance) {
		return BeeManager.beeMutationFactory.createMutation(inA, inB, BeeManager.beeRoot.getTemplate(out), chance);
	}

	public static int col(int r, int g, int b) {
		return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}

}
