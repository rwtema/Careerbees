package com.rwtema.careerbees.mutations;

import com.rwtema.careerbees.helpers.FieldAccessor;
import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IGenome;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.WeakHashMap;

public class MutationRecentExplosion extends CareerBeesMutationBooleanCondition {

	private static final WeakHashMap<World, TObjectIntHashMap<BlockPos>> map = new WeakHashMap<>();
	private static final FieldAccessor<Boolean, Explosion> damagesTerrain = new FieldAccessor<>(Explosion.class, "damagesTerrain", "field_82755_b");

	static {
		MinecraftForge.EVENT_BUS.register(MutationRecentExplosion.class);
	}

	public MutationRecentExplosion() {
		super("Must have been a nearby explosion in the last 10 seconds");
	}

	@SubscribeEvent
	public static void explosion(ExplosionEvent event) {
		Explosion explosion = event.getExplosion();
		if (!damagesTerrain.get(explosion)) {
			return;
		}

		BlockPos pos = new BlockPos(explosion.getPosition());
		map.computeIfAbsent(event.getWorld(), w -> new TObjectIntHashMap<>()).put(pos, 10 + 20 * 12);
	}

	@SubscribeEvent
	public static void worldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}
		TObjectIntHashMap<BlockPos> map = MutationRecentExplosion.map.get(event.world);
		if (map != null) {
			TObjectIntIterator<BlockPos> iterator = map.iterator();
			while (iterator.hasNext()) {
				iterator.advance();
				int i = iterator.value();
				if (i <= 0) {
					iterator.remove();
				} else {
					iterator.setValue(i - 1);
				}
			}

			if (map.isEmpty()) {
				MutationRecentExplosion.map.remove(event.world);
			}
		}
	}

	public static int getManhattenDist(Vec3i a, Vec3i b) {
		return Math.abs(a.getX() - b.getX())
				+ Math.abs(a.getY() - b.getY())
				+ Math.abs(a.getZ() - b.getZ());
	}

	@Override
	protected boolean isAcceptable(World world, BlockPos pos, IAllele allele0, IAllele allele1, IGenome genome0, IGenome genome1, IClimateProvider climate) {
		TObjectIntHashMap<BlockPos> positions = map.get(world);
		if (positions == null) return false;
		for (BlockPos blockPos : positions.keySet()) {
			if (getManhattenDist(blockPos, pos) < 30) {
				return true;
			}
		}
		return false;
	}
}
