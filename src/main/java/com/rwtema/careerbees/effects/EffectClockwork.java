package com.rwtema.careerbees.effects;

import com.google.common.collect.Lists;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.ParticleHelper;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.genetics.IEffectData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EffectClockwork extends EffectBase implements ISpecialBeeEffect.SpecialEffectBlock {
	public static final EffectBase INSTANCE = new EffectClockwork("clockwinding");

	@Nullable
	public Class<? extends TileEntity> tile;

	public EffectClockwork(String rawname) {
		super(rawname);
		try {
			//noinspection unchecked
			tile = (Class<? extends TileEntity>) Class.forName("forestry.energy.tiles.TileEngineClockwork");
		} catch (ClassNotFoundException e) {
			tile = null;
		}
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		if (tile == null) return storedData;

		World worldObj = housing.getWorldObj();
		List<? extends TileEntity> tiles = Lists.newArrayList(getTiles(worldObj, tile, getAABB(genome, housing)));

		tiles.sort(Comparator.<TileEntity, BlockPos>comparing(TileEntity::getPos, Comparator.comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getY)));
		int n = 0;
		for (TileEntity tileEntity : tiles) {
			n++;
			if(n == 3) break;
			processTile(genome, worldObj, tileEntity);
		}

		return storedData;
	}

	public void processTile(@Nonnull IBeeGenome genome, @Nonnull World worldObj, @Nonnull TileEntity tileEntity) {
		NBTTagCompound nbtTagCompound = tileEntity.writeToNBT(new NBTTagCompound());
		if (nbtTagCompound.hasKey("Wound", Constants.NBT.TAG_FLOAT)) {
			float min = Math.min(2 + genome.getSpeed() * 4, 8);
			if (nbtTagCompound.getFloat("Wound") < (min / 2)) {
				IBlockState blockState = worldObj.getBlockState(tileEntity.getPos());
				worldObj.notifyBlockUpdate(tileEntity.getPos(), blockState, blockState, 0);
			}
			if (nbtTagCompound.getFloat("Wound") < min) {
				nbtTagCompound.setFloat("Wound", min);
				tileEntity.readFromNBT(nbtTagCompound);
			}
		}
	}


	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {
		IBeekeepingLogic beekeepingLogic = housing.getBeekeepingLogic();
		List<BlockPos> flowerPositions = new ArrayList<>(beekeepingLogic.getFlowerPositions());
		getTiles(housing.getWorldObj(), tile, getAABB(genome, housing)).stream().map(TileEntity::getPos).forEach(flowerPositions::add);
		ParticleHelper.BEE_HIVE_FX.addBeeHiveFX(housing, genome, flowerPositions);
		return storedData;
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome) {
		return tile != null && tile.isInstance(world.getTileEntity(pos));

	}

	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		if(tile == null) return false;

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tile.isInstance(tileEntity)) {
			processTile(genome, world, tileEntity);
			return true;
		}
		return false;
	}



}
