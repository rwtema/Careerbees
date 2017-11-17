package com.rwtema.careerbees.effects;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class EffectStealPlayer extends EffectSteal<EntityPlayer> {
	public static final EffectSteal PLAYER = new EffectStealPlayer();


	public EffectStealPlayer() {
		super("steal.player", 10);
	}

	@Override
	public boolean canHandle(EntityPlayer livingBase) {
		return livingBase != null;
	}

	@Override
	protected Class<EntityPlayer> getEntityClazz() {
		return EntityPlayer.class;
	}

	@Override
	public boolean steal(EntityPlayer livingBase, IBeeHousing housing, EffectSteal effect) {
		InventoryPlayer inventory = livingBase.inventory;
		int sizeInventory = inventory.getSizeInventory();

		for (int i = 0; i < 5; i++) {
			int slot = housing.getWorldObj().rand.nextInt(sizeInventory);

			ItemStack itemStack = inventory.getStackInSlot(slot);
			if (!itemStack.isEmpty()) {
				ItemStack tempStack = itemStack.copy();
				ItemStack takenStack = inventory.decrStackSize(slot, 1);
				if (housing.getBeeInventory().addProduct(takenStack, true)) {
					GameProfile owner = housing.getOwner();
					TextComponentTranslation chatComponent;
					if (owner != null) {
						if (owner.equals(livingBase.getGameProfile())) {
							chatComponent = new TextComponentTranslation(effect.getUnlocalizedName() + ".message.self", takenStack.getTextComponent());
						} else
							chatComponent = new TextComponentTranslation(effect.getUnlocalizedName() + ".message.player", takenStack.getTextComponent(), new TextComponentString(owner.getName()));
					} else
						chatComponent = new TextComponentTranslation(effect.getUnlocalizedName() + ".message", takenStack.getTextComponent());
					livingBase.sendStatusMessage(chatComponent, true);

					inventory.markDirty();
					return true;
				} else {
					inventory.setInventorySlotContents(slot, tempStack);
				}
			}
		}
		return true;
	}
}
