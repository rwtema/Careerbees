package com.rwtema.careerbees.handlers;

import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public abstract class SimpleFluidTank implements IFluidHandler, IFluidTank {
	public abstract int getStorage();

	public abstract void setStorage(int storage);


	@Override
	public abstract int getCapacity();

	public abstract Fluid getAllowedFluid();

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[]{new FluidTankProperties(
				getStorage() == 0 ? null : new FluidStack(getAllowedFluid(), getStorage()),
				getCapacity(),
				canFill(), canDrain()
		)};
	}

	public boolean canDrain() {
		return true;
	}

	public boolean canFill() {
		return true;
	}

	@Nullable
	@Override
	public FluidStack getFluid() {
		return getStorage() == 0 ? null : new FluidStack(getAllowedFluid(), getStorage());
	}

	@Override
	public int getFluidAmount() {
		return getStorage();
	}

	@Override
	public FluidTankInfo getInfo() {
		return new FluidTankInfo(this);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (resource == null || resource.amount <= 0 || resource.getFluid() != getAllowedFluid()) return 0;

		int toAdd = Math.min(getCapacity() - getStorage(), resource.amount);
		if (doFill && toAdd != 0) {
			setStorage(getStorage() + toAdd);
		}
		return toAdd;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (resource == null || resource.amount <= 0 || resource.getFluid() != getAllowedFluid()) return null;
		return drain(resource.amount, doDrain);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		int drain = Math.min(getStorage(), maxDrain);
		if (doDrain && drain != 0) {
			setStorage(getStorage() - drain);
		}
		return new FluidStack(getAllowedFluid(), drain);
	}
}
