package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.items.ItemIngredients;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class EffectLumber extends EffectWorldInteraction {
	public static final EffectLumber INSTANCE = new EffectLumber("lumber", 200);

	public EffectLumber(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Nonnull
	public static BlockPos.MutableBlockPos searchUpForNextNotEqual(World world, @Nonnull BlockPos startSearch, @Nonnull Predicate<BlockPos> isWood) {
		BlockPos.MutableBlockPos lower = new BlockPos.MutableBlockPos(startSearch);
		BlockPos.MutableBlockPos upper = new BlockPos.MutableBlockPos(startSearch).move(EnumFacing.UP, 4);

		while (isWood.test(upper)) {
			lower.setY(upper.getY());
			upper.setY(startSearch.getY() + (upper.getY() - startSearch.getY()) * 2);
		}

		BlockPos.MutableBlockPos center = new BlockPos.MutableBlockPos(startSearch);
		do {
			center.setY((lower.getY() + upper.getY()) / 2);
			if (isWood.test(center)) {
				lower.setY(center.getY());
			} else {
				upper.setY(center.getY());
			}
		} while (upper.getY() - lower.getY() > 1);

		return upper;
	}

	@Override
	protected boolean performPosEffect(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull IBlockState state, IBeeGenome genome, @Nonnull IBeeHousing housing) {
		if (state.getBlock().isWood(world, blockPos)) {
			BlockPos.MutableBlockPos topBlock = searchUpForNextNotEqual(world, blockPos, s -> world.getBlockState(s).getBlock().isWood(world, s));

			IBlockState leafState = world.getBlockState(topBlock);
			if(!leafState.getBlock().isLeaves(leafState,world, topBlock)){
				return false;
			}

			NonNullList<ItemStack> drops = NonNullList.create();
			state.getBlock().getDrops(drops, world, blockPos, state, 0);
			float chance = ForgeEventFactory.fireBlockHarvesting(drops, world, blockPos, state, 0, 1, false, null);

			for (ItemStack drop : drops) {
				int blockWood = OreDictionary.getOreID("logWood");
				for (int i : OreDictionary.getOreIDs(drop)) {
					if (i == blockWood) {
						if (chance == 1 || world.rand.nextFloat() <= chance) {
							NBTTagCompound tag = drop.writeToNBT(new NBTTagCompound());
							ItemStack barkStack = ItemIngredients.IngredientType.BARK.get();
							barkStack.setTagInfo("bark", tag);
							barkStack.setCount(1 + world.rand.nextInt(1 + world.rand.nextInt(8)));
							housing.getBeeInventory().addProduct(barkStack, false);
						}
						break;
					}
				}
			}
			return true;
		}
		return false;
	}
}
