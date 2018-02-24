package com.rwtema.careerbees.effects;

import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;

import javax.annotation.Nonnull;

public class EffectPolitics extends EffectBase {
	public static final EffectPolitics INSTANCE = new EffectPolitics("politics");
	public EffectPolitics(String rawname) {
		super(rawname);
	}

	@Nonnull
	@Override
	public IEffectData doEffectBase(@Nonnull IBeeGenome genome, @Nonnull IEffectData storedData, @Nonnull IBeeHousing housing, IEffectSettingsHolder settings) {
		return storedData;
	}
}
