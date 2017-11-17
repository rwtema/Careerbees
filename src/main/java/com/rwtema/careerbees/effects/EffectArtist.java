package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EffectArtist extends EffectWorldInteraction {
	EffectArtist INSTANCE = new EffectArtist("artistic", 20 * 10);

	public EffectArtist(String name, float baseTicksBetweenProcessing) {
		super(name, false, false, baseTicksBetweenProcessing, 0.1F);
	}

	@Override
	protected boolean performPosEffect(World world, BlockPos blockPos, IBlockState state, IBeeGenome genome, IBeeHousing housing) {


		return false;
	}


}
