package com.rwtema.careerbees.entity;

import com.google.common.base.Optional;
import com.rwtema.careerbees.ClientFunction;
import com.rwtema.careerbees.ClientRunnable;
import com.rwtema.careerbees.effects.ISpecialBeeEffect;
import com.rwtema.careerbees.helpers.NBTHelper;
import com.rwtema.careerbees.items.ItemBeeGun;
import forestry.api.apiculture.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

@SuppressWarnings("Guava")
public class EntityBeeSwarm extends Entity implements IProjectile {
	public static final ISpecialBeeEffect NONE = new ISpecialBeeEffect() {

		@Override
		public float getCooldown(IBeeGenome genome, Random random) {
			return 0;
		}
	};
	private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityBeeSwarm.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<Optional<BlockPos>> POS = EntityDataManager.createKey(EntityBeeSwarm.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private static final DataParameter<Integer> ENTITY_ID = EntityDataManager.createKey(EntityBeeSwarm.class, DataSerializers.VARINT);
	private static final ClientFunction<EntityBeeSwarm, Void> ticker = new ClientFunction<EntityBeeSwarm, Void>() {

		@Override
		public Void applyServer(EntityBeeSwarm entityBeeSwarm) {
			return null;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Void applyClient(EntityBeeSwarm entityBeeSwarm) {
			entityBeeSwarm.tickClient();
			return null;
		}
	};
	ISpecialBeeEffect cache;
	IAlleleBeeSpecies cacheSpecies;
	IBeeGenome cacheGenome;

	@Nullable
	Entity owner;
	int buzzingTime = 0;
	int searchingTime = 0;

	@SideOnly(Side.CLIENT)
	ParticleBeeSwarm[] particles;

	public EntityBeeSwarm(@Nonnull World worldIn) {
		super(worldIn);
		this.setSize(0.1F, 0.1F);
		noClip = true;
	}

	public EntityBeeSwarm(@Nonnull World worldIn, @Nonnull ItemStack stack, @Nonnull Entity owner) {
		this(worldIn);
		this.owner = owner;
		this.setPosition(owner.posX, owner.posY + (double) owner.getEyeHeight() - 0.10000000149011612D, owner.posZ);
		this.setItem(stack.copy());
	}

	@Override
	protected void entityInit() {
		EntityDataManager dataManager = this.getDataManager();
		dataManager.register(ITEM, ItemStack.EMPTY);
		dataManager.register(POS, Optional.absent());
		dataManager.register(ENTITY_ID, -1);
	}

	public Optional<BlockPos> getTargetPos() {
		return getDataManager().get(POS);
	}

	public Optional<Entity> getTargetEntity() {
		int integer = getDataManager().get(ENTITY_ID);
		if (integer == -1) return Optional.absent();
		return Optional.fromNullable(world.getEntityByID(integer));
	}

	@Nonnull
	public ItemStack getItem() {
		return this.getDataManager().get(ITEM);
	}

	public void setItem(@Nonnull ItemStack stack) {
		this.getDataManager().set(ITEM, stack);
		this.getDataManager().setDirty(ITEM);
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		buzzingTime = compound.getInteger("BuzzingTime");
		searchingTime = compound.getInteger("SearchTime");

		getDataManager().set(ENTITY_ID, compound.getInteger("Entity"));

		if (compound.hasKey("Pos", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound p = compound.getCompoundTag("Pos");
			getDataManager().set(POS, Optional.of(new BlockPos(p.getInteger("x"), p.getInteger("y"), p.getInteger("z"))));
		} else {
			getDataManager().set(POS, Optional.absent());
		}

		NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
		this.setItem(new ItemStack(nbttagcompound));

		if (this.getItem().isEmpty()) {
			this.setDead();
		}
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		compound.setInteger("BuzzingTime", buzzingTime);
		compound.setInteger("SearchTime", searchingTime);
		compound.setInteger("Entity", getDataManager().get(ENTITY_ID));

		if (getTargetPos().isPresent()) {
			BlockPos p = getTargetPos().get();
			compound.setTag("Pos", NBTHelper.builder().setInteger("x", p.getX()).setInteger("y", p.getY()).setInteger("z", p.getZ()).build());
		}

		if (!this.getItem().isEmpty()) {
			compound.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
		}
	}

	public void setAim(Entity shooter, float pitch, float yaw, float p_184547_4_, float velocity, float inaccuracy) {
		float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		float f1 = -MathHelper.sin(pitch * 0.017453292F);
		float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		this.setThrowableHeading((double) f, (double) f1, (double) f2, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;

		if (!shooter.onGround) {
			this.motionY += shooter.motionY;
		}
	}

	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double) f;
		y = y / (double) f;
		z = z / (double) f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		x = x * (double) velocity;
		y = y * (double) velocity;
		z = z * (double) velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == ITEM) {
			cache = null;
			cacheSpecies = null;
			cacheGenome = null;
		}
	}

	@SideOnly(Side.CLIENT)
	private void tickClient() {
		if (particles == null) {
			particles = new ParticleBeeSwarm[8];
		}
		for (int i = 0; i < particles.length; i++) {
			if (particles[i] == null || !particles[i].isAlive()) {
				particles[i] = new ParticleBeeSwarm(world, this);
				Minecraft.getMinecraft().effectRenderer.addEffect(particles[i]);
			}
		}

		int id = getDataManager().get(ENTITY_ID);
		if (id != -1) {
			Entity entityByID = world.getEntityByID(id);
			if (entityByID != null) {
				setPosition(entityByID.posX, entityByID.posY, entityByID.posZ);
				this.motionX = this.motionY = this.motionZ = 0;
				return;
			}
		}

		Optional<BlockPos> blockPosOptional = getDataManager().get(POS);
		if (blockPosOptional.isPresent()) {
			BlockPos pos = blockPosOptional.get();
			setPosition(pos.getX(), pos.getY(), pos.getZ());
			this.motionX = this.motionY = this.motionZ = 0;
			return;
		}

		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		setPosition(posX, posY, posZ);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		ISpecialBeeEffect effect = getEffect();
		if (effect == NONE) {
			setDead();
			return;
		}


		if (world.isRemote) {
			ticker.apply(this);
		} else {
			IBee member = BeeManager.beeRoot.getMember(getItem());
			if (member == null) {
				setDead();
				return;
			}
			IBeeGenome genome = member.getGenome();


			int id = getDataManager().get(ENTITY_ID);
			if (id != -1) {
				buzzingTime++;
				Entity entityByID = world.getEntityByID(id);
				if (entityByID != null && !entityByID.isDead) {
					if ((entityByID.isInWater() || world.isRainingAt(new BlockPos(entityByID))) && !genome.getToleratesRain()) {
						setDead();
						return;
					}

					setPosition(entityByID.posX, entityByID.posY, entityByID.posZ);
					this.motionX = this.motionY = this.motionZ = 0;

					if ((effect instanceof ISpecialBeeEffect.SpecialEffectEntity)) {
						ISpecialBeeEffect.SpecialEffectEntity effectEntity = (ISpecialBeeEffect.SpecialEffectEntity) effect;
						if (!effectEntity.canHandleEntity(entityByID, genome)) {
							setDead();
						} else {
							ItemBeeGun.FakeHousingPlayer housing = new ItemBeeGun.FakeHousingPlayer(this, new BlockPos(entityByID), getItem());
							effectEntity.processingTick(entityByID, genome, housing);
							if (buzzingTime > effectEntity.getCooldown(entityByID, genome, rand)) {
								effectEntity.handleEntityLiving(entityByID, genome, housing);
								setDead();
								return;
							}
						}
					} else if (effect instanceof ISpecialBeeEffect.SpecialEffectItem && entityByID instanceof EntityItem) {
						ISpecialBeeEffect.SpecialEffectItem effectItem = (ISpecialBeeEffect.SpecialEffectItem) effect;
						ItemStack item = ((EntityItem) entityByID).getItem();
						if (!effectItem.canHandleStack(item, genome)) {
							setDead();
						} else {
							if (buzzingTime > effect.getCooldown(genome, rand)) {
								ItemStack newItem = effectItem.handleStack(item, genome, new ItemBeeGun.FakeHousingPlayer(this, new BlockPos(entityByID), getItem()));
								if (newItem != null) {
									((EntityItem) entityByID).setItem(newItem.copy());
								}

								setDead();
							}
						}
					} else {
						setDead();
					}
				} else {
					setDead();
				}

				return;
			}
			Optional<BlockPos> blockPosOptional = getDataManager().get(POS);
			if (blockPosOptional.isPresent()) {
				buzzingTime++;
				BlockPos pos = blockPosOptional.get();
				setPosition(pos.getX(), pos.getY(), pos.getZ());
				this.motionX = this.motionY = this.motionZ = 0;
				if ((effect instanceof ISpecialBeeEffect.SpecialEffectBlock)) {
					ISpecialBeeEffect.SpecialEffectBlock effectBlock = (ISpecialBeeEffect.SpecialEffectBlock) effect;
					if (!effectBlock.canHandleBlock(world, pos, genome)) {
						setDead();
					} else {
						ItemBeeGun.FakeHousingPlayer housing = new ItemBeeGun.FakeHousingPlayer(this, pos, getItem());
						effectBlock.processingTick(world, pos, genome, housing);
						if (buzzingTime > effectBlock.getCooldown(world, pos, genome, world.rand)) {
							effectBlock.handleBlock(world, pos, genome, housing);
							setDead();
							return;
						}
					}
				} else {
					setDead();
				}

				return;
			}


			// travelling
			searchingTime++;
			if (searchingTime > 2*genome.getLifespan() ) {
				setDead();
				return;
			}

			if ((isInWater() || world.isRainingAt(new BlockPos(this))) && !genome.getToleratesRain()) {
				setDead();
				return;
			}

			Vec3d start = new Vec3d(posX, posY, posZ);
			Vec3d end = start.addVector(motionX, motionY, motionZ);

			RayTraceResult trace = world.rayTraceBlocks(start, end, false, true, false);

			AxisAlignedBB axisAlignedBB = new AxisAlignedBB(start, end);

			if (owner != null) {
				if (owner.isDead || !owner.getEntityBoundingBox().grow(0.5).intersects(axisAlignedBB)) {
					owner = null;
				}
			}

			if (effect instanceof ISpecialBeeEffect.SpecialEffectEntity) {

				ISpecialBeeEffect.SpecialEffectEntity effectEntity = (ISpecialBeeEffect.SpecialEffectEntity) effect;


				Entity closest = getClosestEntityType(world, start, end, axisAlignedBB, Entity.class, t -> EntitySelectors.IS_ALIVE.test(t) && t != owner && effectEntity.canHandleEntity(t, genome));

				if (closest != null) {
					getDataManager().set(ENTITY_ID, closest.getEntityId());
					getDataManager().setDirty(ENTITY_ID);
					return;
				}

			}

			if (effect instanceof ISpecialBeeEffect.SpecialEffectItem) {

				ISpecialBeeEffect.SpecialEffectItem effectItem = (ISpecialBeeEffect.SpecialEffectItem) effect;

				EntityItem closest = getClosestEntityType(world, start, end, axisAlignedBB, EntityItem.class, t -> EntitySelectors.IS_ALIVE.test(t) && t != owner && !t.getItem().isEmpty() && effectItem.canHandleStack(t.getItem(), genome));

				if (closest != null) {
					getDataManager().set(ENTITY_ID, closest.getEntityId());
					getDataManager().setDirty(ENTITY_ID);
					return;
				}
			}

			if (trace != null) {
				BlockPos pos = trace.getBlockPos();

				if (effect instanceof ISpecialBeeEffect.SpecialEffectBlock) {
					ISpecialBeeEffect.SpecialEffectBlock effectBlock = (ISpecialBeeEffect.SpecialEffectBlock) effect;
					if (effectBlock.canHandleBlock(world, pos, genome)) {
						getDataManager().set(POS, Optional.of(pos));
						getDataManager().setDirty(POS);
					} else {
						setDead();
						return;
					}
				} else {
					setDead();
					return;
				}

			} else {
				posX += motionX;
				posY += motionY;
				posZ += motionZ;
			}

		}

	}

	public ISpecialBeeEffect getEffect() {
		ISpecialBeeEffect cache = this.cache;
		if (cache == null) {
			IBee member = BeeManager.beeRoot.getMember(getItem());
			if (member == null) {
				this.cache = cache = NONE;
			} else {
				IAlleleBeeEffect effect = member.getGenome().getEffect();
				if (effect instanceof ISpecialBeeEffect) {
					this.cache = cache = (ISpecialBeeEffect) effect;
				} else {
					this.cache = cache = NONE;
				}
			}
		}
		return cache;
	}


	@Nullable
	public <T extends Entity> T getClosestEntityType(@Nonnull World worldIn, @Nonnull Vec3d start, @Nonnull Vec3d end, @Nonnull AxisAlignedBB axisAlignedBB, @Nonnull Class<T> clazz, @Nonnull Predicate<T> filter) {
		T closest = null;
		double closest_dist = Double.MAX_VALUE;
		for (T entityLivingBase : worldIn.getEntitiesWithinAABB(clazz, axisAlignedBB, filter::test)) {
			AxisAlignedBB axisalignedbb = entityLivingBase.getEntityBoundingBox().grow(0.30000001192092896D);
			RayTraceResult ray = axisalignedbb.calculateIntercept(start, end);
			if (ray != null) {
				double v = start.squareDistanceTo(ray.hitVec);

				if (v < closest_dist) {
					closest = entityLivingBase;
					closest_dist = v;
				}
			} else if (axisalignedbb.contains(start)) {
				return entityLivingBase;
			}
		}
		return closest;
	}

	public IAlleleBeeSpecies getPrimary() {
		IAlleleBeeSpecies cache = this.cacheSpecies;
		if (cache == null) {
			IBee member = BeeManager.beeRoot.getMember(getItem());
			if (member != null) {
				IAlleleBeeSpecies effect = member.getGenome().getPrimary();
				this.cacheSpecies = cache = effect;
			}
		}
		return cache;
	}


	public IBeeGenome getGenome() {
		IBeeGenome cache = this.cacheGenome;
		if (cache == null) {
			IBee member = BeeManager.beeRoot.getMember(getItem());
			if (member != null) {
				cache = member.getGenome();
				this.cacheGenome = cache;
			}
		}
		return cache;
	}
}
