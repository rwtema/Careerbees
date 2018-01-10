package com.rwtema.careerbees;

import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class BeeModData {

	public static <T extends WorldSavedData> T getData(@Nonnull WorldServer world, @Nonnull Class<T> clazz, @Nonnull Function<String, T> newData, @Nonnull String dataID) {
		WorldSavedData worldSavedData = world.loadData(clazz, dataID);
		if (worldSavedData != null && clazz.isInstance(worldSavedData)) {
			return clazz.cast(worldSavedData);
		}
		T newDataInstance = newData.apply(dataID);
		world.setData(dataID, newDataInstance);
		return newDataInstance;
	}
}
