package com.rwtema.careerbees.effects;

import com.google.common.collect.Streams;
import com.rwtema.careerbees.blocks.BlockFlowerPedastal;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.effects.settings.Filter;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.ParticleHelper;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class EffectItemModification extends EffectBaseThrottled implements ISpecialBeeEffect.SpecialEffectItem {
	final Filter filter = new Filter(this);

	public EffectItemModification(String name, float baseTicksBetweenProcessing) {
		this(name, false, false, baseTicksBetweenProcessing, 1);
	}

	public EffectItemModification(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Override
	public boolean canAcceptItems() {
		return true;
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, @Nonnull World world, @Nonnull BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		Predicate<ItemStack> matcher = getFilter(housing, world, settings, getAABB(genome, housing), filter);

		List<TileFlowerPedastal> frameList = getPlantFrames(genome, housing, world, pos, settings);
		Collections.shuffle(frameList);
		for (TileFlowerPedastal plantFrame : frameList) {
			ItemStack stack = plantFrame.getStack();
			if (stack.isEmpty() || !matcher.test(stack)) continue;
			ItemStack itemStack = modifyStack(genome, stack, housing);
			if (itemStack != null) {
				plantFrame.setStack(itemStack);
				BlockFlowerPedastal.sendPulse(plantFrame, getParticleType(genome, plantFrame, stack, itemStack));
				if (shouldRelease(genome, plantFrame, stack, itemStack, housing)) {
					plantFrame.setShouldRelease();
				}
				return;
			}
		}
	}

	@Nonnull
	private List<TileFlowerPedastal> getPlantFrames(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing, @Nonnull World world, @Nonnull BlockPos pos, IEffectSettingsHolder settings) {
		Vec3d territory = getTerritory(genome, housing);

		int x_min = MathHelper.floor(pos.getX() - territory.x);
		int x_max = MathHelper.ceil(pos.getX() + territory.x);
		int y_min = MathHelper.floor(pos.getY() - territory.y);
		int y_max = MathHelper.ceil(pos.getY() + territory.y);
		int z_min = MathHelper.floor(pos.getZ() - territory.z);
		int z_max = MathHelper.ceil(pos.getZ() + territory.z);

		Predicate<ItemStack> matcher = filter.getMatcher(settings);

		List<TileFlowerPedastal> frameList = new ArrayList<>();

		for (int chunk_x = x_min >> 4; chunk_x <= x_max >> 4; chunk_x++) {
			for (int chunk_z = z_min >> 4; chunk_z <= z_max >> 4; chunk_z++) {
				Chunk chunk = world.getChunkFromChunkCoords(chunk_x, chunk_z);
				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
					BlockPos key = entry.getKey();
					if (key.getX() >= x_min && key.getX() <= x_max &&
							key.getY() >= y_min && key.getY() <= y_max &&
							key.getZ() >= z_min && key.getZ() <= z_max) {
						TileEntity tileEntity = entry.getValue();
						if (tileEntity instanceof TileFlowerPedastal) {
							TileFlowerPedastal plantFrame = (TileFlowerPedastal) tileEntity;

							if (!plantFrame.accepts(genome)) {
								continue;
							}

							ItemStack stack = plantFrame.getStack();
							if (!stack.isEmpty() && matcher.test(stack)) {
								frameList.add(plantFrame);
							}
						}
					}
				}
			}
		}
		return frameList;
	}

	@Nullable
	@Override
	public ItemStack handleStack(ItemStack stack, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		return acceptItemStack(stack) ? modifyStack(genome, stack, housing) : null;
	}

	public boolean handleBlock(World world, BlockPos pos, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing){
		return false;
	}

	public boolean handleEntityLiving(EntityLivingBase livingBase, @Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing){
		return false;
	}

	@Nonnull
	protected BlockFlowerPedastal.ParticleType getParticleType(IBeeGenome genome, TileFlowerPedastal plantFrame, ItemStack stack, ItemStack itemStack) {
		return BlockFlowerPedastal.ParticleType.YELLOW;
	}


	@Nullable
	public abstract ItemStack modifyStack(IBeeGenome genome, ItemStack stack, @Nullable IBeeHousing housing);

	public boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, ItemStack oldStack, @Nonnull ItemStack newStack, IBeeHousing housing) {
		if (!acceptItemStack(newStack)) return true;

		ItemStack stack = modifyStack(genome, newStack.copy(), housing);
		return stack == null || (stack.isItemEqual(newStack) && Objects.equals(stack.getTagCompound(), newStack.getTagCompound()));
	}

	@Override
	public abstract boolean acceptItemStack(ItemStack stack);

	@Override
	public boolean canHandleStack(ItemStack stack, @Nonnull IBeeGenome genome) {
		return acceptItemStack(stack);
	}

	@Nonnull
	@Override
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {

		World world = housing.getWorldObj();
		BlockPos pos = housing.getCoordinates();
		List<TileFlowerPedastal> frameList = getPlantFrames(genome, housing, world, pos, IEffectSettingsHolder.DEFAULT_INSTANCE);

		if (frameList.isEmpty()) {
			return super.doFX(genome, storedData, housing);
		}

		ParticleHelper.BEE_HIVE_FX.addBeeHiveFX(housing, genome,
				Streams.concat(
						frameList.stream().map(TileEntity::getPos),
						housing.getBeekeepingLogic().getFlowerPositions().stream()
				).collect(Collectors.toList()));
		return storedData;
	}
}
