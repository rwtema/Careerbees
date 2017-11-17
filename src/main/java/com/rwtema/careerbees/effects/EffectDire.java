package com.rwtema.careerbees.effects;

import com.google.common.collect.ImmutableList;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.*;
import forestry.api.genetics.IEffectData;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EffectDire extends EffectBaseThrottled {
	public final static int R = 9;
	public final static int HEIGHT = 6;

	public static EffectDire INSTANCE = new EffectDire("novabynova", 2);

	static {
		MinecraftForge.EVENT_BUS.register(EffectDire.class);
	}

	ImmutableList<Insn> insnList = ImmutableList.<Insn>builder()
			.add(new Floor())
			.addAll(IntStream.range(0, HEIGHT).mapToObj(Sides::new).collect(Collectors.toList()))
			.add(new Torches())
			.add(new Ceiling())
			.build();

	public EffectDire(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectDire(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}



	@Override
	public void performEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, @Nonnull Random random, World world, BlockPos pos, IBeeModifier beeHousingModifier, IBeeModifier beeModeModifier, IEffectSettingsHolder settings) {
		int n = storedData.getInteger(1);
		World worldObj = housing.getWorldObj();
		BlockPos coordinates = housing.getCoordinates();
		int attempts = 20;

		mainLoop:
		while (attempts > 0) {
			int k = n;
			for (Insn insn : insnList) {
				if (k < insn.getSize()) {
					Pair<BlockPos, IBlockState> result = insn.getResult(k);
					BlockPos p = coordinates.add(result.getLeft());
					if (worldObj.isValid(p)) {
						IBlockState state = worldObj.getBlockState(p);
						if (result.getRight().getMaterial() != Material.AIR && state.getBlock().isReplaceable(worldObj, p)) {
							if (state.getMaterial() != Material.AIR && !state.getMaterial().isLiquid())
								worldObj.destroyBlock(p, true);

							worldObj.setBlockState(p, result.getRight());
							break mainLoop;
						} else {
							if (attempts > 0) {
								n = (n + 1) % insnList.stream().mapToInt(Insn::getSize).sum();
								attempts--;
								continue mainLoop;
							} else {
								break mainLoop;
							}
						}
					}
				} else {
					k = k - insn.getSize();
				}
			}
		}
		storedData.setInteger(1, (n + 1) % insnList.stream().mapToInt(Insn::getSize).sum());
	}

	private abstract static class Insn {
		abstract int getSize();

		abstract Pair<BlockPos, IBlockState> getResult(int n);
	}

	private class Floor extends Insn {
		@Override
		int getSize() {
			return R * R;
		}

		@Override
		Pair<BlockPos, IBlockState> getResult(int n) {
			int x = n % R;
			int z = (n - x) / R;
			return Pair.of(new BlockPos(x - (R / 2), -1, z - (R / 2)), Blocks.COBBLESTONE.getDefaultState());
		}
	}

	private class Sides extends Insn {
		final int y;

		private Sides(int y) {
			this.y = y;
		}

		@Override
		int getSize() {
			return 4 * (R - 1);
		}

		@Override
		Pair<BlockPos, IBlockState> getResult(int n) {
			int d = n % (R - 1);
			int dn = (n - d) / (R - 1);

			final int x0, z0, dx, dz;
			switch (dn) {
				case 0:
					x0 = 0;
					z0 = 0;
					dx = 1;
					dz = 0;
					break;
				case 1:
					x0 = R - 1;
					z0 = 0;
					dx = 0;
					dz = 1;
					break;
				case 2:
					x0 = R - 1;
					z0 = R - 1;
					dx = -1;
					dz = 0;
					break;
				case 3:
					x0 = 0;
					z0 = R - 1;
					dx = 0;
					dz = -1;
					break;
				default:
					throw new IllegalArgumentException("" + n);
			}

			int x = x0 - (R / 2) + dx * d;
			int z = z0 - (R / 2) + dz * d;

			if (y <= 1 && (x == 0 || z == 0)) {
				return Pair.of(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
			}
			return Pair.of(new BlockPos(x, y, z), Blocks.COBBLESTONE.getDefaultState());
		}
	}

	private class Torches extends Insn {
		@Override
		int getSize() {
			return 4;
		}

		@Override
		Pair<BlockPos, IBlockState> getResult(int n) {
			EnumFacing dir = EnumFacing.HORIZONTALS[n];
			int i = R / 2 - 1;
			return Pair.of(new BlockPos(dir.getFrontOffsetX() * i, 3, dir.getFrontOffsetZ() * i), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, dir.getOpposite()));
		}
	}

	private class Ceiling extends Insn {
		@Override
		int getSize() {
			return R * R;
		}

		@Override
		Pair<BlockPos, IBlockState> getResult(int n) {
			int maxW = R / 2;
			int d = 0;
			int x = -maxW;
			int z = -maxW;
			for (int i = 0; i < n; i++) {
				switch (d) {
					case 0: //x+
						x = x + 1;
						if (x == maxW)
							d = 1;

						break;
					case 1: //z+
						z = z + 1;
						if (z == maxW)
							d = 2;
						break;
					case 2: //x-
						x = x - 1;
						if (x == -maxW)
							d = 3;
						break;
					case 3: //z-
						z = z - 1;
						if (z == (-maxW + 1)) {
							d = 0;
							maxW = maxW - 1;
						}
						break;
				}
			}

			boolean flag = ((Math.abs(x) == 1 || Math.abs(x) == 2) && (Math.abs(z) == 1 || Math.abs(z) == 2));
			return Pair.of(new BlockPos(x, HEIGHT, z), flag ? Blocks.GLASS.getDefaultState() : Blocks.COBBLESTONE.getDefaultState());
		}
	}
}
