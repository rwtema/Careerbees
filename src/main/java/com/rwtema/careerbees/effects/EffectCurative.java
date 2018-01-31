package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.MethodAccessor;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EffectCurative extends EffectBaseEntity<EntityLivingBase> {
	public static final EffectCurative INSTANCE = new EffectCurative("curative", 10, EntityLivingBase.class);
	//	Method startConverting = PrivateHelper.getMethod(EntityZombieVillager.class, new Class<?>[]{UUID.class, Integer.class}, "startConverting", "func_191991_a");
	MethodAccessor.TwoParam<Void, EntityZombieVillager, UUID, Integer> startConverting = new MethodAccessor.TwoParam<>(EntityZombieVillager.class, UUID.class, Integer.class, "startConverting", "func_191991_a");

	public EffectCurative(String name, float baseTicksBetweenProcessing, Class<EntityLivingBase> entityClazz) {
		super(name, baseTicksBetweenProcessing, entityClazz);
	}


	@Override
	public void processingTick(Entity entity, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		if (entity instanceof EntityZombieVillager) {
			EntityZombieVillager zombieVillager = (EntityZombieVillager) entity;
			if (!zombieVillager.isConverting()) {
				int maxHealth = MathHelper.ceil(zombieVillager.getMaxHealth());
				int base = maxHealth / 4;
				startConverting.invoke(zombieVillager, housing.getOwner() != null ? housing.getOwner().getId() : null, base * 20 + zombieVillager.getEntityWorld().rand.nextInt(1 + (maxHealth - base) * 20));
			} else {
				zombieVillager.attackEntityFrom(DamageSource.CACTUS, 1);
			}
		}
	}

	@Override
	public boolean handleEntityLiving(Entity livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		return super.handleEntityLiving(livingBase, genome, housing);
	}

	@Override
	public float getCooldown(Entity livingBase, IBeeGenome genome, Random rand) {
		if (livingBase instanceof EntityZombieVillager) {
			return Integer.MAX_VALUE;
		}
		return getCooldown(genome, rand);
	}

	@Override
	protected void workOnEntities(@Nonnull List<EntityLivingBase> entities, IBeeGenome genome, IBeeHousing housing, Random random, IEffectSettingsHolder settings) {
		for (EntityLivingBase entity : entities) {
			if (entity instanceof EntityZombieVillager) {
				EntityZombieVillager zombieVillager = (EntityZombieVillager) entity;
				if (!zombieVillager.isConverting()) {
					startConverting.invoke(zombieVillager, housing.getOwner() != null ? housing.getOwner().getId() : null, entity.world.rand.nextInt(1201) + 800);
				}
			} else if (entity.isEntityUndead()) {
				entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 80));
			} else {
				entity.heal(0.1F);
				entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
			}
		}
	}
}

