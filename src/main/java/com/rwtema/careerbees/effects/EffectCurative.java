package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class EffectCurative extends EffectBaseEntity<EntityLivingBase> {
	public static final EffectCurative INSTANCE = new EffectCurative("curative", 10, EntityLivingBase.class );
	public EffectCurative(String name, float baseTicksBetweenProcessing, Class<EntityLivingBase> entityClazz) {
		super(name, baseTicksBetweenProcessing, entityClazz);
	}

	@Override
	protected void workOnEntities(@Nonnull List<EntityLivingBase> entities, IBeeGenome genome, IBeeHousing housing, Random random, IEffectSettingsHolder settings) {
		for (EntityLivingBase entity : entities) {
			if(entity.isEntityUndead()){
				entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 80));
			}else {
				entity.heal(0.1F);
				entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
			}
		}
	}
}

