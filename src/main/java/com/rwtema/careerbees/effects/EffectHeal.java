package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import java.util.List;
import java.util.Random;

public class EffectHeal extends EffectBaseEntity<EntityLivingBase> {
	public static final EffectHeal INSTANCE = new EffectHeal("healing", 10, EntityLivingBase.class );
	public EffectHeal(String name, float baseTicksBetweenProcessing, Class<EntityLivingBase> entityClazz) {
		super(name, baseTicksBetweenProcessing, entityClazz);
	}

	@Override
	protected void workOnEntities(List<EntityLivingBase> entities, IBeeGenome genome, IBeeHousing housing, Random random, IEffectSettingsHolder settings) {
		for (EntityLivingBase entity : entities) {
			entity.heal(0.5F);
			entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 80));
		}
	}
}
