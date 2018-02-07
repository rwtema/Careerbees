package com.rwtema.careerbees.blocks;

import com.rwtema.careerbees.BeeMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockBeeGlow extends Block {
	public final static double SIZE = 6.0 / 16.0;
	public final static AxisAlignedBB LIGHT_BOUNDS = new AxisAlignedBB(
			SIZE, SIZE, SIZE, 1 - SIZE, 1 - SIZE, 1 - SIZE
	);
	public final static Material GLOW = (new Material(MapColor.AIR) {
		{
			setNoPushMobility();
			setReplaceable();
		}
	});

	public BlockBeeGlow() {
		super(Material.AIR);
		setLightLevel(15);
		setHardness(0);
		setRegistryName(BeeMod.MODID, "glowing_bee");
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return LIGHT_BOUNDS;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		return super.getSelectedBoundingBox(state, worldIn, pos);
	}

	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
	}

	public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_)
	{
		return BlockFaceShape.UNDEFINED;
	}

	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.INVISIBLE;
	}
}
