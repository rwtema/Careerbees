package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectBurning extends EffectWorldInteraction {
	public static final EffectBase INSTANCE = new EffectBurning("burning", 4, 0.01F);

	public EffectBurning(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectBurning(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	public EffectBurning(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nullable EnumFacing sideHit) {
		if (world.isAirBlock(pos)) {
			for (EnumFacing enumfacing : EnumFacing.values()) {
				if (enumfacing == EnumFacing.DOWN && world.isSideSolid(pos.offset(EnumFacing.DOWN), EnumFacing.UP)) {
					return true;
				} else if (Blocks.FIRE.canCatchFire(world, pos.offset(enumfacing), enumfacing.getOpposite())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean performPosEffect(World world, BlockPos blockPos, IBlockState state, IBeeGenome genome, IBeeHousing housing) {
		if (world.isAirBlock(blockPos)) {
			for (EnumFacing enumfacing : EnumFacing.values()) {
				if (enumfacing == EnumFacing.DOWN && world.isSideSolid(blockPos.offset(EnumFacing.DOWN), EnumFacing.UP) || Blocks.FIRE.canCatchFire(world, blockPos.offset(enumfacing), enumfacing.getOpposite())) {
					world.setBlockState(blockPos, Blocks.FIRE.getDefaultState(), 3);
					return true;
				}
			}
		}

		return false;
	}
}
