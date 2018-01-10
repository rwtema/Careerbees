package com.rwtema.careerbees.items;

import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.handlers.FakeHousing;
import forestry.api.apiculture.*;
import forestry.api.genetics.IAllele;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ItemBeeGun extends Item {
	int MAX_HONEY = 10000;
	private CareerBeeEntry careerBeeEntry = CareerBeeSpecies.COOK;

	public ItemBeeGun() {
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return EnumActionResult.FAIL;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack heldItem = playerIn.getHeldItem(handIn);
		IBee bee = getCurrentSelectedBee(heldItem);
		if (bee == null) {
			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		}


		IBeeGenome genome = bee.getGenome();
		IAlleleBeeEffect effect = genome.getEffect();
		if (!(effect instanceof EffectBase)) {
			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		}

		EffectBase base = (EffectBase) effect;
		int cooldown = base.getCooldown(playerIn, genome);
		if (cooldown != 0) {
			playerIn.getCooldownTracker().setCooldown(this, cooldown);
		}

		if (worldIn.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
		Vec3i territory = genome.getTerritory();
		int range = Math.max(Math.max(territory.getX(), territory.getY()), territory.getZ());

		float f = playerIn.rotationPitch;
		float f1 = playerIn.rotationYaw;
		double posX = playerIn.posX;
		double posY = playerIn.posY + (double) playerIn.getEyeHeight();
		double posZ = playerIn.posZ;
		Vec3d start = new Vec3d(posX, posY, posZ);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		Vec3d end = start.addVector((double) f6 * (double) range, (double) f5 * (double) range, (double) f7 * (double) range);
		RayTraceResult rayTraceResult = worldIn.rayTraceBlocks(start, end, false, false, false);

		if (rayTraceResult != null) {
			end = rayTraceResult.hitVec;
		}

		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(start, end);

		EntityLivingBase closest = getClosestEntityType(worldIn, start, end, axisAlignedBB, EntityLivingBase.class, t -> EntitySelectors.IS_ALIVE.test(t) && t != playerIn);

		ItemStack queenStack = BeeManager.beeRoot.getMemberStack(bee, EnumBeeType.QUEEN);

		if (closest != null) {
			if (base.handleEntityLiving(closest, genome, new FakeHousingPlayer(playerIn, new BlockPos(closest), queenStack), playerIn)) {
				return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
			}
		}

		EntityItem closestItem = getClosestEntityType(worldIn, start, end, axisAlignedBB, EntityItem.class, t -> t != null && EntitySelectors.IS_ALIVE.test(t) && !t.getItem().isEmpty());
		if (closestItem != null) {
			ItemStack stack = base.handleStack(closestItem.getItem(), genome, new FakeHousingPlayer(playerIn, new BlockPos(closestItem), queenStack), playerIn);
			if (stack != null) {
				closestItem.setItem(stack);
				return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
			}
		}

		if (rayTraceResult != null) {
			BlockPos blockPos = rayTraceResult.getBlockPos();
			if (base.handleBlock(worldIn, blockPos, genome, new FakeHousingPlayer(playerIn, blockPos, queenStack), playerIn)) {
				return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
			}
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
	}

	@Nullable
	public <T extends Entity> T getClosestEntityType(@Nonnull World worldIn, @Nonnull Vec3d start, @Nonnull Vec3d end, @Nonnull AxisAlignedBB axisAlignedBB, @Nonnull Class<T> clazz, @Nonnull Predicate<T> filter) {
		T closest = null;
		double closest_dist = Double.MAX_VALUE;
		for (T entityLivingBase : worldIn.getEntitiesWithinAABB(clazz, axisAlignedBB, filter::test)) {
			AxisAlignedBB axisalignedbb = entityLivingBase.getEntityBoundingBox().grow(0.30000001192092896D);
			RayTraceResult ray = axisalignedbb.calculateIntercept(start, end);
			if (ray != null) {
				double v = start.squareDistanceTo(ray.hitVec);

				if (v < closest_dist) {
					closest = entityLivingBase;
					closest_dist = v;
				}
			}
		}
		return closest;
	}


	@Nullable
	public IBee getCurrentSelectedBee(ItemStack heldItem) {

		IAllele[] template = BeeManager.beeRoot.getTemplate(careerBeeEntry.get());
		return BeeManager.beeRoot.templateAsIndividual(template);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void scroll(@Nonnull MouseEvent event) {
		int k = event.getDwheel();
		if (k == 0) return;
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;
		if (player == null || mc.currentScreen != null) return;
		if (mc.gameSettings.keyBindSneak.isKeyDown()) {
			ItemStack currentItem = player.inventory.getCurrentItem();
			if (currentItem.getItem() == this) {
				int index = CareerBeeSpecies.sorted_bee_entries.indexOf(careerBeeEntry);
				int i;
				if (index == -1) {
					i = 0;
				} else if (k > 0) {
					i = (index + 1) % CareerBeeSpecies.sorted_bee_entries.size();
				} else if (k < 0) {
					i = (index - 1);
					if (i < 0) {
						i = CareerBeeSpecies.sorted_bee_entries.size() - 1;
					}
				} else {
					return;
				}

				careerBeeEntry = CareerBeeSpecies.sorted_bee_entries.get(i);
				mc.player.sendStatusMessage(new TextComponentString(careerBeeEntry.getAlleleName()), true);
				event.setCanceled(true);
			}
		}
	}

	public static class FakeHousingPlayer extends FakeHousing {
		final EntityPlayer player;
		final BlockPos target;
		final ItemStack queen;

		public FakeHousingPlayer(EntityPlayer player, BlockPos target, ItemStack queen) {
			this.player = player;
			this.target = target;
			this.queen = queen;
		}

		@Override
		protected boolean addProduct(@Nonnull ItemStack product, boolean all) {
			if (!all) {
				return player.addItemStackToInventory(product.copy());
			} else {
				InventoryPlayer inventory = player.inventory;
				int emptySlot = -1;
				int storage = -1;
				for (int i = 0; i < 36; i++) {
					ItemStack stackInSlot = inventory.getStackInSlot(i);
					if (stackInSlot.isEmpty()) {
						emptySlot = i;
						break;
					} else if (ItemHandlerHelper.canItemStacksStackRelaxed(product, stackInSlot)) {
						storage += stackInSlot.getMaxStackSize() - stackInSlot.getCount();
						if (storage >= product.getCount()) break;
					}
				}
				if (storage < product.getCount() && emptySlot == -1) return false;

				if (!player.inventory.addItemStackToInventory(product.copy())) {
					player.dropItem(product.copy(), false);
				}

				return true;
			}
		}

		@Override
		protected ItemStack getQueen() {
			return queen;
		}

		@Override
		public World getWorldObj() {
			return player.world;
		}

		@Override
		public BlockPos getCoordinates() {
			return target;
		}
	}
}
