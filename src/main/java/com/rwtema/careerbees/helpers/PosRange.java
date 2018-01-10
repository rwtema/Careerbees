package com.rwtema.careerbees.helpers;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class PosRange {
	final int x0, y0, z0, x1, y1, z1;

	public PosRange(int x0, int y0, int z0, int x1, int y1, int z1) {
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
	}

	public PosRange(@Nonnull BlockPos a, @Nonnull BlockPos b) {
		this(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
	}

	public PosRange(@Nonnull BlockPos a) {
		this(a, a);
	}

	public PosRange(@Nonnull BlockPos a, int n) {
		this(a.getX() - n, a.getY() - n, a.getZ() - n, a.getX() + n, a.getY() + n, a.getZ() + n);
	}


	public boolean contains(@Nonnull BlockPos a) {
		return a.getX() >= x0 && a.getY() >= y0 && a.getZ() >= z0 && a.getX() <= x1 && a.getY() <= y1 && a.getZ() <= z1;
	}


}
