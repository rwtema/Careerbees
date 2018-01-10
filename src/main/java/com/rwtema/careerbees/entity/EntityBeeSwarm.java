package com.rwtema.careerbees.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityBeeSwarm extends Entity implements IProjectile {
	private static final DataParameter<ItemStack> ITEM = EntityDataManager.<ItemStack>createKey(EntityItem.class, DataSerializers.ITEM_STACK);

	public EntityBeeSwarm(World worldIn) {
		super(worldIn);
		this.setSize(0.5F, 0.5F);
		noClip = true;
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(ITEM, ItemStack.EMPTY);
	}

	public ItemStack getItem()
	{
		return (ItemStack)this.getDataManager().get(ITEM);
	}

	public void setItem(ItemStack stack)
	{
		this.getDataManager().set(ITEM, stack);
		this.getDataManager().setDirty(ITEM);
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
		this.setItem(new ItemStack(nbttagcompound));

		if (this.getItem().isEmpty())
		{
			this.setDead();
		}
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		if (!this.getItem().isEmpty())
		{
			compound.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
		}
	}

	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy)
	{
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double)f;
		y = y / (double)f;
		z = z / (double)f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		x = x * (double)velocity;
		y = y * (double)velocity;
		z = z * (double)velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
	}

}
