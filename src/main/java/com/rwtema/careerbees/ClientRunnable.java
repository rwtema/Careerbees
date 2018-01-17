package com.rwtema.careerbees;

public interface ClientRunnable extends Runnable {
	ClientRunnable BLANK_INSTANCE = new ClientRunnable() {
		@Override
		public void run() {

		}
	};

	static void safeRun(ClientRunnable runnable) {
		if (BeeMod.proxy.isClient())
			BeeMod.proxy.run(runnable);
	}

	@Override
	default void run() {

	}
}
