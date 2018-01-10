package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.helpers.NameHelper;
import com.rwtema.careerbees.items.DelayedInsertionHelper;
import com.rwtema.careerbees.items.ItemIngredients;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EffectStealMob extends EffectSteal<EntityLiving> {
	public static final EffectSteal MOB = new EffectStealMob();

	 @Nullable
	 Pair<EntityLiving, IBeeHousing> housingInventoryPair = null;
	@Nullable
	final DamageSource bee_player = new DamageSource(BeeMod.MODID + ".damage.police") {
		@Nullable
		@Override
		public Entity getTrueSource() {
			Pair<EntityLiving, IBeeHousing> housingInventoryPair = EffectStealMob.this.housingInventoryPair;
			if (housingInventoryPair != null) {
				World worldObj = housingInventoryPair.getRight().getWorldObj();
				if (worldObj instanceof WorldServer) {
					return FakePlayerFactory.getMinecraft((WorldServer) worldObj);
				}
			}
			return null;
		}
	};
	final DamageSource bee = new DamageSource(BeeMod.MODID + ".damage.police");

	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public EffectStealMob() {
		super("steal.mob", 10);
	}


	@SubscribeEvent
	public void getDrops(@Nonnull LivingDropsEvent event) {
		Pair<EntityLiving, IBeeHousing> housingInventoryPair = this.housingInventoryPair;
		if (housingInventoryPair == null || event.getEntityLiving() != housingInventoryPair.getLeft())
			return;
		EntityLiving entityLiving = (EntityLiving) event.getEntityLiving();

		List<ItemStack> grabbed_stacks = new ArrayList<>();

		IBeeHousing housing = housingInventoryPair.getRight();
		IBeeHousingInventory housingInv = housing.getBeeInventory();

		event.getDrops().removeIf(
				item -> addStacks(grabbed_stacks, housing, item)
		);

		addAdditionalProducts(entityLiving, grabbed_stacks, housing, housingInv);
	}

	protected void addAdditionalProducts(@Nonnull EntityLiving entityLiving, @Nonnull List<ItemStack> grabbed_stacks, IBeeHousing housing, @Nonnull IBeeHousingInventory housingInv) {
		NBTTagCompound tag = new NBTTagCompound();

		if (entityLiving.hasCustomName()) {
			tag.setString("custom_name", entityLiving.getCustomNameTag());
		} else {
			String s = EntityList.getEntityString(entityLiving);
			if (s == null) s = "generic";
			tag.setString("name", "entity." + s + ".name");
		}

		NBTTagList list = new NBTTagList();
		for (ItemStack grabbed_stack : grabbed_stacks) {
			NBTTagCompound nbt = grabbed_stack.writeToNBT(new NBTTagCompound());
			nbt.setInteger("Count", grabbed_stack.getCount());
			list.appendTag(nbt);
		}

		tag.setTag("drops", list);

		tag.setLong("world_time", entityLiving.world.getTotalWorldTime());
		tag.setInteger("world_dim", entityLiving.world.provider.getDimension());

		tag.setShort("officer_name", NameHelper.getQueenNameSeed(housing));

		ItemStack product = ItemIngredients.IngredientType.REPORT.get();
		product.setTagCompound(tag);
		housingInv.addProduct(product, true);
	}

	private boolean addStacks(@Nonnull List<ItemStack> grabbed_stacks, @Nonnull IBeeHousing housing, @Nonnull EntityItem item) {
		ItemStack stack = item.getItem().copy();
		ItemStack newItemStack = DelayedInsertionHelper.addEntityStack(stack, housing, item);
		if (newItemStack == stack || newItemStack.getCount() == stack.getCount()) {
			return false;
		} else if (newItemStack.isEmpty()) {
			item.setDead();
			item.setItem(ItemStack.EMPTY);
			mergeStackIntoList(grabbed_stacks, stack);
			return true;
		} else {
			item.setItem(newItemStack);

			ItemStack addedStack = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - newItemStack.getCount());
			mergeStackIntoList(grabbed_stacks, addedStack);
			return false;
		}
	}

	@Override
	public boolean canHandle(EntityLiving livingBase) {
		return livingBase instanceof IMob;
	}

	@Nonnull
	@Override
	protected Class<EntityLiving> getEntityClazz() {
		return EntityLiving.class;
	}

	@Override
	public boolean steal(@Nonnull EntityLiving livingBase, IBeeHousing housing, EffectSteal effect) {
		if (livingBase.attackEntityFrom(bee, Math.min(2, livingBase.getHealth() - 0.005F))) {
			if (livingBase.getHealth() < 0.01F) {
				try {
					housingInventoryPair = Pair.of(livingBase, housing);
					if (!livingBase.attackEntityFrom(bee_player, 20)) {
						livingBase.attackEntityFrom(bee_player, 40);
					}
				} finally {
					housingInventoryPair = null;
				}
			}
			return true;
		}
		return false;
	}
}
