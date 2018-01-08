package com.rwtema.careerbees.helpers;

import forestry.api.apiculture.IAlleleBeeSpecies;
import net.bdew.gendustry.api.EnumMutationSetting;
import net.bdew.gendustry.api.GendustryAPI;

class GendustryApiWorker extends GendustryApiHelper {
	@Override
	public void forceMutation(IAlleleBeeSpecies species) {
		GendustryAPI.Registries.getMutatronOverrides().set(species, EnumMutationSetting.DISABLED);
	}
}
