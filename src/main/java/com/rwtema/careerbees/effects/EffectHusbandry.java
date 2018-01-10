package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EffectHusbandry extends EffectBaseThrottled {
	public static final EffectHusbandry INSTANCE = new EffectHusbandry();

	public EffectHusbandry() {
		super("husbandy", 20 * 10 / 10);
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
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
	public boolean handleEntityLiving(EntityLivingBase livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return false;
	}
}
