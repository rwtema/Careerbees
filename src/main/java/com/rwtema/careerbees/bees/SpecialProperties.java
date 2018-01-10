package com.rwtema.careerbees.bees;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.genetics.*;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import java.util.Locale;


@SuppressWarnings("SameParameterValue")
public class SpecialProperties {
	public static IAllele FERTILITY_6;
	public static IAllele FERTILITY_7;
	public static IAllele SPEED_0;
	public static IAllele ETERNAL;
	public static IAllele AGORAPHOBIC;
	public static IAllele LEGENDARY_SPEED;
	public static IAllele LEGENDARY_;

	public static void init() {
		SPEED_0 = createFloat("lazy", 0.001F, true, EnumBeeChromosome.SPEED);
		FERTILITY_6 = createInteger("prolific", 6, false, EnumBeeChromosome.FERTILITY);
		FERTILITY_7 = createInteger("catholic", 7, false, EnumBeeChromosome.FERTILITY);
		ETERNAL = createInteger("immortal", 3000, true, EnumBeeChromosome.LIFESPAN);
		AGORAPHOBIC = createArea("agoraphobic", new Vec3i(0.01, 0.01, 0.01), true, EnumBeeChromosome.TERRITORY);
	}


	@Nonnull
	public static IAlleleInteger createInteger(String valName, int value, boolean isDominant, @Nonnull EnumBeeChromosome chromosome) {
		return createType(AlleleManager.alleleFactory::createInteger, valName, chromosome, value, isDominant);
	}

	@Nonnull
	public static IAlleleFloat createFloat(String valName, float value, boolean isDominant, @Nonnull EnumBeeChromosome chromosome) {
		return createType(AlleleManager.alleleFactory::createFloat, valName, chromosome, value, isDominant);
	}

	@Nonnull
	public static IAlleleBoolean createBoolean(String valName, boolean value, boolean isDominant, @Nonnull EnumBeeChromosome chromosome) {
		return SpecialProperties.createType((modId, category, valueName, value1, isDominant1, type) -> AlleleManager.alleleFactory.createBoolean(modId, category, value1, isDominant1), valName, chromosome, value, isDominant);
	}

	@Nonnull
	public static IAlleleArea createArea(String valName, Vec3i value, boolean isDominant, @Nonnull EnumBeeChromosome chromosome) {
		return createType(AlleleManager.alleleFactory::createArea, valName, chromosome, value, isDominant);
	}

	@Nonnull
	public static <V, K extends IAllele> K createType(@Nonnull RegisterType<V, K> t, String valName, @Nonnull EnumBeeChromosome chromosome, V value, boolean isDominant) {
		K type = t.createType(BeeMod.MODID, chromosome.name().toLowerCase(Locale.ENGLISH), valName, value, isDominant, chromosome);
		Lang.translate(type.getUnlocalizedName(), valName);
		return type;
	}


	public interface RegisterType<V, K extends IAllele> {
		@Nonnull
		K createType(String modId, String category, String valueName, V value, boolean isDominant, IChromosomeType type);
	}
}
