package com.rwtema.careerbees.items;

import com.rwtema.careerbees.effects.EffectBase;
import forestry.api.apiculture.IBeeHousing;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class DelayedInsertionHelper {
	private static final WeakHashMap<IBeeHousing,
			TObjectIntHashMap<InsertEntry>> map = new WeakHashMap<>();

	static {
		MinecraftForge.EVENT_BUS.register(DelayedInsertionHelper.class);
	}

	public static ItemStack addEntityStack(ItemStack stack, IBeeHousing housing, Entity item) {
		return addStack(stack, housing, new Vec3d(item.posX, item.posY, item.posZ));
	}

	public static ItemStack addStack(ItemStack stack, IBeeHousing housing, Vec3d fallback) {
		if (stack.isEmpty()) return stack;

		ItemStack remainder = EffectBase.tryAdd(stack, housing.getBeeInventory());
		if (remainder.isEmpty()) return ItemStack.EMPTY;
		TileEntity tileEntity = housing.getWorldObj().getTileEntity(housing.getCoordinates());
		if (tileEntity instanceof IBeeHousing && ((IBeeHousing) tileEntity).getBeeInventory() == housing.getBeeInventory()) {
			housing = ((IBeeHousing) tileEntity);
		}

		if (housing instanceof TileEntity) {
			TObjectIntHashMap<InsertEntry> entryMap = map.computeIfAbsent(housing, l -> new TObjectIntHashMap<InsertEntry>());
			InsertEntry insertEntry = new InsertEntry(ItemHandlerHelper.copyStackWithSize(remainder, 1), fallback, housing.getWorldObj().provider.getDimension());
			entryMap.adjustOrPutValue(insertEntry, stack.getCount(), stack.getCount());
		} else {
			return remainder;
		}
		return ItemStack.EMPTY;
	}

	@SubscribeEvent
	public static void onTick(TickEvent.ServerTickEvent event) {
		if (map.isEmpty()) return;

		for (Iterator<Map.Entry<IBeeHousing, TObjectIntHashMap<InsertEntry>>> housingIterator = map.entrySet().iterator(); housingIterator.hasNext(); ) {
			Map.Entry<IBeeHousing, TObjectIntHashMap<InsertEntry>> entry = housingIterator.next();
			IBeeHousing beeHousing = entry.getKey();
			if (!(beeHousing instanceof TileEntity) || ((TileEntity) beeHousing).isInvalid()) {
				housingIterator.remove();
				continue;
			}

			TObjectIntHashMap<InsertEntry> list = entry.getValue();
			for (TObjectIntIterator<InsertEntry> stackIterator = list.iterator(); stackIterator.hasNext(); ) {
				stackIterator.advance();
				InsertEntry key = stackIterator.key();

				ItemStack stack = ItemHandlerHelper.copyStackWithSize(key.stack, stackIterator.value());

				ItemStack remainder = EffectBase.tryAdd(stack, beeHousing.getBeeInventory());

				int count = remainder.getCount();
				if (DimensionManager.getWorld(0).rand.nextInt(256) == 0) {
					count--;
				}

				if (count <= 0) {
					stackIterator.remove();
				} else {
					stackIterator.setValue(count);
				}
			}

			if (list.isEmpty()) {
				housingIterator.remove();
			}
		}
	}

	static class InsertEntry {
		@Nonnull
		final ItemStack stack;
		@Nullable
		final Vec3d fallbackpos;
		final int dim;

		InsertEntry(@Nonnull ItemStack stack, Vec3d fallbackpos, int dim) {
			this.stack = stack;
			this.fallbackpos = fallbackpos;
			this.dim = dim;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof InsertEntry)) return false;

			InsertEntry that = (InsertEntry) o;

			if (dim != that.dim) return false;
			if (!ItemHandlerHelper.canItemStacksStack(stack, that.stack)) return false;
			return fallbackpos != null ? fallbackpos.equals(that.fallbackpos) : that.fallbackpos == null;
		}

		@Override
		public int hashCode() {
			int result = stack.getItem().hashCode();
			result = 31 * result + stack.getMetadata();
			result = 31 * result + (stack.hasTagCompound() ? 1 : 0);
			result = 31 * result + (fallbackpos != null ? fallbackpos.hashCode() : 0);
			result = 31 * result + dim;
			return result;
		}
	}
}
