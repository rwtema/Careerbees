package com.rwtema.careerbees.effects;

// import com.rwtema.careerbees.BeeMod; // logger
import forestry.api.apiculture.*;
import forestry.apiculture.PluginApiculture;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectAssassin extends EffectWorldInteraction {
	public static final EffectAssassin INSTANCE = new EffectAssassin("assassin", 10);

	public EffectAssassin(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectAssassin(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	public EffectAssassin(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nullable EnumFacing sideHit) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if ( tileEntity == null )
			return false;
		if (tileEntity instanceof IBeeHousing) {
			ItemStack queen = ((IBeeHousing) tileEntity).getBeeInventory().getQueen();
			if (queen.isEmpty() || BeeManager.beeRoot.getType(queen) != EnumBeeType.QUEEN) return false;
			IBee member = BeeManager.beeRoot.getMember(queen);
			if (member == null) return false;
			IBeeGenome memberGenome = member.getGenome();
			return memberGenome.getPrimary() != genome.getPrimary();
		}
		return false;
	}

	@Override
	protected boolean performPosEffect(World world, BlockPos blockPos, IBlockState state, IBeeGenome genome, IBeeHousing housing) {
		// Fix bugs #22 #25 (do not work on source hive) and use the bee gun's canHandleBlock guard to fix bugs #23 #37
		if (housing.getCoordinates() != blockPos &&
			canHandleBlock(world, blockPos, genome, null)) {

			IBeeHousing house = (IBeeHousing) world.getTileEntity(blockPos);
			// Guarded by canHandleBlock, this should always succeed.

			ItemStack queenStack = house.getBeeInventory().getQueen();
			IBee queen = BeeManager.beeRoot.getMember(queenStack);
			if (queen == null || queenStack.isEmpty() || !queenStack.hasTagCompound())
				return false;

			queen.setHealth(0);
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			queen.writeToNBT(nbttagcompound);
			//BeeMod.logger.info("Assassin Bee profiled: " + queen + " as: " + nbttagcompound);
			queenStack.setTagCompound(nbttagcompound);
			house.getBeeInventory().setQueen(queenStack);
			house.getBeekeepingLogic().canWork();
			//BeeMod.logger.info("Assassin Bee reports back from: " + blockPos);
			return true;
		}
		return false;
	}
}
