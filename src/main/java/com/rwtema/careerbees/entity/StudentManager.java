package com.rwtema.careerbees.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.helpers.RandomHelper;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class StudentManager extends PlacedBeeManager<BeeEntry.StudentBeeEntry> {
	final Set<IErrorState> errorTypes = Sets.newHashSet("no_flower", "no_queen", "no_drone", "no_space").stream().map(s -> "forestry:" + s).map(ForestryAPI.errorStateRegistry::getErrorState).collect(Collectors.toSet());

	public StudentManager() {
		super(CareerBeeSpecies.STUDENT);
	}

	@Override
	protected boolean isValidBeeType(EnumBeeType type, IAlleleBeeSpecies primary, IAlleleBeeSpecies secondary) {
		return type == EnumBeeType.DRONE && primary == CareerBeeSpecies.STUDENT.species && primary == secondary;
	}

	@Override
	protected void onPlaced(@Nonnull EntityPlayer entityPlayer) {
		entityPlayer.sendMessage(new TextComponentTranslation("careerbees.message.placed.student.bee"));
	}

	@Override
	@Nonnull
	protected BeeEntry.StudentBeeEntry createEntry(@Nonnull IBeeHousing tileEntity, NBTTagCompound tag) {
		return new BeeEntry.StudentBeeEntry(
				tag,
				(byte) (checkForErrors(tileEntity) ? 1 : 0)
		);
	}

	@Override
	@Nonnull
	protected BeeEntry.StudentBeeEntry recreateBeeEntry(NBTTagCompound genome) {
		return new BeeEntry.StudentBeeEntry(genome, (byte) 2);
	}

	@SideOnly(Side.CLIENT)
	protected void tickClient(@Nonnull Chunk chunk, @Nonnull Random rand, @Nonnull Map.Entry<BlockPos, BeeEntry.StudentBeeEntry> entry, @Nonnull BlockPos pos, TileEntity tileEntity, IBeeHousing entity) {
		if (entry.getValue().active == 0 && rand.nextInt(5) == 0) {

			ArrayList<EnumFacing> enumFacings = Lists.newArrayList(EnumFacing.values());
			Collections.shuffle(enumFacings);
			for (EnumFacing dir : RandomHelper.getPermutation()) {
				BlockPos offset = pos.offset(dir);
				IBlockState blockState = chunk.getBlockState(offset);
				if (!blockState.isNormalCube()) {
					EnumMap<EnumFacing.Axis, Float> map = new EnumMap<>(EnumFacing.Axis.class);
					for (EnumFacing.Axis axis : EnumFacing.Axis.values()) {

						map.put(axis, axis == dir.getAxis()
								? (dir.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? (1 - rand.nextFloat() * 0.2F) : rand.nextFloat() * 0.2F)
								: rand.nextFloat());
					}

					Minecraft.getMinecraft().effectRenderer.addEffect(
							new ParticleExclamation(
									chunk.getWorld(),
									offset.getX() + map.get(EnumFacing.Axis.X),
									offset.getY() + map.get(EnumFacing.Axis.Y),
									offset.getZ() + map.get(EnumFacing.Axis.Z)
							));
					break;
				}
			}
		}
	}

	@Override
	protected boolean updateTile(Chunk chunk, @Nonnull Map.Entry<BlockPos, BeeEntry.StudentBeeEntry> k, @Nonnull IBeeHousing hou, TileEntity tileEntity) {


		byte t = checkForErrors(hou) ? 0 : (byte) 1;
		if (k.getValue().active != t) {
			k.getValue().active = t;
			EntityChunkData.markChunkDirty(chunk);
		}
		return false;
	}

	private boolean checkForErrors(@Nonnull IBeeHousing hou) {
		IErrorLogic errorLogic = hou.getErrorLogic();
		return errorTypes.stream().anyMatch(errorLogic::contains);
	}

}
