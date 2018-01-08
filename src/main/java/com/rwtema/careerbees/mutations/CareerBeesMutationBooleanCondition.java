package com.rwtema.careerbees.mutations;

import com.rwtema.careerbees.helpers.GendustryApiHelper;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutationCondition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class CareerBeesMutationBooleanCondition implements IMutationCondition {
	static {

	}

	final String desc;

	public CareerBeesMutationBooleanCondition(String desc) {
		this.desc = Lang.translate(desc);
	}

	public CareerBeesMutationBooleanCondition forceMutation(Supplier<IAlleleBeeSpecies> madScientist) {
		GendustryApiHelper.getInstance().forceMutation(madScientist.get());
		return this;
	}

	@Override
	public float getChance(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IAllele allele0, @Nonnull IAllele allele1, @Nonnull IGenome genome0, @Nonnull IGenome genome1, @Nonnull IClimateProvider climate) {
		return isAcceptable(world, pos, allele0, allele1, genome0, genome1, climate) ? 1 : 0;
	}

	protected abstract boolean isAcceptable(World world, BlockPos pos, IAllele allele0, IAllele allele1, IGenome genome0, IGenome genome1, IClimateProvider climate);

	@Nonnull
	@Override
	public String getDescription() {
		return desc;
	}
}
