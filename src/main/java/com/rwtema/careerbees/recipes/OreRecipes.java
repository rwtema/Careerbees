package com.rwtema.careerbees.recipes;

public class OreRecipes {
	public static final OreConversion ORE_TO_INGOT = new OreConversion.PrefixReplace("ore", "ingot");
	public static final OreConversion ORE_TO_DUST = new OreConversion.PrefixReplace("ore", "dust") {
		@Override
		public String getOreMapping(String input) {
			String oreMapping = super.getOreMapping(input);
			if (oreMapping != null && ORE_TO_INGOT.getOreMapping(input) != null) {
				return oreMapping;
			}
			return null;
		}
	};
}
