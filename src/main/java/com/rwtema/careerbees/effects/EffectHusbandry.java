package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EffectHusbandry extends EffectBaseThrottled implements ISpecialBeeEffect.SpecialEffectEntity {
	public static final EffectHusbandry INSTANCE = new EffectHusbandry();

	public EffectHusbandry() {
		super("husbandy", 20 * 10 / 10);
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, @Nonnull World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);

		List<EntityAnimal> animals = world.getEntitiesWithinAABB(
				EntityAnimal.class,
				aabb,
				animal -> animal != null && animal.isEntityAlive() && !animal.isInLove() && animal.getGrowingAge() == 0 && !animal.isChild()
		);
		if (animals.isEmpty() || animals.size() > 16) return;

		Collections.shuffle(animals);

		for (EntityAnimal animal : animals) {
			if (!animal.isInLove() && animal.getGrowingAge() == 0 && !animal.isChild()) {
				animal.setInLove(null);
				return;
			}
		}
	}


	@Override
	public boolean canHandleEntity(Entity livingBase, @Nonnull IBeeGenome genome) {
		if (livingBase instanceof EntityAnimal) {
			EntityAnimal animal = (EntityAnimal) livingBase;
			return !animal.isInLove() && animal.getGrowingAge() == 0 && !animal.isChild();
		}
		return false;
	}

	@Override
	public boolean handleEntityLiving(Entity livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		if (livingBase instanceof EntityAnimal) {
			EntityAnimal animal = (EntityAnimal) livingBase;
			if (!animal.isInLove() && animal.getGrowingAge() == 0 && !animal.isChild()) {
				animal.setInLove(null);
				return true;
			}
		}
		return false;
	}
}
