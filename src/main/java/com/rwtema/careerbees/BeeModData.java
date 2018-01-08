package com.rwtema.careerbees;

import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import java.util.function.Function;

public class BeeModData {

	public static <T extends WorldSavedData> T getData(WorldServer world, Class<T> clazz, Function<String, T> newData, String dataID) {
		WorldSavedData worldSavedData = world.loadData(clazz, dataID);
		if (worldSavedData != null && clazz.isInstance(worldSavedData)) {
			return clazz.cast(worldSavedData);
		}
		T newDataInstance = newData.apply(dataID);
		world.setData(dataID, newDataInstance);
		return newDataInstance;
	}
}
