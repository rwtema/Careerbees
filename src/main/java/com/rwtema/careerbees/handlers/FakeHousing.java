package com.rwtema.careerbees.handlers;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.*;
import forestry.api.climate.IClimateState;
import forestry.api.core.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class FakeHousing implements IBeeHousing {
//	World world;
//	BlockPos pos;

	@Nonnull
	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public IBeeHousingInventory getBeeInventory() {
		return new IBeeHousingInventory() {
			@Nonnull
			@Override
			public ItemStack getQueen() {
				return FakeHousing.this.getQueen();
			}

			@Nonnull
			@Override
			public ItemStack getDrone() {
				return FakeHousing.this.getDrone();
			}

			@Override
			public void setQueen(@Nonnull ItemStack itemstack) {
				addProduct(itemstack, false);
			}

			@Override
			public void setDrone(@Nonnull ItemStack itemstack) {
				addProduct(itemstack, false);
			}

			@Override
			public boolean addProduct(@Nonnull ItemStack product, boolean all) {
				return FakeHousing.this.addProduct(product, all);
			}
		};
	}

	protected abstract boolean addProduct(ItemStack product, boolean all);

	protected ItemStack getDrone() {
		return ItemStack.EMPTY;
	}

	protected abstract ItemStack getQueen();

	public static final IBeekeepingLogic beekeepingLogic = new IBeekeepingLogic() {
		@Override
		public boolean canWork() {
			return true;
		}

		@Override
		public void doWork() {

		}

		@Override
		public void clearCachedValues() {

		}

		@Override
		public void syncToClient() {

		}

		@Override
		public void syncToClient(@Nonnull EntityPlayerMP player) {

		}

		@Override
		public int getBeeProgressPercent() {
			return 0;
		}

		@Override
		public boolean canDoBeeFX() {
			return false;
		}

		@Override
		public void doBeeFX() {

		}

		@Override
		public List<BlockPos> getFlowerPositions() {
			return Collections.emptyList();
		}

		@Override
		public void readFromNBT(@Nonnull NBTTagCompound nbt) {

		}

		@Nonnull
		@Override
		public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
			return nbt;
		}
	};

	@Nonnull
	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return beekeepingLogic;
	}

	@Override
	public int getBlockLightValue() {
		return getWorldObj().isDaytime() ? 15 : 0;
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return getWorldObj().canBlockSeeSky(getCoordinates());
	}

	@Override
	public boolean isRaining() {
		return getWorldObj().isRaining();
	}

	@Nullable
	@Override
	public GameProfile getOwner() {
		return null;
	}

	@Nonnull
	@Override
	public Vec3d getBeeFXCoordinates() {
		BlockPos pos = getCoordinates();
		return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
	}

	@Nonnull
	@Override
	public Biome getBiome() {
		return getWorldObj().getBiome(getCoordinates());
	}

	@Nonnull
	@Override
	public EnumTemperature getTemperature() {
		if (BiomeHelper.isBiomeHellish(getBiome())) {
			return EnumTemperature.HELLISH;
		}
		IClimateState state = ForestryAPI.climateManager.getClimateState(getWorldObj(), getCoordinates());
		float temperature = state.getTemperature();
		return EnumTemperature.getFromValue(temperature);
	}

	@Nonnull
	@Override
	public EnumHumidity getHumidity() {
		IClimateState state = ForestryAPI.climateManager.getClimateState(getWorldObj(), getCoordinates());
		float humidity = state.getHumidity();
		return EnumHumidity.getFromValue(humidity);
	}

	final IErrorLogic logic =  ForestryAPI.errorStateRegistry.createErrorLogic();

	@Nonnull
	@Override
	public IErrorLogic getErrorLogic() {
		return logic;
	}

}
