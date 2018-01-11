package com.rwtema.careerbees.effects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import com.rwtema.careerbees.effects.settings.Setting;
import com.rwtema.careerbees.helpers.ParticleHelper;
import com.rwtema.careerbees.helpers.RandomHelper;
import com.rwtema.careerbees.helpers.StringHelper;
import com.rwtema.careerbees.items.ItemEternalFrame;
import com.rwtema.careerbees.lang.Lang;
import forestry.api.apiculture.*;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IEffectData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EffectBase implements IAlleleBeeEffect {
	public static final HashMap<IAlleleBeeSpecies, EffectBase> registeredEffectSpecies = new HashMap<>();
	protected static final WeakHashMap<IBeeHousing, Iterable<BlockPos>> adjacentPosCache = new WeakHashMap<>();
	static int n = 0;

	static {
		MinecraftForge.EVENT_BUS.register(EffectBase.class);
	}

	@Nonnull
	private final String uuid, rawname, unlocalizedName;
	private final boolean isDominant;
	private final boolean isCombinable;
	@Nonnull
	public Set<IAlleleBeeSpecies> validSpecies = ImmutableSet.of();
	public final List<Setting<?, ?>> settings = new ArrayList<>();

	public EffectBase(String rawname) {
		this(rawname, false, false);
	}

	public EffectBase(@Nonnull String rawname, boolean isDominant, boolean isCombinable) {
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

	public static float getSpeed(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		ItemEternalFrame.checkProduction.set(true);
		float speed = genome.getSpeed() * getModifier(genome, housing, (m, g) -> m.getProductionModifier(g, 1));
		ItemEternalFrame.checkProduction.set(false);
		if ("forestry.apiculture.tiles.TileBeeHouse".equals(housing.getClass().getName())) {
			speed *= 0.4F;
		}

		return speed;
	}

	public static float getModifier(IBeeGenome genome, @Nonnull IBeeHousing housing, @Nonnull BiFunction<IBeeModifier, IBeeGenome, Float> function) {
		World world = housing.getWorldObj();
		IBeekeepingMode mode = BeeManager.beeRoot.getBeekeepingMode(world);

		IBeeModifier beeHousingModifier = BeeManager.beeRoot.createBeeHousingModifier(housing);
		IBeeModifier beeModeModifier = mode.getBeeModifier();
		return function.apply(beeHousingModifier, genome) * function.apply(beeModeModifier, genome);
	}

	public static AxisAlignedBB getAABB(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		Vec3d territory = getTerritory(genome, housing);
		return new AxisAlignedBB(housing.getCoordinates()).grow(territory.x, territory.y, territory.z);
	}

	@Nonnull
	public static Vec3d getTerritory(@Nonnull IBeeGenome genome, @Nonnull IBeeHousing housing) {
		Vec3i territory = genome.getTerritory();
		float modifier = getModifier(genome, housing, (iBeeModifier, genome1) -> iBeeModifier.getTerritoryModifier(genome1, 1));
		return new Vec3d(territory.getX() * modifier, territory.getY() * modifier, territory.getZ() * modifier);
	}

	public static ItemStack tryAdd(@Nonnull ItemStack stack, @Nonnull IBeeHousingInventory inventory) {
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

	@SubscribeEvent
	public static void tickCleanup(@Nonnull TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			n++;
			if (n > 20 * 20) {
				adjacentPosCache.clear();
				n = 0;
			}
		}
	}

	public static int getRand(int a, int b, @Nonnull Random random) {
		if (a == b) return a;
		else if (a < b) {
			return a + random.nextInt(b - a);
		} else {
			return b + random.nextInt(a - b);
		}
	}

	public IEffectSettingsHolder getSettings(@Nonnull IBeeHousing housing) {
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
		if (isValidSpecies(genome) && !housing.getWorldObj().isRemote) {
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

	public void addSpecies(@Nonnull IAlleleBeeSpecies species) {
		validSpecies = ImmutableSet.<IAlleleBeeSpecies>builder().addAll(validSpecies).add(species).build();
		if (registeredEffectSpecies.put(species, this) != null) {
			throw new IllegalStateException();
		}
	}

	public boolean isValidSpecies(@Nonnull IBeeGenome genome) {
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

	public <T extends TileEntity> List<T> getTiles(@Nonnull World world, @Nonnull Class<T> clazz, @Nonnull AxisAlignedBB bounds) {
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

	protected Iterable<BlockPos> getAdjacentTiles(@Nonnull IBeeHousing h) {

		BlockPos pos = h.getCoordinates();
		World world = h.getWorldObj();
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof IBeeHousing)) {
			return Stream.concat(
					Stream.of(pos),
					Stream.of(RandomHelper.getPermutation()).map(pos::offset))
					::iterator;
		}

		return adjacentPosCache.computeIfAbsent(h, this::getBlockPos);
	}

	@Nonnull
	private Iterable<BlockPos> getBlockPos(@Nonnull IBeeHousing h) {
		World world = h.getWorldObj();
		Random rand = world.rand;
		BlockPos pos = h.getCoordinates();
		IBeeHousingInventory beeInventory = h.getBeeInventory();
		HashSet<BlockPos> checked = new HashSet<>();

		ArrayList<BlockPos> adjToHousing = new ArrayList<>();
		LinkedList<BlockPos> toCheck = new LinkedList<>();

		Arrays.stream(RandomHelper.getPermutation(rand)).map(pos::offset).forEach(toCheck::add);
		BlockPos blockPos;
		while ((blockPos = toCheck.poll()) != null) {
			TileEntity te = world.getTileEntity(blockPos);
			if (te instanceof IBeeHousing && ((IBeeHousing) te).getBeeInventory() == beeInventory) {
				for (EnumFacing facing : RandomHelper.getPermutation(rand)) {
					BlockPos newpos = blockPos.offset(facing);
					if (checked.add(newpos)) {
						toCheck.add(newpos);
					}
				}
			} else {
				adjToHousing.add(blockPos);
			}
		}
		return adjToHousing;
	}

	public <C> List<C> getAdjacentCapabilities(@Nonnull IBeeHousing housing, @Nonnull Capability<C> capability) {
		return getAdjacentCapabilities(housing, capability, t -> true);
	}

	public <C> List<C> getAdjacentCapabilities(@Nonnull IBeeHousing housing, @Nonnull Capability<C> capability, Predicate<TileEntity> tileEntityFilter) {
		return Streams.stream(getAdjacentTiles(housing)).map(housing.getWorldObj()::getTileEntity).filter(Objects::nonNull).filter(tileEntityFilter).map(t -> t.getCapability(capability, null)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}

	public float getCooldown(IBeeGenome genome, Random random){
		return 0;
	}
}
