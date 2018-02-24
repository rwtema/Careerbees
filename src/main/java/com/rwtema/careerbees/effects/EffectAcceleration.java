package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class EffectAcceleration extends EffectBase implements ISpecialBeeEffect.SpecialEffectBlock {
	public static final EffectAcceleration INSTANCE = new EffectAcceleration();
	final WeakHashMap<World, TObjectIntHashMap<BlockPos>> posToTick = new WeakHashMap<>();
	boolean processing = false;

	public EffectAcceleration() {
		super("accelerate");
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean canHandleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, EnumFacing sideHit) {
		return true;
	}

	@Override
	public boolean handleBlock(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing facing, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		TObjectIntHashMap<BlockPos> toTickPos = posToTick.computeIfAbsent(world, w -> new TObjectIntHashMap<>());
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof ITickable) {
			toTickPos.put(pos, 40);
		}
		Block block = world.getBlockState(pos).getBlock();
		if (block.getTickRandomly()) {
			world.scheduleUpdate(pos, block, 1);
		}
		return true;
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		if (processing) return storedData;
		World world = housing.getWorldObj();
		if(world.isRemote){
			return storedData;
		}

		if ((world.getTotalWorldTime() % 20) != 0) return storedData;
		BlockPos coordinates = housing.getCoordinates();

		TObjectIntHashMap<BlockPos> toTickPos = posToTick.computeIfAbsent(world, w -> new TObjectIntHashMap<>());
		if (toTickPos.containsKey(coordinates)) {
			return storedData;
		}

		toTickPos.put(coordinates, 0);
		try {
			processing = true;
			Vec3d territory = getTerritory(genome, housing);

			int mx = MathHelper.floor(territory.x);
			int my = MathHelper.floor(territory.y);
			int mz = MathHelper.floor(territory.z);

			for (int dx = -mx; dx <= mx; dx++) {
				for (int dz = -mz; dz <= mz; dz++) {
					for (int dy = -my; dy <= my; dy++) {
						BlockPos pos = new BlockPos(
								coordinates.getX() + dx,
								coordinates.getY() + dy,
								coordinates.getZ() + dz);

						IBlockState blockState = world.getBlockState(pos);
						Block block = blockState.getBlock();
						if (!block.isAir(blockState, world, pos)) {
							TileEntity tileEntity = world.getTileEntity(pos);
							if (tileEntity instanceof ITickable) {
								toTickPos.put(pos.toImmutable(), 40);
							}

							if (block.getTickRandomly()) {
								world.scheduleUpdate(pos, block, 1);
//								for (int i = 0; i < 4; i++) {
//									block.randomTick(world, pos.toImmutable(), blockState, world.rand);
//								}
							}
						}
					}
				}
			}
		} finally {
			processing = false;
		}
		return storedData;
	}

	@SubscribeEvent
	public void worldTick(@Nonnull TickEvent.WorldTickEvent event) {
		if (processing) return;

		TObjectIntHashMap<BlockPos> blockPosSet = posToTick.get(event.world);
		if (blockPosSet != null && !blockPosSet.isEmpty()) {
			try {
				processing = true;
				List<ITickable> toTick = new ArrayList<>(posToTick.size());
				TObjectIntIterator<BlockPos> iterator = blockPosSet.iterator();
				while (iterator.hasNext()) {
					iterator.advance();
					BlockPos blockPos = iterator.key();
					TileEntity tileEntity = event.world.getTileEntity(blockPos);
					if (tileEntity instanceof ITickable) {
						toTick.add((ITickable) tileEntity);

						int value = iterator.value();
						if (value > 0) {
							iterator.setValue(value - 1);
						} else {
							iterator.remove();
						}
					} else {
						iterator.remove();
					}
				}
				for (int i = 0; i < 4; i++) {
					toTick.forEach(ITickable::update);
				}
			} finally {
				processing = false;
			}
		}
	}

}
