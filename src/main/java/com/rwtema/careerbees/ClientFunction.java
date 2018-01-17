package com.rwtema.careerbees;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

public interface ClientFunction<T, R> extends Function<T, R> {
	@Override
	default R apply(T t) {
		return BeeMod.proxy.apply(this, t);
	}

	@SideOnly(Side.SERVER)
	R applyServer(T t);

	@SideOnly(Side.CLIENT)
	R applyClient(T t);

	abstract class RunnableProvider<T> implements ClientFunction<T, ClientRunnable> {

		@Override
		@SideOnly(Side.SERVER)
		public ClientRunnable applyServer(T t) {
			return ClientRunnable.BLANK_INSTANCE;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ClientRunnable applyClient(T t) {
			return new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					RunnableProvider.this.tick(t);
				}
			};
		}

		@SideOnly(Side.CLIENT)
		public abstract void tick(T t);
	}
}
