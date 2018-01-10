package com.rwtema.careerbees.blocks;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.items.ItemIngredients;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Random;

public class BlockFlowerPedastal extends Block {
	public static final IProperty<PlantType> PLANT_TYPE = PropertyEnum.create("type", PlantType.class);

	public BlockFlowerPedastal() {
		super(Material.PLANTS);
		this.setUnlocalizedName(BeeMod.MODID + ":flower_pedastal");
		this.setRegistryName(BeeMod.MODID + ":flower_pedastal");
		this.setCreativeTab(BeeMod.creativeTab);
	}

	public static void sendPulse(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleType particleType) {
		world.addBlockEvent(pos, BeeMod.instance.plantFrame, particleType.ordinal(), 0);
	}

	public static void sendPulse(@Nonnull TileFlowerPedastal plantFrame, @Nonnull ParticleType particleType) {
		sendPulse(plantFrame.getWorld(), plantFrame.getPos(), particleType);
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PLANT_TYPE);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(PLANT_TYPE, meta == 1 ? PlantType.ADVANCED : PlantType.BASIC);
	}

	@Override
	public int getMetaFromState(@Nonnull IBlockState state) {
		return state.getValue(PLANT_TYPE).ordinal();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileFlowerPedastal();
	}

	@Override
	public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof TileFlowerPedastal)) return true;
		TileFlowerPedastal flowerPedastal = (TileFlowerPedastal) tileEntity;
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (ItemIngredients.IngredientType.PHEREMONES.test(heldItem) && flowerPedastal.speciesType.isEmpty() && heldItem.getTagCompound() != null) {
			String species = heldItem.getTagCompound().getString("species");
			IAlleleBeeSpecies checkSpecies = CareerBeeEntry.CustomBeeFactory.STRING_SPECIES_MAP.get(species);
			if (checkSpecies != null) {
				IAlleleBeeEffect iAlleleBeeEffect = CareerBeeEntry.CustomBeeFactory.SPECIES_EFFECT_MAP.get(checkSpecies);
				if (iAlleleBeeEffect instanceof EffectBase) {
					if (((EffectBase) iAlleleBeeEffect).canAcceptItems()) {
						flowerPedastal.speciesType = species;
						worldIn.notifyBlockUpdate(pos, Blocks.AIR.getDefaultState(), state, 0);
						heldItem.shrink(1);
						return true;
					}
				}
			}
			playerIn.sendMessage(Lang.chat("This species does not use flower pedastals"));
			return true;
		}

		ItemStack plantFrameStack = flowerPedastal.getStack();

		if (heldItem.isEmpty() && !plantFrameStack.isEmpty()) {
			playerIn.setHeldItem(hand, plantFrameStack.copy());
			flowerPedastal.setStack(ItemStack.EMPTY);
		} else if (!heldItem.isEmpty()) {
			if (!flowerPedastal.canAcceptStack(heldItem)) return true;

			if (!plantFrameStack.isEmpty()) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), plantFrameStack.copy());
			}
			flowerPedastal.setStack(heldItem.splitStack(1));
		}
		worldIn.notifyBlockUpdate(pos, Blocks.AIR.getDefaultState(), state, 0);
		return true;
	}

	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return NULL_AABB;
	}

	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean eventReceived(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, int id, int param) {
		if (worldIn.isRemote) {
			ParticleType particleType = ParticleType.values()[id];
			switch (particleType) {

				case YELLOW:
					for (int i = 0; i < 10; i++) {
						double x = pos.getX() + worldIn.rand.nextDouble();
						double y = pos.getY() + worldIn.rand.nextDouble();
						double z = pos.getZ() + worldIn.rand.nextDouble();
						worldIn.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 1D, 0.9D, 0.01D);
					}
					break;
				case FIRE:
					for (int i = 0; i < 10; i++) {
						double x = pos.getX() + worldIn.rand.nextDouble();
						double y = pos.getY() + worldIn.rand.nextDouble();
						double z = pos.getZ() + worldIn.rand.nextDouble();
						worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
						worldIn.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
					}
					break;
			}

		}
		return true;
	}

	@Override
	public void randomDisplayTick(IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, Random rand) {
		if (worldIn.getTotalWorldTime() % 8 > 0) {
			return;
		}
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof TileFlowerPedastal)) return;
		String speciesType = ((TileFlowerPedastal) tileEntity).speciesType;
		if (speciesType.isEmpty()) return;

		IAllele species = AlleleManager.alleleRegistry.getAllele(speciesType);
		if (!(species instanceof IAlleleBeeSpecies)) {
			return;
		}
		double x = pos.getX() + worldIn.rand.nextDouble();
		double y = pos.getY() + worldIn.rand.nextDouble() / 16;
		double z = pos.getZ() + worldIn.rand.nextDouble();

		int color = ((IAlleleBeeSpecies) species).getSpriteColour(worldIn.rand.nextInt(2));
		double r = (double) (color >> 16 & 255) / 255.0D;
		double g = (double) (color >> 8 & 255) / 255.0D;
		double b = (double) (color & 255) / 255.0D;

		worldIn.spawnParticle(EnumParticleTypes.SPELL_MOB_AMBIENT, x, y, z, r, g, b);
	}

//	@Override
//	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
//		items.add(new ItemStack(this, 1, 0));
//		items.add(new ItemStack(this, 1, 1));
//	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if ((tileEntity instanceof TileFlowerPedastal)) {
			TileFlowerPedastal plantFrame = (TileFlowerPedastal) tileEntity;
			ItemStack plantFrameStack = plantFrame.getStack();

			if (!plantFrameStack.isEmpty()) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), plantFrameStack.copy());
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	public enum ParticleType {
		YELLOW,
		FIRE
	}

	public enum PlantType implements IStringSerializable {
		BASIC,
		ADVANCED;


		@Override
		public String getName() {
			return toString().toLowerCase(Locale.ENGLISH);
		}
	}
}
