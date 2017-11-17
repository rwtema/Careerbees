package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.RandomHelper;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.genetics.IEffectData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class EffectPower extends EffectBase {
	public static EffectPower INSTANCE = new EffectPower("rf");
	WeakHashMap<IBeeHousing, Iterable<BlockPos>> adjacentPosCache = new WeakHashMap<>();

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

		Iterable<BlockPos> blockPositions = adjacentPosCache.computeIfAbsent(beeHousingTile, h -> {
			BlockPos pos = h.getCoordinates();
			World world = h.getWorldObj();
			if (!(world.getTileEntity(pos) instanceof IBeeHousing)) {
				return null;
			}

			Random rand = world.rand;
			IBeeHousingInventory beeInventory = h.getBeeInventory();
			HashSet<BlockPos> checked = new HashSet<>();

			ArrayList<BlockPos> adjToHousing = new ArrayList<>();
			LinkedList<BlockPos> toCheck = new LinkedList<>();

			Arrays.stream(RandomHelper.getPermutation(rand)).map(pos::offset).forEach(toCheck::add);
			BlockPos blockPos;
			while ((blockPos = toCheck.poll()) != null) {
				TileEntity te = world.getTileEntity(blockPos);
				if (te instanceof IBeeHousing && ((IBeeHousing) te).getBeeInventory() == beeInventory) {
					for (EnumFacing facing : RandomHelper.getPermutation(rand)) {
						BlockPos newpos = blockPos.offset(facing);
						if (checked.add(newpos)) {
							toCheck.add(newpos);
						}
					}
				} else {
					adjToHousing.add(blockPos);
				}
			}
			return adjToHousing;
		});

		if (blockPositions == null) {
			blockPositions = Stream.concat(
					Stream.of(housing.getCoordinates()),
					Stream.of(RandomHelper.getPermutation()).map(housing.getCoordinates()::offset))
					::iterator;
		}

		int energyleft = rfrate;

		for (BlockPos pos : blockPositions) {
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
