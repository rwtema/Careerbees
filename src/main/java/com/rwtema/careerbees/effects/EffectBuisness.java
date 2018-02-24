package com.rwtema.careerbees.effects;

import com.google.common.collect.Lists;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class EffectBuisness extends EffectBaseThrottled {
	public static final EffectBuisness JUNK = new EffectBuisness("junk_sale", 100) {

		@Override
		public int numTrades(Random random) {
			return 2 + random.nextInt(10);
		}

		@Nonnull
		@Override
		public ItemStack getStack(@Nonnull Random random, ItemStack itemToSell) {
			itemToSell = itemToSell.copy();
//			NBTTagCompound nbt = itemToSell.getTagCompound();
//			if (nbt != null) {
//				if (!nbt.hasKey("ench", 9)) {
//					return ItemStack.EMPTY;
//				}
//				nbt.removeTag("ench");
//				if (nbt.hasNoTags()) {
//					itemToSell.setTagCompound(null);
//				} else {
//					return ItemStack.EMPTY;
//				}
//			}

			if (itemToSell.isItemStackDamageable()) {
				int maxDamage = itemToSell.getMaxDamage();
				if (maxDamage > 0) {
					int newDamage = maxDamage - Math.max(1, random.nextInt(maxDamage / 2));
					itemToSell.setItemDamage(newDamage);
				}
			}

			return itemToSell;
		}

		@Override
		public boolean isValidItemToSell(ItemStack itemToSell1) {
			NBTTagCompound tagCompound = itemToSell1.getTagCompound();
			return tagCompound == null || (tagCompound.getKeySet().size() == 1 && tagCompound.hasKey("ench", 9));
		}

		@Override
		public boolean isValidItemToBuy(ItemStack buy1, @Nullable ItemStack buy2) {
			return buy1.getItem() == Items.EMERALD && buy2 == null;
		}
	};
	public static final EffectBuisness INSTANCE = new EffectBuisness("buisness", 100) {
		@Nonnull
		@Override
		public ItemStack getStack(@Nonnull Random random, ItemStack itemToSell) {
			int skim = itemToSell.getCount() / 2;
			if (skim > 0) {
				itemToSell = itemToSell.copy();
				itemToSell.setCount(itemToSell.getCount() - skim + random.nextInt(skim));
			}
			return itemToSell;
		}

		@Override
		public boolean isValidItemToSell(ItemStack itemToSell1) {
			return itemToSell1.getItem() == Items.EMERALD;
		}

		@Override
		public boolean isValidItemToBuy(ItemStack buy1, @Nullable ItemStack buy2) {
			return true;
		}
	};

	private ArrayList<EntityVillager.ITradeList> trades;

	public EffectBuisness(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public ArrayList<EntityVillager.ITradeList> getTrades() {
		ArrayList<EntityVillager.ITradeList> trades = this.trades;
		if (trades == null) {
			trades = new ArrayList<>();
			for (VillagerRegistry.VillagerProfession profession : GameRegistry.findRegistry(VillagerRegistry.VillagerProfession.class).getValues()) {
				HashSet<VillagerRegistry.VillagerCareer> careers = new HashSet<>();
				for (int i = 0; i < 100; i++) {
					if (!careers.add(profession.getCareer(i))) break;
				}

				for (VillagerRegistry.VillagerCareer career : careers) {
					for (int i = 0; i < 20; i++) {
						List<EntityVillager.ITradeList> trade = career.getTrades(i);
						if (trade != null) {
							trades.addAll(trade);
						}
					}

				}
			}
			this.trades = trades;
		}
		return trades;
	}

	public int numTrades(Random random) {
		return 1;
	}


	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {

		int numTrades = numTrades(random);
		if (numTrades == 0) return;
		HashMap<ItemStackTrackerEntry, InvInventory> stacks = new HashMap<>();

		for (IItemHandler handler : getAdjacentCapabilities(housing, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
			for (int i = 0; i < handler.getSlots(); i++) {
				ItemStack itemStack = handler.extractItem(i, 64, true);
				if (!itemStack.isEmpty()) {
					stacks
							.computeIfAbsent(new ItemStackTrackerEntry(itemStack), t -> new InvInventory())
							.track(handler, i, itemStack.getCount());
				}
			}
		}

		if (stacks.isEmpty()) return;

		IMerchant merchant = new MyIMerchant(housing);

		ArrayList<EntityVillager.ITradeList> shuffledTradeLists = Lists.newArrayList(getTrades());
		Collections.shuffle(shuffledTradeLists);

		for (EntityVillager.ITradeList trade : shuffledTradeLists) {
			MerchantRecipeList list = new MerchantRecipeList();
			trade.addMerchantRecipe(merchant, list, random);
			for (MerchantRecipe recipe : list) {
				if (recipe.isRecipeDisabled()) {
					continue;
				}

				ItemStack itemToSell1 = recipe.getItemToSell();
				if (!isValidItemToSell(itemToSell1)) {
					continue;
				}

				ItemStack itemToBuy = recipe.getItemToBuy();
				InvInventory invInventory = stacks.get(new ItemStackTrackerEntry(itemToBuy));
				if (invInventory == null || invInventory.num < itemToBuy.getCount()) continue;

				boolean hasSecondItemToBuy = recipe.hasSecondItemToBuy();
				InvInventory secondInvInventory;
				ItemStack secondItemToBuy;
				if (hasSecondItemToBuy) {
					secondItemToBuy = recipe.getSecondItemToBuy();
					if (ItemHandlerHelper.canItemStacksStack(itemToBuy, secondItemToBuy)) {
						itemToBuy.grow(secondItemToBuy.getCount());
						if (invInventory.num < itemToBuy.getCount()) continue;

						hasSecondItemToBuy = false;
						secondItemToBuy = null;
						secondInvInventory = null;
					} else {
						secondInvInventory = stacks.get(new ItemStackTrackerEntry(secondItemToBuy));
						if (secondInvInventory == null || secondInvInventory.num < itemToBuy.getCount()) continue;
					}
				} else {
					secondItemToBuy = null;
					secondInvInventory = null;
				}

				if (!isValidItemToBuy(itemToBuy, secondItemToBuy)) continue;

				ItemStack itemToSell = itemToSell1;
				itemToSell = getStack(random, itemToSell);
				if (itemToSell.isEmpty()) continue;

				if (invInventory.extract(itemToBuy, false) && (!hasSecondItemToBuy || secondInvInventory.extract(secondItemToBuy, false))) {
					if(housing.getBeeInventory().addProduct(itemToSell, true)) {
						invInventory.extract(itemToBuy, true);
						if (hasSecondItemToBuy) secondInvInventory.extract(secondItemToBuy, true);
					}

				}

				numTrades--;

				if (numTrades <= 0)
					return;
			}
		}
	}

	@Nonnull
	public abstract ItemStack getStack(@Nonnull Random random, ItemStack itemToSell);

	public abstract boolean isValidItemToSell(ItemStack itemToSell1);

	public abstract boolean isValidItemToBuy(ItemStack buy1, @Nullable ItemStack buy2);

	private static class InvInventory {
		int num;
		HashMap<IItemHandler, TIntHashSet> slots = new HashMap<>();

		public void track(IItemHandler inv, int slot, int amount) {
			num += amount;
			slots.computeIfAbsent(inv, t -> new TIntHashSet()).add(slot);
		}

		public boolean extract(@Nonnull ItemStack target, boolean doExtract) {
			int n = target.getCount();
			for (Map.Entry<IItemHandler, TIntHashSet> entry : slots.entrySet()) {
				TIntIterator iterator = entry.getValue().iterator();
				while (iterator.hasNext()) {
					int slot = iterator.next();
					ItemStack item = entry.getKey().extractItem(slot, n, !doExtract);
					if (ItemHandlerHelper.canItemStacksStack(item, target)) {
						n -= item.getCount();
						if (n <= 0) return true;
					}
				}
			}
			return n <= 0;
		}
	}

	public static class ItemStackTrackerEntry {
		public final ItemStack stack;

		public ItemStackTrackerEntry(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this || (obj instanceof ItemStackTrackerEntry && ItemHandlerHelper.canItemStacksStack(stack, ((ItemStackTrackerEntry) obj).stack));
		}

		@Override
		public int hashCode() {
			int hash = System.identityHashCode(stack.getItem());
			hash = hash * 31 + stack.getMetadata();
			hash = hash * 31 + (stack.hasTagCompound() ? 1 : 0);
			return hash;
		}
	}

	public static class MyIMerchant implements IMerchant {
		private final IBeeHousing housing;

		public MyIMerchant(IBeeHousing housing) {
			this.housing = housing;
		}

		@Nullable
		@Override
		public EntityPlayer getCustomer() {
			return null;
		}

		@Override
		public void setCustomer(@Nullable EntityPlayer player) {

		}

		@Nullable
		@Override
		public MerchantRecipeList getRecipes(@Nonnull EntityPlayer player) {
			return null;
		}

		@Override
		public void setRecipes(@Nullable MerchantRecipeList recipeList) {

		}

		@Override
		public void useRecipe(@Nonnull MerchantRecipe recipe) {

		}

		@Override
		public void verifySellingItem(@Nonnull ItemStack stack) {

		}

		@Nonnull
		@Override
		public ITextComponent getDisplayName() {
			return housing.getBeeInventory().getQueen().getTextComponent();
		}

		@Nonnull
		@Override
		public World getWorld() {
			return housing.getWorldObj();
		}

		@Nonnull
		@Override
		public BlockPos getPos() {
			return housing.getCoordinates();
		}
	}
}
