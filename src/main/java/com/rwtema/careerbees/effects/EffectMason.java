package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EffectMason extends EffectWorldInteraction {
	public static EffectMason INSTANCE = new EffectMason("mason", 10);

	public EffectMason(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Override
	protected boolean performPosEffect(World world, BlockPos blockPos, IBlockState state, IBeeGenome genome, IBeeHousing housing) {
		if (state.getBlock() == Blocks.COBBLESTONE) {
			world.setBlockState(blockPos, Blocks.STONEBRICK.getDefaultState());
			return true;
		}
		return false;
	}
}
