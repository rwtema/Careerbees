package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public abstract class EffectWorldInteraction extends EffectBaseThrottled {
	public EffectWorldInteraction(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
//		addSetting();
	}

	public EffectWorldInteraction(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	public EffectWorldInteraction(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}



	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, @Nonnull World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);

		for (int i = 0; i < 40; i++)
			for (int y = Math.max(0, MathHelper.floor(aabb.minY)); y <= Math.min(255, MathHelper.ceil(aabb.maxY)); y++) {
				int x = getRand(MathHelper.floor(aabb.minX), MathHelper.ceil(aabb.maxX), random);
				int z = getRand(MathHelper.floor(aabb.minZ), MathHelper.ceil(aabb.maxZ), random);

				BlockPos blockPos = new BlockPos(x, y, z);
				IBlockState state = world.getBlockState(blockPos);
				if (performPosEffect(world, blockPos, state, genome, housing))
					break;
			}
	}

	protected abstract boolean performPosEffect(World world, BlockPos blockPos, IBlockState state, IBeeGenome genome, IBeeHousing housing);


	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		IBlockState blockState = world.getBlockState(pos);
		return performPosEffect(world, pos, blockState, genome, housing);
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

}
