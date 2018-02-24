package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EffectPriest extends EffectItemModification {
	public static final EffectPriest INSTANCE = new EffectPriest("priest", 100);

	public EffectPriest(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Nonnull
	@Override
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {
		World world = housing.getWorldObj();
		BlockPos pos = housing.getCoordinates();
		List<TileFlowerPedastal> frameList = getPlantFrames(genome, housing, world, pos, IEffectSettingsHolder.DEFAULT_INSTANCE);
		Random rand = world.rand;

		int num = 1;
		for (int i = -2; i <= 2; ++i) {
			for (int j = -2; j <= 2; ++j) {
				if (i > -2 && i < 2 && j == -1) {
					j = 2;
				}

				if (rand.nextInt(16) == 0) {
					for (int k = 0; k <= 1; ++k) {
						BlockPos blockpos = pos.add(i, k, j);

						if (ForgeHooks.getEnchantPower(world, blockpos) > 0) {
							if (!world.isAirBlock(pos.add(i / 2, 0, j / 2))) {
								break;
							}

							num++;
							world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, (double) pos.getX() + 0.5D, (double) pos.getY() + 2.0D, (double) pos.getZ() + 0.5D, (double) ((float) i + rand.nextFloat()) - 0.5D, (double) ((float) k - rand.nextFloat() - 1.0F), (double) ((float) j + rand.nextFloat()) - 0.5D);
						}
					}
				}
			}
		}

		for (TileFlowerPedastal tileFlowerPedastal : frameList) {
			BlockPos p = tileFlowerPedastal.getPos();
			for (int i = 0; i < num; i++)
				world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
						p.getX() + 0.5D,
						p.getY() + 0.5D,
						p.getZ() + 0.5D,
						pos.getX() - p.getX() + rand.nextFloat() - 0.5D,
						pos.getY() - p.getY() + rand.nextFloat() - 0.5D,
						pos.getZ() - p.getZ() + rand.nextFloat() - 0.5D);
		}


		IEffectData iEffectData = super.doFX(genome, storedData, housing);
		return iEffectData;
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, ItemStack stack, @Nullable IBeeHousing housing) {
		float power = 0;
		Random rand;
		if (housing != null) {
			World world = housing.getWorldObj();
			BlockPos position = housing.getCoordinates();
			rand = world.rand;

			for (int j = -1; j <= 1; ++j) {
				for (int k = -1; k <= 1; ++k) {
					if ((j != 0 || k != 0) && world.isAirBlock(position.add(k, 0, j)) && world.isAirBlock(position.add(k, 1, j))) {
						power += ForgeHooks.getEnchantPower(world, position.add(k * 2, 0, j * 2));
						power += ForgeHooks.getEnchantPower(world, position.add(k * 2, 1, j * 2));
						if (k != 0 && j != 0) {
							power += ForgeHooks.getEnchantPower(world, position.add(k * 2, 0, j));
							power += ForgeHooks.getEnchantPower(world, position.add(k * 2, 1, j));
							power += ForgeHooks.getEnchantPower(world, position.add(k, 0, j * 2));
							power += ForgeHooks.getEnchantPower(world, position.add(k, 1, j * 2));
						}
					}
				}
			}
		} else {
			rand = new Random();
		}
//		int maxDamage = stack.getMaxDamage();
//		int i = maxDamage / 10;
//		if(i < 0) i = 1;
//		int newDamage = stack.getItemDamage() + i;
//		newDamage = Math.max(stack.getItemDamage(), Math.min(newDamage, maxDamage - 1));
//		stack.setItemDamage(newDamage);

		int n = EnchantmentHelper.calcItemStackEnchantability(rand, rand.nextInt(3), (int) power, stack);

		return EnchantmentHelper.addRandomEnchantment(rand, stack, n, true);
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		return stack.isItemStackDamageable() && (stack.isItemEnchantable() && !stack.isItemEnchanted());
	}
}
