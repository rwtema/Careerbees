package com.rwtema.careerbees.entity;

import com.rwtema.careerbees.ProxyClient;
import com.rwtema.careerbees.helpers.QuadHelper;
import forestry.api.apiculture.IAlleleBeeSpecies;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector3f;

import javax.vecmath.Matrix4f;

public class ParticleBeeSwarm extends Particle {
	private final static float PI2 = (float) (Math.PI * 2);
	static Vector3f[] vecs = new Vector3f[]{
			new Vector3f(0, 1, 0),
			new Vector3f(1, 0, 0),
			new Vector3f(0, 0, 1),
			new Vector3f(1, 0, 0),
			new Vector3f(0, 1, 0),
			new Vector3f(0, 0, 1),
	};
	float prevPositions[][] = new float[16][];
	int seed, pass;
	float rad = 0;
	private EntityBeeSwarm owner;

	protected ParticleBeeSwarm(World worldIn, EntityBeeSwarm owner) {
		super(worldIn, owner.posX, owner.posY, owner.posZ, 0, 0, 0);
		setParticleTexture(ProxyClient.beeSprite);
		this.owner = owner;
		this.setSize(0.1F, 0.1F);
		this.particleScale *= 0.4F;
		this.particleMaxAge = 0;
		seed = worldIn.rand.nextInt(8192);
		pass = worldIn.rand.nextInt(3) == 0 ? 1 : 0;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		float minU = 0;
		float maxU = 1;
		float minV = 0;
		float maxV = 1;

		if (this.particleTexture != null) {
			minU = particleTexture.getMinU();
			maxU = particleTexture.getMaxU();
			minV = particleTexture.getMinV();
			maxV = particleTexture.getMaxV();
		}

		float f10 = 0.1F * particleScale;
		float f11 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f12 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f13 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int brightnessForRender = this.getBrightnessForRender(partialTicks);
		int b_i = brightnessForRender >> 16 & 65535;
		int b_t = brightnessForRender & 65535;
		buffer.pos(f11 - rotationX * f10 - rotationXY * f10, f12 - rotationZ * f10, f13 - rotationYZ * f10 - rotationXZ * f10).tex(maxU, maxV).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(b_i, b_t).endVertex();
		buffer.pos(f11 - rotationX * f10 + rotationXY * f10, f12 + rotationZ * f10, f13 - rotationYZ * f10 + rotationXZ * f10).tex(maxU, minV).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(b_i, b_t).endVertex();
		buffer.pos(f11 + rotationX * f10 + rotationXY * f10, f12 + rotationZ * f10, f13 + rotationYZ * f10 + rotationXZ * f10).tex(minU, minV).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(b_i, b_t).endVertex();
		buffer.pos(f11 + rotationX * f10 - rotationXY * f10, f12 - rotationZ * f10, f13 + rotationYZ * f10 - rotationXZ * f10).tex(minU, maxV).color(particleRed, particleGreen, particleBlue, 1.0F).lightmap(b_i, b_t).endVertex();


		minU = ProxyClient.blankSprite.getMinU();
		maxU = ProxyClient.blankSprite.getMaxU();
		minV = ProxyClient.blankSprite.getMinV();
		maxV = ProxyClient.blankSprite.getMaxV();


		for (int i = 1; i < (prevPositions.length - 1); i += 1) {
			float[] a = prevPositions[i];
			float[] b = prevPositions[i + 1];
			if (a == null || b == null) break;

			float t = a[6] * (1 - i / (float) prevPositions.length) * 0.9F;
			float next_t = b[6] * (1 - (i + 1) / (float) prevPositions.length) * 0.9F;

			float d1 = t * f10 * 0.25F;
			float d2 = next_t * f10 * 0.25F;

			double ax1;
			double ay1;
			double az1;
			if(i == 0){
				ax1 = f11 + interpPosX;
				ay1 = f12 + interpPosY;
				az1 = f13 + interpPosZ;
			}else {
				ax1 = a[0];
				ay1 = a[1];
				az1 = a[2];
			}

			buffer.pos(ax1 - rotationXY * d1 - interpPosX, ay1 - rotationZ * d1 - interpPosY, az1 - rotationXZ * d1 - interpPosZ).tex(maxU, maxV).color(a[3], a[4], a[5], i == 0 ? 0 : t ).lightmap(b_i, b_t).endVertex();
			buffer.pos(ax1 + rotationXY * d1 - interpPosX, ay1 + rotationZ * d1 - interpPosY, az1 + rotationXZ * d1 - interpPosZ).tex(maxU, minV).color(a[3], a[4], a[5], i == 0 ? 0 : t).lightmap(b_i, b_t).endVertex();
			buffer.pos(b[0] + rotationXY * d2 - interpPosX, b[1] + rotationZ * d2 - interpPosY, b[2] + rotationXZ * d2 - interpPosZ).tex(minU, minV).color(b[3], b[4], b[5], next_t).lightmap(b_i, b_t).endVertex();
			buffer.pos(b[0] - rotationXY * d2 - interpPosX, b[1] - rotationZ * d2 - interpPosY, b[2] - rotationXZ * d2 - interpPosZ).tex(minU, maxV).color(b[3], b[4], b[5], next_t).lightmap(b_i, b_t).endVertex();

			t = next_t;
		}
	}

