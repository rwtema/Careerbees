package com.rwtema.careerbees.blocks;

import com.mojang.authlib.GameProfile;
import com.rwtema.careerbees.gui.ContainerAlvearyFrame;
import com.rwtema.careerbees.gui.GuiAlvearyFrame;
import com.rwtema.careerbees.gui.GuiHandler;
import com.rwtema.careerbees.helpers.NBTSerializer;
import forestry.api.apiculture.*;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.IErrorLogic;
import forestry.api.multiblock.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileAlvearyHiveFrameHolder extends MultiblockTileEntityBase<IMultiblockLogicAlveary> implements IAlvearyComponent.BeeModifier, IAlvearyComponent.BeeListener, GuiHandler.ITileGui, IBeeHousing {
	public static final NBTSerializer<TileAlvearyHiveFrameHolder> serializer = NBTSerializer.getTileEntitySeializer(TileAlvearyHiveFrameHolder.class)
			.addNBTSerializable("inv", f -> f.handler)
			;
	private final ItemStackHandler handler = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			markDirty();
		}

		@Override
		protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
			if (stack.getItem() instanceof IHiveFrame)
				return super.getStackLimit(slot, stack);
			return 0;
		}
	};
	private final IBeeModifier iBeeModifier = new IBeeModifier() {
		private final DefaultBeeModifier defaultBeeModifier = new DefaultBeeModifier();

		@Nonnull
		public IBeeModifier getModifier() {
			ItemStack stackInSlot = handler.getStackInSlot(0);
			Item item = stackInSlot.getItem();
			if (item instanceof IHiveFrame) {
				return ((IHiveFrame) item).getBeeModifier(stackInSlot);
			}
			return defaultBeeModifier;
		}

		@Override
		public float getTerritoryModifier(@Nonnull IBeeGenome genome, float currentModifier) {
			return getModifier().getTerritoryModifier(genome, currentModifier);
		}

		@Override
		public float getMutationModifier(@Nonnull IBeeGenome genome, @Nonnull IBeeGenome mate, float currentModifier) {
			return getModifier().getMutationModifier(genome, mate, currentModifier);
		}

		@Override
		public float getLifespanModifier(@Nonnull IBeeGenome genome, @Nullable IBeeGenome mate, float currentModifier) {
			return getModifier().getLifespanModifier(genome, mate, currentModifier);
		}

		@Override
		public float getProductionModifier(@Nonnull IBeeGenome genome, float currentModifier) {
			return getModifier().getProductionModifier(genome, currentModifier);
		}

		@Override
		public float getFloweringModifier(@Nonnull IBeeGenome genome, float currentModifier) {
			return getModifier().getFloweringModifier(genome, currentModifier);
		}

		@Override
		public float getGeneticDecay(@Nonnull IBeeGenome genome, float currentModifier) {
			return getModifier().getGeneticDecay(genome, currentModifier);
		}

		@Override
		public boolean isSealed() {
			return getModifier().isSealed();
		}

		@Override
		public boolean isSelfLighted() {
			return getModifier().isSelfLighted();
		}

		@Override
		public boolean isSunlightSimulated() {
			return getModifier().isSunlightSimulated();
		}

		@Override
		public boolean isHellish() {
			return getModifier().isHellish();
		}
	};
	@Nullable
	private final DefaultBeeListener beeListener = new DefaultBeeListener() {
		@Override
		public void wearOutEquipment(int amount) {
			ItemStack stackInSlot = handler.getStackInSlot(0);
			if (stackInSlot.isEmpty()) return;
			if (stackInSlot.getItem() instanceof IHiveFrame) {
				IHiveFrame hiveFrame = (IHiveFrame) stackInSlot.getItem();

				IAlvearyController controller = getMultiblockLogic().getController();
				IBee queen = BeeManager.beeRoot.getMember(controller.getBeeInventory().getQueen());
				if (queen != null) {
					ItemStack frameUsed = hiveFrame.frameUsed(controller, stackInSlot, queen, amount);
					handler.setStackInSlot(0, frameUsed);
				}
			}
		}
	};

	public TileAlvearyHiveFrameHolder() {
		super(MultiblockManager.logicFactory.createAlvearyLogic());
	}

	@Nonnull
	@Override
	public BlockPos getCoordinates() {
		return getPos();
	}

	@Nonnull
	@Override
	public IErrorLogic getErrorLogic() {
		return getMultiblockLogic().getController().getErrorLogic();
	}

	@Nonnull
	@Override
	public Biome getBiome() {
		return getMultiblockLogic().getController().getBiome();
	}

	@Nonnull
	@Override
	public EnumTemperature getTemperature() {
		return getMultiblockLogic().getController().getTemperature();
	}

	@Nonnull
	@Override
	public EnumHumidity getHumidity() {
		return getMultiblockLogic().getController().getHumidity();
	}

	@Nonnull
	@Override
	public World getWorldObj() {
		return getWorld();
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
		}
		return super.getCapability(capability, facing);
	}

	@Nonnull
	@Override
	public IBeeModifier getBeeModifier() {
		return iBeeModifier;
	}


	@Nonnull
	@Override
	public IBeeListener getBeeListener() {
		return beeListener;
	}

	@Nullable
	@Override
	public GameProfile getOwner() {
		return getMultiblockLogic().getController().getOwner();
	}

	@Nonnull
	@Override
	public Vec3d getBeeFXCoordinates() {
		return getMultiblockLogic().getController().getBeeFXCoordinates();
	}

	@Override
	public void onMachineAssembled(@Nonnull IMultiblockController multiblockController, @Nonnull BlockPos minCoord, @Nonnull BlockPos maxCoord) {

	}

	@Override
	public void onMachineBroken() {

	}

	@Nonnull
	@Override
	public Container createContainer(@Nonnull EntityPlayer player) {
		return new ContainerAlvearyFrame(this, player);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Object createGui(EntityPlayer player) {
		return new GuiAlvearyFrame(this, player);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		serializer.readFromNBT(this, data);
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		NBTTagCompound nbtTagCompound = super.writeToNBT(data);
		serializer.writeToNBT(this, data);
		return nbtTagCompound;
	}

	@Nonnull
	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return getMultiblockLogic().getController().getBeeModifiers();
	}

	@Nonnull
	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return getMultiblockLogic().getController().getBeeListeners();
	}

	@Nonnull
	@Override
	public IBeeHousingInventory getBeeInventory() {
		return getMultiblockLogic().getController().getBeeInventory();
	}

	@Nonnull
	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return getMultiblockLogic().getController().getBeekeepingLogic();
	}

	@Override
	public int getBlockLightValue() {
		return getMultiblockLogic().getController().getBlockLightValue();
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return getMultiblockLogic().getController().canBlockSeeTheSky();
	}

	@Override
	public boolean isRaining() {
		return getMultiblockLogic().getController().isRaining();
	}

}
