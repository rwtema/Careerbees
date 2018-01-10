package com.rwtema.careerbees.gui;

import com.rwtema.careerbees.blocks.TileAlvearyHiveFrameHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerAlvearyFrame extends Container {
	public ContainerAlvearyFrame(@Nonnull TileAlvearyHiveFrameHolder frameHolder, @Nonnull EntityPlayer player) {
		InventoryPlayer inventoryPlayer = player.inventory;

		addSlotToContainer(new SlotItemHandler(
				frameHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null),
				0, 80, 36
		));

		int y = 80;

		for (int r = 0; r < 3; ++r)
		{
			for (int c = 0; c < 9; ++c)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, c + r * 9 + 9, 8 + c * 18, y + r * 18 ));
			}
		}

		for (int c = 0; c < 9; ++c)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, c, 8 + c * 18, 161 - 103 + y));
		}
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		return true;
	}

	@Nonnull
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			int v = 1;
			if (index < v)
			{
				if (!this.mergeItemStack(itemstack1, v, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 0, v, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

}
