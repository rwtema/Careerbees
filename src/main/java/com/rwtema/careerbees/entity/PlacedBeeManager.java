package com.rwtema.careerbees.entity;

import com.google.common.collect.ImmutableMap;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.bees.SpecialProperties;
import com.rwtema.careerbees.helpers.NBTHelper;
import com.rwtema.careerbees.helpers.ParticleHelper;
import forestry.api.apiculture.*;
import forestry.api.genetics.IAllele;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PlacedBeeManager<B extends BeeEntry> extends ChunkDataModuleManager<Map<BlockPos, B>> {
	final CareerBeeEntry species;
	IBeeGenome genome;

	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	protected PlacedBeeManager(CareerBeeEntry species) {
		this.species = species;
	}

	private final Set<EntityPlayer> playerServer = Collections.newSetFromMap(new WeakHashMap<>());
	private final Set<EntityPlayer> playerClient = Collections.newSetFromMap(new WeakHashMap<>());

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		if (!event.getEntityPlayer().isSneaking()) return;

		ItemStack itemStack = event.getItemStack();

		BlockPos pos = event.getPos();
		if (false && itemStack.isEmpty()) {

			Chunk chunk = event.getWorld().getChunkFromBlockCoords(pos);
			Map<BlockPos, B> chunkData = EntityChunkData.getChunkData(chunk, this, false);

			if (chunkData.get(pos) != null) {
				if (hasPlayerRightClickedAlready(event)) return;

				event.setCanceled(true);


				if (!event.getWorld().isRemote) {
					B k = chunkData.remove(pos);
					NBTTagCompound tag = k.tag;
					if (tag != null) {
						World world = chunk.getWorld();
						double d0 = (world.rand.nextFloat() * 0.5F) + 0.25D;
						double d1 = (world.rand.nextFloat() * 0.5F) + 0.25D;
						double d2 = (world.rand.nextFloat() * 0.5F) + 0.25D;
						EntityItem entityitem = new EntityItem(
								world,
								pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2,
								new ItemStack(tag));
						entityitem.setDefaultPickupDelay();
						world.spawnEntity(entityitem);
						EntityChunkData.markChunkDirty(chunk);
					}
					return;
				}
			}
			return;
		}


		IBee bee = BeeManager.beeRoot.getMember(itemStack);
		if (bee == null) {
			return;
		}

		if (!isValidBeeType(BeeManager.beeRoot.getType(itemStack), bee.getGenome().getPrimary(), bee.getGenome().getSecondary()))
			return;

		TileEntity tileEntity = event.getWorld().getTileEntity(pos);
		if (!(tileEntity instanceof IBeeHousing))
			return;


		Chunk chunk = event.getWorld().getChunkFromBlockCoords(pos);
		Map<BlockPos, B> chunkData = EntityChunkData.getChunkData(chunk, this, true);

		if (chunkData.get(pos) != null) {
			return;
		}

		event.setCanceled(true);
		if (hasPlayerRightClickedAlready(event)) return;

		if (!event.getWorld().isRemote) {
//			event.getEntityPlayer().setSneaking(false);
			B value = createEntry((IBeeHousing) tileEntity, itemStack.splitStack(1).writeToNBT(new NBTTagCompound()));
			chunkData.put(pos, value);
			EntityChunkData.markChunkDirty(chunk);
			onPlaced(event.getEntityPlayer());
		}
	}

	protected boolean hasPlayerRightClickedAlready(PlayerInteractEvent.RightClickBlock event) {
		return !(event.getWorld().isRemote ? playerClient : playerServer).add(event.getEntityPlayer());
	}

	protected abstract boolean isValidBeeType(EnumBeeType type, IAlleleBeeSpecies primary, IAlleleBeeSpecies secondary);

	protected abstract void onPlaced(EntityPlayer entityPlayer);

	@Nonnull
	protected abstract B createEntry(IBeeHousing tileEntity, NBTTagCompound tag);

	@Override
	public Map<BlockPos, B> getCachedBlank() {
		return ImmutableMap.of();
	}

	@Override
	public Map<BlockPos, B> createBlank() {
		return new HashMap<>();
	}

	@Override
	public void writeToNBT(NBTTagCompound base, Map<BlockPos, B> blockPosBeeEntryMap) {
		NBTTagList list = blockPosBeeEntryMap.entrySet().stream()
				.map(e -> NBTHelper.builder().setLong("pos", e.getKey().toLong()).setTag("genome", e.getValue().tag).build())
				.collect(NBTHelper.toNBTTagList());
		base.setTag("list", list);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientTick(Chunk chunk, Map<BlockPos, B> blockPosStudentBeeEntryMap) {
		playerClient.clear();

		Random rand = chunk.getWorld().rand;
		for (Map.Entry<BlockPos, B> entry : blockPosStudentBeeEntryMap.entrySet()) {
			BlockPos pos = entry.getKey();
			TileEntity tileEntity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
			if (tileEntity instanceof IBeeHousing) {
				tickClient(chunk, rand, entry, pos, tileEntity, ((IBeeHousing) tileEntity));

				if (rand.nextInt(256) == 0) {
					if (genome == null || !BeeMod.deobf_folder) {
						IAllele[] template = BeeManager.beeRoot.getTemplate(species.get());
						template[EnumBeeChromosome.TERRITORY.ordinal()] = SpecialProperties.AGORAPHOBIC;
						genome = BeeManager.beeRoot.templateAsGenome(template);
					}

					ParticleHelper.BEE_HIVE_FX.addBeeHiveFX((IBeeHousing) tileEntity, genome, Stream.of(EnumFacing.values()).map(entry.getKey()::offset).collect(Collectors.toList()));


				}

			}
		}

	}

	@SideOnly(Side.CLIENT)

	protected void tickClient(Chunk chunk, Random rand, Map.Entry<BlockPos, B> entry, BlockPos pos, TileEntity tileEntity, IBeeHousing entity) {

	}


	@Override
	public Map<BlockPos, B> readFromNBT(NBTTagCompound tag) {
		Map<BlockPos, B> objectObjectHashMap = createBlank();
		NBTHelper.<NBTTagCompound>wrapList(tag.getTagList("list", Constants.NBT.TAG_COMPOUND))
				.stream().filter(Objects::nonNull)
				.forEach(
						(e) -> objectObjectHashMap.put(
								BlockPos.fromLong(e.getLong("pos")),
								recreateBeeEntry(e.getCompoundTag("genome")))
				);

		return objectObjectHashMap;
	}

	@Nonnull
	protected abstract B recreateBeeEntry(NBTTagCompound genome);

	@Override
	public void writeData(Map<BlockPos, B> value, PacketBuffer buffer) {
		buffer.writeVarInt(value.size());
		value.entrySet().forEach(
				t -> {
					buffer.writeLong(t.getKey().toLong());
					buffer.writeByte(0);
					t.getValue().write(buffer);
				}
		);
	}

	@Override
	public boolean onUpdate(Chunk chunk, Map<BlockPos, B> blockPosBeeEntryMap) {
		playerServer.clear();

		blockPosBeeEntryMap.entrySet().removeIf(
				(k) -> {
					BlockPos pos = k.getKey();
					TileEntity tileEntity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
					if (tileEntity instanceof IBeeHousing) {
						return updateTile(chunk, k, (IBeeHousing) tileEntity, tileEntity);
					}
					NBTTagCompound tag = k.getValue().tag;
					if (tag != null) {
						World world = chunk.getWorld();
						double d0 = (world.rand.nextFloat() * 0.5F) + 0.25D;
						double d1 = (world.rand.nextFloat() * 0.5F) + 0.25D;
						double d2 = (world.rand.nextFloat() * 0.5F) + 0.25D;
						EntityItem entityitem = new EntityItem(
								world,
								pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2,
								new ItemStack(tag));
						entityitem.setDefaultPickupDelay();
						world.spawnEntity(entityitem);
						EntityChunkData.markChunkDirty(chunk);
					}
					return true;
				}
		);
		return blockPosBeeEntryMap.isEmpty();
	}

	protected abstract boolean updateTile(Chunk chunk, Map.Entry<BlockPos, B> k, IBeeHousing hou, TileEntity tileEntity);

	@Override
	public void readData(Map<BlockPos, B> value, PacketBuffer buffer) {
		value.clear();
		try {
			int n = buffer.readVarInt();
			for (int i = 0; i < n; i++) {
				BlockPos key = BlockPos.fromLong(buffer.readLong());
				NBTTagCompound tag = buffer.readCompoundTag();
				B beeEntry = recreateBeeEntry(tag);
				beeEntry.read(buffer);
				value.put(key, beeEntry);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
