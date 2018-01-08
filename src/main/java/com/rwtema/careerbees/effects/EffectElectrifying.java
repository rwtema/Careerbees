package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class EffectElectrifying extends EffectBaseEntity<EntityLivingBase> {
	public static EffectElectrifying INSTANCE = new EffectElectrifying();

	EntityLivingBase currentEntity = null;

	public EffectElectrifying() {
		super("electrifying", 5, 0.1F, EntityLivingBase.class);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void lowerDamage(LivingHurtEvent event) {
		if (event.getEntityLiving() == currentEntity) {
			event.setAmount(event.getAmount() / 20);
		}
	}

	@Override
	protected void workOnEntities(List<EntityLivingBase> entities, IBeeGenome genome, IEffectData storedData, IBeeHousing housing, Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		EntityLightningBolt bolt = new EntityLightningBolt(world, pos.getX() + 0.5F, pos.getY() + 0.5, pos.getZ() + 0.5, true);
		try {
			EntityLivingBase entity = entities.get(random.nextInt(entities.size()));
			{
				if (BeeManager.armorApiaristHelper.wearsItems(entity, "lightning", true) > 0) return;

				currentEntity = entity;

				boolean flag = entity.isBurning();
				if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, bolt)) {
					entity.onStruckByLightning(bolt);
				}
				if (!flag) {
					entity.extinguish();
				}
			}
		} finally {
			currentEntity = null;
		}
	}
}
