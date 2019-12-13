package com.rwtema.careerbees.effects;

// import com.rwtema.careerbees.BeeMod; // logger
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

		World world = housing.getWorldObj();
		for (BlockPos pos : getAdjacentTiles(housing)) {
			TEValidatedFaces TEFaces = getValidEnergyStorageFaces(world, pos);
			int energysent = storeRFTEFaces(TEFaces, energyleft);
			//BeeMod.logger.info("EnergyBee sent " + energysent + " RF to " + pos + " " + TEFaces.te);
			energyleft -= energysent;
		}
		return storedData;
	}

	public int getRFRate(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		float speed = getSpeed(genome, housing);

		return MathHelper.ceil(400 * speed * speed);
	}

	// TEValidatedFaces a helper class for some code consolidation.
	public class TEValidatedFaces {
		boolean     valid;
		World       world;
		BlockPos    pos;
		TileEntity  te;
		ArrayList<EnumFacing> faces;
		public TEValidatedFaces(World _world, BlockPos _pos) {
			valid = false;
			world = _world;
			pos = _pos;
			te = null;
			faces = new ArrayList<EnumFacing>();
		}
	}

	// Create a list of valid energy faces for later iteration.
	private TEValidatedFaces getValidEnergyStorageFaces(World world, BlockPos pos) {
		TEValidatedFaces TEFaces = new TEValidatedFaces(world, pos);
		TEFaces.te = world.getTileEntity(pos);
		if ( TEFaces.te == null )
			return TEFaces;
		// Fix #17, since no sides are being used, the actual desire appears to be testing all possible (including null/internal) sides and powering up the block if possible in any way.
		if ( TEFaces.te.hasCapability(CapabilityEnergy.ENERGY, null) &&
			 TEFaces.te.getCapability(CapabilityEnergy.ENERGY, null).canReceive() ) {
			TEFaces.faces.add(null);
			TEFaces.valid = true;
			//BeeMod.logger.info("EnergyBee can interact with " + pos + " " + TEFaces.te + " on the inner side.");
		}
		for (EnumFacing face : EnumFacing.VALUES) {
			if ( TEFaces.te.hasCapability(CapabilityEnergy.ENERGY, face) &&
				 TEFaces.te.getCapability(CapabilityEnergy.ENERGY, face).canReceive() )
				TEFaces.faces.add(face);
				TEFaces.valid = true;
				//BeeMod.logger.info("EnergyBee can interact with " + pos + " " + TEFaces.te + " on the " + face + " side.");
		}
		//BeeMod.logger.info("EnergyBee doesn't know how to handle the tile entity at " + pos + " on any face of the : " + tile);
		return TEFaces;
	}

	private int storeRFTEFaces(@Nonnull TEValidatedFaces TEFaces, int maxRF) {
		int energyleft = maxRF;
		if ( TEFaces.valid ) {
			for (EnumFacing face : TEFaces.faces) {
				IEnergyStorage storage = TEFaces.te.getCapability(CapabilityEnergy.ENERGY, face);
				if (storage != null) {
					// BeeMod.logger.info("EnergyBee powering block at: " + pos + " with " + rfRate);
					energyleft -= storage.receiveEnergy(energyleft, false); // max rate, simulate (is false; actually do it)
				} else {
					// BeeMod.logger.warn("EnergyBee couldn't get storage for the block at: " + pos);
				}
			}
		}
		return maxRF - energyleft; // energy used/sent
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, EnumFacing sideHit) {
		return getValidEnergyStorageFaces(world, pos).valid; // Is this a valid entity, and will this bee do something do it?
	}

	@Override
	public float getCooldown(IBeeGenome genome, Random random) {
		return Math.min(120, genome.getLifespan()) * 2 * 6;
	}

	@Override
	public void processingTick(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, EnumFacing facing) {
		TEValidatedFaces TEFaces = getValidEnergyStorageFaces(world, pos); // Is this a valid entity, and will this bee do something do it?
		int rfRate = getRFRate(genome, housing);
		storeRFTEFaces(TEFaces, rfRate);
	}

	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing facing, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		return true;
	}

}
