package com.rwtema.careerbees.effects;

import com.google.common.collect.Streams;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EffectRandomSwap extends EffectBase {
	public static final EffectRandomSwap INSTANCE = new EffectRandomSwap();

	public EffectRandomSwap() {
		super("teleposition");
	}
	{
		if(BeeMod.deobf_folder) {
			Field blockHardness;
			try {
				StringBuilder s = new StringBuilder();
				blockHardness = Block.class.getDeclaredField("blockHardness");
				blockHardness.setAccessible(true);
				List<IBlockState> states = Streams.stream(Block.REGISTRY.iterator())
						.filter(block -> "minecraft".equals(block.getRegistryName().getResourceDomain()))
						.flatMap(block -> block.getBlockState().getValidStates().stream())
						.map(state -> state.getBlock().getStateFromMeta(state.getBlock().getMetaFromState(state)))
						.distinct()
						.collect(Collectors.toList());

				for (int i = 0; i < (states.size()-1); i++) {
					IBlockState a = states.get(i);
					if(!isNormalCube(a) || a.getMaterial() == Material.AIR) continue;
					Float a_hard = (Float)blockHardness.get(a.getBlock());
					if(a_hard < 0) continue;
					for (int j = (i+1); j < states.size(); j++) {
						IBlockState b = states.get(j);
						if(!isNormalCube(b) || a.getMaterial() != b.getMaterial()) continue;
						Float b_hard = (Float)blockHardness.get(b.getBlock());
						if(!b_hard.equals(a_hard)) continue;

						s.append(a).append("+").append(b).append("\n");
					}
				}
				BeeMod.logger.info(s.toString());
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw  new RuntimeException(e);
			}


		}
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
		if (worldObj.isAirBlock(a)) return storedData;

		TileEntity tileEntity = worldObj.getTileEntity(a);
		if (tileEntity != null) {
			return storedData;
		}
		IBlockState state = worldObj.getBlockState(a);
		float blockHardness = state.getBlockHardness(worldObj, a);
		if (blockHardness < 0
				|| !isNormalCube(state)
				)
			return storedData;

		BlockPos b = getRandomBlockPosInAABB(rand, aabb);

		if (worldObj.isAirBlock(b)) return storedData;
		tileEntity = worldObj.getTileEntity(b);
		if (tileEntity != null) {
			return storedData;
		}

		IBlockState otherState = worldObj.getBlockState(b);
		if (state == otherState
				|| !isNormalCube(otherState)
				|| state.getMaterial() != otherState.getMaterial()
				|| otherState.getBlockHardness(worldObj, b) != blockHardness)
			return storedData;


		worldObj.setBlockState(a, otherState, 2);
		worldObj.setBlockState(b, state, 2);

		worldObj.notifyNeighborsOfStateChange(a, otherState.getBlock(), true);
		worldObj.notifyNeighborsOfStateChange(b, state.getBlock(), true);

		return storedData;
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
