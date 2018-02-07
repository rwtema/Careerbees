package com.rwtema.careerbees.blocks;

import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.helpers.NBTSerializer;
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
	public static final NBTSerializer<TileFlowerPedastal> serializer = NBTSerializer.getTileEntitySeializer(TileFlowerPedastal.class)
			.addItemStack("stack", TileFlowerPedastal::getStack, TileFlowerPedastal::setStack)
			.addBoolean("canExtract", p -> p.canExtract, (p, s) -> p.canExtract = s)
			.addString("speciesType", p -> p.speciesType, (p, s) -> p.speciesType = s);
	public static final NBTSerializer<TileFlowerPedastal> updateTagSerializer = serializer.getPartial("stack", "speciesType");
	boolean canExtract = false;
	String speciesType = "";
	@Nullable
	final ItemStackHandler stackHandler = new ItemStackHandler(1) {
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

	public void setStack(@Nonnull ItemStack stack) {
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
		updateTagSerializer.readFromNBT(this, tag);
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound updateTag = super.getUpdateTag();
		updateTagSerializer.writeToNBT(this, updateTag);
		return updateTag;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		serializer.readFromNBT(this, compound);
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
		serializer.writeToNBT(this, nbtTagCompound);
		return nbtTagCompound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		NBTTagCompound nbt = pkt.getNbtCompound();
		handleUpdateTag(nbt);
	}

	public void setShouldRelease() {
		canExtract = true;
	}

	public boolean accepts(@Nonnull IBeeGenome genome) {
		return speciesType.isEmpty() || speciesType.equals(genome.getPrimary().getUID());
	}
}
