package com.rwtema.careerbees.effects;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class HousingTerritoryTracker {

	private static final Function<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>> WEAK_MAP_LAMBDA = t -> new WeakHashMap<>();
	private final WeakHashMap<World, HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>>> bounds_server = new WeakHashMap<>();
	private final WeakHashMap<World, HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>>> bounds_client = new WeakHashMap<>();
	private final Predicate<IBeeHousing> isValidPredicate;

	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public HousingTerritoryTracker(Predicate<IBeeHousing> isValidPredicate) {
		this.isValidPredicate = isValidPredicate;
	}

	public HousingTerritoryTracker(EffectBase effectBase) {
		this(housing -> {
			ItemStack queen = housing.getBeeInventory().getQueen();
			if (queen.isEmpty()) return false;
			IBee member = BeeManager.beeRoot.getMember(queen);
			if (member == null) return false;
			IBeeGenome genome = member.getGenome();
			return genome.getEffect() == effectBase && effectBase.isValidSpecies(genome);
		});
	}

	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
		getMap(event.getWorld()).remove(event.getWorld());
	}

	public WeakHashMap<World, HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>>> getMap(World world) {
		return world.isRemote ? bounds_client : bounds_server;
	}

	@SubscribeEvent
	public void worldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;
		World world = event.world;
		HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>> map = getMap(world).get(world);

		if (world.getTotalWorldTime() % 16 != 0) return;

		if (map == null) return;
		for (Iterator<WeakHashMap<IBeeHousing, AxisAlignedBB>> iterator = map.values().iterator(); iterator.hasNext(); ) {
			WeakHashMap<IBeeHousing, AxisAlignedBB> weakHashMap = iterator.next();
			weakHashMap.keySet().removeIf(t -> !isValid(t));
			if (weakHashMap.isEmpty()) iterator.remove();
		}
	}

//	public boolean getAllIntersects(World world, AxisAlignedBB bb, List<AxisAlignedBB> bounds){
//		HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>> m = getMap(world).get(world);
//		if (m == null) return false;
//		for (int dx = MathHelper.floor(bb.minX) >> 4; dx <= MathHelper.floor(bb.maxX) >> 4; dx++) {
//			for (int dz = MathHelper.floor(bb.minZ) >> 4; dx <= MathHelper.floor(bb.maxZ) >> 4; dx++) {
//				byte b = hashChunk(dx, dz);
//				WeakHashMap<IBeeHousing, AxisAlignedBB> s = m.get(b);
//				if(s == null) continue;
//				for (AxisAlignedBB alignedBB : s.values()) {
//
//				}
//			}
//		}
//
//	}

	public boolean isInRange(World world, BlockPos pos) {

		HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>> m = getMap(world).get(world);
		if (m == null) return false;
		WeakHashMap<IBeeHousing, AxisAlignedBB> s = m.get(hashChunk(pos.getX() >> 4, pos.getZ() >> 4));
		if (s == null) return false;
		for (AxisAlignedBB bb : s.values()) {
			if (bb.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValid(IBeeHousing housing) {
		if (housing instanceof TileEntity) {
			if (((TileEntity) housing).isInvalid()) return false;
		} else if (housing instanceof Entity) {
			if (((Entity) housing).isDead) {
				return false;
			}
		}
		return isValidPredicate.test(housing);
	}

	public void register(IBeeHousing housing, AxisAlignedBB territory) {
		HashMap<Byte, WeakHashMap<IBeeHousing, AxisAlignedBB>> boundmap = getMap(housing.getWorldObj()).get(housing.getWorldObj());
		for (int dx = MathHelper.floor(territory.minX) >> 4; dx <= MathHelper.floor(territory.maxX) >> 4; dx++) {
			for (int dz = MathHelper.floor(territory.minZ) >> 4; dx <= MathHelper.floor(territory.maxZ) >> 4; dx++) {
				byte b = hashChunk(dx, dz);
				WeakHashMap<IBeeHousing, AxisAlignedBB> map = boundmap.computeIfAbsent(b, WEAK_MAP_LAMBDA);
				map.put(housing, territory);
			}
		}
	}


	public byte hashChunk(int chunk_x, int chunk_z) {
		return (byte) (15 * chunk_x + chunk_z);
	}

}
