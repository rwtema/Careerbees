package com.rwtema.careerbees;

import java.util.function.Supplier;

public interface ClientSupplier<T> extends Supplier<T> {
	@Override
	default T get() {
		return getServer();
	}

	default T getServer(){
		return null;
	}

	public static class ClientRunnableSupplier implements ClientSupplier<ClientRunnable> {

	}
}
