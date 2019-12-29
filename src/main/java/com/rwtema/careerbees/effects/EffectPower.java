package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.BeeMod; // get BeeMod.logger to record erroring
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
import java.util.ArrayList;

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
		int rfRate = getRFRate(genome, housing);

		TileEntity entity = housing.getWorldObj().getTileEntity(housing.getCoordinates());
		if (!(entity instanceof IBeeHousing)) {

			return storedData;
		}
		IBeeHousing beeHousingTile = (IBeeHousing) entity;
		if (beeHousingTile.getBeeInventory() != housing.getBeeInventory()) {
			return storedData;
		}

		int energyleft = rfRate;

		World world = housing.getWorldObj();
		for (BlockPos pos : getAdjacentTiles(housing)) {
			ArrayList faces = getEnergyStorageFaces(world, pos, true);
			int energysent = storeRFTEFaces(world, pos, faces, rfRate);
			energyleft -= energysent;
			if ( 0 == energyleft )
				break;
		}
		return storedData;
	}

	public int getRFRate(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		float speed = getSpeed(genome, housing);

		return MathHelper.ceil(400 * speed * speed);
	}

	private ArrayList<EnumFacing> getEnergyStorageFaces(World world, BlockPos pos, boolean scanAll) {
		ArrayList<EnumFacing> faces = new ArrayList<EnumFacing>();
		TileEntity te = world.getTileEntity(pos);
		if ( null == te )
			return faces;
		if ( te.hasCapability(CapabilityEnergy.ENERGY, null) &&
			 te.getCapability(CapabilityEnergy.ENERGY, null).canReceive() ) {
			faces.add(null);
			if ( ! scanAll )
				return faces;
		}
		for (EnumFacing face : EnumFacing.VALUES) {
			if ( te.hasCapability(CapabilityEnergy.ENERGY, face) &&
				 te.getCapability(CapabilityEnergy.ENERGY, face).canReceive() )
				faces.add(face);
				if ( ! scanAll )
					return faces;
		}
		return faces;
	}

	private int storeRFTEFaces(World world, BlockPos pos, ArrayList<EnumFacing> faces, int maxRF) {
		int energyLeft = maxRF;
		TileEntity te = world.getTileEntity(pos);
		if ( null == te )
			return 0; // no energy sent
		for (EnumFacing face : faces) {
			IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, face);
			if (storage != null) {
				// max rate, simulate (is false; actually do it)
				int energyStored = storage.receiveEnergy(energyLeft, false);
				if ( energyStored > energyLeft ) {
					BeeMod.logger.trace("The block at " + pos + " on face " + face +
									" consumed (" + energyStored +
									") more energy than was sent (" + energyLeft +
									"); this should never happen.  " +
									"Please report it to the author of the TileEntity: " + te);
					energyLeft = 0;
				} else {
					energyLeft -= energyStored;
				}
			}
			if ( 0 == energyLeft )
				break;
		}
		return maxRF - energyLeft; // return used/sent
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, EnumFacing sideHit) {
		return ! getEnergyStorageFaces(world, pos, false).isEmpty();
	}

	@Override
	public float getCooldown(IBeeGenome genome, Random random) {
		return Math.min(120, genome.getLifespan()) * 2 * 6;
	}

	@Override
	public void processingTick(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, EnumFacing facing) {
		ArrayList<EnumFacing> faces = getEnergyStorageFaces(world, pos, true);
		int rfRate = getRFRate(genome, housing);
		storeRFTEFaces(world, pos, faces, rfRate);
	}

	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing facing, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		return true;
	}

}
