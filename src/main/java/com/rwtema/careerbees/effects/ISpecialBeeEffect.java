package com.rwtema.careerbees.effects;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public interface ISpecialBeeEffect {
	float getCooldown(IBeeGenome genome, Random random);

	default boolean canTravelThroughWalls(){
		return false;
	}

	interface SpecialEffectBlock extends ISpecialBeeEffect {
		boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome);

		boolean handleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing);

		default boolean includeAirBlocks(){
			return false;
		}
	}
	interface SpecialEffectEntity extends ISpecialBeeEffect {
		boolean canHandleEntity(Entity livingBase, @Nonnull IBeeGenome genome);

		boolean handleEntityLiving(Entity livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing);
	}

	interface SpecialEffectItem  extends ISpecialBeeEffect {
		boolean canHandleStack(ItemStack stack, @Nonnull IBeeGenome genome);

		@Nullable
		ItemStack handleStack(ItemStack stack, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing);
	}
}
