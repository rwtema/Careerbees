package com.rwtema.careerbees.helpers;

import forestry.api.apiculture.IAlleleBeeSpecies;
import net.minecraftforge.fml.common.Loader;

public class GendustryApiHelper {

	private static GendustryApiHelper INSTANCE;

	public static GendustryApiHelper getInstance(){
		if(INSTANCE == null){
			if (Loader.isModLoaded("gendustry")) {
				INSTANCE = new GendustryApiWorker();
			} else {
				INSTANCE = new GendustryApiHelper();
			}
		}
		return INSTANCE;
	}

	public void forceMutation(IAlleleBeeSpecies species) {

	}
}
