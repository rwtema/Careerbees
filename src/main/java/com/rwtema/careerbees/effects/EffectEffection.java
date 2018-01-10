package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import forestry.api.apiculture.*;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IChromosome;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class EffectEffection extends EffectItemModification {

	public static EffectEffection INSTANCE = new EffectEffection("effect.restore", 16);

	public EffectEffection(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, ItemStack stack, IBeeHousing housing) {
		return modifyStack(stack);
	}

	private ItemStack modifyStack(ItemStack stack) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null) return null;

		EnumBeeType type = BeeManager.beeRoot.getType(stack);
		if (type != EnumBeeType.DRONE && type != EnumBeeType.PRINCESS) {
			return null;
		}
		IBee bee = BeeManager.beeRoot.getMember(stack);
		if (bee == null) return null;
		IBeeGenome targetGenome = bee.getGenome();
		IAlleleBeeSpecies species = targetGenome.getPrimary();
		IAlleleBeeEffect effect = getiAlleleBeeEffect(species);
		if (effect == null) return null;

		IChromosome[] chromosomes = targetGenome.getChromosomes();
		IAllele[] a = new IAllele[chromosomes.length], b = new IAllele[chromosomes.length];
		for (int i = 0; i < chromosomes.length; i++) {
			a[i] = chromosomes[i].getPrimaryAllele();
			b[i] = chromosomes[i].getSecondaryAllele();
			if (i == EnumBeeChromosome.EFFECT.ordinal()) {
				if (a[i] == effect && b[i] == effect) return null;
				a[i] = effect;
				b[i] = effect;
			}
		}

		IBeeGenome iBeeGenome = BeeManager.beeRoot.templateAsGenome(a, b);

		NBTTagCompound nbt = tagCompound.copy();
		bee.writeToNBT(nbt);
		NBTTagCompound nbtGenome = new NBTTagCompound();
		iBeeGenome.writeToNBT(nbtGenome);
		nbt.setTag("Genome", nbtGenome);

		ItemStack newStack = new ItemStack(stack.getItem(), stack.getCount(), stack.getItemDamage());
		newStack.setTagCompound(nbt);
		return newStack;
	}

	private IAlleleBeeEffect getiAlleleBeeEffect(IAlleleBeeSpecies species) {
		IAlleleBeeEffect iAlleleBeeEffect = CareerBeeEntry.CustomBeeFactory.SPECIES_EFFECT_MAP.get(species);
		if (iAlleleBeeEffect != null)
			return iAlleleBeeEffect;
		IAllele[] template = BeeManager.beeRoot.getTemplate(species);
		IAllele iAllele = template[EnumBeeChromosome.EFFECT.ordinal()];
		if (iAllele instanceof IAlleleBeeEffect) {
			return (IAlleleBeeEffect) iAllele;
		}

		return null;
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null) return false;

		EnumBeeType type = BeeManager.beeRoot.getType(stack);
		if (type != EnumBeeType.DRONE && type != EnumBeeType.PRINCESS) {
			return false;
		}
		IBee bee = BeeManager.beeRoot.getMember(stack);
		if (bee == null) return false;
		IBeeGenome targetGenome = bee.getGenome();
		IAlleleBeeSpecies species = targetGenome.getPrimary();
//		if (targetGenome.getSecondary() != species) return false;
		IAlleleBeeEffect effect = getiAlleleBeeEffect(species);
		if (effect == null) return false;

		IChromosome[] chromosomes = targetGenome.getChromosomes();
		IChromosome chromosome = chromosomes[EnumBeeChromosome.EFFECT.ordinal()];
		return chromosome.getPrimaryAllele() != effect || chromosome.getSecondaryAllele() != effect;
	}

	@Override
	public boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, ItemStack oldStack, ItemStack newStack, IBeeHousing housing) {
		return true;
	}
}
