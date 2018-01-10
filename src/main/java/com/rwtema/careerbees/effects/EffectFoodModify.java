package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public abstract class EffectFoodModify extends EffectItemModification {

	public final static String BEE_FOOD_NBT = "BEE_FOOD_EFFECTS";
	protected static final HashMap<String, EffectFoodModify> callbacks = new HashMap<>();


	static {
		MinecraftForge.EVENT_BUS.register(EffectFoodModify.class);
	}

	public EffectFoodModify(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
		callbacks.put(getName(), this);
	}

	public EffectFoodModify(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
		callbacks.put(getName(), this);
	}

	@SubscribeEvent
	public static void onStartEat(PlayerInteractEvent.RightClickItem event) {
		ItemStack itemStack = event.getItemStack();
		if (!itemStack.isEmpty() && itemStack.hasTagCompound() && itemStack.getItem() instanceof ItemFood) {
			EntityPlayer entityPlayer = event.getEntityPlayer();
			if (entityPlayer.canEat(false) || !entityPlayer.canEat(true)) {
				return;
			}

			NBTTagCompound tagCompound;
			if ((tagCompound = itemStack.getTagCompound()) != null && itemStack.getItem() instanceof ItemFood && tagCompound.hasKey(BEE_FOOD_NBT, Constants.NBT.TAG_COMPOUND)) {
				NBTTagCompound compoundTag = tagCompound.getCompoundTag(BEE_FOOD_NBT);
				for (String s : compoundTag.getKeySet()) {
					EffectFoodModify biConsumer = callbacks.get(s);
					if (biConsumer != null && biConsumer.shouldBeginEattingOverride(entityPlayer, itemStack, compoundTag.getCompoundTag(s))) {
						entityPlayer.setActiveHand(event.getHand());
						event.setCanceled(true);
						event.setCancellationResult(EnumActionResult.SUCCESS);
					}
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack item = event.getItemStack();
		NBTTagCompound tagCompound;
		if ((tagCompound = item.getTagCompound()) != null && item.getItem() instanceof ItemFood && tagCompound.hasKey(BEE_FOOD_NBT, Constants.NBT.TAG_COMPOUND) && event.getEntity() instanceof EntityPlayer) {
			NBTTagCompound compoundTag = tagCompound.getCompoundTag(BEE_FOOD_NBT);
			for (String s : compoundTag.getKeySet()) {
				EffectFoodModify biConsumer = callbacks.get(s);
				if (biConsumer != null) {
					biConsumer.addTooltip(event.getItemStack(), compoundTag.getCompoundTag(s), event.getToolTip());
				}
			}
		}
	}

	@SubscribeEvent
	public static void onFoodEat(LivingEntityUseItemEvent.Finish event) {
		ItemStack item = event.getItem();
		NBTTagCompound tagCompound;
		if ((tagCompound = item.getTagCompound()) != null && tagCompound.hasKey(BEE_FOOD_NBT, Constants.NBT.TAG_COMPOUND) && event.getEntity() instanceof EntityPlayer) {
			if(item.isEmpty()){
				item = item.copy();
				item.setCount(1);
			}

			NBTTagCompound compoundTag = tagCompound.getCompoundTag(BEE_FOOD_NBT);
			for (String s : compoundTag.getKeySet()) {
				EffectFoodModify foodModify = callbacks.get(s);
				if (foodModify != null) {
					foodModify.callback((EntityPlayer) event.getEntity(), compoundTag.getCompoundTag(s), item);
				}
			}
		}
	}

	protected abstract boolean shouldBeginEattingOverride(EntityPlayer entityPlayer, ItemStack itemStack, NBTTagCompound compoundTag);

	@SideOnly(Side.CLIENT)
	protected abstract void addTooltip(ItemStack itemStack, NBTTagCompound compoundTag, List<String> toolTip);

	public abstract void callback(EntityPlayer player, NBTTagCompound tag, ItemStack item);

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, ItemStack stack, IBeeHousing housing) {
		if (stack.getItem() instanceof ItemFood) {
			NBTTagCompound prevTag = stack.getTagCompound() != null && stack.getTagCompound().hasKey(BEE_FOOD_NBT, Constants.NBT.TAG_COMPOUND) && stack.getTagCompound().getCompoundTag(BEE_FOOD_NBT).hasKey(getName(), Constants.NBT.TAG_COMPOUND) ? stack.getTagCompound().getCompoundTag(BEE_FOOD_NBT).getCompoundTag(getName()) : null;

			NBTTagCompound newDataTag = addData(genome, stack, prevTag);
			if (newDataTag != null) {
				ItemStack copy = stack.copy();
				NBTTagCompound copyTagCompound = copy.getTagCompound();
				if (copyTagCompound == null) {
					copy.setTagCompound(copyTagCompound = new NBTTagCompound());
				}
				if (!copyTagCompound.hasKey(BEE_FOOD_NBT, Constants.NBT.TAG_COMPOUND)) {
					copyTagCompound.setTag(BEE_FOOD_NBT, new NBTTagCompound());
				}
				copyTagCompound.getCompoundTag(BEE_FOOD_NBT).setTag(getName(), newDataTag);
				return copy;
			}
		}
		return null;
	}

	@Override
	public boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, ItemStack oldStack, ItemStack newStack, IBeeHousing housing) {
		NBTTagCompound curTag = newStack.getTagCompound().getCompoundTag(BEE_FOOD_NBT).getCompoundTag(getName());
		return shouldRelease(genome, frame, curTag);
	}

	protected abstract boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, NBTTagCompound curTag);

	@Nullable
	protected abstract NBTTagCompound addData(IBeeGenome genome, ItemStack stack, @Nullable NBTTagCompound prevTag);

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		if (!(stack.getItem() instanceof ItemFood)) return false;

		NBTTagCompound tagCompound;
		if ((tagCompound = stack.getTagCompound()) != null && tagCompound.hasKey(BEE_FOOD_NBT, Constants.NBT.TAG_COMPOUND)) {

			NBTTagCompound subTag = tagCompound.getCompoundTag(BEE_FOOD_NBT);
			if (subTag.hasKey(getName(), Constants.NBT.TAG_COMPOUND))
				return acceptItemStack(stack, subTag.getCompoundTag(getName()));

		}

		return acceptItemStack(stack, null);

	}


	public abstract boolean acceptItemStack(ItemStack stack, @Nullable NBTTagCompound curTag);
}