	public Vec3d getTarget(int time) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		float k = seed + time / 4F;
		for (Vector3f vec : vecs) {
			k = ((seed + k) / 1.1F);
			QuadHelper.rotate(k, vec, matrix, matrix);
		}

		return new Vec3d(matrix.m00 + matrix.m03,
				matrix.m10 + matrix.m13,
				matrix.m20 + matrix.m23);
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		particleAge++;

		if (particleMaxAge > 0) {
			posX += motionX;
			posY += motionY;
			posZ += motionZ;

			rad *= 0.9F;
			if(particleAge > particleMaxAge) {
				setExpired();
				return;
			}
			motionX *= 0.95F;
			motionY *= 0.9F;
			motionZ *= 0.95F;
			motionX += 0.05 * world.rand.nextGaussian();
			motionY += 0.05 * world.rand.nextGaussian();
			motionZ += 0.05 * world.rand.nextGaussian();
		} else if (owner.isDead) {
			particleAge = 0;
			particleMaxAge = (int) Math.round(20 / (0.5F + 0.5F * world.rand.nextFloat()));
//			motionX -= owner.motionX * (0.5F + world.rand.nextFloat());
//			motionY -= owner.motionY * (0.5F + world.rand.nextFloat());
//			motionZ -= owner.motionZ * (0.5F + world.rand.nextFloat());

		} else {
			rad = Math.min( 2 - (2 - rad) * 0.9F, 1);

			AxisAlignedBB b = owner.getTargetPos().transform(AxisAlignedBB::new).or(
					owner.getTargetEntity().transform(Entity::getEntityBoundingBox)
							.or(new AxisAlignedBB(owner.posX, owner.posY, owner.posZ, owner.posX, owner.posY, owner.posZ))
			);

			double bx = (b.maxX + b.minX) / 2,
					by = (b.maxY + b.minY) / 2,
					bz = (b.maxZ + b.minZ) / 2,
					d = rad * Math.max(b.maxX - b.minX, Math.max(b.maxY - b.minY, b.maxZ - b.minZ)) / 2 + 0.25;

			Vec3d p1 = getTarget(particleAge);
			Vec3d p2 = getTarget(particleAge + 1);

			double tx1 = bx + p1.x * d;
			double ty1 = by + p1.y * d;
			double tz1 = bz + p1.z * d;
			double tx2 = bx + p2.x * d;
			double ty2 = by + p2.y * d;
			double tz2 = bz + p2.z * d;

			final double f = 0.2;

			motionX = motionX * f + (tx2 - tx1) * (1 - f);
			motionY = motionY * f + (ty2 - ty1) * (1 - f);
			motionZ = motionZ * f + (tz2 - tz1) * (1 - f);
			posX = posX * f + tx1 * (1 - f);
			posY = posY * f + ty1 * (1 - f);
			posZ = posZ * f + tz1 * (1 - f);
		}

		IAlleleBeeSpecies primary = owner.getPrimary();
		if (primary != null) {
			int color = primary.getSpriteColour(pass);
			particleRed = (color >> 16 & 255) / 255.0F;
			particleGreen = (color >> 8 & 255) / 255.0F;
			particleBlue = (color & 255) / 255.0F;
		}

		System.arraycopy(prevPositions, 0, prevPositions, 1, prevPositions.length - 1);
		prevPositions[0] = new float[]{(float) posX, (float) posY, (float) posZ, particleRed, particleGreen, particleBlue, rad};
	}

	// avoid calculating lighting for bees, it is too much processing
	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 15728880;
	}

	// avoid calculating collisions
	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		this.resetPositionToBB();
	}

}
