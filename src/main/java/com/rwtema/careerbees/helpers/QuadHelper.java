package com.rwtema.careerbees.helpers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
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


	public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
		return rotate(angle, axis.x, axis.y, axis.z, src, dest);
	}


	public static Matrix4f rotate(float angle, float x_axis, float y_axis, float z_axis, Matrix4f src, Matrix4f dest) {

		if (dest == null)
			dest = new Matrix4f();
		float c = MathHelper.cos(angle);
		float s = MathHelper.sin(angle);
		float oneminusc = 1.0f - c;

		float xy = x_axis * y_axis;
		float yz = y_axis * z_axis;
		float xz = x_axis * z_axis;
		float xs = x_axis * s;
		float ys = y_axis * s;
		float zs = z_axis * s;

		float f00 = x_axis * x_axis * oneminusc + c;
		float f01 = xy * oneminusc + zs;
		float f02 = xz * oneminusc - ys;
		// n[3] not used
		float f10 = xy * oneminusc - zs;
		float f11 = y_axis * y_axis * oneminusc + c;
		float f12 = yz * oneminusc + xs;
		// n[7] not used
		float f20 = xz * oneminusc + ys;
		float f21 = yz * oneminusc - xs;
		float f22 = z_axis * z_axis * oneminusc + c;

		float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
		float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
		float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
		float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
		float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
		float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
		float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
		float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
		float t20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
		float t21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
		float t22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
		float t23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;

		dest.m00 = t00;
		dest.m01 = t01;
		dest.m02 = t02;
		dest.m03 = t03;
		dest.m10 = t10;
		dest.m11 = t11;
		dest.m12 = t12;
		dest.m13 = t13;
		dest.m20 = t20;
		dest.m21 = t21;
		dest.m22 = t22;
		dest.m23 = t23;
		return dest;
	}

}
