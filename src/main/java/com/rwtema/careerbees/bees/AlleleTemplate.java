package com.rwtema.careerbees.bees;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.core.config.Constants;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;

public class AlleleTemplate {
	final EnumMap<EnumBeeChromosome, IAllele> map = new EnumMap<>(EnumBeeChromosome.class);

	private AlleleTemplate(@Nullable IAlleleBeeSpecies beeSpecies) {
		if (beeSpecies != null) {
			setTemplateAllelle(EnumBeeChromosome.SPECIES, beeSpecies);
		}
		setTemplateAllelle(EnumBeeChromosome.SPEED, "forestry.speedSlow");
		setTemplateAllelle(EnumBeeChromosome.LIFESPAN, "forestry.lifespanLong");
		setTemplateAllelle(EnumBeeChromosome.FERTILITY, "forestry.fertilityNormal");
		setTemplateAllelle(EnumBeeChromosome.TEMPERATURE_TOLERANCE, "forestry.toleranceNone");
		setTemplateAllelle(EnumBeeChromosome.NEVER_SLEEPS, "forestry.boolFalse");
		setTemplateAllelle(EnumBeeChromosome.HUMIDITY_TOLERANCE, "forestry.toleranceNone");
		setTemplateAllelle(EnumBeeChromosome.TOLERATES_RAIN, "forestry.boolFalse");
		setTemplateAllelle(EnumBeeChromosome.CAVE_DWELLING, "forestry.boolFalse");
		setTemplateAllelle(EnumBeeChromosome.FLOWER_PROVIDER, "forestry.flowersVanilla");
		setTemplateAllelle(EnumBeeChromosome.FLOWERING, "forestry.floweringSlow");
		setTemplateAllelle(EnumBeeChromosome.TERRITORY, "forestry.territoryAverage");
		setTemplateAllelle(EnumBeeChromosome.EFFECT, "forestry.effectNone");
	}


	@Nonnull
	public static AlleleTemplate createAlleleTemplate(IAlleleBeeSpecies beeSpecies) {
		return new AlleleTemplate(beeSpecies);
	}

	public void register() {
		IAllele[] alleles = new IAllele[EnumBeeChromosome.values().length];
		for (int i = 0; i < alleles.length; i++) {
			EnumBeeChromosome key = EnumBeeChromosome.values()[i];
			if (!map.containsKey(key)) {
				throw new IllegalStateException(key + " entry not found");
			}
			alleles[i] = map.get(key);
		}
		BeeManager.beeRoot.registerTemplate(alleles);
	}

	@Nonnull
	public IAlleleBeeEffect getEffect() {
		return getValue(EnumBeeChromosome.EFFECT, IAlleleBeeEffect.class);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public <T extends IAllele> T getValue(@Nonnull EnumBeeChromosome chromosome, @Nonnull Class<T> clazz) {
		Validate.isTrue(chromosome.getAlleleClass().isAssignableFrom(clazz));
		IAllele iAllele = map.get(chromosome);
		return (T) iAllele;
	}

	@Nonnull
	public AlleleTemplate setTemplateAllelle(@Nonnull EnumBeeChromosome chromosome, Object value) {
		if (value instanceof IAllele) {
			map.put(chromosome, (IAllele) value);
			return this;
		} else if (value instanceof String) {

			for (IAllele iAllele : AlleleManager.alleleRegistry.getRegisteredAlleles(chromosome)) {
				if (value.equals(iAllele.getUID())) {
					map.put(chromosome, iAllele);
					return this;
				}
			}


			for (IAllele iAllele : AlleleManager.alleleRegistry.getRegisteredAlleles(chromosome)) {
				if (iAllele.getModID().equals(Constants.MOD_ID) && iAllele.getUID().contains((String) value)) {
					map.put(chromosome, iAllele);
					return this;
				}
			}

			String s = AlleleManager.alleleRegistry.getRegisteredAlleles(chromosome)
					.stream()
					.map(IAllele::getUID)
					.collect(StringBuilder::new, (stringBuilder, str) -> stringBuilder.append(str).append(' '),
							StringBuilder::append).toString();
			throw new RuntimeException("Error[" + value + "] + " + chromosome + " {" + s + "}");
		} else {

			throw new RuntimeException("Error[" + value + "] + " + chromosome);
		}
	}
}
