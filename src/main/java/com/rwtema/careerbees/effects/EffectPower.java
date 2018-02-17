package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.Random;

public class EffectPower extends EffectBase implements ISpecialBeeEffect.SpecialEffectBlock {
	public static final EffectPower INSTANCE = new EffectPower("rf");

	public EffectPower(String rawname) {
		super(rawname);
	}

	public EffectPower(String rawname, boolean isDominant, boolean isCombinable) {
		super(rawname, isDominant, isCombinable);
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		int rfrate = getRFRate(genome, housing);

		TileEntity entity = housing.getWorldObj().getTileEntity(housing.getCoordinates());
		if (!(entity instanceof IBeeHousing)) {

			return storedData;
		}
		IBeeHousing beeHousingTile = (IBeeHousing) entity;
		if (beeHousingTile.getBeeInventory() != housing.getBeeInventory()) {
			return storedData;
		}

		int energyleft = rfrate;

		BlockPos housingPos = housing.getCoordinates();
		int x = housingPos.getX(), y = housingPos.getY(), z = housingPos.getZ();
		for (BlockPos pos : getAdjacentTiles(housing)) {
			World world = housing.getWorldObj();
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity != null) {
				EnumFacing side = EnumFacing.getFacingFromVector(x - pos.getX(), y - pos.getY(), z - pos.getZ());
				IEnergyStorage energyStorage = tileEntity.getCapability(CapabilityEnergy.ENERGY, side);
				if (energyStorage != null) {
					energyleft -= energyStorage.receiveEnergy(energyleft, false);
					if (energyleft <= 0) break;
				}
			}
		}
		return storedData;
	}

	public int getRFRate(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		float speed = getSpeed(genome, housing);

		return MathHelper.ceil(400 * speed * speed);
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome) {
		TileEntity tile = world.getTileEntity(pos);
		return tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, null);
	}

	@Override
	public float getCooldown(IBeeGenome genome, Random random) {
		return Math.min(120, genome.getLifespan()) * 2 * 6;
	}

	@Override
	public void processingTick(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile == null || !tile.hasCapability(CapabilityEnergy.ENERGY, null)) return;
		IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, null);
		if (storage != null) {
			int rfRate = getRFRate(genome, housing);
			storage.receiveEnergy(rfRate, false);
		}
	}

	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		return true;
	}

}
