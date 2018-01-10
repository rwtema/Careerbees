package com.rwtema.careerbees.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class ChunkDataModuleManager<T> {

	public abstract T getCachedBlank();

	@Nonnull
	public abstract T createBlank();

	public boolean onUpdate(Chunk chunk, T t) {
		return false;
	}

	public abstract void writeToNBT(NBTTagCompound base, T t);

	@Nonnull
	public abstract T readFromNBT(NBTTagCompound tag);

	public abstract void writeData(T value, PacketBuffer buffer);

	public abstract void readData(T value, PacketBuffer buffer);

	@SideOnly(Side.CLIENT)
	public void clientTick(Chunk chunk, T t) {

	}
}
