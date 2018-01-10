package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EffectPainting extends EffectWorldInteraction {
	public static final EffectPainting INSTANCE = new EffectPainting("painting", 20 * 30);

	public EffectPainting(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectPainting(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	public EffectPainting(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Override
	protected boolean performPosEffect(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull IBlockState state, IBeeGenome genome, IBeeHousing housing) {
		Material material = state.getMaterial();
		if (material != Material.ROCK && material != Material.IRON && material != Material.WOOD) return false;

		EnumFacing horizontal = EnumFacing.HORIZONTALS[world.rand.nextInt(4)];

		BlockPos offset = blockPos.offset(horizontal);
		if(state.isOpaqueCube()  && world.isAirBlock(offset)) {
			EntityPainting painting = new EntityPainting(world, offset, horizontal.getOpposite());
			if (painting.onValidSurface()) {
				painting.playPlaceSound();
				world.spawnEntity(painting);
			}
		}

		return false;
	}
}
