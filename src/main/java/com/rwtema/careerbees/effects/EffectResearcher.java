package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.blocks.TileFlowerPedastal;
import com.rwtema.careerbees.helpers.RandomSelector;
import forestry.api.apiculture.*;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IMutation;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Random;

public class EffectResearcher extends EffectItemModification {
	public static final EffectResearcher INSTANCE = new EffectResearcher("study", 100);

	public EffectResearcher(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing);
	}

	public EffectResearcher(String name, boolean isDominant, boolean isCombinable, float baseTicksBetweenProcessing, float chanceOfProcessing) {
		super(name, isDominant, isCombinable, baseTicksBetweenProcessing, chanceOfProcessing);
	}

	@Nullable
	@Override
	public ItemStack modifyStack(IBeeGenome genome, TileFlowerPedastal frame, ItemStack stack, IBeeHousing housing) {
		if (housing != null && housing.getOwner() != null) {
			EnumBeeType type = BeeManager.beeRoot.getType(stack);
			if(type == null) return null;

			Random rand = housing.getWorldObj().rand;
			switch (type) {
				case DRONE:
					if (rand.nextInt(100) != 0) return rand.nextInt(10) == 0 ? ItemStack.EMPTY : null;
				case PRINCESS:
				case QUEEN:
					if (rand.nextInt(10) != 0) return null;
				case LARVAE:
					break;
			}

			IBee member = BeeManager.beeRoot.getMember(stack);

			if (member != null) {
				if (rand.nextInt(4) == 0) {
					IAlleleBeeSpecies species = rand.nextBoolean() ? member.getGenome().getPrimary() : member.getGenome().getSecondary();
					if (species.isSecret()) {
						return null;
					}
					int complexity = species.getComplexity();
					if(rand.nextInt(1 + 11) < Math.min(complexity, 11)){
						return null;
					}
					return AlleleManager.alleleRegistry.getSpeciesNoteStack(housing.getOwner(), species);
				}else {
					RandomSelector<IMutation> mutation = new RandomSelector<>(rand);
					mutation.selectAny(BeeManager.beeRoot.getCombinations(member.getGenome().getPrimary()));
					mutation.selectAny(BeeManager.beeRoot.getCombinations(member.getGenome().getSecondary()));
					IMutation iMutation = mutation.get();
					if (iMutation == null || iMutation.isSecret()) return null;
					int complexity = iMutation.getAllele0().getComplexity() + iMutation.getAllele1().getComplexity();
					if(rand.nextInt(1 + 22) < Math.min(complexity, 22)){
						return null;
					}

					return AlleleManager.alleleRegistry.getMutationNoteStack(housing.getOwner(), iMutation);
				}
			}
		}
		return null;
	}

	@Override
	public boolean shouldRelease(IBeeGenome genome, TileFlowerPedastal frame, ItemStack oldStack, ItemStack newStack, IBeeHousing housing) {
		return true;
	}

	@Override
	public boolean acceptItemStack(ItemStack stack) {
		IBee member = BeeManager.beeRoot.getMember(stack);
		return member != null && member.isAnalyzed();
	}
}
