package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EffectExplosion extends EffectBaseThrottled {

	public static final EffectExplosion INSTANCE = new EffectExplosion("exploding", 20, 0.05F);

	public EffectExplosion(String name, int baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {

		tryCreateSplosion(random, world, pos);

	}

	public void tryCreateSplosion(@Nonnull Random random, World world, BlockPos pos) {
		Explosion explosion = new Explosion(world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2 + random.nextFloat() * 2, false, true);
		if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion)) return;
		explosion.doExplosionA();
		List<BlockPos> affectedBlockPositions = explosion.getAffectedBlockPositions();
		if (affectedBlockPositions.isEmpty()) return;
		List<BlockPos> collect = affectedBlockPositions.stream().filter(p -> !world.isAirBlock(p) && world.getTileEntity(p) == null).collect(Collectors.toList());
		if (collect.isEmpty()) return;
		affectedBlockPositions.clear();
		affectedBlockPositions.addAll(collect);

		explosion.doExplosionB(true);

		((WorldServer)world).spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
				pos.getX() + 0.5 ,
				pos.getY() + 0.5 ,
				pos.getZ() + 0.5 ,
				1,
				1.0D, 0.0D, 0.0D, 0);
		for (int i = 0; i < 4; i++) {
			((WorldServer)world).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
					pos.getX() + 0.5 + random.nextGaussian() * 2,
					pos.getY() + 0.5 + random.nextGaussian() * 2,
					pos.getZ() + 0.5 + random.nextGaussian() * 2,
					1,
					1.0D, 0.0D, 0.0D, 0);
		}
	}

	@Override
	public boolean handleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		tryCreateSplosion(world.rand, world, pos);
		return true;
	}
}
