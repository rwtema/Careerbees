package com.rwtema.careerbees.effects;

import com.google.common.collect.Lists;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nonnull;
import java.util.Random;

public class EffectCreeper extends EffectBaseThrottled {
	public static final EffectBase INSTANCE = new EffectCreeper("creeper", 2, 0.2F);

	public EffectCreeper(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	public EffectCreeper(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);
		if (world.getEntitiesWithinAABB(EntityCreeper.class, aabb).size() > 20) {
			return;
		}

		Vec3d territory = getTerritory(genome, housing);

		double x = (double) pos.getX() + (random.nextDouble() - random.nextDouble()) * territory.x + 0.5D;
		double y = (double) (pos.getY() + random.nextInt(3) - 1);
		double z = (double) pos.getZ() + (random.nextDouble() - random.nextDouble()) * territory.z + 0.5D;

		EntityCreeper entityCreeper = new EntityCreeper(world);
		entityCreeper.setLocationAndAngles(x, y, z, entityCreeper.rotationYaw, entityCreeper.rotationPitch);

		if (!entityCreeper.isNotColliding()) {
			return;
		}

		if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityCreeper, world, (float) x, (float) y, (float) z, null)) {
			entityCreeper.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityCreeper)), null);
		}
		PotionEffect potioneffectIn = new PotionEffect(MobEffects.WITHER, 1000, 0);
		potioneffectIn.setCurativeItems(Lists.newArrayList());
		entityCreeper.addPotionEffect(potioneffectIn);

		NBTTagCompound compound = new NBTTagCompound();
		entityCreeper.writeEntityToNBT(compound);
		compound.setByte("ExplosionRadius", (byte) 5);
		compound.setString("DeathLootTable", "minecraft:empty");
		entityCreeper.readEntityFromNBT(compound);

		world.spawnEntity(entityCreeper);
	}
}
