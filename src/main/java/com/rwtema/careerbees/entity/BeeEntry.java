package com.rwtema.careerbees.entity;

import forestry.apiculture.genetics.Bee;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BeeEntry {
	@Nullable
	final
	NBTTagCompound tag;

	public BeeEntry(@Nullable NBTTagCompound tag) {
		this.tag = tag;
	}

	public abstract void write(PacketBuffer buffer);

	public abstract void read(PacketBuffer buffer);

	public static class YentaBeeEntry extends BeeEntry {

		public YentaBeeEntry(@Nullable NBTTagCompound tag) {
			super(tag);
		}

		@Override
		public void write(PacketBuffer buffer) {

		}

		@Override
		public void read(PacketBuffer buffer) {

		}
	}

	public static class StudentBeeEntry extends BeeEntry {
		byte active;

		public StudentBeeEntry(@Nullable NBTTagCompound tag, byte active) {
			super(tag);
			this.active = active;
		}

		@Override
		public void write(@Nonnull PacketBuffer buffer) {
			buffer.writeByte(active);
		}

		@Override
		public void read(@Nonnull PacketBuffer buffer) {
			active = buffer.readByte();
		}
	}
}
