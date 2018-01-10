package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.*;
import forestry.api.genetics.IEffectData;
import gnu.trove.list.array.TLongArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.WeakHashMap;

public abstract class EffectBaseThrottled extends EffectBase {
	public final float baseTicksBetweenProcessing;
	public final float chanceOfProcessing;

	@Nullable
	final WeakHashMap<IBeeHousing, Long> lastTickTime = BeeMod.deobf ? new WeakHashMap<>() : null;
	@Nullable
	final TLongArrayList times = BeeMod.deobf ? new TLongArrayList() : null;

	public EffectBaseThrottled(String name, float baseTicksBetweenProcessing) {
		this(name, false, false, baseTicksBetweenProcessing, 1);
	}

	public EffectBaseThrottled(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		this(name, false, false, baseTicksBetweenProcessing, chanceOfProcessing);
	}


	public EffectBaseThrottled(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable);
		this.baseTicksBetweenProcessing = baseTicksBetweenProcessing;
		this.chanceOfProcessing = chanceOfProcessing;
	}

	@Nonnull
	@Override
	public IEffectData validateStorage(@Nullable IEffectData storedData) {
		if (storedData instanceof BaseEffectDataMap.IntMap) return storedData;
		return new BaseEffectDataMap.IntMap();
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		int time = storedData.getInteger(0);
		time++;
		storedData.setInteger(0, time);

		World world = housing.getWorldObj();
		IBeekeepingMode mode = BeeManager.beeRoot.getBeekeepingMode(world);

		IBeeModifier beeHousingModifier = BeeManager.beeRoot.createBeeHousingModifier(housing);
		IBeeModifier beeModeModifier = mode.getBeeModifier();

		// Bee genetic speed * beehousing * beekeeping mode
		float speed = getSpeed(genome, housing);

		if (time * speed < baseTicksBetweenProcessing) {
			return storedData;
		}

		storedData.setInteger(0, 0);

		if (chanceOfProcessing < 1 && world.rand.nextFloat() > chanceOfProcessing) {
			return storedData;
		}

		if (BeeMod.deobf) {
			long totalWorldTime = world.getTotalWorldTime();
			Long prevTime = lastTickTime.put(housing, totalWorldTime);
			if (prevTime != null) {
				times.add(totalWorldTime - prevTime);
				double avgTickTime = (double) times.sum() / (double) times.size();
//				BeeMod.logger.info(baseTicksBetweenProcessing + " " + chanceOfProcessing + " " + avgTickTime);
//				BeeMod.logger.info(avgTickTime / (baseTicksBetweenProcessing / (chanceOfProcessing)));
			}
		}

		performEffect(genome, storedData, housing, world.rand, world, housing.getCoordinates(), beeHousingModifier, beeModeModifier, settings);


		return storedData;
	}


	public abstract void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings);

	public int getCooldown(@Nonnull EntityPlayer playerIn, @Nonnull IBeeGenome genome){
		float speed = genome.getSpeed();
		float base = baseTicksBetweenProcessing / speed;
		float result = base;
		if(chanceOfProcessing < 1){
			while (playerIn.world.rand.nextFloat() > chanceOfProcessing){
				result += base;
			}
		}
		return Math.round(result);
	}
}
