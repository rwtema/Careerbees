package com.rwtema.careerbees.gui;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.networking.BeeNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;

public class ContainerSettings extends Container {
	final ItemStack stack;
	final int slot;

	public ContainerSettings(EntityPlayer player, int slot) {
		this.slot = slot;

		int i = (2) * 18 - 2;

		InventoryPlayer inventoryPlayer = player.inventory;
		stack = inventoryPlayer.getStackInSlot(slot);
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				addSlot(inventoryPlayer, j1 + l * 9 + 9, 18 + 8 + j1 * 18, 103 + l * 18 + i, slot);
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			addSlot(inventoryPlayer, i1, 18 + 8 + i1 * 18, 161 + i, slot);
		}
	}

	private void addSlot(InventoryPlayer inventoryPlayer, int index, int xPosition, int yPosition, int slot) {
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

	public void sendUpdate() {

		NBTTagCompound mainTag = stack.getTagCompound();
		NBTTagCompound settingsTag;
		if (mainTag != null && mainTag.hasKey("settings")) {
			settingsTag = mainTag.getCompoundTag("settings");
			int update_index = settingsTag.getInteger("update_index") + 1;
			settingsTag.removeTag("update_index");
			if (settingsTag.hasNoTags()) {
				settingsTag = null;
			} else {
				settingsTag.setInteger("update_index", update_index);
			}
		} else {
			settingsTag = null;
		}


		BeeNetworking.net.sendToServer(new MessageNBT(settingsTag, slot, windowId));
	}


	public static class MessageNBT extends BeeNetworking.MessageClientToServer {
		NBTTagCompound tag;
		int slot;
		int windowID;

		public MessageNBT() {

		}

		public MessageNBT(NBTTagCompound tag, int slot, int windowId) {
			this.tag = tag;
			this.slot = slot;
		}

		@Override
		protected void runServer(MessageContext ctx, EntityPlayerMP player) {
			ItemStack stackInSlot = player.inventory.getStackInSlot(slot);
			if (stackInSlot.getItem() == BeeMod.instance.itemSettingsFrame) {
				if (tag == null) {
					NBTTagCompound tagCompound = stackInSlot.getTagCompound();
					if (tagCompound != null) {
						tagCompound.removeTag("settings");
					}
				} else {
					stackInSlot.setTagInfo("settings", tag);
				}
				player.inventory.markDirty();
				if (player.openContainer != null) {
					player.openContainer.detectAndSendChanges();
				}
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			tag = readNBT(buf);
			slot = buf.readByte();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			writeNBT(tag, buf);
			buf.writeByte(slot);
		}
	}


}
