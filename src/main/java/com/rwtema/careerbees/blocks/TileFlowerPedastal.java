package com.rwtema.careerbees.blocks;

import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.effects.EffectBase;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeGenome;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Predicate;

public class TileFlowerPedastal extends TileEntity {
	public static final HashMap<String, Predicate<ItemStack>> EFFECT_PREDICATE_MAP = new HashMap<>();
	boolean canExtract = false;
	String speciesType = "";

	ItemStackHandler stackHandler = new ItemStackHandler(1) {
		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
			if (canAcceptStack(stack)) {
				return super.getStackLimit(slot, stack);
			}
			return 0;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (!canExtract && (speciesType.isEmpty() || getStack().isEmpty() || canAcceptStack(getStack()))) {
				return ItemStack.EMPTY;
			}
			return super.extractItem(slot, amount, simulate);
		}

		@Override
		protected void onContentsChanged(int slot) {
			if (canExtract && stacks.get(0).isEmpty()) {
				canExtract = false;
			}
			if (world != null) {
				markDirty();

				BlockPos pos = getPos();
				IBlockState blockState = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, blockState, blockState, 0);
			}
		}
	};

	public boolean canAcceptStack(@Nonnull ItemStack stack) {
		return EFFECT_PREDICATE_MAP.computeIfAbsent(speciesType,
				s -> {
					IAlleleBeeSpecies species = CareerBeeEntry.CustomBeeFactory.STRING_SPECIES_MAP.get(speciesType);
					if (species != null) {
						IAlleleBeeEffect effect = CareerBeeEntry.CustomBeeFactory.SPECIES_EFFECT_MAP.get(species);
						if (effect instanceof EffectBase) {
							return ((EffectBase) effect)::acceptItemStack;
						}
					}
					return i -> true;
				}).test(stack);
	}

	@Nonnull
	public ItemStack getStack() {
		return stackHandler.getStackInSlot(0);
	}

	public void setStack(ItemStack stack) {
		stackHandler.setStackInSlot(0, stack);
	}

	public boolean hasStack() {
		return !getStack().isEmpty();
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return super.hasCapability(capability, facing) || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(stackHandler);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		speciesType = tag.getString("speciesType");
		ItemStack stack = new ItemStack(tag.getCompoundTag("stack"));
		setStack(stack);
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound updateTag = super.getUpdateTag();
		updateTag.setTag("stack", getStack().writeToNBT(new NBTTagCompound()));
		updateTag.setString("speciesType", speciesType);
		return updateTag;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		ItemStack stack = new ItemStack(compound.getCompoundTag("stack"));
		setStack(stack);
		canExtract = compound.getBoolean("canExtract");
		speciesType = compound.getString("speciesType");
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
		nbtTagCompound.setTag("stack", getStack().writeToNBT(new NBTTagCompound()));
		compound.setBoolean("canExtract", canExtract);
		compound.setString("speciesType", speciesType);
		return nbtTagCompound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		NBTTagCompound nbt = pkt.getNbtCompound();
		handleUpdateTag(nbt);
	}

	public void setShouldRelease() {
		canExtract = true;
	}

	public boolean accepts(IBeeGenome genome) {
		return speciesType.isEmpty() || speciesType.equals(genome.getPrimary().getUID());
	}
}
