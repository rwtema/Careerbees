package com.rwtema.careerbees.helpers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.vecmath.Vector4f;

public class QuadHelper {

	public static BakedQuad buildQuad(
			@Nonnull VertexFormat format, @Nonnull TRSRTransformation transform, @Nonnull EnumFacing side, int tint,
			float x0, float y0, float z0, float u0, float v0, int c0,
			float x1, float y1, float z1, float u1, float v1, int c1,
			float x2, float y2, float z2, float u2, float v2, int c2,
			float x3, float y3, float z3, float u3, float v3, int c3, @Nonnull TextureAtlasSprite texture) {
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		putQuad(format, transform, side, tint, x0, y0, z0, u0, v0, c0, x1, y1, z1, u1, v1, c1, x2, y2, z2, u2, v2, c2, x3, y3, z3, u3, v3, c3, builder, texture);
		return builder.build();
	}

	private static void putQuad(@Nonnull VertexFormat format, @Nonnull TRSRTransformation transform, @Nonnull EnumFacing side, int tint,
								float x0, float y0, float z0, float u0, float v0, int c0,
								float x1, float y1, float z1, float u1, float v1, int c1,
								float x2, float y2, float z2, float u2, float v2, int c2,
								float x3, float y3, float z3, float u3, float v3, int c3,
								@Nonnull UnpackedBakedQuad.Builder builder, @Nonnull TextureAtlasSprite texture) {
		builder.setTexture(texture);
		builder.setQuadTint(tint);
		builder.setQuadOrientation(side);
		putVertex(builder, format, transform, side, x0, y0, z0, u0, v0, c0);
		putVertex(builder, format, transform, side, x1, y1, z1, u1, v1, c1);
		putVertex(builder, format, transform, side, x2, y2, z2, u2, v2, c2);
		putVertex(builder, format, transform, side, x3, y3, z3, u3, v3, c3);
	}

	public static void putVertex(@Nonnull UnpackedBakedQuad.Builder builder, @Nonnull VertexFormat format, @Nonnull TRSRTransformation transform, @Nonnull EnumFacing side, float x, float y, float z, float u, float v, int c) {
		Vector4f vec = new Vector4f();
		for (int e = 0; e < format.getElementCount(); e++) {
			switch (format.getElement(e).getUsage()) {
				case POSITION:
					vec.x = x;
					vec.y = y;
					vec.z = z;
					vec.w = 1;
					transform.getMatrix().transform(vec);
					builder.put(e, vec.x, vec.y, vec.z, vec.w);
					break;
				case COLOR:
					builder.put(e, ColorHelper.getR(c) / 255F, ColorHelper.getG(c) / 255F, ColorHelper.getB(c) / 255F, ColorHelper.getA(c) / 255F);
					break;
				case UV:
					if (format.getElement(e).getIndex() == 0) {
						builder.put(e, u, v, 0f, 1f);
						break;
					}
				case NORMAL:
					builder.put(e, (float) side.getFrontOffsetX(), (float) side.getFrontOffsetY(), (float) side.getFrontOffsetZ(), 0f);
					break;
				default:
					builder.put(e);
					break;
			}
		}
	}


	public static BakedQuad buildQuad(@Nonnull VertexFormat format, @Nonnull TRSRTransformation transform, @Nonnull EnumFacing face, int tint,
									  float x0, float y0, float z0, float u0, float v0,
									  float x1, float y1, float z1, float u1, float v1,
									  float x2, float y2, float z2, float u2, float v2,
									  float x3, float y3, float z3, float u3, float v3, int color, @Nonnull TextureAtlasSprite texture) {
		return buildQuad(format, transform, face, tint, x0, y0, z0, u0, v0, color, x1, y1, z1, u1, v1, color, x2, y2, z2, u2, v2, color, x3, y3, z3, u3, v3, color, texture);
	}

	@Nonnull
	public static BakedQuad reverse(@Nonnull BakedQuad input) {
		int[] vertexData = input.getVertexData();
		int[] v = new int[28];

		int col;
		if (input.getFace() == EnumFacing.UP)
			col = -8355712;
		else if (input.getFace() == EnumFacing.DOWN)
			col = -1;
		else
			col = 0;

		for (int i = 0; i < 4; i++) {
			System.arraycopy(vertexData, (3 - i) * 7, v, i * 7, 7);
			if (col != 0)
				v[i * 7 + 3] = col;
		}

		return new BakedQuad(v, input.getTintIndex(), input.getFace(), input.getSprite(), input.shouldApplyDiffuseLighting(), input.getFormat());
	}


}
