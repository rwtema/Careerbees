package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.RandomHelper;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.WeakHashMap;
import java.util.stream.Stream;

public class EffectPower extends EffectBase {
	public static EffectPower INSTANCE = new EffectPower("rf");

	public EffectPower(String rawname) {
		super(rawname);
	}

	public EffectPower(String rawname, boolean isDominant, boolean isCombinable) {
		super(rawname, isDominant, isCombinable);
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		float speed = getSpeed(genome, housing);

		int rfrate = MathHelper.ceil(400 * speed * speed);

		TileEntity entity = housing.getWorldObj().getTileEntity(housing.getCoordinates());
		if (!(entity instanceof IBeeHousing)) {

			return storedData;
		}
		IBeeHousing beeHousingTile = (IBeeHousing) entity;
		if (beeHousingTile.getBeeInventory() != housing.getBeeInventory()) {
			return storedData;
		}

		int energyleft = rfrate;

		for (BlockPos pos : getAdjacentTiles(housing)) {
			World world = housing.getWorldObj();
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity != null) {
				IEnergyStorage energyStorage = tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
				if (energyStorage != null) {
					energyleft -= energyStorage.receiveEnergy(energyleft, false);
					if(energyleft <= 0 )break;
				}
			}
		}
		return storedData;
	}

}
