package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class EffectRandomSwap extends EffectBase implements ISpecialBeeEffect.SpecialEffectBlock {
	public static final EffectRandomSwap INSTANCE = new EffectRandomSwap();

	public EffectRandomSwap() {
		super("teleposition");
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		World worldObj = housing.getWorldObj();
		Random rand = worldObj.rand;
		if (worldObj.isRemote) {
			return storedData;
		}

//		if (rand.nextInt(10) != 0) return storedData;
		AxisAlignedBB aabb = getAABB(genome, housing);
		BlockPos a = getRandomBlockPosInAABB(rand, aabb);
		processPosition(worldObj, rand, aabb, a);
		return storedData;
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, EnumFacing sideHit) {

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null) {
			return false;
		}
		IBlockState state = world.getBlockState(pos);
		return state.getBlockHardness(world, pos) >= 0 && isNormalCube(state);
	}

	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing facing, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		return processPosition(world, world.rand, getAABB(genome, housing), pos);
	}


	public boolean processPosition(@Nonnull World worldObj, @Nonnull Random rand, @Nonnull AxisAlignedBB aabb, @Nonnull BlockPos a) {
		if (worldObj.isAirBlock(a)) return false;

		TileEntity tileEntity = worldObj.getTileEntity(a);
		if (tileEntity != null) {
			return false;
		}
		IBlockState state = worldObj.getBlockState(a);
		float blockHardness = state.getBlockHardness(worldObj, a);
		if (blockHardness < 0
				|| !isNormalCube(state)
				)
			return false;

		BlockPos b = getRandomBlockPosInAABB(rand, aabb);

		if (worldObj.isAirBlock(b)) return false;
		tileEntity = worldObj.getTileEntity(b);
		if (tileEntity != null) {
			return false;
		}

		IBlockState otherState = worldObj.getBlockState(b);
		if (state == otherState
				|| !isNormalCube(otherState)
				|| state.getMaterial() != otherState.getMaterial()
				|| otherState.getBlockHardness(worldObj, b) != blockHardness)
			return false;


		worldObj.setBlockState(a, otherState, 2);
		worldObj.setBlockState(b, state, 2);

		worldObj.notifyNeighborsOfStateChange(a, otherState.getBlock(), true);
		worldObj.notifyNeighborsOfStateChange(b, state.getBlock(), true);

		return true;
	}

	public boolean isNormalCube(@Nonnull IBlockState state) {
		return state.isNormalCube() && state.isBlockNormalCube() && state.isFullCube() && state.isOpaqueCube() && state.getMobilityFlag() == EnumPushReaction.NORMAL && !state.getBlock().hasTileEntity(state);
	}


	@Nonnull
	public BlockPos getRandomBlockPosInAABB(@Nonnull Random rand, @Nonnull AxisAlignedBB aabb) {
		int x = getRand(MathHelper.floor(aabb.minX), MathHelper.ceil(aabb.maxX), rand);
		int y = getRand(MathHelper.floor(aabb.minY), MathHelper.ceil(aabb.maxY), rand);
		int z = getRand(MathHelper.floor(aabb.minZ), MathHelper.ceil(aabb.maxZ), rand);
		return new BlockPos(x, y, z);
	}


}
