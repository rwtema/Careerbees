package com.rwtema.careerbees.entity;

import com.google.common.collect.ImmutableList;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import forestry.api.apiculture.*;
import forestry.api.genetics.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class YenteBeeManager extends PlacedBeeManager<BeeEntry.YentaBeeEntry> {
	static final List<EnumTolerance> toleranceList = ImmutableList.of(EnumTolerance.BOTH_5, EnumTolerance.BOTH_4, EnumTolerance.BOTH_3, EnumTolerance.BOTH_2, EnumTolerance.BOTH_1, EnumTolerance.UP_5, EnumTolerance.DOWN_5, EnumTolerance.UP_4, EnumTolerance.DOWN_4, EnumTolerance.UP_3, EnumTolerance.DOWN_3, EnumTolerance.UP_2, EnumTolerance.DOWN_2, EnumTolerance.UP_1, EnumTolerance.DOWN_1, EnumTolerance.NONE).reverse();

	public YenteBeeManager() {
		super(CareerBeeSpecies.YENTE);
	}

	@Override
	protected void onPlaced(@Nonnull EntityPlayer entityPlayer) {
		entityPlayer.sendMessage(new TextComponentTranslation("careerbees.message.placed.yente.bee"));
	}


	@Override
	protected boolean isValidBeeType(EnumBeeType type, IAlleleBeeSpecies primary, IAlleleBeeSpecies secondary) {
		return primary == secondary && primary == CareerBeeSpecies.YENTE.get() && type == EnumBeeType.PRINCESS;
	}

	@Nonnull
	@Override
	protected BeeEntry.YentaBeeEntry createEntry(IBeeHousing tileEntity, NBTTagCompound tag) {
		return new BeeEntry.YentaBeeEntry(tag);
	}

	@Nonnull
	@Override
	protected BeeEntry.YentaBeeEntry recreateBeeEntry(NBTTagCompound stack) {
		return new BeeEntry.YentaBeeEntry(stack);
	}

	@Override
	protected boolean updateTile(Chunk chunk, Map.Entry<BlockPos, BeeEntry.YentaBeeEntry> k, @Nonnull IBeeHousing hou, @Nonnull TileEntity tileEntity) {

		IBeeHousingInventory beeInventory = hou.getBeeInventory();
		IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if (capability == null) return true;

		if (beeInventory.getQueen().isEmpty()) {
			int bestSlot = -1;
			int bestScore = Integer.MIN_VALUE;
			for (int i = 0; i < capability.getSlots(); i++) {
				ItemStack stackInSlot = capability.extractItem(i, 1, true);
				if (!stackInSlot.isEmpty()) {
					if (BeeManager.beeRoot.getType(stackInSlot) == EnumBeeType.PRINCESS) {
						IBee bee = BeeManager.beeRoot.getMember(stackInSlot);
						if (bee == null) continue;
						int score = bee.getGenome().getPrimary().getComplexity() + bee.getGenome().getSecondary().getComplexity();
						if (score > bestScore) {
							bestSlot = i;
							bestScore = score;
						}
					}
				}
			}

			if (bestSlot != -1) {
				ItemStack itemStack = capability.extractItem(bestSlot, 1, false);
				beeInventory.setQueen(itemStack);
			}
		}

		if (!beeInventory.getQueen().isEmpty() && beeInventory.getDrone().isEmpty()) {
			EnumBeeType type = BeeManager.beeRoot.getType(beeInventory.getQueen());
			IBee queenBee = BeeManager.beeRoot.getMember(beeInventory.getQueen());
			if (type == EnumBeeType.PRINCESS && queenBee != null) {
				IBeeGenome queenGenome = queenBee.getGenome();

				int bestSlot = -1;
				int bestScore = Integer.MIN_VALUE;
				for (int i = 0; i < capability.getSlots(); i++) {
					ItemStack stackInSlot = capability.extractItem(i, 1, true);
					if (!stackInSlot.isEmpty()) {
						if (BeeManager.beeRoot.getType(stackInSlot) == EnumBeeType.DRONE) {
							IBee bee = BeeManager.beeRoot.getMember(stackInSlot);
							if (bee == null) continue;
							int score = 0;
							IBeeGenome droneGenome = bee.getGenome();
							for (int j = 0; j < queenGenome.getChromosomes().length; j++) {
								IChromosome droneChromosome = droneGenome.getChromosomes()[j];
								IChromosome queenChromosome = queenGenome.getChromosomes()[j];
								for (IAllele a : new IAllele[]{droneChromosome.getPrimaryAllele(), droneChromosome.getSecondaryAllele()}) {
									for (IAllele b : new IAllele[]{queenChromosome.getPrimaryAllele(), queenChromosome.getSecondaryAllele()}) {
										if (a == b) {
											score += 5;
										}
									}
								}

								for (IAllele a : new IAllele[]{droneChromosome.getPrimaryAllele(), droneChromosome.getSecondaryAllele()}) {
									score += evaluate(EnumBeeChromosome.values()[j], a);
								}
							}
							if (score > bestScore) {
								bestSlot = i;
								bestScore = score;
							}
						}
					}
				}

				if (bestSlot != -1) {
					ItemStack itemStack = capability.extractItem(bestSlot, 1, false);
					beeInventory.setDrone(itemStack);
				}
			}
		}

		return false;
	}

	public int evaluate(@Nonnull EnumBeeChromosome chromosome, @Nonnull IAllele a) {
		int score = 0;
		switch (chromosome) {
			case SPECIES:
				score += ((IAlleleSpecies) a).getComplexity() * 10;
				break;
			case SPEED:
				score += Math.round(((IAlleleFloat) a).getValue() * 10);
				break;
			case FERTILITY:
				score += ((IAlleleInteger) a).getValue() * 2;
				break;
			case TEMPERATURE_TOLERANCE:
			case HUMIDITY_TOLERANCE:
				EnumTolerance tolerance = ((IAlleleTolerance) a).getValue();
				score += toleranceList.indexOf(tolerance);
				break;
			case NEVER_SLEEPS:
			case TOLERATES_RAIN:
			case CAVE_DWELLING:
				score += ((IAlleleBoolean) a).getValue() ? 5 : 0;
				break;
			case TERRITORY:
				Vec3i value = ((IAlleleArea) a).getValue();
				score += (value.getX() + value.getY() + value.getZ()) / 3;
				break;
			case EFFECT:

				break;
		}

		return score;
	}
}
