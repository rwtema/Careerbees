package com.rwtema.careerbees.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerBeeGun extends Container {
	public static final int[][] beeCoords = new int[][]{
			{81, 18},
			{102, 30},
			{102, 54},
			{81, 66},
			{60, 54},
			{60, 30},
	};


	@Nonnull
	final ItemStack stack;
	final int slot;
	SlotItemHandler[] beeSlots = new SlotItemHandler[6];

	public ContainerBeeGun(@Nonnull EntityPlayer player, int slot) {
		this.slot = slot;

		InventoryPlayer inventoryPlayer = player.inventory;
		stack = inventoryPlayer.getStackInSlot(slot);

		IItemHandler itemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		for (int i1 = 0; i1 < 6; i1++) {
			addBeeSlot(itemHandler, i1);
		}

		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				addSlot(inventoryPlayer, j1 + l * 9 + 9, 8 + j1 * 18, 96 + l * 18, slot);
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			addSlot(inventoryPlayer, i1, 8 + i1 * 18, 154, slot);
		}
	}

	public void addBeeSlot(IItemHandler itemHandler, int index) {
		int x = beeCoords[index][0];
		int y = beeCoords[index][1];
		addSlotToContainer(beeSlots[index] = new SlotItemHandler(itemHandler, index, x, y) {
			@Override
			public boolean isItemValid(@Nonnull ItemStack stack) {
				return super.isItemValid(stack);
			}
		});
	}

	private void addSlot(@Nonnull InventoryPlayer inventoryPlayer, int index, int xPosition, int yPosition, int slot) {
		if (index == slot) {
			this.addSlotToContainer(new Slot(inventoryPlayer, index, xPosition, yPosition) {
				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return false;
				}

				@Override
				public boolean isItemValid(ItemStack stack) {
					return false;
				}
			});
		} else {
			this.addSlotToContainer(new Slot(inventoryPlayer, index, xPosition, yPosition));
		}
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		return playerIn.inventory.getStackInSlot(slot) == stack;
	}

}
