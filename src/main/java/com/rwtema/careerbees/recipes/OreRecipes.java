package com.rwtema.careerbees.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OreRecipes {
	public static final OreConversion ORE_TO_INGOT = new OreConversion.PrefixReplace("ore", "ingot");
	@Nullable
	public static final OreConversion ORE_TO_DUST = new OreConversion.PrefixReplace("ore", "dust") {
		@Override
		public String getOreMapping(@Nonnull String input) {
			String oreMapping = super.getOreMapping(input);
			if (oreMapping != null && ORE_TO_INGOT.getOreMapping(input) != null) {
				return oreMapping;
			}
			return null;
		}
	};
}
