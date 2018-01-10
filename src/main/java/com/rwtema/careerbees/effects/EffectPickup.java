package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.Filter;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.effects.settings.Setting;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class EffectPickup extends EffectBaseThrottled {
	public static final EffectPickup INSTANCE = new EffectPickup("pickup", 20 * 10 / 10);
	final Filter filter = new Filter(this);
	final Setting.YesNo voidExcess = new Setting.YesNo(this, "voidExcess", false);

	public EffectPickup(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, @Nonnull World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, @Nonnull IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);
		Predicate<ItemStack> matcher = filter.getMatcher(settings);
		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, aabb, t -> t != null && !t.isDead && !t.getItem().isEmpty() && matcher.test(t.getItem()));
		Collections.shuffle(list);

		for (EntityItem entityItem : list) {
			ItemStack currentItem = entityItem.getItem();
			ItemStack remainderAfterInsert = tryAdd(currentItem, housing.getBeeInventory());
			if (voidExcess.getValue(settings)) {
				entityItem.setDead();
			} else if (remainderAfterInsert != currentItem) {
				if (remainderAfterInsert.isEmpty()) {
					entityItem.setDead();
				} else {
					entityItem.setItem(remainderAfterInsert);
				}
			}
		}
	}

	@Nullable
	@Override
	public ItemStack handleStack(ItemStack stack, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return null;
	}
}
