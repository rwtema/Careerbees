package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class EffectDairy extends EffectBaseEntity<EntityCow> {
	public EffectDairy(String name, float baseTicksBetweenProcessing) {
		super(name, baseTicksBetweenProcessing, ((Predicate<EntityCow>) EntitySelectors.IS_ALIVE::apply).and(t -> !t.isChild()), EntityCow.class);
	}

	@Override
	protected void workOnEntities(List<EntityCow> entities, IBeeGenome genome, IBeeHousing housing, Random random, IEffectSettingsHolder settings) {
		Fluid milk = FluidRegistry.getFluid("milk");
		if (milk == null) return;
		WorldServer worldObj = (WorldServer) housing.getWorldObj();
		List<IFluidHandler> fluidHandlers = getAdjacentCapabilities(housing, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		if (fluidHandlers.isEmpty()) {
			return;
		}

		for (EntityCow cow : entities) {
			if (cow.getClass() == EntityCow.class) {
				FluidStack fluidStack = new FluidStack(milk, 1000);
				for (IFluidHandler handler : fluidHandlers) {
					int fill = handler.fill(fluidStack, false);
					fluidStack.amount -= fill;
					if (fluidStack.amount == 0) break;
				}
				if (fluidStack.amount != 0) {
					return;
				}
			}

			FakePlayer minecraft = FakePlayerFactory.getMinecraft(worldObj);
			minecraft.capabilities.isCreativeMode = false;

			minecraft.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BUCKET));
			cow.processInteract(minecraft, EnumHand.MAIN_HAND);

			ItemStack heldItem = minecraft.getHeldItem(EnumHand.MAIN_HAND).copy();

			minecraft.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
			FluidStack fluidContained = FluidUtil.getFluidContained(heldItem);

			if (fluidContained == null && heldItem.getItem() == Items.MILK_BUCKET) {
				fluidContained = new FluidStack(milk, 1000);
			}

			if (fluidContained != null && fluidContained.amount > 0) {
				for (IFluidHandler fluidHandler : fluidHandlers) {
					fluidContained.amount -= fluidHandler.fill(fluidContained, true);
					if (fluidContained.amount <= 0) {
						break;
					}
				}
				if(fluidContained.amount > 1000){

				}
			}

			heldItem.setCount(0);
		}
	}


}
