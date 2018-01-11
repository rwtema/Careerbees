package com.rwtema.careerbees.items;

import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.entity.EntityBeeSwarm;
import com.rwtema.careerbees.handlers.FakeHousing;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAllele;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

		if (worldIn.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);

		EntityBeeSwarm swarm = new EntityBeeSwarm(worldIn, BeeManager.beeRoot.getMemberStack(bee, EnumBeeType.QUEEN), playerIn);
		swarm.setAim(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 0.2F, 1.0F);

		worldIn.spawnEntity(swarm);

		return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
	}

//
//	@Nonnull
//	@Override
//	public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, EnumHand handIn) {
//		ItemStack heldItem = playerIn.getHeldItem(handIn);
//		IBee bee = getCurrentSelectedBee(heldItem);
//		if (bee == null) {
//			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//		}
//
//
//		IBeeGenome genome = bee.getGenome();
//		IAlleleBeeEffect effect = genome.getEffect();
//		if (!(effect instanceof ISpecialBeeEffect)) {
//			return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//		}
//
//		ISpecialBeeEffect specialBeeEffect = (ISpecialBeeEffect) effect;
//		int cooldown = Math.round(specialBeeEffect.getCooldown(genome, worldIn.rand));
//		if (cooldown != 0) {
//			playerIn.getCooldownTracker().setCooldown(this, cooldown);
//		}
//
//		if (worldIn.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//		Vec3i territory = genome.getTerritory();
//		int range = Math.max(Math.max(territory.getX(), territory.getY()), territory.getZ());
//
//		float f = playerIn.rotationPitch;
//		float f1 = playerIn.rotationYaw;
//		double posX = playerIn.posX;
//		double posY = playerIn.posY + (double) playerIn.getEyeHeight();
//		double posZ = playerIn.posZ;
//		Vec3d start = new Vec3d(posX, posY, posZ);
//		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
//		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
//		float f4 = -MathHelper.cos(-f * 0.017453292F);
//		float f5 = MathHelper.sin(-f * 0.017453292F);
//		float f6 = f3 * f4;
//		float f7 = f2 * f4;
//		Vec3d end = start.addVector((double) f6 * (double) range, (double) f5 * (double) range, (double) f7 * (double) range);
//		RayTraceResult rayTraceResult = worldIn.rayTraceBlocks(start, end, false, false, false);
//
//		if (rayTraceResult != null) {
//			end = rayTraceResult.hitVec;
//		}
//
//		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(start, end);
//		ItemStack queenStack = BeeManager.beeRoot.getMemberStack(bee, EnumBeeType.QUEEN);
//
//		if (specialBeeEffect instanceof ISpecialBeeEffect.SpecialEffectEntity) {
//			ISpecialBeeEffect.SpecialEffectEntity effectEntity = (ISpecialBeeEffect.SpecialEffectEntity) specialBeeEffect;
//			Entity closest = getClosestEntityType(worldIn, start, end, axisAlignedBB, Entity.class, t -> EntitySelectors.IS_ALIVE.test(t) && t != playerIn && effectEntity.canHandleEntity(t, genome));
//
//
//			if (closest != null) {
//				if (effectEntity.handleEntityLiving(closest, genome, new FakeHousingPlayer(playerIn, new BlockPos(closest), queenStack))) {
//					return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//				}
//			}
//		}
//
//		if (specialBeeEffect instanceof ISpecialBeeEffect.SpecialEffectItem) {
//			ISpecialBeeEffect.SpecialEffectItem effectItem = (ISpecialBeeEffect.SpecialEffectItem) specialBeeEffect;
//			EntityItem closestItem = getClosestEntityType(worldIn, start, end, axisAlignedBB, EntityItem.class, t -> t != null && EntitySelectors.IS_ALIVE.test(t) && !t.getItem().isEmpty() && effectItem.canHandleStack(t.getItem(), genome));
//			if (closestItem != null) {
//				ItemStack stack = effectItem.handleStack(closestItem.getItem(), genome, new FakeHousingPlayer(playerIn, new BlockPos(closestItem), queenStack));
//				if (stack != null) {
//					closestItem.setItem(stack);
//					return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//				}
//			}
//		}
//
//		if (specialBeeEffect instanceof ISpecialBeeEffect.SpecialEffectBlock) {
//			ISpecialBeeEffect.SpecialEffectBlock effectBlock = (ISpecialBeeEffect.SpecialEffectBlock) specialBeeEffect;
//			if (rayTraceResult != null) {
//				BlockPos blockPos = rayTraceResult.getBlockPos();
//				if (effectBlock.canHandleBlock(worldIn, blockPos, genome) && effectBlock.handleBlock(worldIn, blockPos, genome, new FakeHousingPlayer(playerIn, blockPos, queenStack))) {
//					return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//				}
//			}
//		}
//
//		return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
//	}

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
		final Entity player;
		final BlockPos target;
		final ItemStack queen;

		public FakeHousingPlayer(Entity player, BlockPos target, ItemStack queen) {
			this.player = player;
			this.target = target;
			this.queen = queen;
		}

		@Override
		protected boolean addProduct(@Nonnull ItemStack product, boolean all) {
			float f = 0.5F;
			World world = getWorldObj();
			BlockPos pos = getCoordinates();
			double d0 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
			double d1 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
			double d2 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
			EntityItem entityitem = new EntityItem(world, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, product.copy());
			entityitem.setDefaultPickupDelay();
			world.spawnEntity(entityitem);
			return true;
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
