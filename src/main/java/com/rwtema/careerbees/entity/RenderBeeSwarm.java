package com.rwtema.careerbees.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderBeeSwarm extends Render<EntityBeeSwarm> {
	public RenderBeeSwarm(RenderManager renderManager) {
		super(renderManager);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityBeeSwarm entity) {
		return null;
	}
}
