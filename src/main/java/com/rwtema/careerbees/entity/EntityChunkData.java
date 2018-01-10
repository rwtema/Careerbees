package com.rwtema.careerbees.entity;

import com.google.common.collect.HashBiMap;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.networking.BeeNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EntityChunkData extends Entity implements IEntityAdditionalSpawnData {

	@Nonnull
	public static final HashBiMap<String, ChunkDataModuleManager> managers;

	static {
		managers = HashBiMap.create();
		managers.put("bee_student", new StudentManager());
		managers.put("bee_yente", new YenteBeeManager());
//		managers.put("flattransfernodes", FlatTransferNodeHandler.INSTANCE);
	}

	public Map<ChunkDataModuleManager, Object> objectHashMap = new HashMap<>();
	ChunkPos pos;
	private boolean dirty;
	public EntityChunkData(@Nonnull World worldIn) {
		super(worldIn);
		noClip = true;
		isImmuneToFire = true;
		setEntityInvulnerable(true);
		setSize(0, 0);
		setInvisible(true);
	}

	public EntityChunkData(@Nonnull World world, ChunkPos chunkPos) {
		this(world);
		this.pos = chunkPos;
		this.prevPosX = this.posX = (pos.x << 4) + 8;
		this.prevPosY = this.posY = 512;
		this.prevPosZ = this.posZ = (pos.z << 4) + 8;
	}

	public static void init() {
		EntityRegistry.registerModEntity(new ResourceLocation(BeeMod.MODID, "entitychunkdata"), EntityChunkData.class, "entitychunkdata", 0, BeeMod.instance, 64, Integer.MAX_VALUE, false);
	}

	public static void markChunkDirty(@Nonnull Chunk chunk) {
		EntityChunkData dataEntity = getChunkDataEntity(chunk);
		if (dataEntity != null) {
			dataEntity.dirty = true;
		}
	}

	public static <T> T getChunkData(@Nonnull Chunk chunk, @Nonnull ChunkDataModuleManager<T> manager, boolean create) {
		EntityChunkData dataEntity = getChunkDataEntity(chunk);
		if (dataEntity == null) {
			if (create) {
				World world = chunk.getWorld();
				dataEntity = new EntityChunkData(world, new ChunkPos(chunk.x, chunk.z));
				T blank = manager.createBlank();
				dataEntity.objectHashMap.put(manager, blank);
				world.spawnEntity(dataEntity);
				return blank;
			} else {
				return manager.getCachedBlank();
			}
		}

		Object o = dataEntity.objectHashMap.get(manager);
		if (o == null) {
			if (create) {
				T blank = manager.createBlank();
				dataEntity.objectHashMap.put(manager, blank);
				return blank;
			} else {
				return manager.getCachedBlank();
			}
		}
		return (T) o;
	}

	@Nullable
	public static EntityChunkData getChunkDataEntity(@Nonnull Chunk chunk) {
		ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
		for (int i = entityLists.length - 1; i >= 0; i--) {
			ClassInheritanceMultiMap<Entity> entities = entityLists[i];
			if (!entities.isEmpty()) {
				for (EntityChunkData entityChunkData : entities.getByClass(EntityChunkData.class)) {
					if (!entityChunkData.isDead) {
						return entityChunkData;
					}
				}
			}
		}
		return null;
	}

	public static void writeData(@Nonnull ByteBuf buffer, @Nonnull Map<ChunkDataModuleManager, Object> objectHashMap) {
		PacketBuffer packetBuffer = new PacketBuffer(buffer);
		packetBuffer.writeInt(objectHashMap.size());
		for (Map.Entry<ChunkDataModuleManager, Object> entry : objectHashMap.entrySet()) {
			ChunkDataModuleManager manager = entry.getKey();
			packetBuffer.writeString(managers.inverse().get(manager));
			manager.writeData(entry.getValue(), packetBuffer);
		}
	}

	public static void readSpawnData(@Nonnull ByteBuf additionalData, @Nonnull Map<ChunkDataModuleManager, Object> objectHashMap) {
		objectHashMap.clear();
		PacketBuffer buffer = new PacketBuffer(additionalData);
		int n = buffer.readInt();
		for (int i = 0; i < n; i++) {
			ChunkDataModuleManager manager = managers.get(buffer.readString(64));
			Object blank = Validate.notNull(manager).createBlank();
			manager.readData(blank, buffer);
			objectHashMap.put(manager, blank);
		}
	}

	@Override
	public int getMaxInPortalTime() {
		return Integer.MAX_VALUE;
	}

	public <T> boolean hasData(ChunkDataModuleManager<T> manager) {
		return objectHashMap.containsKey(manager);
	}

	@Nonnull
	public <T> T getData(ChunkDataModuleManager<T> manager) {
		return (T) objectHashMap.get(manager);
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		return false;
	}

	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return false;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	@Override
	public boolean canRiderInteract() {
		return false;
	}

	@Override
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Nullable
	@Override
	public Entity changeDimension(int dimensionIn) {
		return null;
	}

	@Override
	protected void dealFireDamage(int amount) {

	}

	@Override
	protected void entityInit() {

	}

	@Override
	public void onUpdate() {
		if (this.world.isRemote) {
			Chunk chunkFromChunkCoords = world.getChunkFromBlockCoords(new BlockPos(this));
			for (Iterator<Map.Entry<ChunkDataModuleManager, Object>> iterator = objectHashMap.entrySet().iterator(); iterator.hasNext(); ) {
				Map.Entry<ChunkDataModuleManager, Object> entry = iterator.next();
				try {
					entry.getKey().clientTick(chunkFromChunkCoords, entry.getValue());
				} catch (ClassCastException exception) {
					exception.printStackTrace();
					iterator.remove();
				}
			}
		} else {
			this.prevPosX = this.posX = (pos.x << 4) + 8;
			this.prevPosY = this.posY = 512;
			this.prevPosZ = this.posZ = (pos.z << 4) + 8;

			if (objectHashMap.isEmpty()) {
				setDead();
			} else {
				Chunk chunkFromChunkCoords = world.getChunkFromChunkCoords(pos.x, pos.z);
				for (Iterator<Map.Entry<ChunkDataModuleManager, Object>> iterator = objectHashMap.entrySet().iterator(); iterator.hasNext(); ) {
					Map.Entry<ChunkDataModuleManager, Object> entry = iterator.next();
					try {
						if (entry.getKey().onUpdate(chunkFromChunkCoords, entry.getValue())) {
							iterator.remove();
							dirty = true;
						}
					} catch (ClassCastException exception) {
						exception.printStackTrace();
						iterator.remove();
						dirty = true;
					}
				}

				if (dirty) {
					dirty = false;

					if (world instanceof WorldServer) {
						EntityTracker tracker = ((WorldServer) world).getEntityTracker();
						for (EntityPlayer player : tracker.getTrackingPlayers(this)) {
							BeeNetworking.net.sendTo(new PacketEntityChunkData(getEntityId(), objectHashMap), (EntityPlayerMP) player);
						}
					}
				}
			}
		}

		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		pos = new ChunkPos(compound.getInteger("chunk_x"), compound.getInteger("chunk_z"));

		objectHashMap.clear();
		NBTTagList data = compound.getTagList("xudata", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < data.tagCount(); i++) {
			NBTTagCompound tagAt = data.getCompoundTagAt(i);
			ChunkDataModuleManager manager = managers.get(tagAt.getString("key"));
			if (manager != null) {
				Object o = manager.readFromNBT(tagAt);
				objectHashMap.put(manager, o);
			}
		}
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		compound.setInteger("chunk_x", pos.x);
		compound.setInteger("chunk_z", pos.z);

		NBTTagList data = new NBTTagList();

		for (Iterator<Map.Entry<ChunkDataModuleManager, Object>> iterator = objectHashMap.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<ChunkDataModuleManager, Object> entry = iterator.next();
			String s = managers.inverse().get(entry.getKey());
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			nbtTagCompound.setString("key", s);
			try {
				entry.getKey().writeToNBT(nbtTagCompound, entry.getValue());
			} catch (ClassCastException exception) {
				iterator.remove();
				exception.printStackTrace();
				continue;
			}

			data.appendTag(nbtTagCompound);
		}

		compound.setTag("xudata", data);
	}

	@Override
	public void writeSpawnData(@Nonnull ByteBuf buffer) {
		writeData(buffer, objectHashMap);
	}

	@Override
	public void readSpawnData(@Nonnull ByteBuf additionalData) {
		readSpawnData(additionalData, objectHashMap);
	}

	public static class PacketEntityChunkData extends BeeNetworking.MessageServerToClient {

		int entityId;
		Map<ChunkDataModuleManager, Object> objectHashMap;

		public PacketEntityChunkData() {
			super();
		}

		public PacketEntityChunkData(int entityId, Map<ChunkDataModuleManager, Object> objectHashMap) {
			this.entityId = entityId;
			this.objectHashMap = objectHashMap;
		}


		@Override
		protected void runClient(MessageContext ctx, EntityPlayer player) {
			Entity entityByID = Minecraft.getMinecraft().world.getEntityByID(entityId);
			if (entityByID instanceof EntityChunkData) {
				((EntityChunkData) entityByID).objectHashMap = objectHashMap;
			}
		}

		@Override
		public void fromBytes(@Nonnull ByteBuf buf) {
			entityId = buf.readInt();
			objectHashMap = new HashMap<>();
			EntityChunkData.readSpawnData(buf, objectHashMap);
		}

		@Override
		public void toBytes(@Nonnull ByteBuf buf) {
			buf.writeInt(entityId);
			EntityChunkData.writeData(buf, objectHashMap);
		}
	}
}
