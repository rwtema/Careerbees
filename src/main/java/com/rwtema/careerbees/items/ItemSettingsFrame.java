package com.rwtema.careerbees.items;

import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.effects.settings.Setting;
import com.rwtema.careerbees.gui.ContainerSettings;
import com.rwtema.careerbees.gui.GuiHandler;
import com.rwtema.careerbees.gui.GuiSettings;
import forestry.api.apiculture.DefaultBeeModifier;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class ItemSettingsFrame extends ItemBaseFrame implements GuiHandler.ItemStackGuiContainer {
	public ItemSettingsFrame() {
		super(new DefaultBeeModifier(), 0);
	}

	@Nonnull
	@Override
	public IBeeModifier getBeeModifier(ItemStack frame) {
		NBTTagCompound tagCompound;
		if ((tagCompound = frame.getTagCompound()) != null && tagCompound.hasKey("settings", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound tag = tagCompound.getCompoundTag("settings");
			return new SettingsModifier(tag);
		}
		return super.getBeeModifier(frame);
	}

	@Nonnull
	@Override
	public ItemStack frameUsed(@Nonnull IBeeHousing housing, @Nonnull ItemStack frame, @Nonnull IBee queen, int wear) {
		return frame;
	}

	@Override
	public Container getContainer(EntityPlayer player, ItemStack stack, int slot) {
		return new ContainerSettings(player, slot);
	}

	@Override
	public Object getGui(EntityPlayer player, ItemStack stack, int slot) {
		return new GuiSettings(player, slot);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (!worldIn.isRemote) {
			playerIn.openGui(BeeMod.instance, 0, worldIn, handIn == EnumHand.MAIN_HAND ? playerIn.inventory.currentItem : playerIn.inventory.mainInventory.size() + playerIn.inventory.armorInventory.size(), 0, 0);
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}

	public static class SettingsModifier extends DefaultBeeModifier implements IEffectSettingsHolder {
		final NBTTagCompound tag;

		public SettingsModifier(NBTTagCompound tag) {
			this.tag = tag;
		}

		@Override
		public <V> V getValue(Setting<V, ?> setting) {
			if (tag.hasKey(setting.getKeyname(), setting.getExpectedType())) {
				NBTBase tag = this.tag.getTag(setting.getKeyname());
				//noinspection unchecked
				return (V) (((Setting) setting).fromNBT(tag));
			}

			return setting.getDefault();
		}
	}
}
