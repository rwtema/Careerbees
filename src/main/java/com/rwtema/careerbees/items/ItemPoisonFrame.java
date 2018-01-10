package com.rwtema.careerbees.items;

import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPoisonFrame extends ItemBaseFrame {

	public ItemPoisonFrame(float lifeSpanModifier) {
		super(new DefaultBeeModifier() {
			@Override
			public float getLifespanModifier(IBeeGenome genome, @Nullable IBeeGenome mate, float currentModifier) {
				return lifeSpanModifier;
			}

			@Override
			public float getGeneticDecay(IBeeGenome genome, float currentModifier) {
				return 2F;
			}
		}, 20);
	}

	@Override
	@Nonnull
	public ItemStack frameUsed(@Nonnull IBeeHousing housing, @Nonnull ItemStack frame, @Nonnull IBee queen, int wear) {
		World worldObj = housing.getWorldObj();
		BlockPos coordinates = housing.getCoordinates();
		if(worldObj instanceof WorldServer){
			((WorldServer)worldObj).spawnParticle(EnumParticleTypes.SPELL,
					coordinates.getX() + 0.5, coordinates.getY()+ 0.5, coordinates.getZ()+ 0.5,
					20,
					1, 1, 1, 0.5, 120,120,120);
		}
//		worldObj.playEvent(2002, coordinates, MobEffects.POISON.getLiquidColor());
//		worldObj.spawnParticle(EnumParticleTypes.CLOUD, coordinates.getX() + 0.5, coordinates.getY() + 0.5, coordinates.getZ() + 0.5, 0, 0, 0);
		AxisAlignedBB expand = new AxisAlignedBB(coordinates).grow(2, 2, 2);
		for (EntityLivingBase entityLivingBase : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, expand)) {
			entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.POISON, 30 * 20, 1));
		}
		return super.frameUsed(housing, frame, queen, wear);
	}
}
