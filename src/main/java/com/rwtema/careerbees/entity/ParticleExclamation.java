package com.rwtema.careerbees.entity;

import com.rwtema.careerbees.ProxyClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleExclamation extends Particle {
	public ParticleExclamation(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		setParticleTexture(ProxyClient.exclamation_sprite);
		this.particleGravity = -0.05F;
		this.particleMaxAge = 20;
		this.motionX = worldIn.rand.nextGaussian()*0.005F;
		this.motionY = 0.0D;
		this.motionZ = worldIn.rand.nextGaussian()*0.005F;
	}

	/**
	 * Retrieve what effect layer (what texture) the particle should be rendered with. 0 for the particle sprite sheet,
	 * 1 for the main Texture atlas, and 3 for a custom texture
	 */
	public int getFXLayer()
	{
		return 1;
	}
//
//	/**
//	 * Renders the particle
//	 */
//	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
//	{
//		float f = this.particleTexture.getMinU();
//		float f1 = this.particleTexture.getMaxU();
//		float f2 = this.particleTexture.getMinV();
//		float f3 = this.particleTexture.getMaxV();
//		float f4 = 0.5F;
//		float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
//		float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
//		float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
//		int i = this.getBrightnessForRender(partialTicks);
//		int j = i >> 16 & 65535;
//		int k = i & 65535;
//		buffer.pos((double)(f5 - rotationX * 0.5F - rotationXY * 0.5F), (double)(f6 - rotationZ * 0.5F), (double)(f7 - rotationYZ * 0.5F - rotationXZ * 0.5F)).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(j, k).endVertex();
//		buffer.pos((double)(f5 - rotationX * 0.5F + rotationXY * 0.5F), (double)(f6 + rotationZ * 0.5F), (double)(f7 - rotationYZ * 0.5F + rotationXZ * 0.5F)).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(j, k).endVertex();
//		buffer.pos((double)(f5 + rotationX * 0.5F + rotationXY * 0.5F), (double)(f6 + rotationZ * 0.5F), (double)(f7 + rotationYZ * 0.5F + rotationXZ * 0.5F)).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(j, k).endVertex();
//		buffer.pos((double)(f5 + rotationX * 0.5F - rotationXY * 0.5F), (double)(f6 - rotationZ * 0.5F), (double)(f7 + rotationYZ * 0.5F - rotationXZ * 0.5F)).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(j, k).endVertex();
//	}
//


}
