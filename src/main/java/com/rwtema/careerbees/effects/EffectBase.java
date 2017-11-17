package com.rwtema.careerbees.effects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.effects.settings.Setting;
import com.rwtema.careerbees.helpers.ParticleHelper;
import com.rwtema.careerbees.helpers.StringHelper;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.*;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IEffectData;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

public abstract class EffectBase implements IAlleleBeeEffect {
	private final String uuid, rawname, unlocalizedName;
	private final boolean isDominant;
	private final boolean isCombinable;
	@Nonnull
	public Set<IAlleleBeeSpecies> validSpecies = ImmutableSet.of();
	public static HashMap<IAlleleBeeSpecies, EffectBase > registeredEffectSpecies = new HashMap<>();
	public List<Setting<?, ?>> settings = new ArrayList<>();

	public EffectBase(String rawname) {
		this(rawname, false, false);
	}

	public EffectBase(String rawname, boolean isDominant, boolean isCombinable) {
		this.rawname = rawname;
		this.unlocalizedName = BeeMod.MODID + ".allele.effect." + rawname;
		if (BeeMod.deobf_folder) {
			String capName = StringHelper.capFirstMulti(rawname);
			Lang.translate(unlocalizedName, capName);
			Lang.translate(unlocalizedName + ".desc", capName + " Description");
		}
		uuid = BeeMod.MODID + ".effect." + rawname;
		this.isDominant = isDominant;
		this.isCombinable = isCombinable;
		AlleleManager.alleleRegistry.registerAllele(this, EnumBeeChromosome.EFFECT);
	}

	public static float getSpeed(IBeeGenome genome, IBeeHousing housing) {
		float speed = genome.getSpeed() * getModifier(genome, housing, (m, g) -> m.getProductionModifier(g, 1));
		if ("forestry.apiculture.tiles.TileBeeHouse".equals(housing.getClass().getName())) {
			speed *= 0.4F;
		}
		return speed;
	}

	public static float getModifier(IBeeGenome genome, IBeeHousing housing, BiFunction<IBeeModifier, IBeeGenome, Float> function) {
		World world = housing.getWorldObj();
		IBeekeepingMode mode = BeeManager.beeRoot.getBeekeepingMode(world);

		IBeeModifier beeHousingModifier = BeeManager.beeRoot.createBeeHousingModifier(housing);
		IBeeModifier beeModeModifier = mode.getBeeModifier();
		return function.apply(beeHousingModifier, genome) * function.apply(beeModeModifier, genome);
	}

	public static AxisAlignedBB getAABB(IBeeGenome genome, IBeeHousing housing) {
		Vec3d territory = getTerritory(genome, housing);
		return new AxisAlignedBB(housing.getCoordinates()).grow(territory.x, territory.y, territory.z);
	}

	public static Vec3d getTerritory(IBeeGenome genome, IBeeHousing housing) {
		Vec3i territory = genome.getTerritory();
		float modifier = getModifier(genome, housing, (iBeeModifier, genome1) -> iBeeModifier.getTerritoryModifier(genome1, 1));
		return new Vec3d(territory.getX() * modifier, territory.getY() * modifier, territory.getZ() * modifier);
	}

	public static ItemStack tryAdd(ItemStack stack, IBeeHousingInventory inventory) {
		if (stack.isEmpty() || inventory.addProduct(stack, true)) {
			return ItemStack.EMPTY;
		}

		int amt = stack.getCount();
		int n;
		while (amt > 0 && (n = Integer.highestOneBit(amt)) > 0) {
			ItemStack copy = ItemHandlerHelper.copyStackWithSize(stack, n);
			if (inventory.addProduct(copy, true)) {
				amt -= n;
			} else {
				break;
			}
		}

		if (amt == stack.getCount()) {
			return stack;
		} else if (amt == 0) {
			return ItemStack.EMPTY;
		} else {
			return ItemHandlerHelper.copyStackWithSize(stack, amt);
		}
	}

