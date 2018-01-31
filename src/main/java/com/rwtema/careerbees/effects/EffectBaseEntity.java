package com.rwtema.careerbees.effects;

import com.google.common.collect.Lists;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public abstract class EffectBaseEntity<T extends Entity> extends EffectBaseThrottled implements ISpecialBeeEffect.SpecialEffectEntity {

	private final Predicate<T> entityPredicate;
	private final Class<T> entityClazz;

	public EffectBaseEntity(String name, float baseTicksBetweenProcessing, Predicate<T> entityAnimalPredicate, Class<T> entityClazz) {
		this(name, baseTicksBetweenProcessing, 1, entityAnimalPredicate, entityClazz);
	}

	public EffectBaseEntity(String name, float baseTicksBetweenProcessing, Class<T> entityClazz) {
		this(name, baseTicksBetweenProcessing, 1, entityClazz);
	}

	public EffectBaseEntity(String name, float baseTicksBetweenProcessing, float chanceOfProcessing, Class<T> entityClazz) {
		this(name, baseTicksBetweenProcessing, chanceOfProcessing, EntitySelectors.IS_ALIVE::test, entityClazz);
	}

	public EffectBaseEntity(String name, float baseTicksBetweenProcessing, float chanceOfProcessing, Predicate<T> entityAnimalPredicate, Class<T> entityClazz) {
		this(name, false, false, baseTicksBetweenProcessing, chanceOfProcessing, entityAnimalPredicate, entityClazz);
	}

	public EffectBaseEntity(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing, Predicate<T> entityAnimalPredicate, Class<T> entityClazz) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
		this.entityPredicate = entityAnimalPredicate;
		this.entityClazz = entityClazz;
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, @Nonnull World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);
		List<T> entities = world.getEntitiesWithinAABB(entityClazz, aabb, entityPredicate::test);
		if (!entities.isEmpty())
			workOnEntities(entities, genome, housing, random, settings);
	}

	protected abstract void workOnEntities(List<T> entities, IBeeGenome genome, IBeeHousing housing, Random random, IEffectSettingsHolder settings);

	@Override
	public boolean canHandleEntity(Entity livingBase, @Nonnull IBeeGenome genome) {
		return entityClazz.isInstance(livingBase) && entityPredicate.test(entityClazz.cast(livingBase)) ;
	}

	@Override
	public boolean handleEntityLiving(Entity livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing){
		if( entityClazz.isInstance(livingBase)){
			T cast = entityClazz.cast(livingBase);
			if (entityPredicate.test(cast)) {
				ArrayList<T> list = Lists.newArrayList(cast);
				workOnEntities(list, genome, housing, housing.getWorldObj().rand, getSettings(housing) );
			}
			return true;
		}
		return false;
	}
}
