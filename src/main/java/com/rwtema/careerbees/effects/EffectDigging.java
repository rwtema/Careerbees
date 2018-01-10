package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import gnu.trove.map.hash.TIntByteHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EffectDigging extends EffectBaseThrottled {
	public final static EffectDigging INSTANCE_NORMAL = new EffectDigging(DigType.NORMAL, "miner");
	public final static EffectDigging INSTANCE_SILKY = new EffectDigging(DigType.SILKY, "miner.silky");
	public final static EffectDigging INSTANCE_FORTUNE = new EffectDigging(DigType.FORTUNE, "miner.fortune");

	public static ThreadLocal<List<ItemStack>> captureStacks = new ThreadLocal<>();

	static {
		MinecraftForge.EVENT_BUS.register(EffectDigging.class);
	}

	final DigType digType;
	TIntByteHashMap oreIDs = new TIntByteHashMap();

	public EffectDigging(DigType digType, String miner) {
		super(miner, 40);
		this.digType = digType;
	}

	@SubscribeEvent
	public static void captureStacks(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityItem) {
			List<ItemStack> stacks = captureStacks.get();
			if (stacks == null) return;
			stacks.add(((EntityItem) event.getEntity()).getItem());
			event.setCanceled(true);
		}
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random rand, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		AxisAlignedBB aabb = getAABB(genome, housing);
		int x0 = MathHelper.floor(aabb.minX);
		int x1 = MathHelper.ceil(aabb.maxX);
		int z0 = MathHelper.floor(aabb.minZ);
		int z1 = MathHelper.ceil(aabb.maxZ);

		if (x1 <= x0 || z1 <= z0) return;

		BlockPos.MutableBlockPos.PooledMutableBlockPos pooledBlockPos = BlockPos.PooledMutableBlockPos.retain();
		for (int i = 0; i < 80; i++) {

			pooledBlockPos.setPos(
					x0 + rand.nextInt(x1 - x0),
					0,
					z0 + rand.nextInt(z1 - z0)
			);
			pooledBlockPos.setY(rand.nextInt(world.getChunkFromBlockCoords(pooledBlockPos).getTopFilledSegment() + 16));

			processPosition(housing, world, pooledBlockPos);
		}
	}

	public boolean processPosition(@Nonnull IBeeHousing housing, World world, BlockPos pooledBlockPos) {
		IBlockState blockState = world.getBlockState(pooledBlockPos);
		Block block = blockState.getBlock();
		if (blockState.getBlock().isAir(blockState, world, pooledBlockPos)) {
			return false;
		}

		ItemStack item = blockState.getBlock().getPickBlock(blockState, new RayTraceResult(new Vec3d(pooledBlockPos), EnumFacing.DOWN, pooledBlockPos), world, pooledBlockPos, FakePlayerFactory.getMinecraft((WorldServer) world));
		if (item.isEmpty()) return false;

		boolean isOre = isOre(item);

		if (!isOre) return false;

		List<ItemStack> products = new ArrayList<>();

		try {
			captureStacks.set(products);

			switch (digType) {
				case NORMAL:
					block.dropBlockAsItem(world, pooledBlockPos, blockState, 0);
					break;
				case FORTUNE:
					block.dropBlockAsItem(world, pooledBlockPos, blockState, 2);
					break;
				case SILKY:
					FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) world);
					ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
					stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
					block.harvestBlock(world, fakePlayer, pooledBlockPos, blockState, world.getTileEntity(pooledBlockPos), stack);
					break;
			}
		} finally {
			captureStacks.set(null);
		}

		boolean addedSomething = false;
		for (Iterator<ItemStack> iterator = products.iterator(); iterator.hasNext(); ) {
			ItemStack product = iterator.next();
			if (product.isEmpty()) {
				iterator.remove();
				continue;
			}
			ItemStack remainder = tryAdd(product, housing.getBeeInventory());
			addedSomething |= (remainder.getCount() != product.getCount());
			if (remainder.isEmpty())
				iterator.remove();
			else
				product.setCount(remainder.getCount());

		}

		if (addedSomething) {
			world.playEvent(2001, pooledBlockPos, Block.getStateId(blockState));
			world.setBlockToAir(pooledBlockPos);
			for (ItemStack product : products) {
				Block.spawnAsEntity(world, pooledBlockPos, product);
			}
		}

		return true;
	}

	private boolean isOre(ItemStack item) {
		for (int oreID : OreDictionary.getOreIDs(item)) {
			byte b = oreIDs.get(oreID);

			if (b == 1) continue;
			else if (b == 2) return true;

			String oreName = OreDictionary.getOreName(oreID);
			if (hasOrePrefix(oreName, "ore") || hasOrePrefix(oreName, "denseore")) {
				oreIDs.put(oreID, (byte) 2);
				return true;
			} else {
				oreIDs.put(oreID, (byte) 1);
			}
		}
		return false;
	}

	private boolean hasOrePrefix(String oreName, String prefix) {
		return oreName.length() > prefix.length() && oreName.startsWith(prefix) && Character.isUpperCase(oreName.charAt(prefix.length()));
	}

	@Override
	public boolean handleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nullable EntityPlayer owner) {
		return processPosition(housing, world, pos);
	}


	public enum DigType {
		NORMAL,
		FORTUNE,
		SILKY
	}
}
