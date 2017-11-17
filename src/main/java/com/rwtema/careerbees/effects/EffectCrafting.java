package com.rwtema.careerbees.effects;

import com.google.common.collect.ComparisonChain;
import com.rwtema.careerbees.blocks.BlockFlowerPedastal;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.helpers.ParticleHelper;
import com.rwtema.careerbees.recipes.BeeCraftingInputEntry;
import com.rwtema.careerbees.recipes.BeeCraftingRecipe;
import com.rwtema.careerbees.recipes.IBeeCraftingRecipe;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class EffectCrafting extends EffectBaseThrottled {

	public final static EffectCrafting INSTANCE;

	static {
		INSTANCE = new EffectCrafting();
	}

	public EffectCrafting() {
		super("crafting", true, false, 20 * 10 / 10, 1);
	}

	@Nonnull
	public static List<TileFlowerPedastal> getPlantFrames(World world, BlockPos pos, Vec3d territory, IBeeGenome genome) {
		List<TileFlowerPedastal> craftingUnits = new ArrayList<>();

		int x_min = MathHelper.floor(pos.getX() - territory.x);
		int x_max = MathHelper.ceil(pos.getX() + territory.x);
		int y_min = MathHelper.floor(pos.getY() - territory.y);
		int y_max = MathHelper.ceil(pos.getY() + territory.y);
		int z_min = MathHelper.floor(pos.getZ() - territory.z);
		int z_max = MathHelper.ceil(pos.getZ() + territory.z);

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
							TileFlowerPedastal frame = (TileFlowerPedastal) tileEntity;
							if (frame.hasStack() && frame.accepts(genome)) {
								craftingUnits.add(frame);
							}
						}
					}
				}
			}
		}
		return craftingUnits;
	}

	@Nonnull
	@Override
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {

		World world = housing.getWorldObj();
		BlockPos pos = housing.getCoordinates();
		Vec3d territory = getTerritory(genome, housing);

		Pair<ItemStack, ArrayList<TileFlowerPedastal>> result = getCraftingResult(world, pos, territory, genome);

		List<BlockPos> flowerPos;

		if (result == null) {
			return super.doFX(genome, storedData, housing);
		}

		flowerPos = new ArrayList<>();
		for (TileFlowerPedastal tileFlowerPedastal : result.getRight()) {
			BlockPos blockPos = tileFlowerPedastal.getPos();
			flowerPos.add(blockPos);
			world.spawnParticle(EnumParticleTypes.REDSTONE,
					blockPos.getX() + world.rand.nextFloat(),
					blockPos.getY() + world.rand.nextFloat(),
					blockPos.getZ() + world.rand.nextFloat(),
					0, 0, 0);
		}

		ParticleHelper.BEE_HIVE_FX.addBeeHiveFX(housing, genome, flowerPos);

		return storedData;
	}

	@Override
	public boolean canAcceptItems() {
		return true;
	}

	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		Vec3d territory = getTerritory(genome, housing);

		Pair<ItemStack, ArrayList<TileFlowerPedastal>> result = getCraftingResult(world, pos, territory, genome);

		if (result != null) {
			ItemStack output = result.getLeft();
			if (!output.isEmpty() && housing.getBeeInventory().addProduct(output, true)) {
				for (TileFlowerPedastal tile : result.getRight()) {
					BlockFlowerPedastal.sendPulse(tile, BlockFlowerPedastal.ParticleType.YELLOW);
					tile.setStack(ForgeHooks.getContainerItem(tile.getStack()));
					world.notifyBlockUpdate(tile.getPos(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), 0);
				}
			}
		} else {
			storedData.setInteger(0, 0);
		}
	}

	@Nullable
	private Pair<ItemStack, ArrayList<TileFlowerPedastal>> getCraftingResult(World world, BlockPos pos, Vec3d territory, IBeeGenome genome) {
		List<TileFlowerPedastal> craftingUnits = getPlantFrames(world, pos, territory, genome);

		Pair<ItemStack, ArrayList<TileFlowerPedastal>> result = null;


		HashMap<BeeCraftingInputEntry, ItemStack> stacks = new HashMap<>();
		if (!craftingUnits.isEmpty()) {

			List<TileFlowerPedastal> units = craftingUnits.stream()
					.sorted((o1, o2) -> {
						BlockPos pos1 = o1.getPos();
						BlockPos pos2 = o2.getPos();
						return ComparisonChain.start().compare(
								pos1.distanceSq(pos),
								pos2.distanceSq(pos)
						).compare(pos1.getX(), pos2.getX())
								.compare(pos1.getZ(), pos2.getZ())
								.compare(pos2.getY(), pos1.getY()).result();
					})
					.collect(Collectors.toList());

			if (units.size() > 0) {
				BitSet bitSet = new BitSet(units.size());


				recipeLoop:
				for (IBeeCraftingRecipe recipe : BeeCraftingRecipe.RECIPES) {
					List<BeeCraftingInputEntry> inputs = recipe.getInputs();
					if (inputs.size() > units.size()) break;

					ArrayList<TileFlowerPedastal> tiles = new ArrayList<>();
					stacks.clear();

					bitSet.clear();
					inputLoop:
					for (BeeCraftingInputEntry input : inputs) {
						for (int i = 0; i < units.size(); i++) {
							if (bitSet.get(i)) continue;
							TileFlowerPedastal tileFlowerPedastal = units.get(i);
							ItemStack stack = tileFlowerPedastal.getStack();
							if (input.test(stack)) {
								bitSet.set(i);
								stacks.put(input, stack);
								tiles.add(tileFlowerPedastal);
								continue inputLoop;
							}
						}
						continue recipeLoop;
					}

					result = Pair.of(recipe.getOutput(stacks), tiles);
					break;
				}
			}
		}
		return result;
	}


}
