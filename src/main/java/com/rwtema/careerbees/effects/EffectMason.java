package com.rwtema.careerbees.effects;

import com.google.common.collect.ImmutableMap;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Map;

public class EffectMason extends EffectWorldInteraction {
	public static final EffectMason INSTANCE = new EffectMason("mason", 10);

	Map<IBlockState, IBlockState> map = ImmutableMap.<IBlockState, IBlockState>builder()
			.put(Blocks.COBBLESTONE.getDefaultState(), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE))
			.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE), Blocks.STONEBRICK.getDefaultState())
			.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH))
			.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH))
			.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH))
			.build();

	public EffectMason(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Override
	protected boolean performPosEffect(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull IBlockState state, IBeeGenome genome, IBeeHousing housing) {
		IBlockState blockState = map.get(state);
		if (blockState != null) {
			world.setBlockState(blockPos, blockState);
			return true;
		}
		return false;
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, EnumFacing sideHit) {
		return map.containsKey(world.getBlockState(pos));
	}
}
