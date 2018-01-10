package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.ParticleHelper;
import forestry.api.apiculture.*;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class EffectSteal<T extends EntityLivingBase> extends EffectBaseThrottled {

	public EffectSteal(String steal, int baseTicksBetweenProcessing) {
		super(steal, true, false, baseTicksBetweenProcessing, 1);
	}


	public static void mergeStackIntoList(List<ItemStack> grabbed_stacks, ItemStack addedStack) {
		for (ItemStack grabbed_stack : grabbed_stacks) {
			if (ItemHandlerHelper.canItemStacksStack(addedStack, grabbed_stack)) {
				grabbed_stack.grow(addedStack.getCount());
				return;
			}
		}
		grabbed_stacks.add(addedStack.copy());
	}


	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB bb = getAABB(genome, housing);
		List<T> entitiesWithinAABB = new ArrayList<>(world.getEntitiesWithinAABB(getEntityClazz(), bb));
		Collections.shuffle(entitiesWithinAABB, random);
		for (T livingBase : entitiesWithinAABB) {
			if (random.nextBoolean()) continue;
			if (!canHandle(livingBase)) continue;

			int count = BeeManager.armorApiaristHelper.wearsItems(livingBase, getUID(), true);
			if (count > 0) continue;


			if (steal(livingBase, housing, this)) {
				return;
			}
		}
	}

	protected abstract boolean steal(T livingBase, IBeeHousing housing, EffectSteal effectSteal);


	protected abstract boolean canHandle(T livingBase);

	protected abstract Class<T> getEntityClazz();


	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {
		IBeekeepingLogic beekeepingLogic = housing.getBeekeepingLogic();
		List<BlockPos> flowerPositions = new ArrayList<>(beekeepingLogic.getFlowerPositions());

		AxisAlignedBB bb = getAABB(genome, housing);


		HashSet<BlockPos> pos = new HashSet<>(flowerPositions);
		for (T entityLivingBase : housing.getWorldObj().getEntitiesWithinAABB(getEntityClazz(), bb)) {
			if (!canHandle(entityLivingBase)) continue;

			int count = BeeManager.armorApiaristHelper.wearsItems(entityLivingBase, getUID(), true);
			if (count > 0) continue;


			BlockPos e = new BlockPos(
					entityLivingBase.posX,
					entityLivingBase.posY + entityLivingBase.getEyeHeight(),
					entityLivingBase.posZ
			);
			if (pos.add(e)) {
				flowerPositions.add(e);
			}
		}

		ParticleHelper.BEE_HIVE_FX.addBeeHiveFX(housing, genome, flowerPositions);
		return storedData;
	}


	@Override
	public boolean handleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return false;
	}

	@Override
	public boolean handleEntityLiving(EntityLivingBase livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		if (getEntityClazz().isInstance(livingBase)) {
			T t = getEntityClazz().cast(livingBase);
			if (canHandle(t)) {
				int count = BeeManager.armorApiaristHelper.wearsItems(livingBase, getUID(), true);
				if (count > 0 || steal(t, housing, this)) return true;
			}
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	public ItemStack handleStack(ItemStack stack, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return null;
	}

}
