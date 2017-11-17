package com.rwtema.careerbees;

public interface ClientRunnable extends Runnable {
	static void safeRun(ClientRunnable runnable) {
		if (BeeMod.proxy.isClient())
			BeeMod.proxy.run(runnable);
	}

	@Override
	default void run() {

	}
}