	private IEffectSettingsHolder getSettings(IBeeHousing housing) {
		if (!settings.isEmpty()) {
			for (IBeeModifier iBeeModifier : housing.getBeeModifiers()) {
				if (iBeeModifier instanceof IEffectSettingsHolder)
					return (IEffectSettingsHolder) iBeeModifier;
			}
		}
		return IEffectSettingsHolder.DEFAULT_INSTANCE;
	}

	@Override
	@Nonnull
	public IEffectData doEffect(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {
		if (isValidSpecies(genome)) {
			return doEffectBase(genome, storedData, housing, getSettings(housing));
		}

		return storedData;
	}

	@Nonnull
	public abstract IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings);

	@Nonnull
	@Override
	public IEffectData doFX(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing) {
		IBeekeepingLogic beekeepingLogic = housing.getBeekeepingLogic();
		List<BlockPos> flowerPositions = beekeepingLogic.getFlowerPositions();

		ParticleHelper.BEE_HIVE_FX.addBeeHiveFX(housing, genome, flowerPositions);
		return storedData;
	}

	@Override
	public boolean isCombinable() {
		return isCombinable;
	}

	@Nonnull
	@Override
	public IEffectData validateStorage(@Nullable IEffectData storedData) {
		if (storedData != null) return storedData;
		return BaseEffectDataMap.None.INSTANCE;
	}

	@Nonnull
	@Override
	public String getUID() {
		return uuid;
	}

	@Override
	public boolean isDominant() {
		return isDominant;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public String getName() {
		return rawname;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	@Nonnull
	@Override
	public String getAlleleName() {
		return net.minecraft.util.text.translation.I18n.translateToLocal(getUnlocalizedName());
	}

	public void addSpecies(IAlleleBeeSpecies species) {
		validSpecies = ImmutableSet.<IAlleleBeeSpecies>builder().addAll(validSpecies).add(species).build();
		if (registeredEffectSpecies.put(species, this) != null) {
			throw new IllegalStateException();
		}
	}

	public boolean isValidSpecies(IBeeGenome genome) {
		return isValidSpecies(genome.getPrimary()) || isValidSpecies(genome.getSecondary());
	}

	public boolean isValidSpecies(IAlleleBeeSpecies species) {
		return validSpecies.contains(species);
	}

	public boolean acceptItemStack(ItemStack stack) {
		return false;
	}

	public boolean canAcceptItems() {
		return false;
	}


	public <V, NBT extends NBTBase> void addSetting(Setting vnbtSetting) {
		settings.add(vnbtSetting);
	}

	public <T extends TileEntity> List<T> getTiles(World world, Class<T> clazz, AxisAlignedBB bounds) {
		int x_min = MathHelper.floor(bounds.minX);
		int y_min = MathHelper.floor(bounds.minY);
		int z_min = MathHelper.floor(bounds.minZ);
		int x_max = MathHelper.ceil(bounds.maxX);
		int y_max = MathHelper.ceil(bounds.maxY);
		int z_max = MathHelper.ceil(bounds.maxZ);

		ImmutableList.Builder<T> builder = ImmutableList.builder();

		for (int chunk_x = x_min >> 4; chunk_x <= x_max >> 4; chunk_x++) {
			for (int chunk_z = z_min >> 4; chunk_z <= z_max >> 4; chunk_z++) {
				Chunk chunk = world.getChunkFromChunkCoords(chunk_x, chunk_z);

				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
					BlockPos key = entry.getKey();
					if (key.getX() >= x_min && key.getX() <= x_max &&
							key.getY() >= y_min && key.getY() <= y_max &&
							key.getZ() >= z_min && key.getZ() <= z_max) {
						TileEntity tileEntity = entry.getValue();

						if (clazz.isInstance(tileEntity)) {
							builder.add(clazz.cast(tileEntity));
						}
					}
				}
			}
		}

		return builder.build();
	}
}
