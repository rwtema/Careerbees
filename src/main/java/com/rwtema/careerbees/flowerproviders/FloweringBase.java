package com.rwtema.careerbees.flowerproviders;

import com.rwtema.careerbees.BeeMod;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.genetics.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import javax.annotation.Nonnull;

public class FloweringBase implements IFlowerProvider {

	public final IAlleleFlowers ALLELE_FLOWERS;

	public FloweringBase(String floweringName, boolean isDominant) {
		ALLELE_FLOWERS = AlleleManager.alleleFactory.createFlowers(BeeMod.MODID, "flowers", floweringName, this, isDominant, EnumBeeChromosome.FLOWER_PROVIDER);
	}

	@Override
	public boolean isAcceptedPollinatable(@Nonnull World world, @Nonnull ICheckPollinatable pollinatable) {
		return pollinatable.getPlantType() != EnumPlantType.Nether;
	}

	@Nonnull
	@Override
	public String getFlowerType() {
		return ALLELE_FLOWERS.getAlleleName();
	}

	@Nonnull
	@Override
	public String getDescription() {
		return ALLELE_FLOWERS.getUnlocalizedName();
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> affectProducts(@Nonnull World world, @Nonnull IIndividual individual, @Nonnull BlockPos pos, @Nonnull NonNullList<ItemStack> products) {
		return products;
	}
}
