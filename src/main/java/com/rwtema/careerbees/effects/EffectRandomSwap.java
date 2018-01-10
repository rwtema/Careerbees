package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class EffectRandomSwap extends EffectBase {
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
	public boolean handleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return processPosition(world, world.rand, getAABB(genome, housing), pos);
	}

	@Override
	public boolean handleEntityLiving(EntityLivingBase livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return false;
	}

	@Nullable
	@Override
	public ItemStack handleStack(ItemStack stack, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return null;
	}


	public boolean processPosition(World worldObj, Random rand, AxisAlignedBB aabb, BlockPos a) {
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

	public boolean isNormalCube(IBlockState state) {
		return state.isNormalCube() && state.isBlockNormalCube() && state.isFullCube() && state.isOpaqueCube() && state.getMobilityFlag() == EnumPushReaction.NORMAL && !state.getBlock().hasTileEntity(state);
	}


	@Nonnull
	public BlockPos getRandomBlockPosInAABB(Random rand, AxisAlignedBB aabb) {
		int x = getRand(MathHelper.floor(aabb.minX), MathHelper.ceil(aabb.maxX), rand);
		int y = getRand(MathHelper.floor(aabb.minY), MathHelper.ceil(aabb.maxY), rand);
		int z = getRand(MathHelper.floor(aabb.minZ), MathHelper.ceil(aabb.maxZ), rand);
		return new BlockPos(x, y, z);
	}


}
