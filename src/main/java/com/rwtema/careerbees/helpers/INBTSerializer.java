package com.rwtema.careerbees.helpers;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public interface INBTSerializer<T> {
	NBTTagCompound writeToNBT(@Nonnull T object, @Nonnull NBTTagCompound tag);

	void readFromNBT(@Nonnull T nbt, @Nonnull NBTTagCompound tag);
}
